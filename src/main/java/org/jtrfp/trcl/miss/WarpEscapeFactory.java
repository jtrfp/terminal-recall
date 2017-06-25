package org.jtrfp.trcl.miss;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.beh.AutoLeveling;
import org.jtrfp.trcl.beh.AutoLeveling.LevelingAxis;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.Cloakable;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.MatchPosition;
import org.jtrfp.trcl.beh.RollLevelingBehavior;
import org.jtrfp.trcl.beh.phy.HasPropulsion;
import org.jtrfp.trcl.beh.phy.RotationalDragBehavior;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;
import org.jtrfp.trcl.beh.ui.PlayerControlBehavior;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.SoundSystemFactory.SoundSystemFeature;
import org.jtrfp.trcl.ext.tr.ViewSelectFactory;
import org.jtrfp.trcl.ext.tr.ViewSelectFactory.ViewSelect;
import org.jtrfp.trcl.flow.TransientExecutor;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.gpu.GL33Model;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.obj.Jumpzone.FinishingRunState;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;
import org.jtrfp.trcl.snd.SoundSystem;
import org.jtrfp.trcl.snd.SoundTexture;
import org.springframework.stereotype.Component;

@Component
public class WarpEscapeFactory implements FeatureFactory<Mission> {
    private final DisablePlayerControlSubmitter disablePlayerControlSubmitter = new DisablePlayerControlSubmitter();
    private final EnablePlayerControlSubmitter  enablePlayerControlSubmitter = new EnablePlayerControlSubmitter();
    private TR tr;
    private GameShell gameShell;
    
    private class DisablePlayerControlSubmitter extends AbstractSubmitter<PlayerControlBehavior>{
   	@Override
   	public void submit(PlayerControlBehavior item) {
   	    if(item instanceof Behavior)
   		((Behavior)item).setEnable(false);
   	}//end submit(...)
       }//end DisablePlayerControlSubmitter
       
       private class EnablePlayerControlSubmitter extends AbstractSubmitter<PlayerControlBehavior>{
   	@Override
   	public void submit(PlayerControlBehavior item) {
   	    if(item instanceof Behavior)
   		((Behavior)item).setEnable(true);
   	}//end submit(...)
       }//end EnablePlayerControlSubmitter
    
    public class WarpEscape implements Feature<Mission>, MissionCompletionHandler{
	private WeakReference<Mission> mission;
	private NavTargetListener navTargetListener = new NavTargetListener();
	private final AtomicBoolean missionAlreadyCompleted = new AtomicBoolean(false);

	@Override
	public void apply(Mission target) {
	    setMission(target);
	    target.addPropertyChangeListener(Mission.CURRENT_NAV_TARGET,navTargetListener);
	}

	@Override
	public void destruct(Mission target) {
	}
	
