/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2016 Chuck Ritola.
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
//Converted to 1.0 for ES 2.0 compatability
#version 100

precision mediump float;

// INPUTS
uniform lowp vec2 pan;
uniform sampler2D soundTexture;
varying float fragTexPos;
varying float fragRow;

// OUTPUTS
//layout (location = 0) out vec2 leftRightOut;

void main(){
 gl_FragColor.rg = pan * texture2D(soundTexture,vec2(fragTexPos,fragRow)).r /** .0000000001 + vec2(fragTexPos*2-1,fragRow*2-1)*/;
 }
