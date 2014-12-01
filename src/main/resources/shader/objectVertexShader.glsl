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
 
#version 330

//CONSTANTS
const uint PAGE_SIZE_VEC4			= 96u;
const uint TEXELS_PER_MATRIX		= 4u;
const uint OBJECT_TEXTURE_WIDTH		= 1024u;
const uint OBJECT_TEXTURE_HEIGHT	= 128u;
const uint MATRICES_PER_ROW			= OBJECT_TEXTURE_WIDTH/TEXELS_PER_MATRIX;
const uint VERTICES_PER_ROW			= MATRICES_PER_ROW+1u;

const float OBJ_TEX_HEIGHT_SCALAR	= 1/float(OBJECT_TEXTURE_HEIGHT);
const float OBJ_TEX_WIDTH_SCALAR	= float(TEXELS_PER_MATRIX)/float(OBJECT_TEXTURE_WIDTH);

//OUTPUTS
flat out mat4 camMatrix;
flat out mat4 noCamMatrix;

//IN
uniform uint 			renderListPageTable[172];
uniform usamplerBuffer 	rootBuffer; 	//Global memory, as a set of uint vec4s.
uniform mat4 			cameraMatrix;
uniform float			objectBufferQuadIncrement;
uniform uint			logicalVec4Offset;

//DUMMY
layout (location = 0) in float dummy;

int renderListLogicalVEC42PhysicalVEC4(uint _logical){
	uint logical = _logical + logicalVec4Offset;
	return int(renderListPageTable
		[logical/PAGE_SIZE_VEC4]*PAGE_SIZE_VEC4
		+logical%PAGE_SIZE_VEC4);
	}//end renderListLogicalVEC42PhysicalVEC4(...)

uint UNibble(uint _input, uint index)
	{return (_input >> 4u*index) & 0x0000000Fu;}

uint UByte(uint _input, uint index)
	{return (_input >> 8u*index) & 0x000000FFu;}

void main(){
 gl_Position.x			= dummy==1234?1:0;
 uint	vid				= uint(gl_VertexID);
 uint	row				= vid/VERTICES_PER_ROW;
 uint	col				= vid%VERTICES_PER_ROW;
 int	objectIndex		= int(row*MATRICES_PER_ROW+col);
 gl_Position.x			+=(float(col)*OBJ_TEX_WIDTH_SCALAR*2)-1;
 gl_Position.y			= 1-(float(row)*OBJ_TEX_HEIGHT_SCALAR*2);
 int 	objectDefIndex	= int(texelFetch(rootBuffer,renderListLogicalVEC42PhysicalVEC4(uint(objectIndex/4)))[objectIndex%4]);
 uvec4 	objectDef 		= texelFetch(rootBuffer,objectDefIndex);
 int 	matrixOffset 	= int(objectDef[0]);
 uint renderMode 		= UByte(objectDef[2],1u);
 noCamMatrix		 	= mat4(
 						uintBitsToFloat(texelFetch(rootBuffer,matrixOffset)),
 						uintBitsToFloat(texelFetch(rootBuffer,matrixOffset+1)),
 						uintBitsToFloat(texelFetch(rootBuffer,matrixOffset+2)),
 						uintBitsToFloat(texelFetch(rootBuffer,matrixOffset+3)));
 uint 	skipCameraMatrix= UNibble(renderMode,1u);
 camMatrix = skipCameraMatrix==0u?cameraMatrix * noCamMatrix:noCamMatrix;
 }
 