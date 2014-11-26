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
const uint VTX_TEXTURE_WIDTH		= 1024u;
const uint VTX_TEXTURE_HEIGHT		= 4096u;
const uint PRIMS_PER_ROW			= PRIM_TEXTURE_WIDTH/PQUAD_SIDE_WIDTH;
const uint VERTICES_PER_ROW			= VTX_TEXTURE_WIDTH;

const float PRIM_TEX_HEIGHT_SCALAR	= float(PQUAD_SIDE_WIDTH)/float(PRIM_TEXTURE_HEIGHT);
const float PRIM_TEX_WIDTH_SCALAR	= float(PQUAD_SIDE_WIDTH)/float(PRIM_TEXTURE_WIDTH);
const float VTX_TEX_HEIGHT_SCALAR   = 1/float(VTX_TEXTURE_HEIGHT);
const float VTX_TEX_WIDTH_SCALAR    = 1/float(VTX_TEXTURE_WIDTH);

//OUTPUTS
flat out mat4 uvzwQuad;
flat out mat4 nXnYnZQuad;// UNUSED: one float

//IN
uniform sampler2D		xyVBuffer;
uniform sampler2D		wVBuffer;
uniform sampler2D		zVBuffer;
uniform sampler2D		uvVBuffer;
uniform sampler2D		nXnYnZVBuffer;

//DUMMY
layout (location = 0) in float dummy;

mat4 affine(vec3 u, vec3 v, vec3 off){//Each row is a column! Feed it vector [x,y,z,1], gives [nX,nY,nZ,1]
 return mat4(
 	u.x-off.x,  u.y-off.y, u.z-off.z,0,
 	v.x-off.x,  v.y-off.y, v.z-off.z,0,
 	off.x,      off.y,     off.z,    1,
 	0,          0,         0,        0
 		);
 }//end affine()

void main(){
 gl_Position.x			= dummy*.00000001;//TODO: Need to compensate for point center offset.
 uint	pid				= uint(gl_VertexID);
 uint	row				= pid/PRIMS_PER_ROW;
 uint	col				= pid%PRIMS_PER_ROW;
 uint	primitiveIndex	= row*PRIMS_PER_ROW+col;
 uint	vertexIndex		= primitiveIndex*3u;
 gl_Position.x			+=((float(col)+.5)*PRIM_TEX_WIDTH_SCALAR*2)-1;
 gl_Position.y			= 1-((float(row)+.5)*PRIM_TEX_HEIGHT_SCALAR*2);
 ivec2 increment		= ivec2(1,0);
 ivec2 v0				= ivec2(
 				vertexIndex%VERTICES_PER_ROW,
 				vertexIndex/VERTICES_PER_ROW);
 ivec2 v1				= increment*1+v0;
 ivec2 v2				= increment*2+v0;
 ////////////////////////////////////////////////TODO: reciprocal-W
 //Convert screen coords to normalized coords.
 mat4 normalizationMatrix = inverse(affine(
  vec3(texelFetch(xyVBuffer,v1,0).xy,0),
  vec3(texelFetch(xyVBuffer,v2,0).xy,0),
  vec3(texelFetch(xyVBuffer,v0,0).xy,0)));
 //Convert normalized coords to uv coords
 mat4 uvMatrix = affine(
  vec3(texelFetch(uvVBuffer,v1,0).xy,0),
  vec3(texelFetch(uvVBuffer,v2,0).xy,0),
  vec3(texelFetch(uvVBuffer,v0,0).xy,0)
 	) * normalizationMatrix;
 //Convert normalized coords to vtx normals
 mat4 nXnYnZmatrix = affine(
  texelFetch(nXnYnZVBuffer,v1,0).xyz,
  texelFetch(nXnYnZVBuffer,v2,0).xyz,
  texelFetch(nXnYnZVBuffer,v0,0).xyz
 	) * normalizationMatrix;
 //Convert normalized coords to zw
 mat4 zwMatrix = affine(
  vec3(texelFetch(zVBuffer,v1,0).x,texelFetch(wVBuffer,v1,0).x,0),
  vec3(texelFetch(zVBuffer,v2,0).x,texelFetch(wVBuffer,v2,0).x,0),
  vec3(texelFetch(zVBuffer,v0,0).x,texelFetch(wVBuffer,v0,0).x,0)
 	) * normalizationMatrix;
 
 const vec4 topLeft    = vec4(-1,1,0,1);
 const vec4 topRight   = vec4(1,1,0,1);
 const vec4 bottomLeft = vec4(-1,-1,0,1);
 const vec4 bottomRight= vec4(1,-1,0,1);
 
 uvzwQuad[0u]=vec4(vec2(uvMatrix*topLeft),vec2(zwMatrix*topLeft));
 uvzwQuad[1u]=vec4(vec2(uvMatrix*topRight),vec2(zwMatrix*topRight));
 uvzwQuad[2u]=vec4(vec2(uvMatrix*bottomLeft),vec2(zwMatrix*bottomLeft));
 uvzwQuad[3u]=vec4(vec2(uvMatrix*bottomRight),vec2(zwMatrix*bottomRight));
 
 nXnYnZQuad[0u]=nXnYnZmatrix*topLeft;
 nXnYnZQuad[1u]=nXnYnZmatrix*topRight;
 nXnYnZQuad[2u]=nXnYnZmatrix*bottomLeft;
 nXnYnZQuad[3u]=nXnYnZmatrix*bottomRight;
 }//end main()
