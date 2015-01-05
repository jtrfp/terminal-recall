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
const uint PRIM_TEXTURE_WIDTH		= 512u;
const uint PRIM_TEXTURE_HEIGHT		= 512u;
const uint VTX_TEXTURE_WIDTH		= 1024u;
const uint VTX_TEXTURE_HEIGHT		= 4096u;
const uint VTX_TEXTURE_USABLE_WIDTH = (VTX_TEXTURE_WIDTH/3u)*3u;//Quantize to floored multiple of 3.
const uint VTX_TEXTURE_USABLE_HEIGHT= (VTX_TEXTURE_HEIGHT/3u)*3u;
const uint PRIMS_PER_ROW			= PRIM_TEXTURE_WIDTH/PQUAD_SIDE_WIDTH;
const uint VERTICES_PER_ROW			= VTX_TEXTURE_USABLE_WIDTH;

const float PRIM_TEX_HEIGHT_SCALAR	= float(PQUAD_SIDE_WIDTH)/float(PRIM_TEXTURE_HEIGHT);
const float PRIM_TEX_WIDTH_SCALAR	= float(PQUAD_SIDE_WIDTH)/float(PRIM_TEXTURE_WIDTH);
const float VTX_TEX_HEIGHT_SCALAR   = 1/float(VTX_TEXTURE_USABLE_HEIGHT);
const float VTX_TEX_WIDTH_SCALAR    = 1/float(VTX_TEXTURE_USABLE_WIDTH);
const vec2 PRIM_TEX_CENTER_OFF		= vec2(PRIM_TEX_HEIGHT_SCALAR,PRIM_TEX_WIDTH_SCALAR);

//OUTPUTS
flat out mat4 uvzwQuad;
flat out mat4 nXnYnZQuad;// UNUSED: one float

//IN
uniform sampler2D		xyVBuffer;
uniform sampler2D		wVBuffer;
uniform sampler2D		zVBuffer;
uniform sampler2D		uvVBuffer;
uniform sampler2D		nXnYnZVBuffer;//TODO: Rename to nXnYVBuffer
uniform sampler2D		nZVBuffer;

//DUMMY
layout (location = 0) in float dummy;

//http://www.math.ucla.edu/~baker/149.1.02w/handouts/i_affine_II.pdf

mat3 affine(vec2 u, vec2 v, vec2 off){//Each row is a column
 return transpose(mat3(											// TODO: Manually transpose
 	u.x-off.x,  v.x-off.x,  off.x,
 	u.y-off.y,  v.y-off.y,	off.y,
 	0,          0,          1
 		));
 }//end affine()

