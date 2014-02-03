package org.jtrfp.trcl;

public class ManuallySetController implements Controller {
    private double frame=0;
    private boolean stale=true;
    private boolean debug=false;
    @Override
    public double getCurrentFrame() {
	if(debug)System.out.println("getCurrentFrame()");
	return frame;
    }
    
    public void setFrame(double f){frame=f;stale=true;}
/*
    @Override
    public void unstale() {
	stale=false;
    }

    @Override
    public boolean isStale() {
	return stale;
    }
*/
    @Override
    public void setDebugMode(boolean b) {
	debug=b;
    }

}//end MenaullySetController
