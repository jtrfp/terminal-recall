/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2015 Chuck Ritola and contributors.
 * See Github project's commit log for contribution details.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 ******************************************************************************/

package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.World;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;

public class SkyCubeCloudModeUpdateBehavior extends Behavior {
    private boolean over=false;
    private GameShell gameShell;
    @Override
    public void tick(long timeInMillis){
	final WorldObject parent = getParent();
	double [] pos = parent.getPosition();
	final TR tr = parent.getTr();
	final World w = tr.getWorld();
	if(pos[1]> w.sizeY/2){
	    if(!over)
	    {over=true;
	    tr.mainRenderer.
	     getSkyCube().
	     setSkyCubeGen(
	      getGameShell().getGame().
	      getCurrentMission().
	      getOverworldSystem().
	      getSkySystem().
	      getAboveCloudsSkyCubeGen());
	     }//end of(!over)
	    }//end if(pos over clouds)
	else{
	    if(over)
	    {over=false;
	    tr.mainRenderer.
	     getSkyCube().
	     setSkyCubeGen(
	      getGameShell().getGame().
	      getCurrentMission().
	      getOverworldSystem().
	      getSkySystem().
	      getBelowCloudsSkyCubeGen());
	     }//end if(over)
	    }//end if(pos under clouds)
    }//end _tick(...)
    public GameShell getGameShell() {
	if(gameShell == null)
	    gameShell = Features.get(getParent().getTr(), GameShell.class);
        return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
        this.gameShell = gameShell;
    }
}//end SkyCubeCloudModeUpdateBehavior
