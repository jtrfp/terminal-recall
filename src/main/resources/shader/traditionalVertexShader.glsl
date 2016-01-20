/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2015 Chuck Ritola and contributors.
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
flat out float 			flatPrimitiveID;
flat out vec4			flatDQPrimID0;
flat out vec4			flatDQPrimID1;

//IN
uniform uint 			renderListPageTable[256];
uniform mat4 			cameraMatrix;
uniform sampler2D		xyBuffer;
uniform sampler2D		uvBuffer;
uniform sampler2D		zBuffer;
uniform sampler2D		wBuffer;
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
gl_Position.xy = vec2(0);
gl_Position.x+=dummy==1234?.0000000001:0;
 ivec2 fetchPos	= ivec2(gl_VertexID%VTX_TEXTURE_USABLE_WIDTH,gl_VertexID/VTX_TEXTURE_USABLE_WIDTH);
 gl_Position.xy	+=texelFetch(xyBuffer,fetchPos,0).xy;
 gl_Position.z	= texelFetch(zBuffer,fetchPos,0).x;
 gl_Position.w	= 1/texelFetch(wBuffer,fetchPos,0).x;
 float pid		= float(gl_VertexID/3 + 1); // Add 1 so that zero represents 'unwritten.'
 flatPrimitiveID	= pid/65536;
 const vec4 LO_DIVISOR = vec4(1,4,16,64);
 const vec4 HI_DIVISOR = vec4(256,1024,4096,16384);
 
 flatDQPrimID0 = floor(mod(pid/LO_DIVISOR,4))/65536;
 flatDQPrimID1 = floor(mod(pid/HI_DIVISOR,4))/65536;
}//end main()
