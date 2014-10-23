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
 
///////// INCOMPLETE ///////////
 
#version 330

//CONSTANTS
const uint PAGE_SIZE_VEC4			= 96u;
const uint PQUAD_SIDE_WIDTH			= 2u;
const uint PRIM_TEXTURE_WIDTH		= 1024u;
const uint PRIM_TEXTURE_HEIGHT		= 4096u;
const uint PRIMS_PER_ROW			= PRIM_TEXTURE_WIDTH/PQUAD_SIDE_WIDTH;
const uint VERTICES_PER_ROW			= PRIMS_PER_ROW;

const float PRIM_TEX_HEIGHT_SCALAR	= float(PQUAD_SIDE_WIDTH)/float(PRIM_TEXTURE_HEIGHT);
const float PRIM_TEX_WIDTH_SCALAR	= float(PQUAD_SIDE_WIDTH)/float(PRIM_TEXTURE_WIDTH);

const vec2 POINT_CENTER_OFFSET		= PRIM_TEX_HEIGHT_SCALAR*2;

//OUTPUTS
flat out mat4 uvzwQuad;
flat out mat4 nXnYQuad;// UNUSED: two floats

//IN
uniform uint			primitiveOffset;
uniform sampler2D		xyVBuffer;
uniform sampler2D		wVBuffer;
uniform sampler2D		uvVBuffer;
uniform sampler2D		nXnYVBuffer;

//DUMMY
layout (location = 0) in float dummy;

mat3 affine(vec2 u, vec2 v, vec2 off){//Each row is a column! Feed it vector [x,y,1], gives [nX,nY,1]
 return mat3(
 	u.x-off.x,  u.y-off.y, 0,
 	v.x-off.x,  v.y-off.y, 0,
 	off.x,      off.y,     1
 		);
 }//end affine()

mat4 affine4(vec3 u, vec3 v, vec3 off){//Each row is a column! Feed it vector [x,y,1], gives [nX,nY,1]
 return mat4(
 	u.x-off.x,  u.y-off.y, u.z-off.z,0,
 	v.x-off.x,  v.y-off.y, v.z-off.z,0,
 	off.x,      off.y,     off.z,    1,
 	0,          0,         0,        0
 		);
 }//end affine4()

void main(){
 gl_Position.x			= dummy*.00000001;//TODO: Need to compensate for point center offset.
 uint	pid				= uint(gl_VertexID);
 uint	row				= pid/PRIMS_PER_ROW;
 uint	col				= pid%VERTICES_PER_ROW;
 int	primitiveIndex	= int(row*PRIMS_PER_ROW+col);
 int	vertexIndex		= primitiveIndex*3;
 gl_Position.x			+=(float(col)*PRIM_TEX_WIDTH_SCALAR*2f)-1f;
 gl_Position.y			= 1f-(float(row)*PRIM_TEX_HEIGHT_SCALAR*2f);
 //Convert screen coords to normalized coords.
 mat3 normalizationMatrix = inverse(affine(
  getVtxXY(vtx+1),
  getVtxXY(vtx+2),
  getVtxXY(vtx+0)));
 //Convert normalized coords to uv coords
 mat3 uvMatrix = affine(
  getVtxUV(vtx+1),
  getVtxUV(vtx+2),
  getVtxUV(vtx+0)
 	) * normalizationMatrix;
 //Convert normalized coords to vtx normals
 mat4 nXnYnZmatrix = affine3(
  getVtxnXnYnZ(vtx+1),
  getVtxnXnYnZ(vtx+2),
  getVtxnXnYnZ(vtx+0)
 	) * normalizationMatrix;
 //Convert normalized coords to zw
 mat3 nXnYnZmatrix = affine(
  getVtxZW(vtx+1),
  getVtxZW(vtx+2),
  getVtxZW(vtx+0)
 	) * normalizationMatrix;
 }//end main()
