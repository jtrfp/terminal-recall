package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.math.Misc;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.WorldObject;

public class RollLevelingBehavior extends Behavior {
    private final double retainment = .93;
    private final double [] imgHdng = new double[3];
    @Override
    public void _tick(long tickTimeMillis){
	WorldObject p = getParent();
	double [] initHdng = p.getHeadingArray();
	double [] initTop = p.getTopArray();
	//Escape on invalid cases
	if(initHdng[1]<=-1)return;
	if(initHdng[1]>=1)return;
	if(initTop[1]==0)return;
	//Create an imaginary heading/top where heading.y=0
	imgHdng[0]=initHdng[0];
	imgHdng[1]=0;
	imgHdng[2]=initHdng[2];
	Vect3D.normalize(imgHdng,imgHdng);
	Rotation rot = new Rotation(new Vector3D(initHdng), new Vector3D(imgHdng));
	Vector3D imgTop = rot.applyTo(new Vector3D(initTop)).normalize();
	double topY = imgTop.getY();
	if(topY==0)return;
	//Retainment softener prevents gimbal swing effect when facing up or down.
	final double retainmentSoftener=Misc.clamp(Math.abs(initHdng[1]), 0, 1);
	final double softenedRetainment=retainment*(1.-retainmentSoftener)+retainmentSoftener;
	if(topY>0){//Rightside up, approach 1.
	    topY=topY*softenedRetainment+1*(1.-softenedRetainment);
	}else{//Upside down, approach -1
	    topY=topY*softenedRetainment+-1*(1.-softenedRetainment);
	}
	Vector3D newTop = rot.applyInverseTo(new Vector3D(imgTop.getX(),topY,imgTop.getZ()).normalize());
	p.setTop(newTop);
    }//end _tick(...)
}//end RollLevelingBehavior
