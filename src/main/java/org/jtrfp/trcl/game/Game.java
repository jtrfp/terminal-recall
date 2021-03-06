/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.game;

import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.shell.GameShellFactory.GameShellConstructed;

public interface Game {
////PROPERTIES
   public static final String PAUSED         = "paused";
   public static final String CURRENT_MISSION= "currentMission";
   public static final String PLAYER         = "player";
   public static final String RUN_MODE       = "runMode";
   
   public interface GameRunMode extends GameShellConstructed{}
   public interface GameConstructingMode extends GameRunMode{}
   public interface GameConstructedMode  extends GameRunMode{}
    public interface GameLoadedMode       extends GameConstructedMode{}
     public interface GameRunningMode      extends GameLoadedMode{}
   public interface GameDestructingMode  extends GameRunMode{}
   public interface GameDestructedMode   extends GameRunMode{}
    
    public void    boot() throws IllegalAccessException, FileNotFoundException, IOException, FileLoadException;
    public void    doGameplay() throws IllegalAccessException, FileNotFoundException, IOException, FileLoadException, CanceledException;
    public boolean isPaused();
    public Game    setPaused(boolean paused);
    public Mission getCurrentMission();
    public void    setCurrentMission(Mission newMission);
    public Player  getPlayer();
    public String  getPlayerName();
    public void    abort();
    public Game    addPropertyChangeListener(String property, PropertyChangeListener listener);
    public Game    removePropertyChangeListener(String property, PropertyChangeListener listener);
    public PropertyChangeListener[] getPropertyChangeListeners();
    public boolean hasListeners(String propertyName);
    public PropertyChangeListener[] getPropertyChangeListeners(
	    String propertyName);
    public void    abortCurrentMission();
    
    public class CanceledException extends Exception{
	private static final long serialVersionUID = 1L;}

}//end Game
