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

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.gpu.GPU;

public class TunnelInstaller {
    TDFFile tdf;
    World world;

    public TunnelInstaller(TDFFile tdf, World world) {
	this.tdf = tdf;
	this.world = world;
	final TR tr = world.getTr();
	final GPU gpu = tr.gpu.get();
	final OverworldSystem overworldSystem = tr.getOverworldSystem();
	final Mission mission = tr.getCurrentMission();
	TDFFile.Tunnel[] tuns = tdf.getTunnels();
	if (tuns != null) {
	    int tIndex = 0;
	    // Build tunnels
	    for (TDFFile.Tunnel tun : tuns) {
		world.getTr()
			.getReporter()
			.report("org.jtrfp.trcl.TunnelInstaller.tunnel."
				+ tIndex + ".entrance", tun.getEntrance());
		world.getTr()
			.getReporter()
			.report("org.jtrfp.trcl.TunnelInstaller.tunnel."
				+ tIndex + ".exit", tun.getExit());
		mission.newTunnel(tun);
		tIndex++;
	    }
	}// end if(tuns!=null)
    }// end constructor
}// end TDFObjectPlacer
