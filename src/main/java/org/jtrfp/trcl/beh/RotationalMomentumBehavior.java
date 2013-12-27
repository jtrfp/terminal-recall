package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.jtrfp.trcl.obj.WorldObject;

public class RotationalMomentumBehavior extends Behavior {
    private double equatorialMomentum=0;//Axis is getTop()
    private double polarMomentum=0;//Axis is getHeading().crossProduct(getTop())
    private double lateralMomentum=0;//Axis is getHeading()
    @Override
    public void _tick(long tickTimeInMillis){
	final WorldObject p = getParent();
	Rotation rot;
	rot = new Rotation(p.getTop(),equatorialMomentum);
	p.setHeading(rot.applyTo(p.getHeading()));
	rot = new Rotation(p.getHeading().crossProduct(p.getTop()),polarMomentum);
	p.setHeading(rot.applyTo(p.getHeading()));
	p.setTop(rot.applyTo(p.getTop()));
	rot = new Rotation(p.getHeading(),lateralMomentum);
	p.setTop(rot.applyTo(p.getTop()));
    }//end _tick(....)
    /**
     * @return the equatorialMomentum
     */
    public double getEquatorialMomentum() {
        return equatorialMomentum;
    }
    /**
     * @param equatorialMomentum the equatorialMomentum to set
     */
    public void setEquatorialMomentum(double equatorialMomentum) {
        this.equatorialMomentum = equatorialMomentum;
    }
    
    public void accellerateEquatorialMomentum(double delta){equatorialMomentum+=delta;}
    
    /**
     * @return the polarMomentum
     */
    public double getPolarMomentum() {
        return polarMomentum;
    }
    
//    public void accelleratePolarMomentum(double delta){pMomentum+=delta;}
    /**
     * @param polarMomentum the polarMomentum to set
     */
    public void setPolarMomentum(double polarMomentum) {
        this.polarMomentum = polarMomentum;
    }
    
    public void accelleratePolarMomentum(double delta){polarMomentum+=delta;}
    /**
     * @return the lateralMomentum
     */
    public double getLateralMomentum() {
        return lateralMomentum;
    }
    
    public void accellerateLateralMomentum(double delta){lateralMomentum+=delta;}
    /**
     * @param lateralMomentum the lateralMomentum to set
     */
    public void setLateralMomentum(double lateralMomentum) {
        this.lateralMomentum = lateralMomentum;
    }
}
