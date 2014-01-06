package org.jtrfp.trcl.obj;

import org.jtrfp.trcl.GammaCorrectingColorProcessor;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.TextureDescription;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.core.TR;

public class NavArrow extends WorldObject2D {
private static final double WIDTH=.08;
private static final double HEIGHT=.08;
private static final double Z=.0001;
    public NavArrow(TR tr) {
	super(tr);
	try{
	final Model m = new Model(false);
	final TextureDescription tex = tr.getResourceManager().getRAWAsTexture("NAVTAR01.RAW", tr.getGlobalPalette(), GammaCorrectingColorProcessor.singleton, tr.getGPU().getGl());
	Triangle [] tris = Triangle.quad2Triangles(
		new double[]{-WIDTH,WIDTH,WIDTH,-WIDTH}, 
		new double[]{-HEIGHT,-HEIGHT,HEIGHT,HEIGHT}, 
		new double[]{Z,Z,Z,Z}, 
		new double[]{0,1,1,0}, 
		new double[]{0,0,1,1}, tex, RenderMode.DYNAMIC, true);
	m.addTriangles(tris);
	m.finalizeModel();
	setModel(m);
	}//end try{}
	catch(Exception e){e.printStackTrace();}
    }//end constructor
}//end NavArrow
