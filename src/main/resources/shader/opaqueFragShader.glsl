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

// CONSTANTS

// UNIFORMS
uniform sampler2D	texturePalette;
uniform int			useTextureMap;

// INPUTS
smooth in vec2 fragTexCoord;
smooth in vec3 fragNormal;
flat in float  flatTextureID;

// OUTPUTS
layout(location = 0) out vec2  fragTexCoordOut;
layout(location = 1) out vec3  fragNormalOut;
layout(location = 2) out float textureID;

uint bit(uint _input, uint index)
	{return (_input >> index) & 0x00000001u;}

uint UNibble(uint _input, uint index)
	{return (_input >> 4u*index) & 0x0000000Fu;}

void main(){
fragTexCoordOut.rg = fragTexCoord;
textureID	       = flatTextureID;
fragNormalOut      = fragNormal;//Pass it along
}//end main()
