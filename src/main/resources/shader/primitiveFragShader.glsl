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
flat in mat4 nXnYQuad;

//OUTPUTS
vec4 out		uvzwBuffer;
vec2 out		nXnYBuffer;

void main(){
 uint quadrant = gl_FragCoord.x%2+(gl_FragCoord.y%2)*2;
 uvzwBuffer = uvzwQuad[quadrant];
 nXnYBuffer = nXnYBuffer[quadrant];
 }
 