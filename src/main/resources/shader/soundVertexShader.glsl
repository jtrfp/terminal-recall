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

const int SAMPLES_PER_ROW		= 1024;
const int  SAMPLES_PER_ROW_2	= SAMPLES_PER_ROW*2;

// INPUTS
uniform vec2 pan;
uniform float start;
uniform float lengthPerRow;
uniform uint numRows;

// OUTPUTS
noperspective out float fragTexPos;
noperspective out float fragRow;
flat out float vid;
flat out vec2 panLR;

//DUMMY
layout (location = 0) in float dummy;

void main(){
 // U/V Zig-Zag pattern
 int glvid2=int((gl_VertexID+1) / 2);
 int sweep = glvid2 % 2;
 int row = gl_VertexID / 2;
 fragTexPos = sweep;
 fragRow = ((float(row)+.5)/float(numRows));
 float rowsX = float(glvid2 + gl_VertexID/SAMPLES_PER_ROW_2);
 vid = gl_VertexID / 64;
 
 panLR = pan;
 gl_Position.x= (dummy==1234?.0000000001:0) + start+rowsX*lengthPerRow;
 gl_Position.yzw=vec3(0,1,1);
}