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
uniform sampler2D 		primaryRendering;
uniform sampler2D 		depthTexture;
uniform sampler2D 		texturePalette;
uniform sampler2D 		normTexture;
uniform usampler2D 		textureIDTexture;
uniform sampler2DArray 	rgbaTiles;
uniform usamplerBuffer 	rootBuffer; 	//Global memory, as a set of uint vec4s.
uniform sampler2DMS		depthQueueTexture;
uniform vec3 			fogColor;
uniform uint 			screenWidth;
uniform uint 			screenHeight;
uniform vec3 			sunVector;

// OUTPUTS
layout(location = 0) out vec4 fragColor;

// CONSTANTS
const uint TOC_OFFSET_VEC4_HEADER				=91u;//1456/16
const uint TOC_HEADER_OFFSET_QUADS_WIDTH		=0u;
const uint TOC_HEADER_OFFSET_QUADS_HEIGHT		=1u;
const uint TOC_HEADER_OFFSET_QUADS_RENDER_FLAGS	=2u;
const uint TOC_HEADER_OFFSET_QUADS_MAGIC		=3u;

const uint RENDER_FLAGS_WRAP					=0x1u;

const float TILE_PAGE_SIDE_WIDTH_TEXELS = 128;
const uint CODE_SIDE_WIDTH_TEXELS 		= 4u;
const uint CODE_PAGE_SIDE_WIDTH_CODES	= uint(TILE_PAGE_SIDE_WIDTH_TEXELS) / CODE_SIDE_WIDTH_TEXELS;
const uint CODES_PER_CODE_PAGE 			= CODE_PAGE_SIDE_WIDTH_CODES * CODE_PAGE_SIDE_WIDTH_CODES;
const uint CODE_PAGE_SIDE_WIDTH_TEXELS	= CODE_PAGE_SIDE_WIDTH_CODES * CODE_SIDE_WIDTH_TEXELS;
const float CODE_PAGE_TEXEL_SIZE_UV		= 1/float(CODE_PAGE_SIDE_WIDTH_TEXELS);

const uint SUBTEXTURE_SIDE_WIDTH_CODES  = 38u;
const uint SUBTEXTURE_SIDE_WIDTH_TEXELS = SUBTEXTURE_SIDE_WIDTH_CODES * CODE_SIDE_WIDTH_TEXELS;
const uint SUBTEXTURE_START_CODE_TABLE_OFFSET_VEC4
										= 91u;

const vec3 sunColor 					= vec3(1.4,1.4,1.2);

const int DEPTH_QUEUE_SIZE				= 8;

//Adapted from http://www.geeks3d.com/20091216/geexlab-how-to-visualize-the-depth-buffer-in-glsl/
float linearizeDepth(float z){
float zNear = 6554;
float zFar = 1114112;
z = (2*zNear) / (zFar + zNear - z * (zFar - zNear));
return z;
}

uint UByte(uint _input, uint index)
	{return (_input >> 8u*index) & 0x000000FFu;}

