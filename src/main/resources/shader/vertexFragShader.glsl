/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2014 Chuck Ritola.
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
 
// Vertex Processor.

#version 330

//CONSTANTS
const float COORD_DOWNSCALER		=512+1;

const uint PACKED_DATA_RENDER_MODE	=0u;	//UNibble

const int GPU_VERTICES_PER_BLOCK	=96;
const uint PAGE_SIZE_VEC4			=96u;

//const float V_COORD_PACK_SCALE		=16;

// INPUTS
uniform uint 			renderListPageTable[172];
uniform uint			logicalVec4Offset;
uniform usamplerBuffer 	rootBuffer; 	//Global memory, as a set of uint vec4s.
uniform sampler2D		camMatrixBuffer;
uniform sampler2D		noCamMatrixBuffer;

// OUTPUTS
layout (location=0) out vec2 xy;
layout (location=1) out vec2 uv;
layout (location=2) out float z;
layout (location=3) out float w;
layout (location=4) out float flatTextureID;
layout (location=5) out vec2 nXY;
layout (location=6) out float nZ;

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

int renderListLogicalVEC42PhysicalVEC4(uint _logical){
	uint logical = _logical + logicalVec4Offset;
	return int(renderListPageTable
		[logical/PAGE_SIZE_VEC4]*PAGE_SIZE_VEC4
		+logical%PAGE_SIZE_VEC4);
	}//end renderListLogicalVEC42PhysicalVEC4(...)

////////////// STRUCT LAYOUTS ///////////////

/*Object definition VEC4
	uint matrix offset
	uint vertex offset
	byte numVerticesInBlock, byte primitiveRenderMode, byte modelScalar, byte ???
	uint ???
*/

/*Triangle Vertex VEC4
	uint short x,y // XYZ Scaled by COORD_DOWNSCALER
	uint short z, byte normX, byte normY
	uint short u,v // scaled by 4096
	uint byte normZ, textureIDlo, textureIDmid, textureIDhi // 3 bytes unused
*/

void main(){
 int 	vertexIndex 			= int(gl_FragCoord.x)+int(gl_FragCoord.y)*1024;
 int 	objectIndex 			= (vertexIndex / GPU_VERTICES_PER_BLOCK);
 int 	intraObjectVertexIndex 	= vertexIndex % GPU_VERTICES_PER_BLOCK;
 int 	objectDefIndex			= int(texelFetch(rootBuffer,renderListLogicalVEC42PhysicalVEC4(uint(objectIndex/4)))[objectIndex%4]);
 uvec4 	objectDef 				= texelFetch(rootBuffer,objectDefIndex);
 int	numVertices 			= int(UByte(objectDef[2],0u));
 
 if(intraObjectVertexIndex<numVertices){
			uint renderMode 	= UByte(objectDef[2],1u);
			int matrixOffset 	= int(objectDef[0]);
			int vertexOffset 	= int(objectDef[1]);
			int modelScalar 	= int(UByte(objectDef[2],2u))-16;//Numerical domain offset for negatives
			mat4 matrixNoCam 	= mat4(uintBitsToFloat(texelFetch(rootBuffer,matrixOffset)),uintBitsToFloat(texelFetch(rootBuffer,matrixOffset+1)),
										uintBitsToFloat(texelFetch(rootBuffer,matrixOffset+2)),uintBitsToFloat(texelFetch(rootBuffer,matrixOffset+3)));
			ivec2 mOff			= ivec2((objectIndex%256)*4,127-(objectIndex/256));
			mat4 matrix			= mat4(
								texelFetch(camMatrixBuffer,mOff,0),
								texelFetch(camMatrixBuffer,mOff+ivec2(1,0),0),
								texelFetch(camMatrixBuffer,mOff+ivec2(2,0),0),
								texelFetch(camMatrixBuffer,mOff+ivec2(3,0),0));
			/*mat4 matrixNoCam	= mat4(
								texelFetch(noCamMatrixBuffer,mOff,0),
								texelFetch(noCamMatrixBuffer,mOff+ivec2(1,0),0),
								texelFetch(noCamMatrixBuffer,mOff+ivec2(2,0),0),
								texelFetch(noCamMatrixBuffer,mOff+ivec2(3,0),0));*/
			// objectDef[3] unused.
			uint 	skipCameraMatrix= UNibble(renderMode,PACKED_DATA_RENDER_MODE);
			uvec4 	packedVertex 	= texelFetch(rootBuffer,vertexOffset+intraObjectVertexIndex);
			flatTextureID 			= uintBitsToFloat(PAGE_SIZE_VEC4 * (UByte(packedVertex[3u],1u) | (UByte(packedVertex[3u],2u) << 8u ) | (UByte(packedVertex[3u],3u) << 16u)));
			vec4 	vertexCoord;
			vertexCoord.xyz 		= exp2(float(modelScalar))*vec3(float(firstSShort(packedVertex[0])),float(secondSShort(packedVertex[0])),
												float(firstSShort(packedVertex[1])));
			vertexCoord.w=1;
    		vec2 fragTexCoord 		= vec2(float(firstSShort(packedVertex[2]))/4096.,float(secondSShort(packedVertex[2]))/4096.);
    		vec4 position 			= matrix * vertexCoord;
			xy.xy					= (position.xy);
			uv.xy					= fragTexCoord.xy;
			w						= position.w;
			z						= position.z;
			vec4 nNoCam				= matrixNoCam * (vec4(float(SByte(packedVertex[1],2u))/128,
										   float(SByte(packedVertex[1],3u))/128,
										   float(SByte(packedVertex[3],0u))/128, 0));
    		nXY			 			= nNoCam.xy;
			nZ 						= nNoCam.z;
    		}//end if(object)
    		
    		else{
    		 xy=vec2(0,0);
    		 uv=vec2(0,0);
    		 z=0;
    		 w=0;
    		 flatTextureID=0;
    		}//end not-in-block
}//end main()
