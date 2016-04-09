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

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.media.opengl.GL3;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.HUDSystem;
import org.jtrfp.trcl.KeyStatus;
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.WeakPropertyChangeListener;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.MatchDirection;
import org.jtrfp.trcl.beh.MatchPosition;
import org.jtrfp.trcl.beh.MatchPosition.TailOffsetMode;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.ctl.ControllerInput;
import org.jtrfp.trcl.ctl.ControllerInputs;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.gui.CockpitLayout;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.obj.MiniMap;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.RelevantEverywhere;
import org.jtrfp.trcl.obj.WorldObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ViewSelectFactory implements FeatureFactory<Game> {
    //PROPERTIES
    public static final String HUD_VISIBLE      = "hudVisible";
    //CONSTANTS
    public static final String VIEW             = "View Select",
	                       INSTRUMENTS_VIEW = "Instruments View",
	                       VIEW_MODE        = "View Mode",
	                       INSTRUMENT_MODE  = "Instrument Mode";
    private static final boolean INS_ENABLE = true;
    private final ControllerInput view, iView;
    private CockpitLayout cockpitLayout;
    private RenderableSpacePartitioningGrid grid;
    
    @Autowired
    private TR tr;
    private Model cockpitModel;
    private static final int TAIL_DISTANCE = 15000,
	                     FLOAT_HEIGHT  = 5000;

    public interface ViewMode{
	    public void apply();
	}

	private interface InstrumentMode{
	    public boolean apply();
	}
   
	@Autowired
 public ViewSelectFactory(ControllerInputs inputs){
     view    = inputs.getControllerInput(VIEW);
     iView   = inputs.getControllerInput(INSTRUMENTS_VIEW);
 }//end constructor
 
 public class ViewSelect implements Feature<Game>{
     private boolean hudVisible = false;
     private WorldObject cockpit;
     private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
     private ViewMode viewMode;
     private InstrumentMode instrumentMode;
     private boolean hudVisibility = false;
     private MiniMap miniMap;
     private MatchPosition.TailOffsetMode tailOffsetMode;
     
     private int viewModeItr = 0, instrumentModeItr = 1;
     
     public final InstrumentMode 
      FULL_COCKPIT    = new FullCockpitInstruments(),
      HUD_INSTRUMENTS = new HeadsUpDisplayInstruments(),
      NO_INSTRUMENTS  = new NoInstruments();
     
     public final ViewMode
      COCKPIT_VIEW = new CockpitView(),
      OUTSIDE_VIEW = new OutsideView(),
      CHASE_VIEW   = new ChaseView();
     
     private final ViewMode [] viewModes = new ViewMode[]{
		COCKPIT_VIEW,
		OUTSIDE_VIEW,
		CHASE_VIEW
	    };
	    private final InstrumentMode [] instrumentModes = new InstrumentMode[]{
		NO_INSTRUMENTS,
		HUD_INSTRUMENTS,
		FULL_COCKPIT
	    };
	    
     private final PropertyChangeListener viewSelectPropertyChangeListener          = new ViewSelectPropertyChangeListener();
     private final PropertyChangeListener instrumentViewSelectPropertyChangeListener= new InstrumentViewSelectPropertyChangeListener();
     private final PropertyChangeListener runStateListener                          = new RunStatePropertyChangeListener();
     private PropertyChangeListener weakVSPCL, weakIVSPCL, weakRSPCL;//HARD REFERENCES. DO NOT REMOVE
     
     @Override
     public void apply(Game game) {
         view .addPropertyChangeListener(weakVSPCL  = new WeakPropertyChangeListener(viewSelectPropertyChangeListener,view));
         iView.addPropertyChangeListener(weakIVSPCL = new WeakPropertyChangeListener(instrumentViewSelectPropertyChangeListener,iView));
         
         /*final IndirectProperty<Mission> missionIP = new IndirectProperty<Mission>();
         ((TVF3Game)game).addPropertyChangeListener(Game.CURRENT_MISSION, missionIP);*/
         
         /*missionIP.addTargetPropertyChangeListener(Mission.MISSION_MODE, */
        tr.addPropertyChangeListener(TR.RUN_STATE, weakRSPCL = new WeakPropertyChangeListener(runStateListener,tr));
     }//end apply(...)
     
     public ViewMode getViewMode() {
	    return viewMode;
	}

	public void setViewMode(ViewMode viewMode) {
	    final ViewMode oldViewMode = viewMode;
	    this.viewMode = viewMode;
	    if(viewMode!=null)
	     viewMode.apply();
	    else//Remove the state
		noViewMode();
	    pcs.firePropertyChange(VIEW_MODE, oldViewMode, viewMode);
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
		//cockpit.setModelOffset(0, -100, 0);
		cockpit.addBehavior(new MatchPosition());
		cockpit.addBehavior(new MatchDirection());
		final SpacePartitioningGrid<PositionedRenderable> grid = getGrid();
		grid.add(cockpit);
		//grid.add(getMiniMap());//TODO: Uncomment when miniMap is ready
		cockpit.setVisible(false);
		cockpit.notifyPositionChange();
	    }
	    return cockpit;
	}//end getCockpit()

	private void setHUDVisibility(boolean visible){
	    if(hudVisibility==visible)
		 return;
	    this.hudVisibility=visible;
	    final Game game = tr.getGame();
	    final HUDSystem hud = ((TVF3Game)game).getHUDSystem();
	    final NAVSystem nav = ((TVF3Game)game).navSystem;
	    final RenderableSpacePartitioningGrid grid = tr.getDefaultGrid();
	    if(!visible){
	     grid.nonBlockingRemoveBranch (hud);
	     grid.nonBlockingRemoveBranch (nav);
	     }
	    else{
	     grid.nonBlockingAddBranch    (hud);
	     grid.nonBlockingAddBranch    (nav);
	     }
	}//end setHUDVisibility(...)

	private class NoInstruments implements InstrumentMode{
	   @Override
	   public boolean apply() {
		setHUDVisibility(false);
		getCockpit().setVisible(false);
		return true;
	   }
	}//end NoInstruments

	private class FullCockpitInstruments implements InstrumentMode{
	   @Override
	   public boolean apply() {
		if(!(getViewMode() instanceof CockpitView))
		    return false;
		getCockpit().setVisible(true);
		setHUDVisibility(true);
		return true;
	   }
	}//end FullCockpitInstruments

	private class HeadsUpDisplayInstruments implements InstrumentMode{
	   @Override
	   public boolean apply() {
		getCockpit().setVisible(false);
		setHUDVisibility(true);
		return true;
	   }//end HeadsUpDisplayInstruments
	}//end HeadsUpDisplayInstruments

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
		if((Double)evt.getNewValue()>.7){
		    incrementInstrumentMode();
		    reEvaluateState();
		    }
	   }
	}//end InstrumentViewSelectPropertyChangeListener
	
	private class RunStatePropertyChangeListener implements PropertyChangeListener {
	    @Override
	    public void propertyChange(PropertyChangeEvent arg0) {
		reEvaluateState();
	    }
	    
	}//end RunStatePropertyChangeListener

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
		if(!(tr.getRunState() instanceof Mission.PlayerActivity))
		    return;
		if((Double)evt.getNewValue()>.7){
		    incrementViewMode();
		    reEvaluateState();
		    }
	   }//end propertyChange(...)
	}//end ViewSelectPropertyChangeListener

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
		     mp.setOffsetMode(new TailOffsetMode(new Vector3D(0,0,-TAIL_DISTANCE), Vector3D.ZERO));
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
		     mp.setOffsetMode(new TailOffsetMode(new Vector3D(0,0,-TAIL_DISTANCE), new Vector3D(0,FLOAT_HEIGHT,0)));
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

	private class Cockpit extends WorldObject implements RelevantEverywhere{
	    
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

	public boolean setInstrumentMode(InstrumentMode instrumentMode) {
	    if(!INS_ENABLE)return true;
	    final InstrumentMode oldInstrumentMode = instrumentMode;
	    this.instrumentMode = instrumentMode;
	    pcs.firePropertyChange(INSTRUMENT_MODE, oldInstrumentMode, instrumentMode);
	    if(instrumentMode!=null)
		return instrumentMode.apply();
	    else
		return new HeadsUpDisplayInstruments().apply();
	}//end setInstrumentMode()
	
	public ViewMode[] getViewModes() {
	    return viewModes;
	}

	@Override
	public void destruct(Game target) {
	    if(cockpit != null)
	     tr.mainRenderer.get().getCamera().getRootGrid().remove(cockpit);
	}
	
	private void incrementViewMode(){
	    viewModeItr++;
	    viewModeItr%=viewModes.length;
	}
	
	private void incrementInstrumentMode(){
	    instrumentModeItr++;
	    instrumentModeItr%=instrumentModes.length;
	}
	
	private void reEvaluateState(){
	    final SpacePartitioningGrid<PositionedRenderable> rootGrid = tr.mainRenderer.get().getCamera().getRootGrid();
            final RenderableSpacePartitioningGrid grid = getGrid();
	    if(!isAppropriateToDisplay()){
		setInstrumentMode(new NoInstruments());
		if(rootGrid.containsBranch(grid))
		    rootGrid.nonBlockingRemoveBranch(grid);
		return;
	    }//end if(!isAppropriateToDisplay()
	    if(!rootGrid.containsBranch(grid)){
		getMiniMap().setTextureMesh(tr.getGame().getCurrentMission().getOverworldSystem().getTextureMesh());
		rootGrid.nonBlockingAddBranch(grid);
	    }//end if(!containsBranch)
	    if(getViewMode()!=viewModes[viewModeItr])
		setViewMode(viewModes[viewModeItr]);
	    if(getInstrumentMode()!=instrumentModes[instrumentModeItr])
		setInstrumentMode(instrumentModes[instrumentModeItr]);
	    if(getViewMode()!=COCKPIT_VIEW && getInstrumentMode() == FULL_COCKPIT){
		    incrementInstrumentMode();
		    reEvaluateState();//Recursive
		    }
	}//end reEvaluateState()
	
	private boolean isAppropriateToDisplay(){
	    if(tr.getRunState() instanceof Mission.PlayerActivity){
		if(tr.getGame().getCurrentMission().isSatelliteView())
		    return false;
		return true;
	    }//end if(PlayerActivity)
	    return false;
	}//end isAppropriateToDisplay()
	
	public MiniMap getMiniMap() {
	    if(miniMap == null){
		miniMap = new MiniMap(tr);
		miniMap.setImmuneToOpaqueDepthTest(true);
		miniMap.setModelSize(new double[]{1200,1200});
		final MatchPosition mp;
		miniMap.addBehavior(mp = new MatchPosition());
		final WorldObject cockpit = getCockpit();
		miniMap.addBehavior(new MiniMapCockpitBehavior(cockpit));
		//Sorry, I'm just not smart enough to fix it the right way at this moment. - Chuck
		miniMap.setMapHack(new Rotation(Vector3D.PLUS_I, Vector3D.PLUS_J,Vector3D.MINUS_I, Vector3D.PLUS_J));
		mp.setTarget(cockpit);
		mp.setOffsetMode(tailOffsetMode = new MatchPosition.TailOffsetMode(new Vector3D(0, -1450, 8454), Vector3D.ZERO));
	    }//end if(null)
	    return miniMap;
	}//end getMiniMap()
	
	private class MiniMapCockpitBehavior extends Behavior {
	    private final WorldObject cockpit;
	    private final KeyStatus keyStatus = new KeyStatus();
	    private static final double INCREMENT = 50;
	    //private Rotation offsetRot = new Rotation(Vector3D.PLUS_K, Vector3D.PLUS_J, 
	//	    new Vector3D(0.8958871503, 0.1646309237, -0.4126534537),new Vector3D(-0.2201229509, 0.9712743572, -0.0903991674));
	    private Rotation offsetRot = Rotation.IDENTITY;
	    
	    public MiniMapCockpitBehavior(WorldObject cockpit){
		this.cockpit = cockpit;
	    }
	    
	    @Override
	    public void tick(long tickTimeMillis){
		placerTick();
		final MiniMap parent = (MiniMap)getParent();
		//HEADING
		//Vect3D.scalarMultiply(cockpit.getHeadingArray(), -1., parent.getHeadingArray());
		//TODO: Apply top/heading as rotation to offsetRot. Make offsetRot a vector
		Rotation rot = new Rotation(Vector3D.PLUS_K,Vector3D.PLUS_J,cockpit.getHeading().negate(),cockpit.getTop());
		parent.setHeading(rot.applyTo(offsetRot.applyTo(Vector3D.PLUS_K)));
		//TOP ORIGIN
		parent.setTopOrigin(rot.applyTo(offsetRot.applyTo(Vector3D.PLUS_J)));
		parent.notifyPositionChange();
	    }
	    
	    private void placerTick(){
		final MiniMap parent = (MiniMap)getParent();
		if(keyStatus.isPressed(KeyEvent.VK_U))
		    tailOffsetMode.setTailVector(tailOffsetMode.getTailVector().add(new Vector3D(0,INCREMENT,0)));
		if(keyStatus.isPressed(KeyEvent.VK_D))
		    tailOffsetMode.setTailVector(tailOffsetMode.getTailVector().add(new Vector3D(0,-INCREMENT,0)));
		if(keyStatus.isPressed(KeyEvent.VK_G))
		    tailOffsetMode.setTailVector(tailOffsetMode.getTailVector().add(new Vector3D(0,0,INCREMENT)));
		if(keyStatus.isPressed(KeyEvent.VK_B))
		    tailOffsetMode.setTailVector(tailOffsetMode.getTailVector().add(new Vector3D(0,0,-INCREMENT)));
		if(keyStatus.isPressed(KeyEvent.VK_L))
		    tailOffsetMode.setTailVector(tailOffsetMode.getTailVector().add(new Vector3D(-INCREMENT,0,0)));
		if(keyStatus.isPressed(KeyEvent.VK_R))
		    tailOffsetMode.setTailVector(tailOffsetMode.getTailVector().add(new Vector3D(INCREMENT,0,0)));
		if(keyStatus.isPressed(KeyEvent.VK_COMMA))
		    rotate(2*Math.PI*.001);
		if(keyStatus.isPressed(KeyEvent.VK_PERIOD))
		    rotate(-2*Math.PI*.001);
		System.out.println("TAIL OFF= "+tailOffsetMode.getTailVector()+" ROT TOP="+parent.getTopOrigin()+" ROT HED="+parent.getHeading());
	    }//end placerTick()
		
		private void rotate(double theta){
		    final MiniMap parent = (MiniMap)getParent();
		    final Rotation rot = new Rotation(Vector3D.PLUS_I, theta);
		    offsetRot = rot.applyTo(offsetRot);
		    parent.setHeading(rot.applyTo(parent.getHeading()));
		    parent.setTopOrigin(rot.applyTo(parent.getTopOrigin()));
		}
	}//end MiniMapCockpitBehavior
 }//end ViewSelectFeature
 
