package com.ritolaaudio.trcl;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.ritolaaudio.trcl.file.DirectionVector;
import com.ritolaaudio.trcl.file.TDFFile;

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
				world.getTr().releaseGL();
				DirectionVector entranceDV= tun.getEntrance();
				DirectionVector exitDV=tun.getExit();
				Vector3D entranceVector = new Vector3D((double)entranceDV.getZ()/65535.,-.1,(double)entranceDV.getX()/65535.).normalize();
				Vector3D exitVector = new Vector3D((double)exitDV.getZ()/65535.,-.1,(double)exitDV.getX()/65535.).normalize();
				world.getTr().takeGL();
				Tunnel tunnel = new Tunnel(world.getRootGrid(),world,tun);
				if(tIndex++==0)tunnel.activate();//TODO: Remove
				}
			}//end if(tuns!=null)
		}//end constructor
	}//end TDFObjectPlacer
