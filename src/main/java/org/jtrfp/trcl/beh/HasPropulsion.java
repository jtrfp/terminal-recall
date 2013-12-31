package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.obj.Propelled;

public class HasPropulsion extends Behavior implements Propelled {
    private double propulsion=0;
    private double max=Double.POSITIVE_INFINITY;
    private double min=Double.NEGATIVE_INFINITY;
    @Override
    public Propelled setPropulsion(double magnitude) {
	propulsion=magnitude;return this;
    }

    @Override
    public double getPropulsion() {
	return propulsion;
    }

    @Override
    public void setMaxPropulsion(double max) {
	this.max=max;
	
    }

    @Override
    public double getMaxPropulsion() {
	return max;
    }

    @Override
    public void deltaPropulsion(double delta) {
	propulsion+=delta;
	if(propulsion>max){propulsion=max;}
	else if(propulsion<min){propulsion=min;}
    }

    @Override
    public void setMinPropulsion(double min) {
	this.min=min;
    }

    @Override
    public double getMinPropulsion() {
	return min;
    }

}//end HasPropulsion
