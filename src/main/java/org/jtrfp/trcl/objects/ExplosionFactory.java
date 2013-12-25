package org.jtrfp.trcl.objects;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;

public class ExplosionFactory {
    	private int billowIndex=0,blastIndex=0,bigExplosionIndex=0;
    	private final TR tr;
	private final Explosion [] billowExplosions = new Explosion[10];
	private final Explosion [] blastExplosions = new Explosion[10];
	private final Explosion [] bigExplosions = new Explosion[10];
	public ExplosionFactory(TR tr){
	    this.tr=tr;
	    int i;
	    for(i=0; i<billowExplosions.length; i++){
		    billowExplosions[i]=new Explosion(tr,Explosion.ExplosionType.Billow);
	    }
	    for(i=0; i<blastExplosions.length; i++){
		blastExplosions[i]=new Explosion(tr,Explosion.ExplosionType.Blast);
	    }
	    for(i=0; i<bigExplosions.length; i++){
		bigExplosions[i]=new Explosion(tr,Explosion.ExplosionType.BigExplosion);
	    }
	}//end constructor()
	
	public Explosion triggerBillowExplosion(Vector3D location){
	    billowIndex++;billowIndex%=billowExplosions.length;
	    Explosion result = billowExplosions[billowIndex];
	    result.destroy();
	    result.resetExplosion();
	    result.setPosition(location);
	    tr.getWorld().add(result);
	    return result;
	}
	public Explosion triggerBigExplosion(Vector3D location){
	    bigExplosionIndex++;bigExplosionIndex%=bigExplosions.length;
	    Explosion result = bigExplosions[bigExplosionIndex];
	    result.destroy();
	    result.resetExplosion();
	    result.setPosition(location);
	    tr.getWorld().add(result);
	    return result;
	}
	public Explosion triggerBlastExplosion(Vector3D location){
	    blastIndex++;blastIndex%=blastExplosions.length;
	    Explosion result = blastExplosions[blastIndex];
	    result.destroy();
	    result.resetExplosion();
	    result.setPosition(location);
	    tr.getWorld().add(result);
	    return result;
	}
}//end ExplosionFactory
