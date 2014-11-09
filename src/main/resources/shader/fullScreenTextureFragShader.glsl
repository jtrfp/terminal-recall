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

// Fills the screen with a texture.

#version 330

// INPUTS
noperspective in vec2 screenLoc;
uniform sampler2D textureToUse;
uniform vec4 scalar;
uniform vec4 offset;

// OUTPUTS
layout (location = 0) out vec4 color;

void main(){
 color = clamp(texture(textureToUse,screenLoc)*scalar+offset,0,1);
 }
