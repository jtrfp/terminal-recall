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
uniform sampler2D 		primitiveIDTexture;
uniform sampler2D		vertexTextureIDTexture;
uniform sampler2D		primitiveUVZWTexture;
uniform sampler2D		primitivenXnYnZLTexture;
uniform sampler2DArray 	rgbaTiles;
uniform sampler2DArray	ESTuTvTiles;
uniform sampler2D		layerAccumulator0;
uniform sampler2D		layerAccumulator1;
uniform usamplerBuffer 	rootBuffer; 	//Global memory, as a set of uint vec4s.
uniform samplerCube 	cubeTexture;
uniform sampler2DArray	portalTexture;
smooth in vec3 			norm;

uniform vec3			ambientLight;
uniform vec3			sunColor;
uniform vec3 			sunVector;
uniform uint			bypassAlpha;
uniform vec2			screenDims;
uniform float			fogScalar;

struct CompositeTexel {
 vec4 rgba;
 vec4 ESTuTv;
 };

//noperspective in vec2	screenLoc;
vec2 screenLoc									= gl_FragCoord.xy/screenDims;

// OUTPUTS
layout(location = 0) out vec4 fragColor;

// CONSTANTS
const uint TOC_OFFSET_VEC4_HEADER				=91u;//1456/16
const uint TOC_HEADER_OFFSET_QUADS_WIDTH		=0u;
const uint TOC_HEADER_OFFSET_QUADS_HEIGHT		=1u;
const uint TOC_HEADER_OFFSET_QUADS_RENDER_FLAGS	=2u;
const uint TOC_HEADER_OFFSET_QUADS_MAGIC		=3u;

const uint RENDER_FLAGS_WRAP					=0x1u;

const float ALPHA_THRESHOLD						=.02;

const float TILE_PAGE_SIDE_WIDTH_TEXELS = 512;
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

const int DEPTH_QUEUE_SIZE				= 11; // floor(23b / 2b) = 11
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
const float PQUAD_DENOM				= (float(PRIM_TEXTURE_WIDTH)/2);

const uint NUM_PORTALS				= 4u;

const vec4 LO_ACC_MULTIPLIER        = vec4(1,4,16,64);// 2 bits each
const vec4 HI_ACC_MULTIPLIER        = LO_ACC_MULTIPLIER * 256;

vec2	halfScreenLocOffset = (screenLoc / 2) + (1/(float(OVERSAMPLING*4u)));
vec3	fogCubeColor;

float warpFog(float z){
 const float ZNEAR = 6554 * 8;
 const float ZFAR = 65536 * 16;
 return clamp((((z*fogScalar)-ZNEAR))/(ZFAR-ZNEAR),0,1);
}

uint UByte(uint _input, uint index)
	{return (_input >> 8u*index) & 0x000000FFu;}
	
CompositeTexel cMix(CompositeTexel left, CompositeTexel right, float amt){
 CompositeTexel result;
 result.rgba = mix(left.rgba,right.rgba,amt);
 result.ESTuTv = mix(left.ESTuTv,right.ESTuTv,amt);
 return result;
}

