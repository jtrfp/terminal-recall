package org.jtrfp.trcl;

public class ManuallySetController implements Controller {
    private double frame=0;
    @Override
    public double getCurrentFrame() {
	return frame;
    }
    
    public void setFrame(double f){frame=f;}

}
