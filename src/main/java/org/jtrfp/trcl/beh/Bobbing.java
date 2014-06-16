/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.WorldObject;

public class Bobbing extends Behavior {
    private double amplitude=TR.mapSquareSize/5;
    private double additionalHeight=TR.mapSquareSize;
    private double bobPeriodMillis=10*1000;
    private double phase=0;
    private double initialY=0;
    private boolean first=true;
    @Override
    public void _tick(long timeMillis){
	final WorldObject thisObject = getParent();
	double [] thisPos=thisObject.getPosition();
	if(first)initialY=thisPos[1];
	thisPos[1]=Math.sin((timeMillis/bobPeriodMillis)*Math.PI*2+phase)*amplitude+amplitude+additionalHeight+initialY;
	thisObject.notifyPositionChange();
	first=false;
    }//end _tick()
    /**
     * @return the amplitude
     */
    public double getAmplitude() {
        return amplitude;
    }
    /**
     * @param amplitude the amplitude to set
     */
    public Bobbing setAmplitude(double amplitude) {
        this.amplitude = amplitude;
        return this;
    }
    /**
     * @return the additionalHeight
     */
    public double getAdditionalHeight() {
        return additionalHeight;
    }
    /**
     * @param additionalHeight the additionalHeight to set
     */
    public Bobbing setAdditionalHeight(double additionalHeight) {
        this.additionalHeight = additionalHeight;
        return this;
    }
    /**
     * @return the bobPeriodMillis
     */
    public double getBobPeriodMillis() {
        return bobPeriodMillis;
    }
    /**
     * @param bobPeriodMillis the bobPeriodMillis to set
     */
    public Bobbing setBobPeriodMillis(double bobPeriodMillis) {
        this.bobPeriodMillis = bobPeriodMillis;
        return this;
    }
    /**
     * @return the phase
     */
    public double getPhase() {
        return phase;
    }
    /**
     * @param phase the phase to set
     */
    public Bobbing setPhase(double phase) {
        this.phase = phase;
        return this;
    }
}//end BobbingBehavior
