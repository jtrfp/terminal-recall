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

// CONSTANTS
vec3 pos[6] = vec3[] 
	(
	vec3(-1,-1,0),
	vec3(1,-1,0),
	vec3(-1,1,0),
	vec3(-1,1,0),
	vec3(1,1,0),
	vec3(1,-1,0)
	);

void main(){
gl_Position.x=dummy*0;
gl_Position.xyz = pos[gl_VertexID];
}//end main()