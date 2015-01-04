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

// Blended fragment rendering for stencil-routed A-buffer referred internally as a 'depth queue'.
#version 330

// UNIFORMS
uniform		sampler2D	depthTexture;

// INPUTS
flat in float flatTextureID;
noperspective in vec2 screenLoc;

// OUTPUTS
layout(location=0) out vec4 pushToDepthQueue;

void main(){
 float 	depth 		= texture(depthTexture,screenLoc)[0];
 if(gl_FragCoord.z>depth)discard;//TODO: Attach opaque Z-buffer and let fixed stages handle it.
 float tid = floor(flatTextureID * 65536);
 vec4 tidLMH = floor(mod(tid/vec4(1,16,256,4096),16));
 pushToDepthQueue = tidLMH;
}//end main()
