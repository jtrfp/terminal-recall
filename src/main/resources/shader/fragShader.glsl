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

//#define DEBUG 1

// CONSTANTS

// UNIFORMS
uniform sampler2D texturePalette;
uniform int useTextureMap;

// INPUTS
smooth in vec2 fragTexCoord;
smooth in vec3 norm;

// OUTPUTS
layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec3 fragNormal;

uint bit(uint _input, uint index)
	{return (_input >> index) & 0x00000001u;}

float alphaFromProximity(vec3 clearColor, vec3 testColor, float sharpness)
	{
	vec3 delta = vec3(1,1,1)-abs(clearColor-testColor);
	return 1-pow(delta.r*delta.g*delta.b,sharpness);
	}

uint UNibble(uint _input, uint index)
	{return (_input >> 4u*index) & 0x0000000Fu;}

void main()
{
fragNormal=norm;//Pass it along
	if(useTextureMap!=0)
		{fragColor = texture2D(texturePalette,fragTexCoord);}
		else{fragColor.rg = fragTexCoord;}
}