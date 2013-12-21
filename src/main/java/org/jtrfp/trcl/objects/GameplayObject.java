package org.jtrfp.trcl.objects;

import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.ai.ObjectBehavior;

public class GameplayObject extends MobileObject implements Damageable
	{
	private int health;
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

	}//end GameplayObject
