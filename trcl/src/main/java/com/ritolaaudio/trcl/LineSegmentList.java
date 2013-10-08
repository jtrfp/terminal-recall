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
package com.ritolaaudio.trcl;

import javax.media.opengl.GL3;

public class LineSegmentList extends PrimitiveList<LineSegment,GPULineSegment>
	{//NOTE: ANIMATION NOT SUPPORTED
	private final LineSegment [][]lineSegments;
	
	public LineSegmentList(LineSegment [][] lineSegments, String debugName)
		{
		super(debugName, lineSegments, GPULineSegment.createLineSegmentBlock(lineSegments[0].length));
		this.lineSegments=lineSegments;
		}

	@Override
	public void uploadToGPU(GL3 gl)
		{
		int sIndex=0;
		LineSegment []frame = lineSegments[0];
		for(LineSegment ls:frame)
			{
			int i=0;
			GPULineSegment gls = getVec4s()[sIndex++];
			
			//P1
			gls.x1.set((short)applyScale(Math.round(ls.getX()[i])));
			gls.y1.set((short)applyScale(Math.round(ls.getY()[i])));
			gls.z1.set((short)applyScale(Math.round(ls.getZ()[i])));
			//P2
			i++;
			gls.x2.set((short)applyScale(Math.round(ls.getX()[i])));
			gls.y2.set((short)applyScale(Math.round(ls.getY()[i])));
			gls.z2.set((short)applyScale(Math.round(ls.getZ()[i])));
			//RGB
			gls.red.set((byte)ls.getColor().getRed());
			gls.green.set((byte)ls.getColor().getGreen());
			gls.blue.set((byte)ls.getColor().getBlue());
			//THICKNESS
			gls.thickness.set((byte)Math.round(ls.getThickness()/(coordDownScaler/8)));
			}//end for(lineSegments)
		}//end uploadToGPU

	@Override
	public int getPrimitiveSizeInVec4s()
		{
		return 1;
		}

	@Override
	public int getGPUVerticesPerPrimitive()
		{
		return 6;
		}
	@Override
	public byte getPrimitiveRenderMode()
		{return PrimitiveRenderMode.RENDER_MODE_LINES;}

	@Override
	public com.ritolaaudio.trcl.PrimitiveList.RenderStyle getRenderStyle()
		{return RenderStyle.TRANSPARENT;}
	
	public double getMaximumVertexValue()
		{
		double result=0;
		LineSegment [][]t=getPrimitives();
		for(LineSegment [] frame:t)
			{
			for(LineSegment ls:frame)
				{
				for(int i=0; i<2; i++)
					{
					double v;
					v=Math.abs(ls.getX()[i]);
					result=result<v?v:result;
					}//end for(vertex)
				}//end for(triangle)
			}//end for(triangles)
		return result;
		}//end getMaximumVertexValue()
	}//end LineSegmentList
