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

// Fills the screen with an attributeless quad.

#version 330

// INPUTS
layout (location = 0) in float dummy;

// OUTPUTS
noperspective out vec2 screenLoc;

// CONSTANTS
vec2 pos[6] = vec2[] 
	(
	vec2(-1,-1),
	vec2(1,-1),
	vec2(-1,1),
	vec2(-1,1),
	vec2(1,-1),
	vec2(1,1)
	);
vec2 screenLocation[6] = vec2[]
	(
	vec2(0,0),
	vec2(1,0),
	vec2(0,1),
	vec2(0,1),
	vec2(1,0),
	vec2(1,1)
	);

void main(){
gl_Position.w   = 1;
gl_Position.xy 	= dummy==1234?vec2(0,0):pos[gl_VertexID];
//gl_Position.xy 	= pos[gl_VertexID];
screenLoc 		= screenLocation[gl_VertexID];
}//end main()