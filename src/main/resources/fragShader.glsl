/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
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
const uint RENDER_MODE_TRIANGLES=0u;
const uint RENDER_MODE_LINES=1u;

const uint PACKED_DATA_RENDER_MODE=0u;	//UNibble
const uint PACKED_DATA_COLOR_RED=1u;		//UNibble
const uint PACKED_DATA_COLOR_GREEN=2u;	//UNibble
const uint PACKED_DATA_COLOR_BLUE=3u;		//UNibble

// UNIFORMS
uniform sampler2D textureMap;
uniform vec3 fogColor;

// INPUTS
smooth in float fogLevel;
smooth in vec2 fragTexCoord;
flat in uint packedFragData;

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
switch(UNibble(packedFragData,PACKED_DATA_RENDER_MODE))
	{
case RENDER_MODE_TRIANGLES:
	gl_FragColor = mix(
	    texture2D(textureMap,fragTexCoord),
	    vec4(fogColor,1),
	    fogLevel);
	break;
case RENDER_MODE_LINES:
	float alpha;
	alpha=(1-abs(fragTexCoord.y*pow(fragTexCoord.x,.5)))*.5;
	gl_FragColor = mix(
	    vec4(
	    	float(UNibble(packedFragData,PACKED_DATA_COLOR_RED))/16,
	    	float(UNibble(packedFragData,PACKED_DATA_COLOR_GREEN))/16,
	    	float(UNibble(packedFragData,PACKED_DATA_COLOR_BLUE))/16,alpha
	    	),
	    	vec4(fogColor,1),
	    	fogLevel);
	break;
	}//end switch(RENDER_MODE)
}