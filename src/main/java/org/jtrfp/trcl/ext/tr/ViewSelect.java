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

package org.jtrfp.trcl.ext.tr;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.Callable;

import javax.media.opengl.GL3;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.beh.MatchDirection;
import org.jtrfp.trcl.beh.MatchPosition;
import org.jtrfp.trcl.beh.MatchPosition.OffsetMode;
import org.jtrfp.trcl.core.ControllerInput;
import org.jtrfp.trcl.core.ControllerInputs;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRFuture;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.flow.IndirectProperty;
import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.RelevantEverywhere;
import org.jtrfp.trcl.obj.WorldObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ViewSelect {
    public static final String VIEW             = "View Select",
	                       INSTRUMENTS_VIEW = "Instruments View",
	                       VIEW_MODE        = "View Mode",
	                       INSTRUMENT_MODE  = "Instrument Mode";
    private final ControllerInput view, iView;
    private int viewModeItr = 1, instrumentModeItr = 1;
    private final TR tr;
    private static final int TAIL_DISTANCE = 15000,
	                     FLOAT_HEIGHT  = 5000;
    private WorldObject cockpit;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private ViewMode viewMode;
    private InstrumentMode instrumentMode;
    private Model cockpitModel;
    private final ViewMode [] viewModes = new ViewMode[]{
	new CockpitView(),
	new OutsideView(),
	new ChaseView()
    };
    private final InstrumentMode [] instrumentModes = new InstrumentMode[]{
	//new FullCockpitInstruments(),
	//new HeadsUpDisplayInstruments(),
	new NoInstruments()
    };
 @Autowired
 public ViewSelect(final TR tr, ControllerInputs inputs){
     view    = inputs.getControllerInput(VIEW);
     iView   = inputs.getControllerInput(INSTRUMENTS_VIEW);
     this.tr = tr;
     view .addPropertyChangeListener(new ViewSelectPropertyChangeListener());
     iView.addPropertyChangeListener(new InstrumentViewSelectPropertyChangeListener());
     final IndirectProperty<Game> gameIP = new IndirectProperty<Game>();
     tr.addPropertyChangeListener(TR.GAME, gameIP);
     final IndirectProperty<Mission> missionIP = new IndirectProperty<Mission>();
     gameIP.addTargetPropertyChangeListener(Game.CURRENT_MISSION, missionIP);
     gameIP.setTarget(tr.getGame());
     //Install when in gameplay
     missionIP.addTargetPropertyChangeListener(Mission.MISSION_MODE, new PropertyChangeListener(){
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    if(evt.getNewValue()!=null && evt.getNewValue() instanceof Mission.GameplayMode 
		    && !(evt.getOldValue() instanceof Mission.GameplayMode))
		setViewMode(getViewMode());
	    else if(evt.getNewValue()==null)
		{noViewMode();}
	}});
 }//end constructor
 
 private class NoInstruments implements InstrumentMode{
    @Override
    public void apply() {
	final Game game = tr.getGame();
	if(game==null)
	    return;
	//game.getHUDSystem().
    }
 }//end NoInstruments
 
 private class InstrumentViewSelectPropertyChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	final Game game = tr.getGame();
	if(game==null)
	    return;
	final Mission mission = game.getCurrentMission();
	if(mission==null)
	    return;
	if(mission.isSatelliteView())
	    return;
	if(!(getViewMode() instanceof CockpitView))
	    return;
	if((Double)evt.getNewValue()>.7)
	 setInstrumentMode(instrumentModes[instrumentModeItr++%instrumentModes.length]);
    }
 }//end InstrumentViewSelectPropertyChangeListener
 
 private class ViewSelectPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	final Game game = tr.getGame();
	if(game==null)
	    return;
	final Mission mission = game.getCurrentMission();
	if(mission==null)
	    return;
	if(mission.isSatelliteView())
	    return;
	if((Double)evt.getNewValue()>.7)
	 setViewMode(viewModes[viewModeItr++%viewModes.length]);
    }//end propertyChange(...)
 }//end ViewSelectPropertyChangeListener
 
 private interface ViewMode{
     public void apply();
 }
 
 private interface InstrumentMode{
     public void apply();
 }
 
 public class CockpitView implements ViewMode{
     @Override
     public void apply(){
	     final Game game = tr.getGame();
	     if(game==null)
		 return;
	     final Player player = game.getPlayer();
	     if(player==null)
		 return;
	     player.setVisible(false);
	     final WorldObject cockpit = getCockpit();
	     cockpit.probeForBehavior(MatchPosition.class) .setTarget(player);
	     cockpit.probeForBehavior(MatchDirection.class).setTarget(player);
	     //cockpit.setVisible(true);//TODO: Depend on "C" value
	     final Camera cam = tr.mainRenderer.get().getCamera();
	     cam.probeForBehavior(MatchPosition.class).setOffsetMode(MatchPosition.NULL);
	     final RealMatrix lookAtMatrix = new Array2DRowRealMatrix( new double[][]{//Identity
		     new double [] {1, 0, 0, 0},
		     new double [] {0, 1, 0, 0},
		     new double [] {0, 0, 1, 0},
		     new double [] {0, 0, 0, 1}
	     });
	     final RealMatrix topMatrix    = new Array2DRowRealMatrix( new double[][]{//Identity
		     new double [] {1, 0, 0, 0},
		     new double [] {0, 1, 0, 0},
		     new double [] {0, 0, 1, 0},
		     new double [] {0, 0, 0, 1}
	     });
	     final MatchDirection md = cam.probeForBehavior(MatchDirection.class);
	     md.setEnable(true);
	     md.setLookAtMatrix4x4 (lookAtMatrix);
	     md.setTopMatrix4x4    (topMatrix);
	 }//end apply()  
 }//end CockpitView
 
 public class OutsideView implements ViewMode{
     @Override
     public void apply(){
	     final Game game = tr.getGame();
	     if(game==null)
		 return;
	     final Player player = game.getPlayer();
	     if(player==null)
		 return;
	     player.setVisible(true);
	     getCockpit().setVisible(false);
	     final Camera cam = tr.mainRenderer.get().getCamera();
	     final MatchPosition mp = cam.probeForBehavior(MatchPosition.class);
	     mp.setOffsetMode(new OffsetMode(){
		 private double [] workArray = new double[3];
		@Override
		public void processPosition(double[] position, MatchPosition mp) {
		    final double [] lookAt    = player.getHeadingArray();
		    System.arraycopy(lookAt, 0, workArray, 0, 3);
		    workArray[1]= workArray[0]!=0&&workArray[2]!=0?0:1;
		    Vect3D.normalize(workArray, workArray);
		    Vect3D.scalarMultiply(workArray, -TAIL_DISTANCE, workArray);
		    Vect3D.add(position, workArray, position);
		}});
	     final RealMatrix lookAtMatrix = new Array2DRowRealMatrix( new double[][]{//Flat to horizon
		     new double [] {1, 0, 0, 0},
		     new double [] {0, 0, 0, 0},
		     new double [] {0, 0, 1, 0},
		     new double [] {0, 0, 0, 1}
	     });
	     final RealMatrix topMatrix    = new Array2DRowRealMatrix( new double[][]{//Flat to horizon
		     new double [] {0, 0, 0, 0},
		     new double [] {0, 0, 0, 1},
		     new double [] {0, 0, 0, 0},
		     new double [] {0, 0, 0, 1}
	     });
	     final MatchDirection md = cam.probeForBehavior(MatchDirection.class);
	     md.setEnable(true);
	     md.setLookAtMatrix4x4 (lookAtMatrix);
	     md.setTopMatrix4x4    (topMatrix);
	 }//end apply()
 }//end ForwardChaseView
 
 public class ChaseView implements ViewMode{
     @Override
     public void apply(){
	     final Game game = tr.getGame();
	     if(game==null)
		 return;
	     final Player player = game.getPlayer();
	     if(player==null)
		 return;
	     player.setVisible(true);
	     getCockpit().setVisible(false);
	     final Camera cam = tr.mainRenderer.get().getCamera();
	     final MatchPosition mp = cam.probeForBehavior(MatchPosition.class);
	     mp.setOffsetMode(new OffsetMode(){
		 private double [] workArray = new double[3];
		@Override
		public void processPosition(double[] position, MatchPosition mp) {
		    final double [] lookAt    = player.getHeadingArray();
		    System.arraycopy(lookAt, 0, workArray, 0, 3);
		    Vect3D.normalize(workArray, workArray);
		    //Need to take care of y before x and z due to dependency order.
		    workArray[1]*=-TAIL_DISTANCE;
		    workArray[1]+= FLOAT_HEIGHT*Math.sqrt(workArray[0]*workArray[0]+workArray[2]*workArray[2]);
		    
		    workArray[0]*=-TAIL_DISTANCE;
		    workArray[2]*=-TAIL_DISTANCE;
		    Vect3D.add(position, workArray, position);
		}});
	     final RealMatrix lookAtMatrix = new Array2DRowRealMatrix( new double[][]{//Flat to horizon
		     new double [] {1, 0, 0, 0},
		     new double [] {0, 1, 0, 0},
		     new double [] {0, 0, 1, 0},
		     new double [] {0, 0, 0, 1}
	     });
	     final RealMatrix topMatrix    = new Array2DRowRealMatrix( new double[][]{//Flat to horizon
		     new double [] {1, 0, 0, 0},
		     new double [] {0, 1, 0, 0},
		     new double [] {0, 0, 1, 0},
		     new double [] {0, 0, 0, 1}
	     });
	     final MatchDirection md = cam.probeForBehavior(MatchDirection.class);
	     md.setEnable(true);
	     md.setLookAtMatrix4x4 (lookAtMatrix);
	     md.setTopMatrix4x4    (topMatrix);
	 }//end apply()
 }//end BehindAbove

