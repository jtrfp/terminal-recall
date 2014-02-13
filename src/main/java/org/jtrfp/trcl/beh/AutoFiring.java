package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class AutoFiring extends Behavior implements AIFiringBehavior {
    private double maxFiringDistance=TR.mapSquareSize*15;
    private double minFiringDistance=TR.mapSquareSize*0;
    private int lastIndexVisited=0;
    private boolean [] firingPattern = new boolean []
	    {true,true,false,false,false,false,false,false,true,false,false,false,false};
    private int timePerPatternEntry=250;
    private int totalFiringPatternTimeMillis=firingPattern.length*timePerPatternEntry;
    private ProjectileFiringBehavior projectileFiringBehavior;
    private final double [] firingVector = new double[3];
    private int patternOffsetMillis=0;
    @Override
    public void _tick(long timeMillis){
	final WorldObject thisObject = getParent();
	final double [] thisPos = thisObject.getPosition();
	final Player player = thisObject.getTr().getPlayer();
	final double [] playerPos = player.getPosition();
	final double dist = Vect3D.distance(thisPos, playerPos);
	if(dist<maxFiringDistance||dist>minFiringDistance){
	    final int patIndex=(int)(((timeMillis+patternOffsetMillis)%totalFiringPatternTimeMillis)/timePerPatternEntry);
	    if(patIndex!=lastIndexVisited){//end if(lastVisited)
		if(firingPattern[patIndex]){
		    Vect3D.subtract(playerPos, thisPos, firingVector);
		    projectileFiringBehavior.requestFire(new Vector3D(Vect3D.normalize(firingVector,firingVector)));}}
	    lastIndexVisited=patIndex;
	}//end in range
    }//end _tick(...)
    /**
     * @return the maxFiringDistance
     */
    public double getMaxFiringDistance() {
        return maxFiringDistance;
    }
    /**
     * @param maxFiringDistance the maxFiringDistance to set
     */
    public AutoFiring setMaxFiringDistance(double maxFiringDistance) {
        this.maxFiringDistance = maxFiringDistance;
        return this;
    }
    /**
     * @return the minFiringDistance
     */
    public double getMinFiringDistance() {
        return minFiringDistance;
    }
    /**
     * @param minFiringDistance the minFiringDistance to set
     */
    public AutoFiring setMinFiringDistance(double minFiringDistance) {
        this.minFiringDistance = minFiringDistance;
        return this;
    }
    /**
     * @return the lastIndexVisited
     */
    public int getLastIndexVisited() {
        return lastIndexVisited;
    }
    /**
     * @param lastIndexVisited the lastIndexVisited to set
     */
    public AutoFiring setLastIndexVisited(int lastIndexVisited) {
        this.lastIndexVisited = lastIndexVisited;
        return this;
    }
    /**
     * @return the firingPattern
     */
    public boolean[] getFiringPattern() {
        return firingPattern;
    }
    /**
     * @param firingPattern the firingPattern to set
     */
    public AutoFiring setFiringPattern(boolean[] firingPattern) {
        this.firingPattern = firingPattern;
        totalFiringPatternTimeMillis=firingPattern.length*timePerPatternEntry;
        return this;
    }
    /**
     * @return the timePerPatternEntry
     */
    public int getTimePerPatternEntry() {
        return timePerPatternEntry;
    }
    /**
     * @param timePerPatternEntry the timePerPatternEntry to set
     */
    public AutoFiring setTimePerPatternEntry(int timePerPatternEntry) {
        this.timePerPatternEntry = timePerPatternEntry;
        totalFiringPatternTimeMillis=firingPattern.length*timePerPatternEntry;
        return this;
    }
    /**
     * @return the totalFiringPatternTimeMillis
     */
    public int getTotalFiringPatternTimeMillis() {
        return totalFiringPatternTimeMillis;
    }
    /**
     * @param totalFiringPatternTimeMillis the totalFiringPatternTimeMillis to set
     */
    public AutoFiring setTotalFiringPatternTimeMillis(int totalFiringPatternTimeMillis) {
        this.totalFiringPatternTimeMillis = totalFiringPatternTimeMillis;
        return this;
    }
    /**
     * @return the projectileFiringBehavior
     */
    public ProjectileFiringBehavior getProjectileFiringBehavior() {
        return projectileFiringBehavior;
    }
    /**
     * @param projectileFiringBehavior the projectileFiringBehavior to set
     */
    public AutoFiring setProjectileFiringBehavior(
    	ProjectileFiringBehavior projectileFiringBehavior) {
        this.projectileFiringBehavior = projectileFiringBehavior;
        return this;
    }
    /**
     * @return the patternOffsetMillis
     */
    public int getPatternOffsetMillis() {
        return patternOffsetMillis;
    }
    /**
     * @param patternOffsetMillis the patternOffsetMillis to set
     */
    public AutoFiring setPatternOffsetMillis(int patternOffsetMillis) {
        this.patternOffsetMillis = patternOffsetMillis;
        return this;
    }
}//end AutoFiring
