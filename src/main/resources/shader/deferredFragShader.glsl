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
uniform sampler2D 		normTexture;
uniform sampler2D 		primitiveIDTexture;
uniform sampler2D		vertexTextureIDTexture;
uniform sampler2D		primitiveUVZWTexture;
uniform sampler2D		primitivenXnYnZTexture;
uniform sampler2DArray 	rgbaTiles;
uniform sampler2D		layerAccumulator;
uniform usamplerBuffer 	rootBuffer; 	//Global memory, as a set of uint vec4s.
//uniform sampler2DMS		depthQueueTexture;
uniform vec3 			fogColor;
uniform uint 			screenWidth;
uniform uint 			screenHeight;
uniform vec3 			sunVector;

noperspective in vec2	screenLoc;

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

const uint SUBTEXTURE_SIDE_WIDTH_CODES  = 36u;
const uint SUBTEXTURE_SIDE_WIDTH_CODES_WITH_BORDER =
										  SUBTEXTURE_SIDE_WIDTH_CODES + 2u;
const uint SUBTEXTURE_SIDE_WIDTH_TEXELS = SUBTEXTURE_SIDE_WIDTH_CODES_WITH_BORDER * CODE_SIDE_WIDTH_TEXELS;
const uint SUBTEXTURE_START_CODE_TABLE_OFFSET_VEC4
										= 91u;

const vec3 sunColor 					= vec3(1.4,1.4,1.2);

const int DEPTH_QUEUE_SIZE				= 5;
const float DEAD_BEEF					= 100024;
const uint PAGE_SIZE_VEC4				= 96u;

const uint VTX_TEXTURE_WIDTH			= 1024u;
const uint VTX_TEXTURE_USABLE_WIDTH = (VTX_TEXTURE_WIDTH/3u)*3u;

const uint PQUAD_SIDE_WIDTH			= 2u;
const uint PRIM_TEXTURE_WIDTH		= 512u;
const uint PRIM_TEXTURE_HEIGHT		= 512u;
const float PRIM_TEX_HEIGHT_SCALAR	= 1/float(PRIM_TEXTURE_HEIGHT);
const float PRIM_TEX_WIDTH_SCALAR	= 1/float(PRIM_TEXTURE_WIDTH);
const uint PRIMS_PER_ROW			= PRIM_TEXTURE_WIDTH/PQUAD_SIDE_WIDTH;
const uint OVERSAMPLING				= 4u;

const  vec2 PRIM_QUAD_BL 			= vec2(PRIM_TEX_WIDTH_SCALAR,PRIM_TEX_HEIGHT_SCALAR)/(2*float(OVERSAMPLING));

float warpFog(float z){
const float ZNEAR = 6554 * 32;
const float ZFAR = 65536 * 16;
return clamp((z-ZNEAR)/(ZFAR-ZNEAR),0,1);
}

uint UByte(uint _input, uint index)
	{return (_input >> 8u*index) & 0x000000FFu;}

