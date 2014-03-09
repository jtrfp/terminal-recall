/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
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
//#extension GL_ARB_explicit_uniform_location : enable

//CONSTANTS
//const uint NUM_RENDERLIST_PAGES=
const float COORD_DOWNSCALER=512+1;
const uint RENDER_MODE_TRIANGLES=0u;
const uint RENDER_MODE_LINES=1u;

const uint PACKED_DATA_RENDER_MODE=0u;	//UNibble
const uint PACKED_DATA_COLOR_RED=1u;		//UNibble
const uint PACKED_DATA_COLOR_GREEN=2u;	//UNibble
const uint PACKED_DATA_COLOR_BLUE=3u;		//UNibble

//OUT
smooth out vec2 fragTexCoord;
smooth out float fogLevel;
flat out uint packedFragData;

//IN
uniform uint renderListOffset;
uniform float fogStart;
uniform float fogEnd;
uniform uint renderListPageTable[85];

layout (location = 0) in float dummy;
uniform usamplerBuffer rootBuffer; 	//Global memory, as a set of uint vec4s.

//RENDER MODES
const uint OPAQUE_PASS=0u;
const uint BLEND_PASS=1u;

//FUNCTIONS
uint bit(uint _input, uint index)
	{return (_input >> index) & 0x00000001u;}

uint UNibble(uint _input, uint index)
	{return (_input >> 4u*index) & 0x0000000Fu;}

int SNibble(uint _input, uint index)
	{
	int result =  int((_input >> 4u*index) & 0x0000000Fu);
	if(result>=8)result-=16;
	return result;
	}

uint setUNibble(uint _input, uint index, uint value)
	{
	uint erasureMask = 0x0000000Fu << 4u*index;
	//Invert
	erasureMask ^= 0xFFFFFFFFu;
	//Erase
	_input &= erasureMask;
	//Overwrite
	_input |= (value << 4u*index);
	return _input;
	}

uint UByte(uint _input, uint index)
	{return (_input >> 8u*index) & 0x000000FFu;}

int SByte(uint _input, uint index)
	{
	int result =  int((_input >> 8u*index) & 0x000000FFu);
	if(result>=128)result-=256;
	return result;
	}
//////
uint secondUShort(uint _input)
	{return (_input >> 16u) & 0x0000FFFFu;}

uint firstUShort(uint _input)
	{return _input & 0x0000FFFFu;}

int secondSShort(uint _input)
	{
	int result = int((_input >> 16u) & 0x0000FFFFu);
	if(result>=32768)result-=65536;
	return result;
	}

int firstSShort(uint _input)
	{
	int result = int(_input & 0x0000FFFFu);
	if(result>=32768)result-=65536;
	return result;
	}

int renderListLogicalVEC42PhysicalVEC4(uint logical)
	{
	return int(renderListPageTable
		[logical/96u]*96u
		+logical%96u);
	}

/*Object definition VEC4
	uint matrix offset
	uint vertex offset
	byte numVerticesInBlock, byte primitiveRenderMode, byte modelScalar, byte ???
	uint ???
*/

/*Triangle Vertex VEC4
	uint short x,y //Scaled by COORD_DOWNSCALER
	uint short z,???
	uint short u,v // scaled by 4096
	uint short ???? //32 bits unused.
*/

/*Line Segment VEC4
	uint short x1,y1 //Scaled by COORD_DOWNSCALER
	uint short z1,x2
	uint short y2,z2
	uint byte thickness,R,G,B //Scaled by COORD_DOWNSCALER, 255, 255, 255
*/

