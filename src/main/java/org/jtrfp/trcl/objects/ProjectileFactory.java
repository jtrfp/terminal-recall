package org.jtrfp.trcl.objects;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.LineSegment;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.core.TR;

public class ProjectileFactory {
    private int laserIndex=0;
    private static final double SEG_LEN=2000000000;
    private static final double SKL_SPEED=40;
    private final TR tr;
    private final LaserProjectile [] lasers = new LaserProjectile[10];
    public ProjectileFactory(TR tr){
	this.tr=tr;
	int i;
	Model m = new Model(false);
	LineSegment ls = new LineSegment();
	ls.getZ()[1]=SEG_LEN;
	ls.setThickness(170);
	ls.setColor(Color.yellow);
	m.addLineSegment(ls);
	m.finalizeModel();
	for(i=0; i<lasers.length; i++){
	    lasers[i]=new LaserProjectile(tr,m);}
       }
    public LaserProjectile triggerLaser(Vector3D location,Vector3D heading){
	laserIndex++;laserIndex%=lasers.length;
	final LaserProjectile result = lasers[laserIndex];
	result.destroy();
	result.reset(location, heading.scalarMultiply(SKL_SPEED));
	tr.getWorld().add(result);
	return result;
	}
}//end ProjectileFactory
