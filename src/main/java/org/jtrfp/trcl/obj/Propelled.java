package org.jtrfp.trcl.obj;

public interface Propelled {
    public Propelled setPropulsion(double magnitude);

    public double getPropulsion();

    public Propelled setMaxPropulsion(double max);

    public double getMaxPropulsion();

    public Propelled setMinPropulsion(double min);

    public double getMinPropulsion();

    public Propelled deltaPropulsion(double delta);
}
