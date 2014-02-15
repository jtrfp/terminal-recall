package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.obj.WorldObject;

public class PositionLimit extends Behavior {
    private final double [] positionMaxima = new double[]{Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
    private final double [] positionMinima = new double[]{Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
    @Override
    public void _tick(long timeInMillis){//I'm in the mood to use ASM coding style...
	final 	WorldObject 	thisObject	=	getParent();
	final 	double [] 	thisPos 	=	thisObject.getPosition();
		boolean 	changed		=	false;
	
	if(thisPos[0]>positionMaxima[0]){thisPos[0]=positionMaxima[0];changed=true;}
	if(thisPos[1]>positionMaxima[1]){thisPos[1]=positionMaxima[1];changed=true;}
	if(thisPos[2]>positionMaxima[2]){thisPos[2]=positionMaxima[2];changed=true;}
	
	if(thisPos[0]<positionMinima[0]){thisPos[0]=positionMinima[0];changed=true;}
	if(thisPos[1]<positionMinima[1]){thisPos[1]=positionMinima[1];changed=true;}
	if(thisPos[2]<positionMinima[2]){thisPos[2]=positionMinima[2];changed=true;}
	
	if(changed)thisObject.notifyPositionChange();
    }//end _tick()
    /**
     * @return the positionMinima
     */
    public double[] getPositionMinima() {
        return positionMinima;
    }
    /**
     * @return the positionMaxima
     */
    public double[] getPositionMaxima() {
        return positionMaxima;
    }
}//end PositionLimit
