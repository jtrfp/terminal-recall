package org.jtrfp.trcl.objects;

import java.awt.event.KeyEvent;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.ai.AccelleratedByPropulsion;
import org.jtrfp.trcl.ai.AutoLeveling;
import org.jtrfp.trcl.ai.Behavior;
import org.jtrfp.trcl.ai.BouncesOffSurfaces;
import org.jtrfp.trcl.ai.BouncesOffTunnelWalls;
import org.jtrfp.trcl.ai.CollidesWithTerrain;
import org.jtrfp.trcl.ai.DamageableBehavior;
import org.jtrfp.trcl.ai.HasPropulsion;
import org.jtrfp.trcl.ai.LoopingPositionBehavior;
import org.jtrfp.trcl.ai.MovesByVelocity;
import org.jtrfp.trcl.ai.RotationalDragBehavior;
import org.jtrfp.trcl.ai.RotationalMomentumBehavior;
import org.jtrfp.trcl.ai.UserInputRudderElevatorControlBehavior;
import org.jtrfp.trcl.ai.UserInputThrottleControlBehavior;
import org.jtrfp.trcl.ai.VelocityDragBehavior;
import org.jtrfp.trcl.core.Camera;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.ThreadManager;

import com.jogamp.newt.event.KeyListener;

public class Player extends WorldObject
	{
	private final Camera camera;
	private int cameraDistance=0;
	private int afterburnerQuantity;
	private static final int SINGLE_SKL=0;
	private int rtlLevel;
	private int pacLevel;
	private int rtlQuantity;
	private int pacQuantity;
	private int ionQuantity;
	private int mamQuantity;
	private int sadQuantity;
	private int swtQuantity;
	private int damQuantity;
	public static final int CLOAK_COUNTDOWN_START=ThreadManager.GAMEPLAY_FPS*30;//30sec
	private int cloakCountdown;
	public static final int INVINCIBILITY_COUNTDOWN_START=ThreadManager.GAMEPLAY_FPS*30;//30sec
	private int invincibilityCountdown;

	public Player(TR tr,Model model)
		{super(tr,model);
		addBehavior(new PlayerBehavior());
		addBehavior(new DamageableBehavior());
		addBehavior(new MovesByVelocity());
		addBehavior(new HasPropulsion());
		addBehavior(new AccelleratedByPropulsion());
		addBehavior(new BouncesOffTunnelWalls(true,true));
		addBehavior(new UserInputThrottleControlBehavior());
		addBehavior(new VelocityDragBehavior());
		addBehavior(new AutoLeveling());
		addBehavior(new UserInputRudderElevatorControlBehavior());
		addBehavior(new RotationalMomentumBehavior());
		addBehavior(new RotationalDragBehavior());
		addBehavior(new CollidesWithTerrain());
		addBehavior(new LoopingPositionBehavior());
		addBehavior(new BouncesOffSurfaces());
		camera = tr.getRenderer().getCamera();
		getBehavior().probeForBehavior(VelocityDragBehavior.class).setDragCoefficient(.86);
		getBehavior().probeForBehavior(Propelled.class).setMinPropulsion(0);
		getBehavior().probeForBehavior(Propelled.class).setMaxPropulsion(1300000);
		getBehavior().probeForBehavior(RotationalDragBehavior.class).setDragCoefficient(.86);
		}
	
	private class PlayerBehavior extends Behavior{
		@Override
		public void _tick(long tickTimeInMillis){
			updateCountdowns();
			if(getTr().getKeyStatus().isPressed(KeyEvent.VK_SPACE)){
			    	getTr().getResourceManager().
			    	getProjectileFactory().
			    	triggerLaser(getPosition().add(getHeading().crossProduct(getTop()).normalize().
			    		scalarMultiply(10000)).add(new Vector3D(0,-3000,0)), getHeading().add(getHeading().scalarMultiply(CollisionManager.SHIP_COLLISION_DISTANCE+512)));}
			}
		}//end PlayerBehavior
	
	public void updateCountdowns(){
		if(cloakCountdown>0){
			if(--cloakCountdown==0)
				{setVisible(true);}
			else setVisible(false);}
		if(invincibilityCountdown>0){
			--invincibilityCountdown;}
		}

	@Override
	public void setHeading(Vector3D lookAt){
		camera.setLookAtVector(lookAt);
		camera.setPosition(getPosition().subtract(lookAt.scalarMultiply(cameraDistance)));
		super.setHeading(lookAt);
		}
	@Override
	public void setTop(Vector3D top){
		camera.setUpVector(top);
		super.setTop(top);
		}
	@Override
	public void setPosition(Vector3D pos){
		camera.setPosition(pos.subtract(getLookAt().scalarMultiply(cameraDistance)));
		super.setPosition(pos);
		}

	/**
	 * @return the afterburnerQuantity
	 */
	public int getAfterburnerQuantity()
		{
		return afterburnerQuantity;
		}

	/**
	 * @param afterburnerQuantity the afterburnerQuantity to set
	 */
	public void setAfterburnerQuantity(int afterburnerQuantity)
		{this.afterburnerQuantity = afterburnerQuantity;}

	/**
	 * @return the rtlQuantity
	 */
	public int getRtlQuantity()
		{return rtlQuantity;}

	/**
	 * @param rtlQuantity the rtlQuantity to set
	 */
	public void setRtlQuantity(int rtlQuantity){
		if(this.rtlQuantity>0&& rtlLevel<2 && rtlQuantity>this.rtlQuantity)this.rtlLevel++;
		this.rtlQuantity = rtlQuantity;
		}

	/**
	 * @return the pacQuantity
	 */
	public int getPacQuantity()
		{return pacQuantity;}

	/**
	 * @param pacQuantity the pacQuantity to set
	 */
	public void setPacQuantity(int pacQuantity)
		{if(this.pacQuantity>0&& pacLevel<2 && pacQuantity>this.pacQuantity)this.pacLevel++;
		this.pacQuantity = pacQuantity;
		}

	/**
	 * @return the ionQuantity
	 */
	public int getIonQuantity()
		{return ionQuantity;}

	/**
	 * @param ionQuantity the ionQuantity to set
	 */
	public void setIonQuantity(int ionQuantity)
		{
		this.ionQuantity = ionQuantity;
		}

	/**
	 * @return the mamQuantity
	 */
	public int getMamQuantity()
		{return mamQuantity;}

	/**
	 * @param mamQuantity the mamQuantity to set
	 */
	public void setMamQuantity(int mamQuantity)
		{this.mamQuantity = mamQuantity;}

	/**
	 * @return the sadQuantity
	 */
	public int getSadQuantity()
		{return sadQuantity;}

	/**
	 * @param sadQuantity the sadQuantity to set
	 */
	public void setSadQuantity(int sadQuantity)
		{this.sadQuantity = sadQuantity;}

	/**
	 * @return the swtQuantity
	 */
	public int getSwtQuantity()
		{return swtQuantity;}

	/**
	 * @param swtQuantity the swtQuantity to set
	 */
	public void setSwtQuantity(int swtQuantity)
		{this.swtQuantity = swtQuantity;}

	/**
	 * @return the damQuantity
	 */
	public int getDamQuantity()
		{return damQuantity;}

	/**
	 * @param damQuantity the damQuantity to set
	 */
	public void setDamQuantity(int damQuantity)
		{this.damQuantity = damQuantity;}

	/**
	 * @return the cloakCountdown
	 */
	public int getCloakCountdown()
		{return cloakCountdown;}

	/**
	 * @param cloakCountdown the cloakCountdown to set
	 */
	public void setCloakCountdown(int cloakCountdown)
		{this.cloakCountdown = cloakCountdown;}

	/**
	 * @return the invincibilityCountdown
	 */
	public int getInvincibilityCountdown()
		{return invincibilityCountdown;}

	/**
	 * @param invincibilityCountdown the invincibilityCountdown to set
	 */
	public void setInvincibilityCountdown(int invincibilityCountdown)
		{this.invincibilityCountdown = invincibilityCountdown;}
	}//end Player
