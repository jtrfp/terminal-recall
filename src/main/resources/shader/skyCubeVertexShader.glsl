/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2015 Chuck Ritola and contributors.
 * See Github project's commit log for contribution details.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 ******************************************************************************/

// Sky Cube

#version 330

// INPUTS
uniform mat4 projectionRotationMatrix;

// OUTPUTS
smooth out vec3 norm;

// http://antongerdelan.net/opengl/cubemaps.html

vec3 coords[] = vec3[36](
  vec3(-1.0,  1.0, -1.0),
  vec3(-1.0, -1.0, -1.0),
  vec3(1.0, -1.0, -1.0),
  vec3(1.0, -1.0, -1.0),
  vec3(1.0,  1.0, -1.0),
  vec3(-1.0,  1.0, -1.0),
  
  vec3(-1.0, -1.0,  1.0),
  vec3(-1.0, -1.0, -1.0),
  vec3(-1.0,  1.0, -1.0),
  vec3(-1.0,  1.0, -1.0),
  vec3(-1.0,  1.0,  1.0),
  vec3(-1.0, -1.0,  1.0),
  
  vec3(1.0, -1.0, -1.0),
  vec3(1.0, -1.0,  1.0),
  vec3(1.0,  1.0,  1.0),
  vec3(1.0,  1.0,  1.0),
  vec3(1.0,  1.0, -1.0),
  vec3(1.0, -1.0, -1.0),
   
  vec3(-1.0, -1.0,  1.0),
  vec3(-1.0,  1.0,  1.0),
  vec3(1.0,  1.0,  1.0),
  vec3(1.0,  1.0,  1.0),
  vec3(1.0, -1.0,  1.0),
  vec3(-1.0, -1.0,  1.0),
  
  vec3(-1.0,  1.0, -1.0),
  vec3(1.0,  1.0, -1.0),
  vec3(1.0,  1.0,  1.0),
  vec3(1.0,  1.0,  1.0),
  vec3(-1.0,  1.0,  1.0),
  vec3(-1.0,  1.0, -1.0),
  
  vec3(-1.0, -1.0, -1.0),
  vec3(-1.0, -1.0,  1.0),
  vec3(1.0, -1.0, -1.0),
  vec3(1.0, -1.0, -1.0),
  vec3(-1.0, -1.0,  1.0),
  vec3(1.0, -1.0,  1.0)
 );

void main(){
 gl_Position = projectionRotationMatrix * vec4(10000 * (norm = coords[uint(gl_VertexID)]),1);
}