vec4 codeTexel(vec2 texelXY, uint textureID, vec2 tDims, uint renderFlags){
 		texelXY		= (renderFlags&RENDER_FLAGS_WRAP)!=0u?mod(texelXY,tDims):clamp(texelXY,vec2(0,0),tDims-vec2(1,1));
 vec2	codeXY		= mod(texelXY,float(CODE_SIDE_WIDTH_TEXELS));
 // Clamp sub-pixels within vector.
 codeXY				= clamp(codeXY,0,3)+vec2(.5,.5);
 vec2	subTexXY	= mod(texelXY,SUBTEXTURE_SIDE_WIDTH_TEXELS);
 uint	tTOCIdx		= uint(texelXY.x)/SUBTEXTURE_SIDE_WIDTH_TEXELS + (uint(texelXY.y)/SUBTEXTURE_SIDE_WIDTH_TEXELS) * 19u;
 uint	tTOCvec4Idx	= tTOCIdx / 4u;
 uint	tTOCsubIdx	= tTOCIdx % 4u;
 // Sub-Texture
 uint	subTexV4Addr= texelFetch(rootBuffer,int(textureID+tTOCvec4Idx))[tTOCsubIdx];
 vec2	subTexUVblnd= mod(texelXY,CODE_PAGE_TEXEL_SIZE_UV);//Subtexel to blend between texels
 uint	subTexByIdx = (uint(subTexXY.x)/CODE_SIDE_WIDTH_TEXELS + (uint(subTexXY.y)/CODE_SIDE_WIDTH_TEXELS) * SUBTEXTURE_SIDE_WIDTH_CODES);
 uint	startCodeIdx= subTexByIdx/256u;
 uint	startCode	= texelFetch(rootBuffer,int(subTexV4Addr+SUBTEXTURE_START_CODE_TABLE_OFFSET_VEC4+(startCodeIdx/4u)))[startCodeIdx%4u];
 uint	subTexV4Idx	= subTexByIdx / 16u;
 uint	subTexV4Sub = subTexByIdx % 16u;
 // Codebook
 uint	codeIdx		= UByte((texelFetch(rootBuffer,int(subTexV4Idx+subTexV4Addr))[subTexV4Sub/4u]),subTexV4Sub%4u)+startCode;
 uint	codeBkPgNum	= codeIdx / CODES_PER_CODE_PAGE;
 vec2	subTexUVsub	= codeXY*CODE_PAGE_TEXEL_SIZE_UV;
 vec2	codePgUV	= (vec2(float(codeIdx % CODE_PAGE_SIDE_WIDTH_CODES),float((codeIdx / CODE_PAGE_SIDE_WIDTH_CODES)%CODE_PAGE_SIDE_WIDTH_CODES))/float(CODE_PAGE_SIDE_WIDTH_CODES))+subTexUVsub;
 return				  texture(rgbaTiles,vec3(codePgUV,codeBkPgNum));
 }
 
 vec4 intrinsicCodeTexel(float linearDepth,uint textureID,vec3 norm,vec2 uv){
 // TOC
 if(textureID==0u)return vec4(0,1,0,1);//Green means textureID=zero
 //if(textureID>100024u||textureID<0u)return vec4(1,1,0,1);//Yellow means 0xDEADBEEF (unwritten) reverse[4022250974][3735928559u]
 uvec4 	tocHeader 	= texelFetch(rootBuffer,int(textureID+TOC_OFFSET_VEC4_HEADER));
 if(tocHeader[TOC_HEADER_OFFSET_QUADS_MAGIC]!=1337u)return vec4(1,0,1,1);//Magenta means invalid texture.
 vec2	tDims		= vec2(float(tocHeader[TOC_HEADER_OFFSET_QUADS_WIDTH]),float(tocHeader[TOC_HEADER_OFFSET_QUADS_HEIGHT]));
 vec2	texelXY		= tDims*vec2(uv.x,1-uv.y);
 vec2	ceilTexXY	= ceil(texelXY);
 vec2	codeXY		= mod(texelXY,float(CODE_SIDE_WIDTH_TEXELS));
 vec2	dH			= clamp(vec2(codeXY.x - 3,codeXY.y - 3),0,1);
 vec2	idH			= 1-dH;
 uint	renderFlags = tocHeader[TOC_HEADER_OFFSET_QUADS_RENDER_FLAGS];
 vec4	cTexel  	= codeTexel(texelXY,textureID,tDims,renderFlags);
 
 if(dH.x>0 && dH.y<=0) cTexel = //Far right
	cTexel * (idH.x) + codeTexel(vec2(ceilTexXY.x,texelXY.y),textureID,tDims,renderFlags) * (dH.x);
 else if(dH.y>0 && dH.x<=0)cTexel = //Far down
	cTexel * (idH.y) + codeTexel(vec2(texelXY.x,ceilTexXY.y),textureID,tDims,renderFlags) * (dH.y);
 else if(dH.y>0 && dH.x>0)cTexel = //Corner
	cTexel * (idH.x)*(idH.y)+ //Bottom left
	codeTexel(vec2(ceilTexXY.x,texelXY.y),textureID,tDims,renderFlags) * dH.x *(idH.y)+ //Bottom right
	codeTexel(ceilTexXY,textureID,tDims,renderFlags) * dH.x*dH.y+ //Top right
	codeTexel(vec2(texelXY.x,ceilTexXY.y),textureID,tDims,renderFlags) * (idH.x)*(dH.y); //Top left

 float sunIllumination			= clamp(dot(sunVector,normalize(norm)),0,1);
 if(length(norm)>.1)cTexel.rgb	= cTexel.rgb*fogColor+cTexel.rgb*sunIllumination*sunColor;
 cTexel 						= mix(cTexel,vec4(fogColor*sunColor,1),clamp(pow(linearDepth,3)*1.5,0,1));//FOG
 return cTexel;
 }//end intrinsicCodeTexel

