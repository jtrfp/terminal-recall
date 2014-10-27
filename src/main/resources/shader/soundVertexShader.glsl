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
uniform vec2 pan;
uniform float start;
uniform float length;

// OUTPUTS
smooth out float fragTexPos;
flat out vec2 panLR;

void main(){
 panLR = pan;
 gl_Position.x=((start+(gl_VertexID%2)*length));
 fragTexPos=gl_VertexID%2;
 gl_Position.y=0;
 gl_Position.z=1;
 gl_Position.w=1;
 }