void main(){
 gl_Position.x			= dummy*.00000001;//TODO: Need to compensate for point center offset.
 uint	pid				= uint(gl_VertexID);
 uint	row				= pid/PRIMS_PER_ROW;
 uint	col				= pid%PRIMS_PER_ROW;
 uint	primitiveIndex	= pid;
 uint	vertexIndex		= primitiveIndex*3u;
 gl_Position.xy			= PRIM_TEX_CENTER_OFF;
 gl_Position.x			+=((float(col))*PRIM_TEX_WIDTH_SCALAR*2)-1;
 gl_Position.y			+= ((float(row))*PRIM_TEX_HEIGHT_SCALAR*2)-1;
 const ivec2 increment	= ivec2(1,0);
 ivec2 v0				= ivec2(
 				vertexIndex%VERTICES_PER_ROW,
 				vertexIndex/VERTICES_PER_ROW);
 ////////////////////////////////////////////////TODO: reciprocal-W
 //Convert screen coords to normalized coords.
 mat3 debugAffine;
 
 // Convert from cartesian to normalized [0,1] by adding 1 and mul by .5
 // Also perform a perspective divide. Passed W is a reciprocal so a multiply is in order instead.
 vec3 wvb = vec3(
 	texelFetch(wVBuffer,v0,0).x,
 	texelFetchOffset(wVBuffer,v0,0,ivec2(1,0)).x,
 	texelFetchOffset(wVBuffer,v0,0,ivec2(2,0)).x);
 mat3 normalizationMatrix = inverse(debugAffine = affine(
  (texelFetchOffset(xyVBuffer,v0,0,ivec2(1,0)).xy*wvb[1u]+1)*.5,
  (texelFetchOffset(xyVBuffer,v0,0,ivec2(2,0)).xy*wvb[2u]+1)*.5,
  (texelFetch(xyVBuffer,v0,0).xy*wvb[0u]+1)*.5));
 //Convert normalized coords to uv coords
 mat3 uvMatrix = affine(
  vec2(texelFetchOffset(uvVBuffer,v0,0,ivec2(1,0)).xy),
  vec2(texelFetchOffset(uvVBuffer,v0,0,ivec2(2,0)).xy),
  vec2(texelFetch(uvVBuffer,v0,0).xy)
 	) * normalizationMatrix;
 //Convert normalized coords to vtx normals
 //TODO: Perform 3 texel fetches, swizzle for xy and z
 mat3 nXnYmatrix = affine(
  texelFetchOffset(nXnYnZVBuffer,v0,0,ivec2(1,0)).xy,
  texelFetchOffset(nXnYnZVBuffer,v0,0,ivec2(2,0)).xy,
  texelFetch(nXnYnZVBuffer,v0,0).xy
 	) * normalizationMatrix;
 mat3 nZmatrix = affine(// passed y-component is a dummy
  vec2(texelFetchOffset(nZVBuffer,v0,0,ivec2(1,0)).x,1),
  vec2(texelFetchOffset(nZVBuffer,v0,0,ivec2(2,0)).x,0),
  vec2(texelFetch(nZVBuffer,v0,0).x,.5)
 	) * normalizationMatrix;
 //Convert normalized coords to zw
 mat3 zwMatrix = affine(
  vec2(texelFetchOffset(zVBuffer,v0,0,ivec2(1,0)).x*wvb[1u],wvb[1u]),
  vec2(texelFetchOffset(zVBuffer,v0,0,ivec2(2,0)).x*wvb[2u],wvb[2u]),
  vec2(texelFetch(zVBuffer,v0,0).x*wvb[0u],wvb[0u])
 	) * normalizationMatrix;
 
 const vec3 topLeft    = vec3(0,1,1);
 const vec3 topRight   = vec3(1,1,1);
 const vec3 bottomLeft = vec3(0,0,1);
 const vec3 bottomRight= vec3(1,0,1);
 
 vec3 tlUV = uvMatrix*topLeft;
 vec3 trUV = uvMatrix*topRight;
 vec3 blUV = uvMatrix*bottomLeft;
 vec3 brUV = uvMatrix*bottomRight;
 
 uvzwQuad[0u]=vec4(vec2(blUV),vec2(zwMatrix*bottomLeft));
 uvzwQuad[1u]=vec4(vec2(brUV),vec2(zwMatrix*bottomRight));
 uvzwQuad[2u]=vec4(vec2(tlUV),vec2(zwMatrix*topLeft));
 uvzwQuad[3u]=vec4(vec2(trUV),vec2(zwMatrix*topRight));
 
 nXnYnZQuad[0u].xy=(nXnYmatrix*bottomLeft).xy;
 nXnYnZQuad[1u].xy=(nXnYmatrix*bottomRight).xy;
 nXnYnZQuad[2u].xy=(nXnYmatrix*topLeft).xy;
 nXnYnZQuad[3u].xy=(nXnYmatrix*topRight).xy;
 
 nXnYnZQuad[0u].z=(nZmatrix*bottomLeft).x;
 nXnYnZQuad[1u].z=(nZmatrix*bottomRight).x;
 nXnYnZQuad[2u].z=(nZmatrix*topLeft).x;
 nXnYnZQuad[3u].z=(nZmatrix*topRight).x;
 }//end main()
