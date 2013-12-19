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
			{updateMovement();}
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
	}//end Player
