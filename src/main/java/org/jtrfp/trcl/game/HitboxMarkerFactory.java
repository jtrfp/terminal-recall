/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2021 Chuck Ritola
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

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.TypeRunStateHandler;
import org.jtrfp.trcl.UpfrontDisplay;
import org.jtrfp.trcl.WeakPropertyChangeListener;
import org.jtrfp.trcl.beh.HitBoxFollowBehavior;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.flow.TransientExecutor;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.obj.BillboardSprite;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.DEFObject.HitBox;
import org.jtrfp.trcl.obj.Positionable;
import org.jtrfp.trcl.obj.WorldObject;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
public class HitboxMarkerFactory implements FeatureFactory<TVF3Game> {
    @Override
    public Feature<TVF3Game> newInstance(TVF3Game target)
	    throws FeatureNotApplicableException {
	return new HitboxMarkerFeature();
    }

    @Override
    public Class<TVF3Game> getTargetClass() {
	return TVF3Game.class;
    }

    @Override
    public Class<HitboxMarkerFeature> getFeatureClass() {
	return HitboxMarkerFeature.class;
    }

    public static class HitboxMarkerFeature implements Feature<TVF3Game>{
	private final GameStatePCL         gameStatePCL = new GameStatePCL();
	private WeakPropertyChangeListener weakStatePCL;
	private TVF3Game target;
	private volatile boolean destructed = false;
	private final String SYSTEM_PROPERTY = "org.jtrfp.trcl.obj.DEFObject.markHitBoxes";
	@Getter
	private boolean hitboxVisibilityEnabled = false;

	@Override
	public void apply(TVF3Game target) {
	    setTarget(target);
	    final TR tr =  target.getTr();
	    weakStatePCL = new WeakPropertyChangeListener(gameStatePCL, tr);
	    tr.addPropertyChangeListener(TRFactory.RUN_STATE, weakStatePCL);
	    final Properties props = System.getProperties();
	    if(props.containsKey(SYSTEM_PROPERTY)) {
		if(props.get(SYSTEM_PROPERTY).toString().toLowerCase().contentEquals("true"))
		    setHitboxVisibilityEnabled(true);
	    }//end if(SYSTEM_PROPERTY)
	}//end apply(...)

	@Override
	public void destruct(TVF3Game target) {
	    if(!destructed)
		target.getTr().removePropertyChangeListener(weakStatePCL);
	    destructed = true;
	}

	protected TVF3Game getTarget() {
	    return target;
	}

	protected void setTarget(TVF3Game target) {
	    this.target = target;
	}

	private class GameStatePCL extends TypeRunStateHandler {

	    public GameStatePCL() {
		super(Mission.LoadingComplete.class);
	    }

	    @Override
	    public void enteredRunState(Object oldState, Object newState) {
		try {installHitBoxMarkers();}
		catch(Exception e) {e.printStackTrace();}
	    }

	    @Override
	    public void exitedRunState(Object oldState, Object newState) {}

	}//end GamestatePCL

	public void setHitboxVisibilityEnabled(boolean newState) {
	    if( newState == this.hitboxVisibilityEnabled)
		return;
	    this.hitboxVisibilityEnabled = newState;
	    final UpfrontDisplay disp = getTarget().getUpfrontDisplay();
	    disp.submitMomentaryUpfrontMessage("Hitbox Markers "+(newState?"On":"Off"));
	    updateHitboxVisibilityStates();
	}//end setHitboxVisibilityEnabled(...)

	private void installHitBoxMarkers() throws IllegalAccessException, IOException, FileLoadException {
	    final TVF3Game game = getTarget();
	    final TR tr = game.getTr();

	    if(game != null) {
		final Mission mission = game.getCurrentMission();
		if(mission != null) {
		    final List<DEFObject> defs = mission.getOverworldSystem().getDefList();
		    if( defs != null ) {
			final boolean markersVisible = this.isHitboxVisibilityEnabled();
			for( DEFObject def : defs ) {
			    final HitBox [] hitBoxes = def.getHitBoxes();
			    if(hitBoxes != null) {
				System.out.println("Found hitboxes for "+def);
				final ResourceManager rm = tr.getResourceManager();
				final Texture hitBoxTexture = rm.getRAWAsTexture("TARG1.RAW", tr.getDarkIsClearPaletteVL(), null, true, false, true);
				@SuppressWarnings("unchecked")
				final SpacePartitioningGrid<Positionable> spg = (SpacePartitioningGrid<Positionable>)def.getContainingGrid();
				final ArrayList<BillboardSprite> toAdd = new ArrayList<>();
				for (HitBox hitBox : hitBoxes) {
				    final HitBoxFollowBehavior beh = new HitBoxFollowBehavior(def, hitBox);
				    final BillboardSprite bbs = new BillboardSprite();
				    bbs.setPosition(def.getPositionWithOffset());
				    bbs.addBehavior(beh);
				    bbs.setBillboardSize(new Dimension((int)hitBox.getSize(),(int)hitBox.getSize()));
				    bbs.setTexture(hitBoxTexture, true);
				    bbs.setActive(true);
				    bbs.setVisible(markersVisible);
				    def.getSubObjects().add(bbs);
				    toAdd.add(bbs);
				} // end for(boxes)
				
				TransientExecutor.getSingleton().execute(()->{
					for(BillboardSprite bbs:toAdd)
					    spg.add(bbs);
				    });
			    }//end if(markHitBoxes)
			}//end for(defs)
		    }//end if(defs)
		}//end if(mission)
	    }//end if(game)
	}//end installHitboxMarkers()

	private void updateHitboxVisibilityStates() {
	    final TVF3Game game = getTarget();
	    if(game != null) {
		final Mission mission = game.getCurrentMission();
		if(mission != null) {
		    final OverworldSystem ows = mission.getOverworldSystem();
		    if(ows != null) {
			final List<DEFObject> defs = mission.getOverworldSystem().getDefList();
			if( defs != null ) {
			    final boolean markersVisible = this.isHitboxVisibilityEnabled();
			    for( DEFObject def : defs ) {
				final HitBox [] hitBoxes = def.getHitBoxes();
				final List<WorldObject> subObjects = def.getSubObjects();
				if(hitBoxes != null && subObjects != null) {
				    for(WorldObject sub : subObjects) {
					if(sub instanceof BillboardSprite && sub.probeForBehavior(HitBoxFollowBehavior.class) != null)
					    sub.setVisible(markersVisible);
				    }//end for(subObject)
				}//end if(markHitBoxes)
			    }//end for(defs)
			}//end if(defs)
		    }//end if(OverworldSystem)
		}//end if(mission)
	    }//end if(game)
	}//end installHitboxMarkers()

    }//end HitboxMarkerFeature

}//end HitboxMarkerFactory
