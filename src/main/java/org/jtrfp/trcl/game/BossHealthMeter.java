/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017 Chuck Ritola
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.CharLineDisplay;
import org.jtrfp.trcl.GLFont;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.flow.GameVersion;
import org.jtrfp.trcl.flow.IndirectProperty;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.obj.MeterBar;
import org.jtrfp.trcl.obj.WorldObject;

public class BossHealthMeter {
    private final RenderableSpacePartitioningGrid spg = new RenderableSpacePartitioningGrid();
    private RenderableSpacePartitioningGrid targetSPG;
    private TVF3Game game;
    private boolean  initialized = false;
    private CharLineDisplay charLineDisplay;
    private MeterBar        healthBar;
    private GLFont          font;
    private Texture         meterTexture;
    protected static final double FONT_SIZE    = .06,
	                          METER_HEIGHT = .04,
                                  METER_LENGTH = .7;
    protected static final double [] HEALTH_BAR_POS = new double []{.1,-.9,.0001};
    protected static final double [] CHAR_LINE_POS  = new double []{-1,-.93,.0001};
    
    private IndirectProperty<Game> gameIP; //HARD REFERENCE; DO NOT REMOVE
    private BossFightListener bossFightListener = new BossFightListener();
    private HealthListener    healthListener    = new HealthListener();
    private boolean active = false;

    public BossHealthMeter(){}

    protected void proposeLazyInit(){
	if(initialized)
	    return;
	if(getMeterTexture() != null && getFont() != null && getTargetSPG() != null && getGame() != null)
	    lazyInit();
    }//end proposeLazyInit()
    
    protected void lazyInit(){
	initialized = true;
	final MeterBar healthBar = getHealthBar();
	
	final CharLineDisplay charLineDisplay = getCharLineDisplay();
	final Game game = getGame();
	final IndirectProperty<Game> gameIP    = this.gameIP    = new IndirectProperty<>();
	
	healthBar.setPosition(HEALTH_BAR_POS);
	healthBar.setVisible(true);
	spg.add(healthBar);
	
	charLineDisplay.setPosition(CHAR_LINE_POS);
	charLineDisplay.setVisible(true);
	charLineDisplay.setContent("Boss");
	
	game.addPropertyChangeListener(Game.CURRENT_MISSION, gameIP);
	gameIP.addTargetPropertyChangeListener(Mission.CURRENT_BOSS, bossFightListener);
	
	if(game instanceof TVF3Game){
	    final GameVersion gameVersion = ((TVF3Game)game).getGameVersion(); 
	    charLineDisplay.setContent( gameVersion == GameVersion.TV? "Boss":"Guardian");
	    }
    }//end lazyInit()
    
    private class BossFightListener implements PropertyChangeListener {
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    final Object newValue = evt.getNewValue();
	    setActive( newValue != null );
	}
    }//end RunStatePCL
    
    private class HealthListener implements PropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    final Integer newHealth = (Integer)evt.getNewValue();
	    System.out.println("HealthListener pc="+evt.toString());
	    if(newHealth != null)
	        updateHealthBar((double)newHealth);
	}
    }//end HealthListener
    
    protected void updateHealthBar(double newNonNormalizedHealth){
	final WorldObject boss = getGame().getCurrentMission().getCurrentBoss();
	final double maxHealth = boss.probeForBehavior(DamageableBehavior.class).getMaxHealth();
	getHealthBar().getController().setFrame(1-(newNonNormalizedHealth/maxHealth));
    }//end updateHealthBar
    
    public void setActive(final boolean isActive){
	System.out.println("BossHealthMeter.setActive "+isActive);
	if( isActive == active )
	    return;
	System.out.println("BossHealthMeter.setActive TRANSIENT "+isActive);
	active = isActive;
	final RenderableSpacePartitioningGrid targetSPG = getTargetSPG();
	if(isActive)
	    attachToBoss(getGame().getCurrentMission().getCurrentBoss());
	World.relevanceExecutor.submit(new Runnable(){
	    @Override
	    public void run() {
		if(isActive)
		    targetSPG.addBranch(spg);
		else
		    targetSPG.removeBranch(spg);
	    }});
    }//end setActive(...)
    
    protected void attachToBoss(WorldObject boss){
	final DamageableBehavior db = boss.probeForBehavior(DamageableBehavior.class);
	updateHealthBar(db.getHealth());//Initial setting
	db.addPropertyChangeListener(DamageableBehavior.HEALTH, healthListener);
    }

    public TVF3Game getGame() {
	return game;
    }

    public void setGame(TVF3Game game) {
	this.game = game;
	proposeLazyInit();
    }

    public CharLineDisplay getCharLineDisplay() {
	if(charLineDisplay == null)
	    charLineDisplay = new CharLineDisplay(spg, FONT_SIZE, "Guardian".length(), getFont());
        return charLineDisplay;
    }

    public MeterBar getHealthBar() {
	if(healthBar == null)
	    healthBar = new MeterBar(getMeterTexture(), METER_HEIGHT, METER_LENGTH, true, "BossHealthMeter");
        return healthBar;
    }

    public GLFont getFont() {
        return font;
    }

    public void setFont(GLFont font) {
        this.font = font;
    }

    public Texture getMeterTexture() {
        return meterTexture;
    }

    public void setMeterTexture(Texture meterTexture) {
        this.meterTexture = meterTexture;
        proposeLazyInit();
    }

    public RenderableSpacePartitioningGrid getTargetSPG() {
        return targetSPG;
    }

    public void setTargetSPG(RenderableSpacePartitioningGrid targetSPG) {
        this.targetSPG = targetSPG;
        proposeLazyInit();
    }
}//end BossHealthMeter
