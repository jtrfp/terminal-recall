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

package org.jtrfp.trcl;

import java.util.TimerTask;

import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;

public class UpfrontDisplay extends RenderableSpacePartitioningGrid {
    private static final double Z = -1;
    private final CharLineDisplay upfrontBillboard;
    private int upfrontDisplayCountdown = 0;
    public UpfrontDisplay(SpacePartitioningGrid<PositionedRenderable> parent, TR tr) {
	super();
	upfrontBillboard = new CharLineDisplay(this, .1, 35, ((TVF3Game)Features.get(tr, GameShell.class).getGame()).getUpfrontFont());
	upfrontBillboard.setPosition(0, .2, Z);
	upfrontBillboard.setVisible(false);
	upfrontBillboard.setCentered(true);
	tr.getThreadManager().getLightweightTimer()
	.scheduleAtFixedRate(new TimerTask() {
	    @Override
	    public void run() {
		try{
		if(upfrontDisplayCountdown>0)
		    upfrontDisplayCountdown -= 200;
		if (upfrontDisplayCountdown <= 0
			&& upfrontDisplayCountdown != Integer.MIN_VALUE) {
		    upfrontBillboard.setVisible(false);
		    upfrontDisplayCountdown = Integer.MIN_VALUE;
		}// end if(timeout)
		}catch(Exception e){e.printStackTrace();}
	    }
	}, 1, 200);
    }//end cosntructor
    
    public UpfrontDisplay submitMomentaryUpfrontMessage(String message) {
	upfrontBillboard.setContent(message);
	upfrontDisplayCountdown = 2000;
	upfrontBillboard.setVisible(true);
	return this;
    }

    public UpfrontDisplay submitPersistentMessage(String message) {
	upfrontBillboard.setContent(message);
	upfrontDisplayCountdown = 2000;
	upfrontBillboard.setVisible(true);
	upfrontDisplayCountdown=Integer.MAX_VALUE;
	return this;
    }
    
    public UpfrontDisplay removePersistentMessage(){
	upfrontDisplayCountdown=0;
	return this;
    }
}//end UpfrontDisplay()
