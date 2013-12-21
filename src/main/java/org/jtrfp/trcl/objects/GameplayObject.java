package org.jtrfp.trcl.objects;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.ai.ObjectBehavior;

public class GameplayObject extends SmartObject implements Damageable, Velocible
	{
	private int health;
	private Vector3D velocity = Vector3D.ZERO;
	private Vector3D drag = Vector3D.ZERO;
	public GameplayObject(Model model, ObjectBehavior behavior, World world)
		{
		super(model, behavior, world);
		health=65535;
		}
	
	@Override
	public void damage(int dmg)
		{health-=dmg;
		}

	@Override
	public int getHealth()
		{return health;
		}

	@Override
	public void unDamage(int amt)
		{health+=amt;
		if(health>65535)health=65535;
		}

	@Override
	public void unDamage()
		{health=65535;}
	
	/**
	 * @return the velocity
	 */
	public Vector3D getVelocity()
		{return velocity;
		}

	/**
	 * @param velocity the velocity to set
	 */
	public void setVelocity(Vector3D velocity)
		{this.velocity = velocity;}

	/**
	 * @return the drag
	 */
	public Vector3D getDrag()
		{return drag;}

	/**
	 * @param drag the drag to set
	 */
	public void setDrag(Vector3D drag)
		{this.drag = drag;}
	}//end GameplayObject
