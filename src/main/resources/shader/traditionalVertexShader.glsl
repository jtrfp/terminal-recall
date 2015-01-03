/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/

#version 330

//CONSTANTS
const float COORD_DOWNSCALER		=512+1;

const uint PACKED_DATA_RENDER_MODE	=0u;	//UNibble

const int GPU_VERTICES_PER_BLOCK	=96;
const uint PAGE_SIZE_VEC4			=96u;

const int VTX_TEXTURE_WIDTH		   = 1024;
const int VTX_TEXTURE_HEIGHT	   = 4096;
const int VTX_TEXTURE_USABLE_WIDTH = (VTX_TEXTURE_WIDTH/3)*3;
const int VTX_TEXTURE_USABLE_HEIGHT= (VTX_TEXTURE_HEIGHT/3)*3;

//OUT
noperspective out vec2 	fragTexCoord;
noperspective out vec3 	fragNormal;
noperspective out float	w;
flat out float 			flatTextureID; //TODO: Nomenclature to primitiveID
noperspective out vec2	screenLoc;

//IN
uniform uint 			renderListPageTable[172];
uniform mat4 			cameraMatrix;
uniform sampler2D		xyBuffer;
uniform sampler2D		uvBuffer;
uniform sampler2D		zBuffer;
uniform sampler2D		wBuffer;
uniform sampler2D		normXYBuffer;
uniform sampler2D		normZBuffer;
uniform sampler2D		texIDBuffer;
uniform uint			logicalVec4Offset;

layout (location = 0) in float dummy;

//FUNCTIONS
uint bit(uint _input, uint index)
	{return (_input >> index) & 0x00000001u;}

uint UNibble(uint _input, uint index)
	{return (_input >> 4u*index) & 0x0000000Fu;}

int SNibble(uint _input, uint index){
	int result =  int((_input >> 4u*index) & 0x0000000Fu);
	if(result>=8)result-=16;
	return result;
	}

uint setUNibble(uint _input, uint index, uint value){
	uint erasureMask = 0x0000000Fu << 4u*index;
	//Invert
	erasureMask ^= 0xFFFFFFFFu;
	//Erase
	_input &= erasureMask;
	//Overwrite
	_input |= (value << 4u*index);
	return _input;
	}

uint UByte(uint _input, uint index)
	{return (_input >> 8u*index) & 0x000000FFu;}

int SByte(uint _input, uint index){
	int result =  int((_input >> 8u*index) & 0x000000FFu);
	if(result>=128)result-=256;
	return result;
	}
//////
uint secondUShort(uint _input)
	{return (_input >> 16u) & 0x0000FFFFu;}

uint firstUShort(uint _input)
	{return _input & 0x0000FFFFu;}

int secondSShort(uint _input){
	int result = int((_input >> 16u) & 0x0000FFFFu);
	if(result>=32768)result-=65536;
	return result;
	}

int firstSShort(uint _input){
	int result = int(_input & 0x0000FFFFu);
	if(result>=32768)result-=65536;
	return result;
	}

////////////// STRUCT LAYOUTS ///////////////

/*Object definition VEC4
	uint matrix offset
	uint vertex offset
	byte numVerticesInBlock, byte primitiveRenderMode, byte modelScalar, byte ???
	uint ???
*/

/*Triangle Vertex VEC4
	uint short x,y // XYZ Scaled by COORD_DOWNSCALER
	uint short z, byte fragNormalX, byte fragNormalY
	uint short u,v // scaled by 4096
	uint byte fragNormalZ, textureIDlo, textureIDmid, textureIDhi // 3 bytes unused
*/

/////////////// MAIN ////////////////////////////////

void main(){
gl_Position.x+=dummy*.000000000000001;
//if(dummy==123){/// DEBUG
    		ivec2 fetchPos	= ivec2(gl_VertexID%VTX_TEXTURE_USABLE_WIDTH,gl_VertexID/VTX_TEXTURE_USABLE_WIDTH);
    		gl_Position.xy	+=texelFetch(xyBuffer,fetchPos,0).xy;
    		gl_Position.z	= texelFetch(zBuffer,fetchPos,0).x;
    		w				= texelFetch(wBuffer,fetchPos,0).x;
    		gl_Position.w	= 1/w;
    		fragTexCoord	= texelFetch(uvBuffer,fetchPos,0).xy;
    		flatTextureID	= texelFetch(texIDBuffer,fetchPos,0).x;//TODO: Remove later
    		uint pid		= uint(gl_VertexID)/3u + 1u; // Add 1 so that zero represents 'unwritten.'
    		if(flatTextureID!=-1234)flatTextureID	= float(pid)/(65536);
			 screenLoc		= (((gl_Position.xy/gl_Position.w)+1)/2);
			vec2 normXY		= texelFetch(normXYBuffer,fetchPos,0).xy;
			float normZ		= texelFetch(normZBuffer,fetchPos,0).x;
						//Crunch this into [0,1] domain
			fragNormal 		= vec3(normXY,normZ);
			//}//DEBUG
	/*		
//DEBUG ///////////////////
else{
 ivec2 fetchPos	= ivec2(gl_VertexID%VTX_TEXTURE_USABLE_WIDTH,gl_VertexID/VTX_TEXTURE_USABLE_WIDTH);
 flatTextureID	= texelFetch(texIDBuffer,fetchPos,0).x;
 gl_Position.xy += pos[gl_VertexID%6];
 gl_Position.z = .1;
 gl_Position.w = 1;
 fragTexCoord = screenLocation[gl_VertexID%6];
 screenLoc		= (((gl_Position.xy/gl_Position.w)+1)/2);
 fragNormal = vec3(0,0,0);
 w = 1;
 }
 */
}//end main()