CompositeTexel codeTexel(uvec2 texelXY, uint textureID, uint subTexV4Idx, uint subTexV4Sub, uint subTexV4Addr, uint startCode){
 // Codebook
 uint	codeIdx		= UByte((texelFetch(rootBuffer,int(subTexV4Idx+subTexV4Addr))[subTexV4Sub/4u]),subTexV4Sub%4u)+startCode;
 uint	codeBkPgNum	= codeIdx / CODES_PER_CODE_PAGE;
 uvec2	codeXY		= texelXY%4u;
 vec2	subTexUVsub	= codeXY*CODE_PAGE_TEXEL_SIZE_UV;
 ivec2	codePgXY	= ivec2(codeIdx % CODE_PAGE_SIDE_WIDTH_CODES,(codeIdx / CODE_PAGE_SIDE_WIDTH_CODES)%CODE_PAGE_SIDE_WIDTH_CODES)*4+ivec2(codeXY);
 CompositeTexel result;
 result.rgba  = texelFetch(rgbaTiles,ivec3(codePgXY,codeBkPgNum),0);
 result.ESTuTv= texelFetch(ESTuTvTiles,ivec3(codePgXY,codeBkPgNum),0);  
 return	result;
 }
 
 vec4 portalFetch(uint textureID){
 //if(textureID!=7u) return vec4(1,0,0,1);
 return texelFetch(portalTexture,ivec3(gl_FragCoord.xy,int(65535u-(textureID/96u))),0);
 }
 
 vec4 vqConstruct(uint textureID,vec3 norm,vec2 uv){
 if(textureID==0u)return vec4(0,1,0,1);//Green means textureID=zero
 if(textureID==DEAD_BEEF)return vec4(1,1,0,1);//Yellow means 0xDEADBEEF (unwritten) reverse[4022250974][3735928559u]
 if(textureID>=(65536u-NUM_PORTALS)*96u)return portalFetch(textureID);
 //if(textureID<NUM_PORTALS+1u&&textureID>0u)return portalFetch(textureID);
 //uvec4 	tocHeader 	= texelFetch(rootBuffer,int(textureID+TOC_OFFSET_VEC4_HEADER));
 //if(tocHeader[TOC_HEADER_OFFSET_QUADS_MAGIC]!=1337u)return vec4(1,0,1,1);//Magenta means invalid texture.
 
 //uint	renderFlags = tocHeader[TOC_HEADER_OFFSET_QUADS_RENDER_FLAGS];
 uv = clamp(uv-.5,0,4096);
 vec2 sub = mod(uv,1);
 uvec2 iuv = uvec2(uv);
 CompositeTexel cTexel;
 uvec4 iuv4A,iuv4B, tTOCIdx,tTOCvec4Idx,tTOCsubIdx,subTexV4Addr;
 uvec4	subTexV4Idx, subTexV4Sub, startCode;
 iuv4A		 = uvec4(iuv,iuv+uvec2(0,1));
 //iuv4B		 = uvec4(iuv,iuv)+uvec4(1,0,1,1);
 // PRECALC
 //////////////// Subtexture-level convergence check
 if(false){//Test for subtexture convergence
   // If everything is on the same subtexture (and it likely is) we can share the value on all texels and skip computing each.
   uint tti = iuv4A.x/SUBTEXTURE_SIDE_WIDTH_TEXELS + (iuv4A.y/SUBTEXTURE_SIDE_WIDTH_TEXELS) * 19u;
   uint ttv4idx = (tti / 4u) + textureID;
   uint ttsi	= tti % 4u;
   uint stv4a = texelFetch(rootBuffer,int(ttv4idx))[ttsi];
   ///////////// Tile-level-convergence check
   if(false){
    //Use intra-tile bilinear sampling
    uvec2	stXY	= iuv4A.xy%SUBTEXTURE_SIDE_WIDTH_TEXELS;
  	uint	stbi = (stXY.x/CODE_SIDE_WIDTH_TEXELS + (((stXY.y/CODE_SIDE_WIDTH_TEXELS)) * SUBTEXTURE_SIDE_WIDTH_CODES_WITH_BORDER));
  	uint	scI= stbi/256u;
  	uint	startC	= texelFetch(rootBuffer,int(stv4a+SUBTEXTURE_START_CODE_TABLE_OFFSET_VEC4+(scI/4u)))[scI%4u];
   	uint	stv4idx	= stbi / 16u;
   	uint	stv4sub = stbi % 16u;
    // Codebook
 	uint	codeIdx		= UByte((texelFetch(rootBuffer,int(stv4idx+stv4a))[stv4sub/4u]),stv4sub%4u)+startC;
 	uint	codeBkPgNum	= codeIdx / CODES_PER_CODE_PAGE;
 	vec2	codeXY		= stXY%4u;
 	vec2	subTexUVsub	= codeXY*CODE_PAGE_TEXEL_SIZE_UV;
 	vec2	codePgXY	= vec2(codeIdx % CODE_PAGE_SIDE_WIDTH_CODES,(codeIdx / CODE_PAGE_SIDE_WIDTH_CODES)%CODE_PAGE_SIDE_WIDTH_CODES)*4+codeXY+sub;
 	cTexel.rgba			= textureLod(rgbaTiles,vec3(codePgXY*CODE_PAGE_TEXEL_SIZE_UV,codeBkPgNum),0);
 	cTexel.ESTuTv		= textureLod(ESTuTvTiles,vec3(codePgXY*CODE_PAGE_TEXEL_SIZE_UV,codeBkPgNum),0);
 	vec4 illumination;
 	if(dot(norm.xyz,norm.xyz)>.01)illumination.rgb
 								=((clamp(-dot(sunVector,norm),0,1)*sunColor)+ambientLight) + cTexel.ESTuTv.x;
 	return cTexel.rgba * illumination;
    }else{
     subTexV4Addr = uvec4(stv4a);
     }
   uvec4	subTexXY_A	= iuv4A%SUBTEXTURE_SIDE_WIDTH_TEXELS;
   uvec4	subTexXY_B  = iuv4B%SUBTEXTURE_SIDE_WIDTH_TEXELS;
   uvec4	subTexByIdx = uvec4(
   	(subTexXY_A.x/CODE_SIDE_WIDTH_TEXELS) + ((subTexXY_A.y/CODE_SIDE_WIDTH_TEXELS) * SUBTEXTURE_SIDE_WIDTH_CODES_WITH_BORDER),
   	(subTexXY_A.z/CODE_SIDE_WIDTH_TEXELS) + ((subTexXY_A.w/CODE_SIDE_WIDTH_TEXELS) * SUBTEXTURE_SIDE_WIDTH_CODES_WITH_BORDER),
   	(subTexXY_B.x/CODE_SIDE_WIDTH_TEXELS) + ((subTexXY_B.y/CODE_SIDE_WIDTH_TEXELS) * SUBTEXTURE_SIDE_WIDTH_CODES_WITH_BORDER),
   	(subTexXY_B.z/CODE_SIDE_WIDTH_TEXELS) + ((subTexXY_B.w/CODE_SIDE_WIDTH_TEXELS) * SUBTEXTURE_SIDE_WIDTH_CODES_WITH_BORDER));
   uvec4	startCodeIdx = subTexByIdx/256u;
   startCode	= uvec4(
   	texelFetch(rootBuffer,int(subTexV4Addr[0u]+SUBTEXTURE_START_CODE_TABLE_OFFSET_VEC4+(startCodeIdx[0u]/4u)))[startCodeIdx[0u]%4u],
   	texelFetch(rootBuffer,int(subTexV4Addr[1u]+SUBTEXTURE_START_CODE_TABLE_OFFSET_VEC4+(startCodeIdx[1u]/4u)))[startCodeIdx[1u]%4u],
   	texelFetch(rootBuffer,int(subTexV4Addr[2u]+SUBTEXTURE_START_CODE_TABLE_OFFSET_VEC4+(startCodeIdx[2u]/4u)))[startCodeIdx[2u]%4u],
   	texelFetch(rootBuffer,int(subTexV4Addr[3u]+SUBTEXTURE_START_CODE_TABLE_OFFSET_VEC4+(startCodeIdx[3u]/4u)))[startCodeIdx[3u]%4u]);
   subTexV4Idx	= subTexByIdx / 16u;
   subTexV4Sub  = subTexByIdx % 16u;
  } else{ ////////////////// Compute the hard way
  iuv4B		 = uvec4(iuv,iuv)+uvec4(1,0,1,1);
  tTOCIdx = uvec4(
  	iuv4A.x/SUBTEXTURE_SIDE_WIDTH_TEXELS + (iuv4A.y/SUBTEXTURE_SIDE_WIDTH_TEXELS)*19u,
  	iuv4A.z/SUBTEXTURE_SIDE_WIDTH_TEXELS + (iuv4A.w/SUBTEXTURE_SIDE_WIDTH_TEXELS)*19u,
  	iuv4B.x/SUBTEXTURE_SIDE_WIDTH_TEXELS + (iuv4B.y/SUBTEXTURE_SIDE_WIDTH_TEXELS)*19u,
  	iuv4B.z/SUBTEXTURE_SIDE_WIDTH_TEXELS + (iuv4B.w/SUBTEXTURE_SIDE_WIDTH_TEXELS)*19u);
  tTOCvec4Idx  = (tTOCIdx / 4u) + textureID;
  tTOCsubIdx   = tTOCIdx % 4u;
  subTexV4Addr = uvec4(
  	texelFetch(rootBuffer,int(tTOCvec4Idx[0u]))[tTOCsubIdx[0u]],
  	texelFetch(rootBuffer,int(tTOCvec4Idx[1u]))[tTOCsubIdx[1u]],
  	texelFetch(rootBuffer,int(tTOCvec4Idx[2u]))[tTOCsubIdx[2u]],
  	texelFetch(rootBuffer,int(tTOCvec4Idx[3u]))[tTOCsubIdx[3u]]
  	);
   uvec4	subTexXY_A	= iuv4A%SUBTEXTURE_SIDE_WIDTH_TEXELS;
   uvec4	subTexXY_B  = iuv4B%SUBTEXTURE_SIDE_WIDTH_TEXELS;
   uvec4	subTexByIdx = uvec4(
   	(subTexXY_A.x/CODE_SIDE_WIDTH_TEXELS) + ((subTexXY_A.y/CODE_SIDE_WIDTH_TEXELS) * SUBTEXTURE_SIDE_WIDTH_CODES_WITH_BORDER),
   	(subTexXY_A.z/CODE_SIDE_WIDTH_TEXELS) + ((subTexXY_A.w/CODE_SIDE_WIDTH_TEXELS) * SUBTEXTURE_SIDE_WIDTH_CODES_WITH_BORDER),
   	(subTexXY_B.x/CODE_SIDE_WIDTH_TEXELS) + ((subTexXY_B.y/CODE_SIDE_WIDTH_TEXELS) * SUBTEXTURE_SIDE_WIDTH_CODES_WITH_BORDER),
   	(subTexXY_B.z/CODE_SIDE_WIDTH_TEXELS) + ((subTexXY_B.w/CODE_SIDE_WIDTH_TEXELS) * SUBTEXTURE_SIDE_WIDTH_CODES_WITH_BORDER));
   uvec4	startCodeIdx = subTexByIdx/256u;
   startCode	= uvec4(
   	texelFetch(rootBuffer,int(subTexV4Addr[0u]+SUBTEXTURE_START_CODE_TABLE_OFFSET_VEC4+(startCodeIdx[0u]/4u)))[startCodeIdx[0u]%4u],
   	texelFetch(rootBuffer,int(subTexV4Addr[1u]+SUBTEXTURE_START_CODE_TABLE_OFFSET_VEC4+(startCodeIdx[1u]/4u)))[startCodeIdx[1u]%4u],
   	texelFetch(rootBuffer,int(subTexV4Addr[2u]+SUBTEXTURE_START_CODE_TABLE_OFFSET_VEC4+(startCodeIdx[2u]/4u)))[startCodeIdx[2u]%4u],
   	texelFetch(rootBuffer,int(subTexV4Addr[3u]+SUBTEXTURE_START_CODE_TABLE_OFFSET_VEC4+(startCodeIdx[3u]/4u)))[startCodeIdx[3u]%4u]);
   subTexV4Idx	= subTexByIdx / 16u;
   subTexV4Sub  = subTexByIdx % 16u;
  }
  //Perform 4-way texel mix
  cTexel		 = cMix(codeTexel(iuv4A.xy,textureID, subTexV4Idx[0u], subTexV4Sub[0u],subTexV4Addr[0u],startCode[0u]),codeTexel(iuv4A.zw,textureID, subTexV4Idx[1u], subTexV4Sub[1u],subTexV4Addr[1u],startCode[1u]),sub.y);
  cTexel		 = cMix(cTexel,cMix(codeTexel(iuv4B.xy,textureID, subTexV4Idx[2u], subTexV4Sub[2u], subTexV4Addr[2u],startCode[2u]),codeTexel(iuv4B.zw,textureID,subTexV4Idx[3u],subTexV4Sub[3u],subTexV4Addr[3u],startCode[3u]),sub.y),sub.x);
 
 vec4 illumination = vec4(1);
 if(dot(norm.xyz,norm.xyz)>.01)illumination.rgb
 								=((clamp(-dot(sunVector,norm),0,1)*sunColor)+ambientLight) + cTexel.ESTuTv.x;
 return cTexel.rgba * illumination;
 }//end vqConstruct
 
 float getTextureID(uint primitiveID, uint lod){
 uint vertexID = primitiveID * 3u + lod;
 return texelFetch(
  vertexTextureIDTexture,
  ivec2(vertexID%VTX_TEXTURE_USABLE_WIDTH,
  vertexID/VTX_TEXTURE_USABLE_WIDTH),0).x
   *(65536u*PAGE_SIZE_VEC4);
}

