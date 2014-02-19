package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class HorizAimAtPlayerBehavior extends Behavior {
    private WorldObject chaseTarget;
    private double equatorialAccelleration=.004;
    private final double [] vectorToTargetVar = new double[3];
    private final double [] headingVarianceDelta = new double[3];
    private boolean reverse = false;
    private boolean leftHanded = true;
    private double hysteresis=.02;//Prevents gimbal shake.
    public HorizAimAtPlayerBehavior(WorldObject chaseTarget){super();this.chaseTarget=chaseTarget;}
    @Override
    public void _tick(long timeInMillis){
	if(chaseTarget!=null){
	    WorldObject thisObject = getParent();
	    final Player player = thisObject.getTr().getPlayer();
	    if(player.getBehavior().probeForBehavior(Cloakable.class).isCloaked())return;
	    final RotationalMomentumBehavior rmb = thisObject.getBehavior().probeForBehavior(RotationalMomentumBehavior.class);
	    double [] vectorToTarget = Vect3D.normalize(TR.twosComplimentSubtract(chaseTarget.getPosition(), thisObject.getPosition(),vectorToTargetVar),vectorToTargetVar);
	    vectorToTarget[1]=0;
	    Vect3D.normalize(vectorToTarget,vectorToTarget);
	    final Vector3D thisHeading=new Vector3D(thisObject.getHeading().getX(),0,thisObject.getHeading().getZ()).normalize();
	    Vect3D.subtract(thisHeading.toArray(), vectorToTarget, headingVarianceDelta);	
	    if(Math.sqrt(headingVarianceDelta[2]*headingVarianceDelta[2]+headingVarianceDelta[0]*headingVarianceDelta[0])<hysteresis)return;
	    if(!reverse)Vect3D.negate(vectorToTarget);
	    Rotation rot = new Rotation(new Vector3D(vectorToTarget),thisHeading);
	    final Vector3D deltaVector=rot.applyTo(Vector3D.PLUS_K);
	    if((deltaVector.getZ()>0||deltaVector.getX()<0)==leftHanded){rmb.accellerateEquatorialMomentum(-equatorialAccelleration);}
	    else{rmb.accellerateEquatorialMomentum(equatorialAccelleration);}
	}//end if(target!null)
    }
    /**
     * @return the reverse
     */
    public boolean isReverse() {
        return reverse;
    }
    /**
     * @param reverse the reverse to set
     * @return 
     */
    public HorizAimAtPlayerBehavior setReverse(boolean reverse) {
        this.reverse = reverse;
        return this;
    }
    public void setTurnAcceleration(double accelleration) {
	equatorialAccelleration=accelleration;
    }
    /**
     * @return the leftHanded
     */
    public boolean isLeftHanded() {
        return leftHanded;
    }
    /**
     * @param leftHanded the leftHanded to set
     */
    public HorizAimAtPlayerBehavior setLeftHanded(boolean leftHanded) {
        this.leftHanded = leftHanded;
        return this;
    }
    /**
     * @return the hysteresis
     */
    public double getHysteresis() {
        return hysteresis;
    }
    /**
     * @param hysteresis the hysteresis to set
     */
    public HorizAimAtPlayerBehavior setHysteresis(double hysteresis) {
        this.hysteresis = hysteresis;
        return this;
    }
}//end ChaseBehavior
