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

// Blended fragment rendering for stencil-routed K/A buffer referred internally as a 'depth queue'.
#version 330

// UNIFORMS
uniform		sampler2D	depthTexture; // Texture unit 0
uniform 	uint 	screenWidth;
uniform 	uint 	screenHeight;

// INPUTS
smooth in vec2 fragTexCoord;
smooth in uint texturePageIdx;
flat in uint flatTextureID;

// OUTPUTS
layout(location=0) out uvec4 pushToDepthQueue;

void main(){
vec2	screenLoc 	= vec2(gl_FragCoord.x/screenWidth,gl_FragCoord.y/screenHeight);
float 	depth 		= texture(depthTexture,screenLoc)[0];
if(gl_FragCoord.z>depth)discard;
pushToDepthQueue = uvec4(floatBitsToInt(fragTexCoord),flatTextureID,gl_FragCoord.z);
}