vec4 primitiveLayer(vec3 pQuad, vec4 vUVZI, bool disableAlpha, float w){
 vec4	nXnYnZL		= textureProjLod(primitivenXnYnZLTexture,pQuad,0);
 uint   lod         = uint((1/nXnYnZL.w)-.5); //TODO: Trilinear filtering? Optimize!
 /*if(lod != 12345u){
  return vec4(vec3(floor((1/nXnYnZL.w)-.5))/2,1);
  }*/
 vec2	uv			= vUVZI.xy / exp2(lod);
 vec3 	norm 		= nXnYnZL.xyz/w;
 float  textureID   = getTextureID(uint(vUVZI[3u]), lod);
 vec4	texel		= vqConstruct(uint(textureID),norm,uv);
 if(disableAlpha)	texel.a=1;
 if(bypassAlpha==0u)texel.rgb = mix(texel.rgb,fogCubeColor,warpFog(vUVZI.z));
 return texel;
}

uint getPrimitiveIDFromQueue(vec4 layerAccumulator0, vec4 layerAccumulator1, float level){//TODO: Clean and optmize
 float bitShifter = pow(4,level);
 float result     = 0;// Must be zeroed else it will persist to future calls!!!!  D:
 result +=  dot(mod( floor(layerAccumulator0/bitShifter),4 ),LO_ACC_MULTIPLIER);
 result +=  dot(mod( floor(layerAccumulator1/bitShifter),4 ),HI_ACC_MULTIPLIER);
 return uint(result);
}