	private class NavTargetListener implements PropertyChangeListener{
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getNewValue()==null){
		    final Executor executor = TransientExecutor.getSingleton();
		    final WarpEscape warpEscape = WarpEscape.this;
		    synchronized(executor){
			executor.execute(new Runnable(){
			    @Override
			    public void run() {
				warpEscape.
				    missionComplete(null);
			    }});
		    }//end sync(executor)
		}//end if(null)
	    }//end propertyChange(...)
	}//end NavTargetListener
	
	public void missionComplete(WorldObject optionalJumpzone){
	    if(missionAlreadyCompleted.get())
		return;
	    missionAlreadyCompleted.set(true);
	    final Game game = getGameShell().getGame();
	    final ViewSelect viewSelect = Features.get(game, ViewSelect.class);
	    final ViewSelectFactory.ViewMode currentViewMode = viewSelect.getViewMode();
	    final TR tr = getTr();
	    tr.setRunState(new FinishingRunState(){});
	    viewSelect.setViewMode(viewSelect.OUTSIDE_VIEW);
	    // Turn off all player control behavior
	    final Player player = game.getPlayer();
	    player.probeForBehavior(DamageableBehavior.class).addInvincibility(5000);
	    //Cloak player
	    player.probeForBehavior(Cloakable.class).setEnable(true);

	    player.probeForBehaviors(disablePlayerControlSubmitter, PlayerControlBehavior.class);
	    final double [] playerPos = player.getPositionWithOffset();
	    if(optionalJumpzone != null){
		final double [] dunPos = optionalJumpzone.getPositionWithOffset();
		player.setHeading(new Vector3D(playerPos[0]-dunPos[0],0,playerPos[2]-dunPos[2]).normalize());
	    }

	    player.setTop(Vector3D.PLUS_J);
	    final HasPropulsion hp = player.probeForBehavior(HasPropulsion.class);
	    hp.setPropulsion(hp.getMaxPropulsion());
	    //Tilt up
	    //Disable existing auto-leveling
	    final RollLevelingBehavior oldRollLevelingBehavior = player.probeForBehavior(RollLevelingBehavior.class);
	    oldRollLevelingBehavior.setEnable(false);
	    final AutoLeveling autoLeveling = new AutoLeveling().setLevelingAxis(LevelingAxis.HEADING).setLevelingVector(Vector3D.PLUS_J).setRetainmentCoeff(.99, .99, .99);
	    player.addBehavior(autoLeveling);
	    //Spin
	    player.probeForBehavior(RotationalDragBehavior.class).setEnable(false);
	    player.probeForBehavior(RotationalMomentumBehavior.class).setLateralMomentum(.04);
	    //Wait 3s
	    try{Thread.sleep(3000);}
	    catch(InterruptedException e){e.printStackTrace();return;}
	    // Lightning shell on
	    final ResourceManager resourceManager = tr.getResourceManager();
	    final WorldObject lightningShell = new WorldObject();
	    lightningShell.addBehavior(new RotationalMomentumBehavior().setEquatorialMomentum(.053).setLateralMomentum(.04).setPolarMomentum(-.045));
	    //ZAP!
	    SoundTexture zapSound = resourceManager.soundTextures.get("ELECTRIC.WAV");
	    final SoundSystem soundSystem = Features.get(tr,SoundSystemFeature.class);
	    soundSystem.enqueuePlaybackEvent(soundSystem.getPlaybackFactory().create(zapSound, new double[]{SoundSystem.DEFAULT_SFX_VOLUME,SoundSystem.DEFAULT_SFX_VOLUME}));

	    //Load and install lightning sphere model
	    try{final Texture shieldTexture = resourceManager.getRAWAsTexture("SHEILD0.RAW", tr.getDarkIsClearPaletteVL(), null, false, true);
	    final GL33Model shieldModel = resourceManager.getBINModel("GLOBE.BIN", shieldTexture, 5, false, tr.getDarkIsClearPaletteVL(), null);
	    final ArrayList<Triangle> ttris = shieldModel.getRawTransparentTriangleLists().get(0);
	    for(ArrayList<Triangle> tris: shieldModel.getRawTriangleLists()){
		for(Triangle tri:tris){
		    tri.setAlphaBlended(true);
		    ttris.add(tri);
		}//end for(tris)
		    tris.clear();
	    }//end for(rawTriangleLists)
	    lightningShell.setModel(shieldModel);
	    }catch(Exception e){e.printStackTrace();}

	    lightningShell.addBehavior(new MatchPosition().setTarget(player));
	    final Camera mainCamera = tr.mainRenderer.getCamera();
	    mainCamera.getRootGrid().add(lightningShell);
	    lightningShell.setVisible(true);
	    // Wait 1/2 second
	    try  {Thread.sleep(500);}
	    catch(InterruptedException e){}
	    //Behind view
	    viewSelect.setViewMode(viewSelect.CHASE_VIEW);
	    //Charge thrusters
	    SoundTexture boostSound = resourceManager.soundTextures.get("BLAST4.WAV");
	    soundSystem.enqueuePlaybackEvent(soundSystem.getPlaybackFactory().create(boostSound, new double[]{SoundSystem.DEFAULT_SFX_VOLUME,SoundSystem.DEFAULT_SFX_VOLUME}));
	    // Wait 500ms
	    try  {Thread.sleep(1000);}
	    catch(InterruptedException e){}
	    //Lightning shell off
	    lightningShell.setVisible(false);
	    //Decouple Camera
	    tr.mainRenderer.getCamera().probeForBehavior(MatchPosition.class).setEnable(false);
	    //Turbo forward
	    final double prevMaxPropulsion = hp.getMaxPropulsion();
	    final double newMaxPropulsion  = prevMaxPropulsion * 8;
	    hp.setMaxPropulsion(newMaxPropulsion);
	    hp.setPropulsion(newMaxPropulsion);
	    // Wait 1s
	    try{Thread.sleep(1000);}
	    catch(InterruptedException e){e.printStackTrace();return;}
	    //End sound
	    SoundTexture endSound = resourceManager.soundTextures.get("BLAST7.WAV");
	    soundSystem.enqueuePlaybackEvent(soundSystem.getPlaybackFactory().create(endSound, new double[]{SoundSystem.DEFAULT_SFX_VOLUME,SoundSystem.DEFAULT_SFX_VOLUME}));
	    // Wait 1s
	    try{Thread.sleep(1000);}
	    catch(InterruptedException e){e.printStackTrace();return;}

	    System.out.println("MISSION COMPLETE.");
	    //Cleanup
	    oldRollLevelingBehavior.setEnable(true);
	    player.removeBehavior(autoLeveling);
	    tr.mainRenderer.getCamera().probeForBehavior(MatchPosition.class).setEnable(true);
	    player.probeForBehaviors(enablePlayerControlSubmitter, PlayerControlBehavior.class);
	    player.probeForBehavior(HasPropulsion.class).setMaxPropulsion(prevMaxPropulsion).setPropulsion(0);
	    tr.mainRenderer.getCamera().getRootGrid().remove(lightningShell);
	    player.probeForBehavior(RotationalDragBehavior.class).setEnable(true);
	    player.probeForBehavior(RotationalMomentumBehavior.class).setLateralMomentum(.0);
	    player.setVisible(false);//Avoid the flicker
	    viewSelect.setViewMode(currentViewMode);
	    player.probeForBehavior(Cloakable.class).setEnable(false);
	    
	    
	    final Mission mission = getMission();
	    
	    mission.notifyMissionComplete();
	}//end warpEscapeSequence()

	public Mission getMission() {
	    return mission.get();
	}

	public void setMission(Mission mission) {
	    this.mission = new WeakReference<Mission>(mission);
	}
	
	public TR getTr(){
	    return getMission().getTr();
	}
    }//end WarpEscape

    @Override
    public Feature<Mission> newInstance(Mission target) {
	return new WarpEscape();
    }

    @Override
    public Class<Mission> getTargetClass() {
	return Mission.class;
    }

    @Override
    public Class<? extends Feature<Mission>> getFeatureClass() {
	return WarpEscape.class;
    }
    
    public GameShell getGameShell() {
	if(gameShell == null){
	    final TR tr = Features.get(Features.getSingleton(),TR.class);
	    gameShell = Features.get(tr, GameShell.class);}
        return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
        this.gameShell = gameShell;
    }
}//end WarpEscapeFactory
