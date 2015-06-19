/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2015 Chuck Ritola.
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

// Fills the screen with an attributeless 1-triangle pseudo-quad
// http://image.slidesharecdn.com/vertexshadertricks-billbilodeau-140403092257-phpapp01/95/vertex-shader-tricks-by-bill-bilodeau-amd-at-gdc14-12-638.jpg?cb=1407856474

#version 330

// INPUTS

// OUTPUTS
noperspective out vec2 screenLoc;

void main(){
vec2 pos = vec2((gl_VertexID%2),gl_VertexID/2)*3;
gl_Position.xy 	= pos*2-1;
gl_Position.w   = 1;
screenLoc 		= pos;
}//end main()