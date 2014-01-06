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
package org.jtrfp.trcl.beh;

import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.TNLFile.Segment;
import org.jtrfp.trcl.obj.TunnelSegment;
import org.jtrfp.trcl.obj.Velocible;
import org.jtrfp.trcl.obj.WorldObject;

public class BouncesOffTunnelWalls extends Behavior{
    private final boolean changeHeadingAndTop, alwaysTopUp;
    
    private TunnelSegment seg;
    private Vector3D surfaceNormalVar;
    
    public BouncesOffTunnelWalls(boolean changeHeadingAndTop, boolean alwaysTopUp){
	super();this.changeHeadingAndTop=changeHeadingAndTop;this.alwaysTopUp=alwaysTopUp;
    }
	protected void _proposeCollision(WorldObject other){
		final WorldObject parent = getParent();
		final Velocible velocible = getParent().getBehavior().probeForBehavior(Velocible.class);
		if(other instanceof TunnelSegment)
			{seg=(TunnelSegment)other;
			if(parent.getPosition().
					getX()>seg.getPosition().getX()&&
					parent.getPosition().getX()<seg.getPosition().getX()+seg.getSegmentLength())
				{final Segment s = seg.getSegmentData();
				final double segLen=seg.getSegmentLength();
				final Vector3D start =seg.getPosition();
				final Vector3D end = start.add(new Vector3D(segLen,seg.getEndY(),-seg.getEndX()));//ZYX
				final Vector3D tunnelSpineNoNorm=TR.twosComplimentSubtract(end, start);
				final Vector3D tunnelSpineNorm=tunnelSpineNoNorm.normalize();
				
				final double depthDownSeg=start.getX()-parent.getPosition().getX();
				final double pctDownSeg=depthDownSeg/segLen;
				Vector3D circleCenter=
						start.add(tunnelSpineNorm.scalarMultiply(TR.deltaRollover(parent.getPosition().getX()-start.getX())));
				circleCenter = (new Vector3D(parent.getPosition().getX(),circleCenter.getY(),circleCenter.getZ()));
				final double startWidth=TunnelSegment.getStartWidth(s);
				final double startHeight=TunnelSegment.getStartHeight(s);
				final double endWidth=TunnelSegment.getEndWidth(s);
				final double endHeight=TunnelSegment.getEndHeight(s);
				// 0.7 is a fudge-factor to ensure the player doesn't see the outside of the tunnel (due to clipping) before bouncing off.
				final double widthHere=.7*(startWidth*(1.-pctDownSeg)+endWidth*pctDownSeg);
				final double heightHere=.7*(startHeight*(1.-pctDownSeg)+endHeight*pctDownSeg);
				//Parent position relative to tunnel
				final Vector3D pprtt = TR.twosComplimentSubtract(parent.getPosition(), circleCenter);
				final double protrusion =(pprtt.getZ()*pprtt.getZ())/(widthHere*widthHere)+(pprtt.getY()*pprtt.getY())/(heightHere*heightHere); 
				if(protrusion>1){
					//Execute the "bounce"
				    	final Vector3D oldPosition = parent.getPosition();
				    	//Barrier
				    	parent.setPosition(circleCenter.scalarMultiply(.01).add(oldPosition.scalarMultiply(.99)));
				    	final Vector3D inwardNormal = TR.twosComplimentSubtract(circleCenter, oldPosition).normalize();
					surfaceNormalVar = inwardNormal;
					//System.exit(10);
					//Notify listeners
					parent.getBehavior().probeForBehaviors(sub, SurfaceImpactListener.class);
					//final RotationalMomentumBehavior rmb = parent.getBehavior().probeForBehavior(RotationalMomentumBehavior.class);
					/*
					if(rmb!=null){//If this is a spinning object, reverse its spin momentum
				    	    rmb.setLateralMomentum(rmb.getLateralMomentum()*-1);
				    	    rmb.setEquatorialMomentum(rmb.getEquatorialMomentum()*-1);
				    	    rmb.setPolarMomentum(rmb.getPolarMomentum()*-1);
				    	    }
					*/
					}//end if collided with tunnel
				else{seg.setVisible(true);}
				}//end if(in range of segment)
			}//end if(TunnelSegment)
		}//end proposeCollision
	
	private final Submitter<SurfaceImpactListener>sub = new Submitter<SurfaceImpactListener>(){
	    @Override
	    public void submit(SurfaceImpactListener item) {
		item.collidedWithSurface(seg, surfaceNormalVar);
	    	}
	    @Override
	    public void submit(Collection<SurfaceImpactListener> items) {
		for(SurfaceImpactListener l:items){submit(l);}
	    	}
		};
	}//end TVBehavior