////////// STRUCT LAYOUT DOCUMENTATION ///////////////
/**
textureTOC{
	19^2 x 4B
	offset 1456B or 91VEC4:
	TOCHeader {4B width, 4B height, 4B startCode, 4B ???}
	Unused 54B
	}
**/

void main(){
vec2	screenLoc 	= vec2(gl_FragCoord.x/screenWidth,gl_FragCoord.y/screenHeight);
float 	depth 		= texture(depthTexture,screenLoc)[0];
gl_FragDepth 		= depth;
float 	linearDepth = linearizeDepth(depth);

uint 	textureID 	= texture(textureIDTexture,screenLoc)[0u];
bool	oldTex		= textureID==960u;//Switch for old texturing mode.
		fragColor 	= texture(primaryRendering,screenLoc);//GET UV
vec3 	norm 		= texture(normTexture,screenLoc).xyz*2-vec3(1,1,1);//UNPACK NORM
vec2	uv			= fragColor.xy;
vec3	color;

// S O L I D   B A C K D R O P
color = vec3(intrinsicCodeTexel(linearDepth,textureID,norm,uv));
uint relevantSize=0u;
vec4 depthQueue[DEPTH_QUEUE_SIZE];

// D E P T H   P O P U L A T E
for(int i=0; i<DEPTH_QUEUE_SIZE; i++){
 vec4	depthQueueTexel	= texelFetch(depthQueueTexture,ivec2(gl_FragCoord.xy),i);
		textureID		= floatBitsToUint(depthQueueTexel[2u]);
		//TODO: LinearDepth. Alpha is depth.
		//TODO: Norm. Calculate from future primitive table implementation?
 if(textureID!=0u){// Found a valid point
 	depthQueue[relevantSize]=depthQueueTexel;
 	relevantSize++;
 	}//end if(valid point)
 }//end for(DEPTH_QUEUE_SIZE)
 
 // D E P T H   S O R T
 if(relevantSize>0u){
 //Perform the not-so-quick sort
 vec4 intermediary;
 for(uint i=0u; i<relevantSize-1u; i++){
  for(uint j=i+1u; j<relevantSize; j++){
   if(depthQueue[j].a>depthQueue[i].a){//Found new deepest
    //Trade
    intermediary = depthQueue[i];
    depthQueue[i] = depthQueue[j];
    depthQueue[j] = intermediary;
    }//end if(new deepest)
   }//end for(lower end)
  }//end for(relevantSize)
  }//end if(relevantSize>0u)
  
  // D E P T H   A S S E M B L Y
  for(uint i=0u; i<relevantSize; i++){
   vec4 dqColor	= intrinsicCodeTexel(0,floatBitsToUint(depthQueue[i][2u]),vec3(0,0,0),depthQueue[i].rg);
   color 		= mix(color.rgb,dqColor.rgb,dqColor.a);
  }//end for(relevantSize)
  
fragColor.rgb		 	= oldTex?texture(texturePalette,fragColor.xy).rgb:
						  color;//GET COLOR
}//end main()