package org.jtrfp.trcl.ai;

import org.jtrfp.trcl.objects.Damageable;

public class DamageableBehavior extends ObjectBehavior implements Damageable
	{
	private int health=65535;

	@Override
	public void damage(int dmg)
		{
		health-=dmg;
		}

	@Override
	public int getHealth()
		{
		return health;
		}

	@Override
	public void unDamage(int amt)
		{
		health+=amt;
		}

	@Override
	public void unDamage()
		{
		health=65535;
		}
	
	}//end DamageableBehavior
