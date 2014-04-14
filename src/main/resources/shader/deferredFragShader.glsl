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

// Deferred fragment rendering.

#version 330

// INPUTS
uniform sampler2D primaryRendering;
uniform sampler2D depthTexture;
uniform sampler2D texturePalette;
uniform sampler2D normTexture;
uniform usampler2D textureIDTexture;
uniform sampler2DArray rgbaTiles;
uniform usamplerBuffer rootBuffer; 	//Global memory, as a set of uint vec4s.
uniform vec3 fogColor;
uniform uint screenWidth;
uniform uint screenHeight;
uniform vec3 sunVector;

// OUTPUTS
layout(location = 0) out vec4 fragColor;
const vec3 sunColor = vec3(1.4,1.4,1.2);


//Adapted from http://www.geeks3d.com/20091216/geexlab-how-to-visualize-the-depth-buffer-in-glsl/
float linearizeDepth(float z)
{
float zNear = 6554;
float zFar = 1114112;
z = (2*zNear) / (zFar + zNear - z * (zFar - zNear));
return z;
}

void main()
{
vec2 screenLoc=vec2(gl_FragCoord.x/screenWidth,gl_FragCoord.y/screenHeight);
float depth = texture(depthTexture,screenLoc)[0];
gl_FragDepth = depth;
float linearDepth = linearizeDepth(depth);
uint textureID = texture(textureIDTexture,screenLoc)[0u];
fragColor = (textureID==10u?texture(primaryRendering,screenLoc):vec4(0,0,0,0));//GET UV
//vec3 origColor = textureGrad(texturePalette,fragColor.xy,dFdx(fragColor.xy),dFdy(fragColor.xy)).rgb;//GET COLOR
vec3 origColor = texture(texturePalette,fragColor.xy).rgb;//GET COLOR
vec3 norm = texture(normTexture,screenLoc).xyz*2-vec3(1,1,1);//UNPACK NORM

uvec4 toc = texelFetch(rootBuffer,int(fragColor.x*64));
uint indexPage;
uint codeBook;
uint tileID;
uvec4 tile;

//norm += float(toc[0])*.00000000001;


// DUMMY CODE TO SIMULATE PROCESSING LOAD OF FUTURE IMPLEMENTATION
for(int i=0;i<1;i++)
	{
	indexPage = texelFetch(rootBuffer,int(toc[0u])+i).x;
	codeBook = texelFetch(rootBuffer,int(indexPage)).y;
	tileID = texelFetch(rootBuffer,int(codeBook)).z;
	tileID = texelFetch(rootBuffer,int(tileID)).z;
	tile = texelFetch(rootBuffer,int(tileID));
	norm += float(tile.w)*.000000000001;
	}

// Illumination. Near-zero norm means assume full lighting
float sunIllumination = length(norm)>.1?clamp(dot(sunVector,normalize(norm)),0,1):.5;
fragColor.rgb =origColor*fogColor+origColor*sunIllumination*sunColor;
fragColor.r += (texture(rgbaTiles,vec3(.5,.5,1)).x*.00000000001);//DUMMY
fragColor = mix(fragColor,vec4(fogColor*sunColor,1),clamp(pow(linearDepth,3)*1.5,0,1));//FOG
}