public Model getCockpitModel() {
    if(cockpitModel==null){
	final GPU gpu = tr.gpu.get();
	final GL3 gl = gpu.getGl();
	final ColorPaletteVectorList cpvl = tr.getGlobalPaletteVL();
	try{
	    cockpitModel = tr.getResourceManager().getBINModel("COCKMDL.BIN", cpvl, null, gl);
	}
	catch(Exception e){e.printStackTrace();}
    }//end if(null)
    return cockpitModel;
}//end getCockpitModel()

public void setCockpitModel(Model cockpitModel) {
    this.cockpitModel = cockpitModel;
}

@Override
public Feature<Game> newInstance(Game target) {
    return new ViewSelect();
}

@Override
public Class<Game> getTargetClass() {
    return Game.class;
}

@Override
public Class<? extends Feature> getFeatureClass() {
    return ViewSelect.class;
}

protected CockpitLayout getCockpitLayout() {
    if(cockpitLayout == null)
	cockpitLayout = new CockpitLayout.Default();
    return cockpitLayout;
}

protected void setCockpitLayout(CockpitLayout cockpitLayout) {
    this.cockpitLayout = cockpitLayout;
}

protected RenderableSpacePartitioningGrid getGrid() {
    if(grid == null)
	grid = new RenderableSpacePartitioningGrid();
    return grid;
}

protected void setGrid(RenderableSpacePartitioningGrid grid) {
    this.grid = grid;
}

}//end ViewSelect
