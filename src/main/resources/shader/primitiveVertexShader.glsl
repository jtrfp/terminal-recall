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

void main(){
 gl_Position.x			= dummy*.00000001;//TODO: Need to compensate for point center offset.
 uint	pid				= uint(gl_VertexID);
 uint	row				= pid/PRIMS_PER_ROW;
 uint	col				= pid%VERTICES_PER_ROW;
 int	primitiveIndex	= int(row*PRIMS_PER_ROW+col);
 int	vertexIndex		= primitiveIndex*3;
 gl_Position.x			+=(float(col)*PRIM_TEX_WIDTH_SCALAR*2f)-1f;
 gl_Position.y			= 1f-(float(row)*PRIM_TEX_HEIGHT_SCALAR*2f);
 vec4 v0				= getVtxUVZW(pid,0);
 vec4 v1				= getVtxUVZW(pid,1);
 vec4 v2				= getVtxUVZW(pid,2);
 }//end main()
