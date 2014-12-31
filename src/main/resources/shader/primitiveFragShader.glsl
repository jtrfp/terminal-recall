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

//INPUTS
flat in mat4 uvzwQuad;
flat in mat4 nXnYnZQuad;

//OUTPUTS
layout(location = 0) out vec4	uvzwBuffer;
layout(location = 1) out vec4	nXnYnZBuffer;

const float OVERSAMPLING         = 4;

void main(){
 // +1 to invert the row order for the upside-down GL textures.
 //uint quadrant = uint(gl_FragCoord.x)%2u+(uint(gl_FragCoord.y)%2u)*2u;
 vec2 coord = (mod(floor(gl_FragCoord.xy),2*OVERSAMPLING))/(OVERSAMPLING);
 
 // Appears both Intel and AMD aren't getting this var right.
 // https://software.intel.com/en-us/forums/topic/286351
 // https://www.opengl.org/discussion_boards/showthread.php/166384-gl_PointCoord-is-giving-me-junk-values!
 // The AMD bug is quite old and supposedly fixed but I seem to be getting it in fglrx 14.501.1003.
 // TODO: Set up a confirm case to verify that it isn't just a bug in this program.
 //vec2 coord = gl_PointCoord.st;
 
 uvzwBuffer = mix(
 				mix(uvzwQuad[0u],uvzwQuad[1u],coord.x),
 				mix(uvzwQuad[2u],uvzwQuad[3u],coord.x),
 				coord.y);
 nXnYnZBuffer = mix(
 				mix(nXnYnZQuad[0u],nXnYnZQuad[1u],coord.x),
 				mix(nXnYnZQuad[2u],nXnYnZQuad[3u],coord.x),
 				coord.y);
 }
 