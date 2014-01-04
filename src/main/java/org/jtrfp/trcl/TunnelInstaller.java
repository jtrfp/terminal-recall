package org.jtrfp.trcl;

import org.jtrfp.trcl.file.DirectionVector;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.obj.TunnelEntranceObject;

public class TunnelInstaller
	{
	TDFFile tdf;
	World world;
	
	public TunnelInstaller(TDFFile tdf, World world)
		{
		this.tdf=tdf;
		this.world=world;
		TDFFile.Tunnel []tuns = tdf.getTunnels();
		if(tuns!=null)
			{
			int tIndex=0;
			//Build tunnels
			for(TDFFile.Tunnel tun:tuns)
				{
				world.getTr().getGPU().releaseGL();
				DirectionVector entranceDV= tun.getEntrance();
				DirectionVector exitDV=tun.getExit();
				//Vector3D entranceVector = new Vector3D((double)entranceDV.getZ()/65535.,-.1,(double)entranceDV.getX()/65535.).normalize();
				//Vector3D exitVector = new Vector3D((double)exitDV.getZ()/65535.,-.1,(double)exitDV.getX()/65535.).normalize();
				world.getTr().getGPU().takeGL();
				Tunnel tunnel = new Tunnel(world,tun);
				world.getTr().
				getOverworldSystem().
				add(new TunnelEntranceObject(
					world.
					getTr(),tunnel));
				//if(tIndex++==0)tunnel.activate();//TODO: Remove
				}
			}//end if(tuns!=null)
		}//end constructor
	}//end TDFObjectPlacer
