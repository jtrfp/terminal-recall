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

// Blended fragment rendering for float-shift queue.
#version 330

// UNIFORMS
//uniform		sampler2D	depthTexture;

// INPUTS
flat in vec4 flatDQPrimID0;
flat in vec4 flatDQPrimID1;

// OUTPUTS
layout(location=0) out vec4 pushToDepthQueue0;
layout(location=1) out vec4 pushToDepthQueue1;

void main(){
 pushToDepthQueue0 = flatDQPrimID0;
 pushToDepthQueue1 = flatDQPrimID1;
}//end main()
