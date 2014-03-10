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
uniform vec3 fogColor;
uniform uint screenWidth;
uniform uint screenHeight;

// OUTPUTS
layout(location = 0) out vec4 fragColor;

void main()
{
vec2 primaryUV = vec2(gl_FragCoord.x/screenWidth,gl_FragCoord.y/screenHeight);
float depth = texture2D(depthTexture,primaryUV)[0];
// FOG
float fogLevel;
fogLevel=pow(depth,80);
gl_FragDepth = depth;
fragColor = texture2D(primaryRendering,primaryUV);
fragColor = mix(fragColor,vec4(fogColor,1),fogLevel);
}