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

const uint SAMPLES_PER_ROW		= 1024u;

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
 
 int sweep = int((gl_VertexID+1) / 2) % 2;
 int row = gl_VertexID / 2;
 fragTexPos = sweep;
 float texelHeight = 1/float(numRows);
 fragRow = (float(row)*texelHeight) + texelHeight/2;
 float rowsX = floor((gl_VertexID+1)/2) + floor(gl_VertexID/2)/float(SAMPLES_PER_ROW);
 vid = gl_VertexID / 64;
 
 panLR = pan;
 gl_Position.x= dummy * .0000000001 + start+rowsX*lengthPerRow;
 gl_Position.y=0;
 gl_Position.z=1;
 gl_Position.w=1;
 /*
 //// DEBUG
 gl_Position.x*=dummy==1234?0:1;
 gl_Position.x+=(gl_VertexID==0?-1:1);
 gl_Position.y=0;
 gl_Position.z=1;
 gl_Position.w=1;
 */
 }
