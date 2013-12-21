package org.jtrfp.trcl.objects;

public interface Damageable
	{
	public void damage(int dmg);
	public int getHealth();
	public void unDamage(int amt);
	public void unDamage();
	}