public ViewMode[] getViewModes() {
    return viewModes;
}

public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
}

public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
}

public PropertyChangeListener[] getPropertyChangeListeners() {
    return pcs.getPropertyChangeListeners();
}

public void addPropertyChangeListener(String propertyName,
	PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(propertyName, listener);
}

public void removePropertyChangeListener(String propertyName,
	PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(propertyName, listener);
}

public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
    return pcs.getPropertyChangeListeners(propertyName);
}

public boolean hasListeners(String propertyName) {
    return pcs.hasListeners(propertyName);
}

public ViewMode getViewMode() {
    return viewMode;
}

public void setViewMode(ViewMode viewMode) {
    final ViewMode oldViewMode = viewMode;
    this.viewMode = viewMode;
    pcs.firePropertyChange(VIEW_MODE, oldViewMode, viewMode);
    if(viewMode!=null)
     viewMode.apply();
    else//Remove the state
	noViewMode();
}//end setViewMode

public void noViewMode(){
    final WorldObject cockpit = getCockpit();
    cockpit.setVisible(false);
    final Camera cam = tr.mainRenderer.get().getCamera();
    cam.probeForBehavior(MatchPosition.class).setOffsetMode(MatchPosition.NULL);
}

public WorldObject getCockpit() {
    if(cockpit == null){
	cockpit = new Cockpit(tr);
	cockpit.setModel(getCockpitModel());
	cockpit.addBehavior(new MatchPosition());
	cockpit.addBehavior(new MatchDirection());
	tr.mainRenderer.get().getCamera().getRootGrid().add(cockpit);
	cockpit.setVisible(false);
	cockpit.notifyPositionChange();
    }
    return cockpit;
}

