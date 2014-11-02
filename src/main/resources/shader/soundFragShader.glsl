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

// INPUTS
uniform sampler2D soundTexture;
noperspective in float fragTexPos;
noperspective in float fragRow;
//flat in float vid;
flat in vec2 panLR;

// OUTPUTS
layout (location = 0) out vec2 leftRightOut;

void main(){
 leftRightOut.rg = panLR * texture(soundTexture,vec2(fragTexPos,fragRow),0).r /** .0000000001 + vec2(fragTexPos*2-1,fragRow*2-1)*/;
 }
