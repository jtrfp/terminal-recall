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
	final GPU gpu = tr.getGPU();
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
