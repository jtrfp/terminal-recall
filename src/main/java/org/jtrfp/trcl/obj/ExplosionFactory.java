package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.Smoke.SmokeType;

public class ExplosionFactory {
    	//private int billowIndex=0,blastIndex=0,bigExplosionIndex=0;
    	private final TR tr;
    	private final int MAX_EXPLOSIONS_PER_POOL=20;
    	private final Explosion[][] allExplosions = new Explosion[ExplosionType.values().length][];
    	private final int [] indices = new int[ExplosionType.values().length];
	public ExplosionFactory(TR tr){
	    this.tr=tr;
	    int i;
	    for(ExplosionType t:ExplosionType.values()){
		allExplosions[t.ordinal()]=new Explosion[MAX_EXPLOSIONS_PER_POOL];
		for(i=0; i<MAX_EXPLOSIONS_PER_POOL; i++){
			allExplosions[t.ordinal()][i]=new Explosion(tr,t);
		    }
	    }
	}//end constructor()
	/*
	public synchronized Explosion triggerBillowExplosion(Vector3D location){
	    billowIndex++;billowIndex%=billowExplosions.length;
	    Explosion result = billowExplosions[billowIndex];
	    result.destroy();
	    result.resetExplosion();
	    result.setPosition(location);
	    tr.getWorld().add(result);
	    return result;
	}
	public synchronized Explosion triggerBigExplosion(Vector3D location){
	    bigExplosionIndex++;bigExplosionIndex%=bigExplosions.length;
	    Explosion result = bigExplosions[bigExplosionIndex];
	    result.destroy();
	    result.resetExplosion();
	    result.setPosition(location);
	    tr.getWorld().add(result);
	    return result;
	}
	public synchronized Explosion triggerBlastExplosion(Vector3D location){
	    blastIndex++;blastIndex%=blastExplosions.length;
	    Explosion result = blastExplosions[blastIndex];
	    result.destroy();
	    result.resetExplosion();
	    result.setPosition(location);
	    tr.getWorld().add(result);
	    return result;
	}
*/
	public Explosion triggerExplosion(double [] position, ExplosionType type) {
	    indices[type.ordinal()]++;indices[type.ordinal()]%=MAX_EXPLOSIONS_PER_POOL;
	    Explosion result = allExplosions[type.ordinal()][indices[type.ordinal()]];
	    result.destroy();
	    result.resetExplosion();
	    final double [] pos = result.getPosition();
	    pos[0]=position[0];
	    pos[1]=position[1];
	    pos[2]=position[2];
	    result.notifyPositionChange();
	    final SmokeFactory sf = tr.getResourceManager().getSmokeFactory();
	    final int NUM_PUFFS=1;
	    for(int i=0; i<NUM_PUFFS; i++){
		final double [] work =
			new double[]{
			Math.random()*10000,
			Math.random()*10000,
			Math.random()*10000};
		sf.triggerSmoke(Vect3D.add(
			position,
			work,
			work),
			SmokeType.Puff);
	    }//end for(i)
	    tr.getWorld().add(result);
	    return result;
	    
	}
}//end ExplosionFactory
