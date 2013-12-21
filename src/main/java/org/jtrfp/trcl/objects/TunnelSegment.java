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
package org.jtrfp.trcl.objects;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.TextureDescription;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.TNLFile.Segment;

public class TunnelSegment extends WorldObject
	{
	public static final int TUNNEL_DIA_SCALAR=256;
	Segment segment;
	private final double segmentLength;
	private final double endX,endY;
	//private double width,height;
	
	public TunnelSegment(TR tr, Segment s, TextureDescription[] tunnelTexturePalette, double segLen, double endX, double endY)
		{
		super(tr, createModel(s,segLen, tunnelTexturePalette,endX,endY));
		segmentLength=segLen;
		this.endX=endX;
		this.endY=endY;
		this.segment=s;
		System.out.println("TUNNEL SEGMENT segLen="+segLen+" endX="+endX+" endY="+endY);
		}
	
	public static double getStartWidth(Segment s)
		{return TR.legacy2Modern(s.getStartWidth()*TUNNEL_DIA_SCALAR*3);}
	public static double getEndWidth(Segment s)
		{return TR.legacy2Modern(s.getEndWidth()*TUNNEL_DIA_SCALAR*3);}
	public static double getStartHeight(Segment s)
		{return TR.legacy2Modern(s.getStartHeight()*TUNNEL_DIA_SCALAR*3);}
	public static double getEndHeight(Segment s)
		{return TR.legacy2Modern(s.getEndHeight()*TUNNEL_DIA_SCALAR*3);}
	
	private static Model createModel(Segment s,double segLen, TextureDescription[] tunnelTexturePalette, double endX,double endY)
		{
		Model m = new Model(true);
		m.setDebugName("Tunnel Segment");
		//System.out.println("Start width (legacy): "+s.getStartWidth());
		//System.out.println("Start width (modern*TUNNEL_DIA_SCALAR): "+TR.legacy2Modern(s.getStartWidth()*TUNNEL_DIA_SCALAR));
		final int numPolys=s.getNumPolygons();
		double startWidth=getStartWidth(s);
		double startHeight=getStartHeight(s);
		double endWidth=getEndWidth(s);
		double endHeight=getEndHeight(s);
		//TODO: x,y, rotation, 
		double startAngle1=((double)s.getStartAngle1()/65535.)*2.*Math.PI;
		double startAngle2=((double)s.getStartAngle2()/65535.)*2.*Math.PI;
		double startAngle=startAngle1;
		final double endAngle1=((double)s.getStartAngle1()/65535.)*2.*Math.PI;
		final double endAngle2=((double)s.getStartAngle2()/65535.)*2.*Math.PI;
		double endAngle=endAngle1;
		final double dAngleStart=(startAngle2-startAngle1)/(double)numPolys;
		final double dAngleEnd=(endAngle2-endAngle1)/(double)numPolys;
		final double startX=0;
		final double startY=0;
		final double zStart=0;
		final double zEnd=segLen;
		//Poly quads
		for(int pi=0; pi<s.getNumPolygons(); pi++)
			{
			Vector3D p0=segPoint(startAngle,zStart,startWidth,startHeight,startX,startY);
			Vector3D p1=segPoint(endAngle,zEnd,endWidth,endHeight,endX,endY);
			Vector3D p2=segPoint(endAngle+dAngleEnd,zEnd,endWidth,endHeight,endX,endY);
			Vector3D p3=segPoint(startAngle+dAngleStart,zStart,startWidth,startHeight,startX,startY);
			m.addTriangles(Triangle.quad2Triangles(
						new double[]{p0.getX(),p1.getX(),p2.getX(),p3.getX()},
						new double[]{p0.getY(),p1.getY(),p2.getY(),p3.getY()},
						new double[]{p0.getZ(),p1.getZ(),p2.getZ(),p3.getZ()},
						
						new double[]{0,0,1,1},
						new double[]{0,1,1,0},
						tunnelTexturePalette[s.getPolyTextureIndices().get(pi)], RenderMode.DYNAMIC));
			startAngle+=dAngleStart;
			endAngle+=dAngleEnd;
			}//for(polygons)
		
		//The slice quad
		Vector3D p0=segPoint(startAngle1,zStart,startWidth,startHeight,startX,startY);
		Vector3D p1=segPoint(endAngle1,zEnd,endWidth,endHeight,endX,endY);
		Vector3D p2=segPoint(endAngle2,zEnd,endWidth,endHeight,endX,endY);
		Vector3D p3=segPoint(startAngle2,zStart,startWidth,startHeight,startX,startY);
		m.addTriangles(Triangle.quad2Triangles(
					new double[]{p0.getX(),p1.getX(),p2.getX(),p3.getX()},
					new double[]{p0.getY(),p1.getY(),p2.getY(),p3.getY()},
					new double[]{p0.getZ(),p1.getZ(),p2.getZ(),p3.getZ()},
					
					new double[]{0,0,1,1},
					new double[]{0,1,1,0},
					tunnelTexturePalette[s.getPolyTextureIndices().get(0)], RenderMode.DYNAMIC));
		
		return m.finalizeModel();
		}
	
	private static Vector3D segPoint(double angle, double z, double w, double h, double x, double y)
		{return new Vector3D(Math.cos(angle)*w+x,Math.sin(angle)*h+y,z);}
	
	public Segment getSegmentData(){return segment;}
	public double getSegmentLength(){return segmentLength;}
	public double getEndX(){return endX;}
	public double getEndY(){return endY;}
	}//end TunnelSegment
