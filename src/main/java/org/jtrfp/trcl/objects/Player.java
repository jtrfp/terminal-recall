package org.jtrfp.trcl.objects;

import java.awt.event.KeyEvent;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.KeyStatus;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.core.Camera;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.ThreadManager;

public class Player extends RigidMobileObject
	{
	private final Camera camera;
	//private int cameraDistance=10000;
	private int cameraDistance=0;
	private int shieldQuantity=65535;
	private int speed;
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

	public Player(Model model, World world)
		{
		super(model, null, world);
		setBehavior(new PlayerBehavior());
		camera = getTr().getRenderer().getCamera();
		}
	
	private class PlayerBehavior extends ObjectBehavior
		{
		@Override
		public void tick(long tickTimeInMillis)
			{updateMovement();
			updateCountdowns();
			}
		}//end PlayerBehavior
	
	@Override
	public void proposeCollision(WorldObject other)
		{
		if(other instanceof PowerupObject)
			{PowerupObject pow=(PowerupObject)other;
			if(other.getPosition().distance(getPosition())<CollisionManager.SHIP_COLLISION_DISTANCE)
				{System.out.println("Got powerup "+pow.getPowerupType());other.destroy();
				pow.applyToPlayer(this);
				}//end if(collided)
			}
		super.proposeCollision(other);
		}//end proposeCollision()
	
	public void updateCountdowns()
		{if(cloakCountdown>0)
			{if(--cloakCountdown==0)
				{setVisible(true);}
			else setVisible(false);}
		if(invincibilityCountdown>0)
			{--invincibilityCountdown;}
		}

	@Override
	public void setHeading(Vector3D lookAt)
		{camera.setLookAtVector(lookAt);
		camera.setPosition(getPosition().subtract(lookAt.scalarMultiply(cameraDistance)));
		super.setHeading(lookAt);
		}
	@Override
	public void setTop(Vector3D top)
		{camera.setUpVector(top);
		//camera.setPosition(getPosition().subtract(getLookAt().scalarMultiply(cameraDistance)));
		super.setTop(top);
		}
	@Override
	public void setPosition(Vector3D pos)
		{camera.setPosition(pos.subtract(getLookAt().scalarMultiply(cameraDistance)));
		super.setPosition(pos);
		}
	
	private void updateMovement()
		{
		final double manueverSpeed = 20. / (double) ThreadManager.RENDER_FPS;
		final double nudgeUnit = TR.mapSquareSize / 9.;
		final double angleUnit = Math.PI * .015 * manueverSpeed;
		
		boolean positionChanged = false, lookAtChanged = false;
		// double
		// newX=getCameraPosition().getX(),newY=getCameraPosition().getY(),newZ=getCameraPosition().getZ();
		final TR tr = getTr();
		Vector3D newPos = this.getPosition();
		Vector3D newLookAt = this.getLookAt();
		final KeyStatus keyStatus = tr.getKeyStatus();
		if (keyStatus.isPressed(KeyEvent.VK_UP))
			{newPos = newPos.add(this.getLookAt().scalarMultiply(
					nudgeUnit * manueverSpeed));
			positionChanged = true;
			}
		if (keyStatus.isPressed(KeyEvent.VK_DOWN))
			{newPos = newPos.subtract(this.getLookAt().scalarMultiply(
					nudgeUnit * manueverSpeed));
			positionChanged = true;
			}
		if (keyStatus.isPressed(KeyEvent.VK_PAGE_UP))
			{newPos = newPos.add(this.getTop().scalarMultiply(nudgeUnit
					* manueverSpeed));
			positionChanged = true;
			}
		if (keyStatus.isPressed(KeyEvent.VK_PAGE_DOWN))
			{newPos = newPos.subtract(this.getTop().scalarMultiply(nudgeUnit
					* manueverSpeed));
			positionChanged = true;
			}

		Rotation turnRot = new Rotation(this.getTop(), angleUnit);

		if (keyStatus.isPressed(KeyEvent.VK_LEFT))
			{newLookAt = turnRot.applyInverseTo(newLookAt);
			lookAtChanged = true;
			}
		if (keyStatus.isPressed(KeyEvent.VK_RIGHT))
			{newLookAt = turnRot.applyTo(newLookAt);
			lookAtChanged = true;
			}

		// Loop correction
		if (WorldObject.LOOP)
			{
			if (newPos.getX() > TR.mapWidth)
				newPos = newPos.subtract(new Vector3D(TR.mapWidth, 0, 0));
			if (newPos.getY() > TR.mapWidth)
				newPos = newPos.subtract(new Vector3D(0, TR.mapWidth, 0));
			if (newPos.getZ() > TR.mapWidth)
				newPos = newPos.subtract(new Vector3D(0, 0, TR.mapWidth));

			if (newPos.getX() < 0)
				newPos = newPos.add(new Vector3D(TR.mapWidth, 0, 0));
			if (newPos.getY() < 0)
				newPos = newPos.add(new Vector3D(0, TR.mapWidth, 0));
			if (newPos.getZ() < 0)
				newPos = newPos.add(new Vector3D(0, 0, TR.mapWidth));
			}

		if (lookAtChanged)
			this.setHeading(newLookAt);
		if (positionChanged)
			this.setPosition(newPos);
		}

	/**
	 * @return the shieldQuantity
	 */
	public int getShieldQuantity()
		{
		return shieldQuantity;
		}

	/**
	 * @param shieldQuantity the shieldQuantity to set
	 */
	public void setShieldQuantity(int shieldQuantity)
		{
		this.shieldQuantity = shieldQuantity<=65535?shieldQuantity:65535;
		}

	/**
	 * @return the speed
	 */
	public int getSpeed()
		{
		return speed;
		}

	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(int speed)
		{
		this.speed = speed;
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
		{
		this.afterburnerQuantity = afterburnerQuantity;
		}

	/**
	 * @return the rtlQuantity
	 */
	public int getRtlQuantity()
		{
		return rtlQuantity;
		}

	/**
	 * @param rtlQuantity the rtlQuantity to set
	 */
	public void setRtlQuantity(int rtlQuantity)
		{if(this.rtlQuantity>0&& rtlLevel<2 && rtlQuantity>this.rtlQuantity)this.rtlLevel++;
		this.rtlQuantity = rtlQuantity;
		}

	/**
	 * @return the pacQuantity
	 */
	public int getPacQuantity()
		{
		return pacQuantity;
		}

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
		{
		return ionQuantity;
		}

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
		{
		return mamQuantity;
		}

	/**
	 * @param mamQuantity the mamQuantity to set
	 */
	public void setMamQuantity(int mamQuantity)
		{
		this.mamQuantity = mamQuantity;
		}

	/**
	 * @return the sadQuantity
	 */
	public int getSadQuantity()
		{
		return sadQuantity;
		}

	/**
	 * @param sadQuantity the sadQuantity to set
	 */
	public void setSadQuantity(int sadQuantity)
		{
		this.sadQuantity = sadQuantity;
		}

	/**
	 * @return the swtQuantity
	 */
	public int getSwtQuantity()
		{
		return swtQuantity;
		}

	/**
	 * @param swtQuantity the swtQuantity to set
	 */
	public void setSwtQuantity(int swtQuantity)
		{
		this.swtQuantity = swtQuantity;
		}

	/**
	 * @return the damQuantity
	 */
	public int getDamQuantity()
		{
		return damQuantity;
		}

	/**
	 * @param damQuantity the damQuantity to set
	 */
	public void setDamQuantity(int damQuantity)
		{
		this.damQuantity = damQuantity;
		}

	/**
	 * @return the cloakCountdown
	 */
	public int getCloakCountdown()
		{
		return cloakCountdown;
		}

	/**
	 * @param cloakCountdown the cloakCountdown to set
	 */
	public void setCloakCountdown(int cloakCountdown)
		{
		this.cloakCountdown = cloakCountdown;
		}

	/**
	 * @return the invincibilityCountdown
	 */
	public int getInvincibilityCountdown()
		{
		return invincibilityCountdown;
		}

	/**
	 * @param invincibilityCountdown the invincibilityCountdown to set
	 */
	public void setInvincibilityCountdown(int invincibilityCountdown)
		{
		this.invincibilityCountdown = invincibilityCountdown;
		}
	}//end Player
