package org.jtrfp.trcl;

public class ManuallySetController implements Controller {
    private double frame=0;
    private boolean stale=true;
    @Override
    public double getCurrentFrame() {
	return frame;
    }
    
    public void setFrame(double f){frame=f;stale=true;}

    @Override
    public void unstale() {
	stale=false;
    }

    @Override
    public boolean isStale() {
	return stale;
    }

}//end MenaullySetController
