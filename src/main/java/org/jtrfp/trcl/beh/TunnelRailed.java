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

import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.file.TNLFile.Segment;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.TunnelSegment;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;

public class TunnelRailed extends Behavior implements CollisionBehavior {
    private final double[] circleCenter = new double[3];
    private final double[] pprtt = new double[3];
    //private TR tr;
    private GameShell gameShell;

    public TunnelRailed() {
	//this.tr = tr;
    }

    @Override
    public void proposeCollision(WorldObject other) {
	if(getGameShell().getGame()==null)return;
	if(getGameShell().getGame().getCurrentMission()==null)return;
	if(getGameShell().getGame().getCurrentMission().getOverworldSystem()==null)return;
	if (!getGameShell().getGame().getCurrentMission().getOverworldSystem().isTunnelMode())
	    return;
	final WorldObject parent = getParent();
	if (other instanceof TunnelSegment) {
	    final TunnelSegment seg = (TunnelSegment) other;
	    final double[] pPos = parent.getPosition();
	    final double[] segPos = seg.getPosition();
	    if (pPos[0] > segPos[0]
		    && pPos[0] < segPos[0] + seg.getSegmentLength()) {
		final Segment s = seg.getSegmentData();
		final double segLen = seg.getSegmentLength();
		final double[] start = segPos;
		final double[] end = Vect3D.add(start, segLen, seg.getEndY(),
			-seg.getEndX(), new double[3]);// ZYX
		final double[] tunnelSpineNoNorm = TRFactory.twosComplementSubtract(
			end, start, new double[3]);
		final double[] tunnelSpineNorm = Vect3D
			.normalize(tunnelSpineNoNorm);

		final double depthDownSeg = start[0] - pPos[0];
		final double pctDownSeg = depthDownSeg / segLen;
		Vect3D.scalarMultiply(tunnelSpineNorm,
			TRFactory.deltaRollover(pPos[0] - start[0]), circleCenter);
		Vect3D.add(start, circleCenter, circleCenter);
		final double startHeight = TunnelSegment.getStartHeight(s);
		final double endHeight = TunnelSegment.getEndHeight(s);

		final double heightHere = (startHeight * (1. - pctDownSeg) + endHeight
			* pctDownSeg);
		boolean groundLocked = false;
		if (parent instanceof DEFObject) {
		    groundLocked = ((DEFObject) parent).isGroundLocked();
		}
		Vect3D.subtract(pPos, circleCenter, pprtt);
		pPos[1] = groundLocked ? circleCenter[1] - heightHere / 2.
			: circleCenter[1];
		pPos[2] = circleCenter[2];
		parent.notifyPositionChange();
	    }// end if(in range of segment)
	}// end if(TunnelSegment)
    }// end proposeCollision
    
    public GameShell getGameShell() {
	if(gameShell == null)
	    gameShell = Features.get(getParent().getTr(), GameShell.class);
        return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
        this.gameShell = gameShell;
    }
}//end TunnelRailed