vec4 codeTexel(vec2 texelXY, uint textureID, vec2 tDims, uint renderFlags){
 		texelXY		= (renderFlags&RENDER_FLAGS_WRAP)!=0u?mod(texelXY,tDims):clamp(texelXY,vec2(0,0),tDims-vec2(1,1));
 vec2	codeXY		= mod(texelXY,float(CODE_SIDE_WIDTH_TEXELS));
 // Clamp sub-pixels within vector.
 codeXY				= clamp(codeXY,0,3)+.5;
 uint	tTOCIdx		= uint(texelXY.x)/SUBTEXTURE_SIDE_WIDTH_TEXELS + (uint(texelXY.y)/SUBTEXTURE_SIDE_WIDTH_TEXELS) * 19u;
 uint	tTOCvec4Idx	= tTOCIdx / 4u;
 uint	tTOCsubIdx	= tTOCIdx % 4u;
 // Sub-Texture
 uint	subTexV4Addr= texelFetch(rootBuffer,int(textureID+tTOCvec4Idx))[tTOCsubIdx];
 vec2	subTexUVblnd= mod(texelXY,CODE_PAGE_TEXEL_SIZE_UV);//Subtexel to blend between texels
 vec2	subTexXY	= mod(texelXY,SUBTEXTURE_SIDE_WIDTH_TEXELS);
 uint	subTexByIdx = 0u+(uint(subTexXY.x)/CODE_SIDE_WIDTH_TEXELS + ((0u+(uint(subTexXY.y)/CODE_SIDE_WIDTH_TEXELS)) * SUBTEXTURE_SIDE_WIDTH_CODES_WITH_BORDER));
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
 
 vec4 intrinsicCodeTexel(uint textureID,vec3 norm,vec2 uv){
 // TOC
 if(textureID==0u)return vec4(0,1,0,1);//Green means textureID=zero
 if(textureID==DEAD_BEEF)return vec4(1,1,0,1);//Yellow means 0xDEADBEEF (unwritten) reverse[4022250974][3735928559u]
 uvec4 	tocHeader 	= texelFetch(rootBuffer,int(textureID+TOC_OFFSET_VEC4_HEADER));
 if(tocHeader[TOC_HEADER_OFFSET_QUADS_MAGIC]!=1337u)return vec4(1,0,1,1);//Magenta means invalid texture.
 vec2	tDims		= vec2(float(tocHeader[TOC_HEADER_OFFSET_QUADS_WIDTH]),float(tocHeader[TOC_HEADER_OFFSET_QUADS_HEIGHT]));
 vec2	texelXY		= tDims*vec2(uv.x,1-uv.y);
 vec2	ceilTexXY	= ceil(texelXY);
 vec2	codeXY		= mod(texelXY,float(CODE_SIDE_WIDTH_TEXELS));
 vec2	dH			= codeXY-3;
 uint	renderFlags = tocHeader[TOC_HEADER_OFFSET_QUADS_RENDER_FLAGS];
 vec4	cTexel  	= codeTexel(texelXY,textureID,tDims,renderFlags);
 
 if(dH.x>0 && dH.y<=0) cTexel = //Far right
    mix(cTexel,codeTexel(vec2(ceilTexXY.x,texelXY.y),textureID,tDims,renderFlags),dH.x);
 else if(dH.y>0 && dH.x<=0)cTexel = //Far down
 	mix(cTexel,codeTexel(vec2(texelXY.x,ceilTexXY.y),textureID,tDims,renderFlags),dH.y);
 else if(dH.y>0 && dH.x>0)cTexel = //Corner
 	mix(
 	 mix(cTexel,codeTexel(vec2(texelXY.x,ceilTexXY.y),textureID,tDims,renderFlags),dH.y),//Left
 	 mix(codeTexel(vec2(ceilTexXY.x,texelXY.y),textureID,tDims,renderFlags),codeTexel(ceilTexXY,textureID,tDims,renderFlags),dH.y),//Right
 	   dH.x);//Vertical
 
 float sunIllumination			= -dot(sunVector,norm);
 if(dot(norm.xyz,norm.xyz)>.2)cTexel.rgb
 								=((clamp(sunIllumination,0,1)*sunColor)+fogColor) * cTexel.rgb;
 return cTexel;
 }//end intrinsicCodeTexel

vec4 primitiveLayer(vec2 pQuad, vec4 vUVZI){
 /*
 uint		vertexID	= primitiveID*3u;
 float		textureID	= texelFetch(vertexTextureIDTexture,ivec2(vertexID%VTX_TEXTURE_USABLE_WIDTH,vertexID/VTX_TEXTURE_USABLE_WIDTH),0).x*65536*PAGE_SIZE_VEC4;
 uint	row				= primitiveID/PRIMS_PER_ROW;
 uint	col				= primitiveID%PRIMS_PER_ROW;
 uint	primitiveIndex	= row*PRIMS_PER_ROW+col;
 uint	vertexIndex		= primitiveIndex*3u;
 vec2	pQuadBL			= PRIM_QUAD_BL;
 pQuadBL.x			+=float(col*2)*PRIM_TEX_WIDTH_SCALAR;// x2 because each quad is 2 texels wide.
 pQuadBL.y			+=float(row*2)*PRIM_TEX_HEIGHT_SCALAR;
 vec2	pQuad		= pQuadBL+vec2(PRIM_TEX_WIDTH_SCALAR*screenLoc.x,PRIM_TEX_HEIGHT_SCALAR*screenLoc.y);
 vec4	uvzw		= textureLod(primitiveUVZWTexture,pQuad,0);
 */
 vec4	nXnYnZ		= textureLod(primitivenXnYnZTexture,pQuad,0);
 vec2	uv			= texture(primaryRendering,screenLoc).xy;//GET UV //TODO: Remove
 if(uv.x!=-.1234)uv	= vUVZI.xy; //TODO: Remove conditional, keep assignment.
 vec3 	norm 		= texture(normTexture,screenLoc).xyz*2-1;//UNPACK NORM    //TODO: Remove
 if(norm.x!=-.1234)norm = (nXnYnZ.xyz*2)-1;//TODO: Remove conditional, keep assignment.
 vec4	texel		= intrinsicCodeTexel(uint(vUVZI[3u]),norm,uv);
 texel.a 			*=1-warpFog(vUVZI.z);
 return texel;
}

uint getPrimitiveIDFromQueue(vec4 layerAccumulator, int level){
 const vec4 ACC_MULTIPLIER = vec4(1,16,256,4096);
 return uint(dot(mod(floor(layerAccumulator/pow(16.,float(level))),16)*ACC_MULTIPLIER,vec4(1)));
}

//UNTESTED
float logn(float value, float base){
 return log2(value)/log2(base);
}

//DOES NOT WORK
uint depthOfFloatShiftQueue(vec4 fsq){
 return uint(logn(fsq.x,16));
}

vec2 getPQuad(uint primitiveID){
 uint	row			= primitiveID/PRIMS_PER_ROW;
 uint	col			= primitiveID%PRIMS_PER_ROW;
 vec2	pQuadBL		= PRIM_QUAD_BL;
 pQuadBL.x			+=float(col*2u)*PRIM_TEX_WIDTH_SCALAR;// x2 because each quad is 2 texels wide.
 pQuadBL.y			+=float(row*2u)*PRIM_TEX_HEIGHT_SCALAR;
 return				pQuadBL+vec2(PRIM_TEX_WIDTH_SCALAR*screenLoc.x,PRIM_TEX_HEIGHT_SCALAR*screenLoc.y);
}

float getTextureID(uint primitiveID){
 uint vertexID = primitiveID * 3u;
 return texelFetch(vertexTextureIDTexture,ivec2(vertexID%VTX_TEXTURE_USABLE_WIDTH,vertexID/VTX_TEXTURE_USABLE_WIDTH),0).x*65536*PAGE_SIZE_VEC4;
}

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
float 	depth 		= texture(depthTexture,screenLoc)[0];
gl_FragDepth 		= depth;
float 	warpedFog 	= warpFog(depth);
uint	primitiveID = uint(texture(primitiveIDTexture,screenLoc)[0u]*65536);
vec4	color;
/*vec3	illuminatedFog
					= fogColor*sunColor;*/

// S O L I D   B A C K D R O P
if(primitiveID>0u){
 primitiveID--; //Compensate for zero representing "unwritten."
 vec2 pq = getPQuad(primitiveID);
 vec4 _uvzw	= textureLod(primitiveUVZWTexture,pq,0);
 _uvzw.xyz /= _uvzw.w;
 color = primitiveLayer(pq, vec4(_uvzw.xyz,getTextureID(primitiveID)) );
 }

vec4	fsq			= texture(layerAccumulator,screenLoc);

uint relevantSize=0u/*depthOfFloatShiftQueue(fsq)*/;
vec4 vUVZI[DEPTH_QUEUE_SIZE]; // U,V, depth, texture ID
vec2 pQuads[DEPTH_QUEUE_SIZE];
int ordering[DEPTH_QUEUE_SIZE];

// D E P T H   P O P U L A T E
for(int i=0; i<DEPTH_QUEUE_SIZE; i++){
 primitiveID = getPrimitiveIDFromQueue(fsq,i);
 if(primitiveID==0u || primitiveID>65535u)
  break;
 primitiveID--; //Compensate for zero representing "unwritten."
 vec2 pQuad = pQuads[i]= getPQuad(primitiveID);
 vec4 _uvzw	= textureLod(primitiveUVZWTexture,pQuad,0);
 _uvzw.xyz /= _uvzw.w;
 vUVZI[i]   = vec4(_uvzw.xyz,getTextureID(primitiveID));
 ordering[i]=i;
 relevantSize++;
 }//end for(DEPTH_QUEUE_SIZE)
 
 // D E P T H   S O R T
 if(relevantSize>0u){
 float alphaAccumulator=0;
 //Perform the not-so-quick sort
 int intermediary;
 for(uint i=0u; i<relevantSize-1u; i++){
  for(uint j=i+1u; j<relevantSize; j++){
   if(vUVZI[ordering[j]].z>vUVZI[ordering[i]].z){//Found new deepest
    //Trade
    intermediary = ordering[i];
    ordering[i] = ordering[j];
    ordering[j] = intermediary;
    }//end if(new deepest)
   }//end for(lower end)
  }//end for(relevantSize)
  }//end if(relevantSize>0)
  
  // D E P T H   A S S E M B L Y
  for(uint i=0u; i<relevantSize; i++){
   vec4 dqColor = primitiveLayer(pQuads[ordering[i]],vUVZI[ordering[i]]);
   color.rgb 	= mix(color.rgb,dqColor.rgb,dqColor.a*( (1-color.a)+(dqColor.a*color.a) ));
   color.a		= 1-((1-color.a)*(1-dqColor.a));
  }//end for(relevantSize)

fragColor		 	= color;
}//end main()