//UNTESTED
float logn(float value, float base){
 return log2(value)/log2(base);
}

//DOES NOT WORK
uint depthOfFloatShiftQueue(vec4 fsq0){
 return uint(logn(fsq0.x,16));
}

vec3 getPQuad(uint primitiveID){
 vec2	corner		= vec2(primitiveID%PRIMS_PER_ROW,primitiveID/PRIMS_PER_ROW);
 return				vec3(corner + halfScreenLocOffset,PQUAD_DENOM);
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

vec4 reverseBlend(in vec4 src, in vec4 dest, in float alpha){
 dest.rgb += (dest.a*src.a)*src.rgb;
 dest.a *= 1-src.a;
 return dest;
}

void main(){
gl_FragColor = vec4(0,0,0,1);

uint	primitiveID;
vec4	color		= vec4(0,0,0,1);
vec4	fsq0			= texelFetch(layerAccumulator0,ivec2(gl_FragCoord),0)*65536;
bool    populatedFSQ    = dot(fsq0,vec4(1))!=0;
vec4	fsq1;
if(populatedFSQ){
 fsq1 = texelFetch(layerAccumulator1,ivec2(gl_FragCoord),0)*65536;
 }
                          
fogCubeColor	    = texture(cubeTexture,norm).rgb;
uint relevantSize=0u/*depthOfFloatShiftQueue(fsq0)*/;
vec4 vUVZI[DEPTH_QUEUE_SIZE]; // U,V, depth, texture ID
vec3 pQuads[DEPTH_QUEUE_SIZE];
float _w[DEPTH_QUEUE_SIZE];
int ordering[DEPTH_QUEUE_SIZE];

// D E P T H   P O P U L A T E
if(populatedFSQ){
 for(int i=0; i<DEPTH_QUEUE_SIZE; i++){
 primitiveID = getPrimitiveIDFromQueue(fsq0,fsq1,i);
   if(primitiveID==0u || primitiveID>65535u)
   break;
  primitiveID--; //Compensate for zero representing "unwritten."
  vec3 pQuad = pQuads[i]= getPQuad(primitiveID);
  vec4 _uvzw	= textureProjLod(primitiveUVZWTexture,pQuad,0);
  _uvzw.xyz /= _uvzw.w;
  vUVZI[i]   = vec4(_uvzw.xyz,primitiveID);
  _w[i]		= _uvzw.w;
  ordering[i]=i;
  relevantSize++;
  }//end for(DEPTH_QUEUE_SIZE)
 
 // D E P T H   S O R T
 if(relevantSize>1u){
 //Perform the not-so-quick sort
 int intermediary;
 for(uint i=0u; i<relevantSize-1u; i++){
  for(uint j=i+1u; j<relevantSize; j++){
   if(vUVZI[ordering[j]].z<vUVZI[ordering[i]].z){//Found new closest
    //Trade
    intermediary = ordering[i];
    ordering[i] = ordering[j];
    ordering[j] = intermediary;
    }//end if(new deepest)
   }//end for(lower end)
  }//end for(relevantSize)
 }//end if(relevantSize>1)
  
  // D E P T H   A S S E M B L Y
  for(uint i=0u; i<relevantSize; i++){
   vec4 dqColor = primitiveLayer(pQuads[ordering[i]],vUVZI[ordering[i]], false, _w[ordering[i]]);
   color = reverseBlend(dqColor,color,dqColor.a);
   if(color.a < ALPHA_THRESHOLD)
    break;
  }//end for(relevantSize)
 }//end if(!emptyFSQ)

if(color.a > ALPHA_THRESHOLD){
 // S O L I D   B A C K D R O P
 uint opaquePrimID = uint(texelFetch(primitiveIDTexture,ivec2(gl_FragCoord),0)[0u]*65536);
 if(opaquePrimID>0u){
  opaquePrimID--; //Compensate for zero representing "unwritten."
  vec3 pq = getPQuad(opaquePrimID);
  vec4 _uvzw	= textureProjLod(primitiveUVZWTexture,pq,0);
  _uvzw.xyz /= _uvzw.w;
  vec4 oColor = primitiveLayer(pq, vec4(_uvzw.xyz,opaquePrimID) ,true,_uvzw.w);
  if(bypassAlpha==0u){
   oColor = vec4(mix(texture(cubeTexture,norm).rgb,oColor.rgb,oColor.a),1);
   color.rgb = reverseBlend(oColor,color,oColor.a).rgb;}
  else
   color.rgb = oColor.rgb;
  }//end if(written)
 else{//Sky
  color.rgb = mix(color.rgb,texture(cubeTexture,norm).rgb,color.a);
  }
 }//end if(visible)
if(bypassAlpha==0u && color.a > 1234)//TODO: Is bypassAlpha being used anymore?
 gl_FragColor.rgb = mix(color.rgb,fogCubeColor,color.a);
else gl_FragColor.rgb = color.rgb;
}//end main()