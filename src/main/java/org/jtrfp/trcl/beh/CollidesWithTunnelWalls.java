/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.beh;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;

import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.beh.phy.Velocible;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.file.TNLFile.Segment;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.TunnelSegment;
import org.jtrfp.trcl.obj.WorldObject;

public class CollidesWithTunnelWalls extends Behavior implements CollisionBehavior{//TODO: Cleanup
    private final boolean changeHeadingAndTop, alwaysTopUp;
    
    private WeakReference<TunnelSegment> segmt;
    private double [] surfaceNormalVar;
    private final double [] pprtt = new double[]{0,0,0}, circleCenter = new double []{0,0,0};
    private final double [] protrusionVector = new double[]{0,0,0};
    
    public CollidesWithTunnelWalls(boolean changeHeadingAndTop, boolean alwaysTopUp){
	super();this.changeHeadingAndTop=changeHeadingAndTop;this.alwaysTopUp=alwaysTopUp;
    }
	public void proposeCollision(WorldObject other){
		final WorldObject parent = getParent();
		final Velocible velocible = getParent().probeForBehavior(Velocible.class);
		if(other instanceof TunnelSegment){
		    	final TunnelSegment seg = (TunnelSegment)other;
		    	segmt = new WeakReference<TunnelSegment>((TunnelSegment)other);
			final double [] pPos = parent.getPosition();
			final double [] segPos = seg.getPosition();
			if(pPos[0]>segPos[0]&&
					pPos[0]<segPos[0]+seg.getSegmentLength())
				{final Segment s = seg.getSegmentData();
				final double segLen=seg.getSegmentLength();
				final double [] start =segPos;
				final double depthDownSeg=Math.abs(pPos[0]-start[0]);
				final double pctDownSeg=depthDownSeg/segLen;
				circleCenter[0]=pPos[0];
				circleCenter[1]=start[1]+(seg.getEndY()*pctDownSeg);
				circleCenter[2]=start[2]-(seg.getEndX()*pctDownSeg);
				final double startWidth =TunnelSegment.getStartWidth(s);
				final double startHeight=TunnelSegment.getStartHeight(s);
				final double endWidth   =TunnelSegment.getEndWidth(s);
				final double endHeight  =TunnelSegment.getEndHeight(s);
				// fudge-factor to ensure the player doesn't see the outside of the tunnel (due to clipping) before bouncing off.
				final double widthHere=.9*(startWidth*(1.-pctDownSeg)+endWidth*pctDownSeg);
				final double heightHere=.9*(startHeight*(1.-pctDownSeg)+endHeight*pctDownSeg);
				Vect3D.subtract(pPos, circleCenter, pprtt);
				final double protrusion =Math.sqrt((pprtt[2]*pprtt[2])/(widthHere*widthHere)+(pprtt[1]*pprtt[1])/(heightHere*heightHere)); 
				
				if(protrusion>1){
					//Execute the "bounce"
				    	final double [] oldPosition = Arrays.copyOf(pPos,3);
				    	//Barrier
				    	Vect3D.scalarMultiply(circleCenter, .01, pPos);
				    	Vect3D.add(pPos, Vect3D.scalarMultiply(oldPosition, .99, new double[3]), pPos);
				    	//parent.setPosition(circleCenter.scalarMultiply(.01).add(oldPosition.scalarMultiply(.99)));
				    	final double[]delta = TRFactory.twosComplementSubtract(circleCenter, oldPosition, new double[3]);
				    	if(delta[0]==0 && delta[1]==0 && delta[2]==0)
				    	    return;
				    	final double[]inwardNormal = Vect3D.normalize(delta);
					surfaceNormalVar = inwardNormal;
					//Notify listeners
					parent.probeForBehaviors(sub, SurfaceImpactListener.class);
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
		final TunnelSegment seg = segmt.get();
		if(seg==null)return;
		item.collidedWithSurface(segmt.get(), surfaceNormalVar);
	    	}
	    @Override
	    public void submit(Collection<SurfaceImpactListener> items) {
		for(SurfaceImpactListener l:items){submit(l);}
	    	}
		};
	}//end TVBehavior
