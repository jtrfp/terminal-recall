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
package org.jtrfp.trcl.ai;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.file.TNLFile.Segment;
import org.jtrfp.trcl.objects.TunnelSegment;
import org.jtrfp.trcl.objects.Velocible;
import org.jtrfp.trcl.objects.WorldObject;

public class BouncesOffTunnelWalls extends Behavior{
    private final boolean changeHeadingAndTop, alwaysTopUp;
    public BouncesOffTunnelWalls(boolean changeHeadingAndTop, boolean alwaysTopUp){
	super();this.changeHeadingAndTop=changeHeadingAndTop;this.alwaysTopUp=alwaysTopUp;
    }
	protected void _proposeCollision(WorldObject other){
		final WorldObject parent = getParent();
		final Velocible velocible = getParent().getBehavior().probeForBehavior(Velocible.class);
		if(other instanceof TunnelSegment)
			{TunnelSegment seg=(TunnelSegment)other;
			if(parent.getPosition().
					getX()>seg.getPosition().getX()&&
					parent.getPosition().getX()<seg.getPosition().getX()+seg.getSegmentLength())
				{final Segment s = seg.getSegmentData();
				final double segLen=seg.getSegmentLength();
				final Vector3D start =seg.getPosition();
				final Vector3D end = start.add(new Vector3D(segLen,seg.getEndY(),seg.getEndX()));//ZYX
				final Vector3D tunnelSpineNoNorm=end.subtract(start);
				final Vector3D tunnelSpineNorm=tunnelSpineNoNorm.normalize();
				
				final double depthDownSeg=start.getX()-parent.getPosition().getX();
				final double pctDownSeg=depthDownSeg/segLen;
				final Vector3D circleCenter=
						start.add(tunnelSpineNorm.scalarMultiply(parent.getPosition().getX()-start.getX()));
				final double startWidth=TunnelSegment.getStartWidth(s);
				final double startHeight=TunnelSegment.getStartHeight(s);
				final double endWidth=TunnelSegment.getEndWidth(s);
				final double endHeight=TunnelSegment.getEndHeight(s);
				// 0.7 is a fudge-factor to ensure the player doesn't see the outside of the tunnel (due to clipping) before bouncing off.
				final double widthHere=.7*(startWidth*(1.-pctDownSeg)+endWidth*pctDownSeg);
				final double heightHere=.7*(startHeight*(1.-pctDownSeg)+endHeight*pctDownSeg);
				//Parent position relative to tunnel
				final Vector3D pprtt = parent.getPosition().subtract(circleCenter);
				if((pprtt.getZ()*pprtt.getZ())/(widthHere*widthHere)+(pprtt.getY()*pprtt.getY())/(heightHere*heightHere)>1){
					//Execute the "bounce"
					final Vector3D oldPosition = parent.getPosition();
					parent.setPosition(circleCenter.scalarMultiply(.2).add(oldPosition.scalarMultiply(.8)));
					final Vector3D oldHeading = parent.getHeading();
					final Vector3D oldVelocity = velocible.getVelocity();
					final Vector3D oldTop = parent.getTop();
					final Vector3D inwardNormal = circleCenter.subtract(oldPosition).normalize();
					//Bounce the heading and velocity
					if(changeHeadingAndTop){
					    Vector3D newHeading = (inwardNormal.scalarMultiply(inwardNormal.dotProduct(oldHeading)*-2).add(oldHeading));
					    parent.setHeading(newHeading);
					    final Rotation resultingRotation = new Rotation(oldHeading,newHeading);
					    Vector3D newTop = resultingRotation.applyTo(oldTop);
					    if(newTop.getY()<0)newTop=new Vector3D(newTop.getX(),newTop.getY()*-1,newTop.getZ());
        				    parent.setTop(newTop);
					    }
					final RotationalMomentumBehavior rmb = parent.getBehavior().probeForBehavior(RotationalMomentumBehavior.class);
					velocible.setVelocity((inwardNormal.scalarMultiply(inwardNormal.dotProduct(oldVelocity)*-2).add(oldVelocity)));
					if(rmb!=null){//If this is a spinning object, reverse its spin momentum
				    	    rmb.setLateralMomentum(rmb.getLateralMomentum()*-1);
				    	    rmb.setEquatorialMomentum(rmb.getEquatorialMomentum()*-1);
				    	    rmb.setPolarMomentum(rmb.getPolarMomentum()*-1);
				    	    }
					}
				else{seg.setVisible(true);}
				}//end if(in range of segment)
			}//end if(TunnelSegment)
		}//end proposeCollision
	}//end TVBehavior
