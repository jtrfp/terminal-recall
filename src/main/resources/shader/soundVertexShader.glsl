/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2016 Chuck Ritola.
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

//Made fairly-convoluted w/ float ops to work with GL ES 2
#version 100

const float SAMPLES_PER_ROW		= 1024.;
const float  SAMPLES_PER_ROW_2	= SAMPLES_PER_ROW*2.;

// INPUTS
uniform vec2 pan;
uniform float start;
uniform float lengthPerRow;
uniform float numRows;

attribute float vertexID;

// OUTPUTS
varying float fragTexPos;
varying float fragRow;

//DUMMY
//layout (location = 0) in float dummy;

void main(){
 // U/V Zig-Zag pattern
 //float vertexID = float(gl_VertexID);
 float glvertexID2=floor((vertexID+1.) / 2.);
 float sweep = mod(glvertexID2,2.);
 float row = floor(vertexID / 2.);
 fragTexPos = sweep;
 fragRow = (row+.5)/numRows;
 float rowsX = float(glvertexID2 + vertexID/SAMPLES_PER_ROW_2);
 
 gl_Position.x= start+rowsX*lengthPerRow;
 gl_Position.yzw=vec3(0,1,1);
}