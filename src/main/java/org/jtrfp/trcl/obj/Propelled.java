package org.jtrfp.trcl.obj;

public interface Propelled
	{public Propelled setPropulsion(double magnitude);
	public double getPropulsion();
	public void setMaxPropulsion(double max);
	public double getMaxPropulsion();
	public void setMinPropulsion(double min);
	public double getMinPropulsion();
	public void deltaPropulsion(double delta);
	}
