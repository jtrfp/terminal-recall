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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.TNLFile.Segment;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.TunnelSegment;
import org.jtrfp.trcl.obj.Velocible;
import org.jtrfp.trcl.obj.WorldObject;

public class CollidesWithTunnelWalls extends Behavior{
    private final boolean changeHeadingAndTop, alwaysTopUp;
    
    private TunnelSegment seg;
    private Vector3D surfaceNormalVar;
    
    public CollidesWithTunnelWalls(boolean changeHeadingAndTop, boolean alwaysTopUp){
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
				//I have no idea why Y has to be cut in half.
				final Vector3D end = start.add(new Vector3D(segLen,seg.getEndY(),-seg.getEndX()));//ZYX
				//final Vector3D end = (new Vector3D(start.getX()+segLen,seg.getEndY(),-seg.getEndX()));//ZYX
				final Vector3D tunnelSpineNoNorm=TR.twosComplimentSubtract(end, start);
				final Vector3D tunnelSpineNorm=tunnelSpineNoNorm.normalize();
				
				final double depthDownSeg=parent.getPosition().getX()-start.getX();
				final double pctDownSeg=depthDownSeg/segLen;
				Vector3D circleCenter=
						start.add(tunnelSpineNorm.scalarMultiply(TR.deltaRollover(parent.getPosition().getX()-start.getX())));
				circleCenter = (new Vector3D(parent.getPosition().getX(),circleCenter.getY(),circleCenter.getZ()));
				//parent.setPosition(circleCenter);//Rail-mode debug code.
				final double startWidth=TunnelSegment.getStartWidth(s);
				final double startHeight=TunnelSegment.getStartHeight(s);
				final double endWidth=TunnelSegment.getEndWidth(s);
				final double endHeight=TunnelSegment.getEndHeight(s);
				// fudge-factor to ensure the player doesn't see the outside of the tunnel (due to clipping) before bouncing off.
				final double widthHere=.9*(startWidth*(1.-pctDownSeg)+endWidth*pctDownSeg);
				final double heightHere=.9*(startHeight*(1.-pctDownSeg)+endHeight*pctDownSeg);
				//Parent position relative to tunnel
				//final Vector3D pprtt = TR.twosComplimentSubtract(parent.getPosition(), circleCenter);
				final Vector3D pprtt = parent.getPosition().subtract(circleCenter);
				final double protrusion =(pprtt.getZ()*pprtt.getZ())/(widthHere*widthHere)+(pprtt.getY()*pprtt.getY())/(heightHere*heightHere); 
				//if(parent instanceof Player){System.out.println("width="+widthHere+" height="+heightHere+" pctDownSeg="+pctDownSeg);}//TODO
				if(protrusion>1){
					//Execute the "bounce"
				    	final Vector3D oldPosition = parent.getPosition();
				    	//Barrier
				    	parent.setPosition(circleCenter.scalarMultiply(.01).add(oldPosition.scalarMultiply(.99)));
				    	final Vector3D inwardNormal = TR.twosComplimentSubtract(circleCenter, oldPosition).normalize();
					surfaceNormalVar = inwardNormal;
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