private static class Cockpit extends WorldObject implements RelevantEverywhere{

    public Cockpit(TR tr) {
	super(tr);
    }
    
    @Override
    public boolean supportsLoop(){
	return false;
    }
    
    @Override
    public boolean recalcMatrixWithEachFrame(){
	return true;
    }
 }//end Cockpit

public InstrumentMode getInstrumentMode() {
    return instrumentMode;
}

public void setInstrumentMode(InstrumentMode instrumentMode) {
    final InstrumentMode oldInstrumentMode = instrumentMode;
    this.instrumentMode = instrumentMode;
    pcs.firePropertyChange(INSTRUMENT_MODE, oldInstrumentMode, instrumentMode);
    if(instrumentMode!=null)
	instrumentMode.apply();
    else
	new NoInstruments().apply();
}

public Model getCockpitModel() {
    if(cockpitModel==null){
	final GPU gpu = tr.gpu.get();
	final GL3 gl = gpu.getGl();
	final ColorPaletteVectorList cpvl = tr.getGlobalPaletteVL();
	try{cockpitModel = tr.getResourceManager().getBINModel("COCKMDL.BIN", cpvl, null, gl);}
	catch(Exception e){e.printStackTrace();}
    }//end if(null)
    return cockpitModel;
}//end getCockpitModel()

public void setCockpitModel(Model cockpitModel) {
    this.cockpitModel = cockpitModel;
}
}//end ViewChange