void main()
{
gl_Position.x=dummy*0;

		//TODO: Look into optimizing this by moving some of it into the numVertices block below
		int objectIndex = (gl_VertexID / 96);
		int intraObjectVertexIndex = gl_VertexID % 96;
		int adjustedListIndex=objectIndex+int(renderListOffset);
		int objectDefIndex=int(texelFetch(rootBuffer,renderListLogicalVEC42PhysicalVEC4(uint(adjustedListIndex/4)))[adjustedListIndex%4]);
		uvec4 objectDef = texelFetch(rootBuffer,objectDefIndex);
		
		int numVertices = int(UByte(objectDef[2],0u));
		
		if(intraObjectVertexIndex<numVertices)
			{
			packedFragData=setUNibble(packedFragData,0u,UByte(objectDef[2],1u));
			int matrixOffset = int(objectDef[0]);
			int vertexOffset = int(objectDef[1]);
			int modelScalar = int(UByte(objectDef[2],2u))-16;//Numerical domain offset for negatives
			mat4 matrix = mat4(uintBitsToFloat(texelFetch(rootBuffer,matrixOffset)),uintBitsToFloat(texelFetch(rootBuffer,matrixOffset+1)),
										uintBitsToFloat(texelFetch(rootBuffer,matrixOffset+2)),uintBitsToFloat(texelFetch(rootBuffer,matrixOffset+3)));
			// objectDef[3] unused.
			switch(UNibble(packedFragData,PACKED_DATA_RENDER_MODE))
				{
			case RENDER_MODE_TRIANGLES:
				uvec4 packedVertex = texelFetch(rootBuffer,vertexOffset+intraObjectVertexIndex);
				vec4 vertexCoord;
				vertexCoord.xyz = exp2(float(modelScalar))*vec3(float(firstSShort(packedVertex[0])),float(secondSShort(packedVertex[0])),
												float(firstSShort(packedVertex[1])));
				vertexCoord.w=1;
    			fragTexCoord = vec2(float(firstSShort(packedVertex[2]))/4096.,float(secondSShort(packedVertex[2]))/4096.);
    			gl_Position = matrix * vertexCoord;
    			
    			fogLevel=(length(gl_Position)-fogStart)/(fogEnd-fogStart);
    			fogLevel=clamp(fogLevel,0.0,1.0);
    			break;//end RENDER_MODE_TRIANGLES
    		case RENDER_MODE_LINES:
    			//LOAD DATA
    			int segmentIndex=(intraObjectVertexIndex/6);
    			uvec4 packedSegment = texelFetch(rootBuffer,vertexOffset+segmentIndex);
    			int vID = intraObjectVertexIndex%6;
    			vec4 p1,p2,scalingProbe,scalingRef;
    			
    			p1.xyz=exp2(float(modelScalar))*vec3(float(firstSShort(packedSegment[0])),float(secondSShort(packedSegment[0])),
												float(firstSShort(packedSegment[1])));
				p1.w=1;
				p2.xyz=exp2(float(modelScalar))*vec3(float(secondSShort(packedSegment[1])),float(firstSShort(packedSegment[2])),
					float(secondSShort(packedSegment[2])));
				p2.w=1;
				scalingProbe=(vID==0||vID==1||vID==5)?p1:p2;
				
				//MATRIX OPS
				p1=matrix*p1;
				p2=matrix*p2;
				//FOG
				vec4 fogP;
				fogP=(vID==0||vID==1||vID==5)?p1:p2;
				fogLevel=(length(fogP)-fogStart)/(fogEnd-fogStart);
    			fogLevel=clamp(fogLevel,0.0,1.0);
				p1/=abs(p1.w);//Normalize
    			p2/=abs(p2.w);//Normalize
    			vec2 perpSlope = vec2(p2.y-p1.y,p1.x-p2.x)/length(p1.xy-p2.xy);
    			scalingRef = (vID==0||vID==1||vID==5)?p1:p2;
    			
    			//Line thickness probe
    			scalingProbe.y+=(float(UByte(packedSegment[3],0u)))*(COORD_DOWNSCALER/8u);
				scalingProbe=matrix*scalingProbe;
				scalingProbe/=abs(scalingProbe.w);
				
				//THICKNESS CALCULATION (Heavily-optimized voodoo below)
    			float thickness = length(scalingProbe-scalingRef);
    			fragTexCoord.y=1-(vID%2)*2;
    			fragTexCoord.x=((vID+7)%6)<3?-1:1;
    			vec4 thicknessVec = vec4(perpSlope*thickness*fragTexCoord.y,0,0);
    			gl_Position=scalingRef+thicknessVec;
				
				//COLOR
				packedFragData = setUNibble(packedFragData,PACKED_DATA_COLOR_RED,UByte(packedSegment[int(3)],1u)/16u);//red
				packedFragData = setUNibble(packedFragData,PACKED_DATA_COLOR_GREEN,UByte(packedSegment[int(3)],2u)/16u);//green
				packedFragData = setUNibble(packedFragData,PACKED_DATA_COLOR_BLUE,UByte(packedSegment[int(3)],3u)/16u);//blue
				break;//end RENDER_MODE_LINES
    			}//end switch(RENDER_MODE)
    		}//end if(object)
}//end main()