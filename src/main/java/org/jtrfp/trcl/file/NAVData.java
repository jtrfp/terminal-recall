/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2024 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.file;

import java.util.List;

public interface NAVData {
    public int getNumNavigationPoints();
    public void setNumNavigationPoints(int numNavigationPoints);
    public List<? extends NAVSubObjectData> getNavObjects();
    public void setNavObjects(List<? extends NAVSubObjectData> newObjects);
    
    public interface NAVSubObjectData {
	public Location3D getLocationOnMap();
	public void setLocationOnMap(Location3D loc);
	
	public int getPriority();
	public void setPriority(int newPriority);
	
	public String getCompletionSoundFile();
	public void setCompletionSoundFile(String newCompletionSoundFile);
	
	public String getCompletionText();
	public void setCompletionText(String completionText);
	
	public String getProximitySoundFile();
	public void setProximitySoundFile(String newProximitySoundFile);
	
	public String getDescription();
	public void setDescription(String newDescription);
	
    }//end NAVSubObjectData
    
    public interface DestroyTarget extends NAVSubObjectData {
	public int getNumTargets(); public void setNumTargets(int numTargets);
	public int [] getTargets(); public void setTargets(int [] newTargets);
    }
    public interface EnterTunnel extends NAVSubObjectData {
	public String getUnused(); public void setUnused(String newVal);
	public String getTunnelFileName(); public void setTunnelFileName(String newTunnelFileName);
    }
    public interface Checkpoint extends NAVSubObjectData {}
    public interface Jumpzone extends NAVSubObjectData {}
    public interface ExitTunnel extends NAVSubObjectData {
	public String getUnused1(); public void setUnused1(String newVal);
	public String getUnused2(); public void setUnused2(String newVal);
    }
    public interface Boss extends NAVSubObjectData {
	public int getNumTargets(); public void setNumTargets(int numTargets);
	public int [] getTargets(); public void setTargets(int [] newTargets);
	public int getBossIndex(); public void setBossIndex(int newIndex);
	public String getMusicFile(); public void setMusicFile(String newMusicFileName);
    }
    public interface StartingPoint extends NAVSubObjectData {
	public int getRoll(); public int getPitch(); public int getYaw();
	public void setRoll(int newRoll); public void setPitch(int newPitch); public void setYaw(int newYaw);
    }
    public interface SynchronizationPoint extends NAVSubObjectData {}
    public interface DropBeacon extends NAVSubObjectData {}
    public interface NAVListEnd extends NAVSubObjectData {}
    public interface EscortObject extends NAVSubObjectData {}
    public interface PickupMessagePod extends NAVSubObjectData {}
    public interface Nyx extends NAVSubObjectData {
	public int getNyxIndex(); public void setNyxIndex(int newIndex);
    }
}//end NAVData
