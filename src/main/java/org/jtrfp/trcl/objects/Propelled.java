package org.jtrfp.trcl.objects;

public interface Propelled
	{public void setPropulsion(double magnitude);
	public double getPropulsion();
	public void setMaxPropulsion(double max);
	public double getMaxPropulsion();
	public void setMinPropulsion(double min);
	public double getMinPropulsion();
	public void deltaPropulsion(double delta);
	}
