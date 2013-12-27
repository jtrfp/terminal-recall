package org.jtrfp.trcl.objects;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.GammaCorrectingColorProcessor;
import org.jtrfp.trcl.LineSegment;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.TextureDescription;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.core.TR;

public class ProjectileFactory {
    private int redLaserIndex=0;
    private static final double SEG_LEN=5000;
    private static final double SKL_SPEED=40;
    private final TR tr;
    private final LaserProjectile [] whiteLasers = new LaserProjectile[20];
    private final LaserProjectile [] redLasers = new LaserProjectile[20];
    public ProjectileFactory(TR tr){
    	this.tr=tr;
    	try{
    	 int i;
    	 
    	 Model m = new Model(false);
    	 final Color [] stdPalette = tr.getGlobalPalette();
    	 
    	 //White lasers
    	 TextureDescription t = tr.getResourceManager().getRAWAsTexture(
    		"NEWLASER.RAW", 
    		stdPalette, 
    		GammaCorrectingColorProcessor.singleton, 
    		tr.getGPU().getGl());
    	 Triangle [] tris =(Triangle.quad2Triangles(new double[]{0,SEG_LEN,SEG_LEN,0}, //X
    		new double[]{0,0,0,0}, new double[]{0,0,SEG_LEN*7,SEG_LEN*7}, //YZ
    		new double[]{1,0,0,1}, new double[]{0,0,1,1}, t, RenderMode.STATIC));//UVtr
    	 tris[0].setAlphaBlended(true);
    	 tris[1].setAlphaBlended(true);
    	 m.addTriangles(tris);
    	 m.finalizeModel();
    	 for(i=0; i<whiteLasers.length; i++){
    	    whiteLasers[i]=new LaserProjectile(tr,m);}
    	 //Red lasers.
    	 final Color [] redPalette = new Color[256];
    	 //I can't for the life of me find the red laser texture, so an explosion texture is used instead.
    	 for(i=0; i<256; i++){
    	     redPalette[i]=new Color(stdPalette[i].getBlue(),stdPalette[i].getGreen(),stdPalette[i].getRed(),stdPalette[i].getAlpha());
    	  }//end for(colors)
    	 
    	 m = new Model(false);
    	 t = tr.getResourceManager().getRAWAsTexture(
    		"BIGEX8.RAW",
    		stdPalette, 
    		GammaCorrectingColorProcessor.singleton, 
    		tr.getGPU().getGl());
    	 tris =(Triangle.quad2Triangles(new double[]{0,SEG_LEN/2,SEG_LEN/2,0}, //X
    		new double[]{0,0,0,0}, new double[]{0,0,SEG_LEN*7,SEG_LEN*7}, //YZ
    		new double[]{1,0,0,1}, new double[]{0,0,1,1}, t, RenderMode.STATIC));//UVtr
    	 tris[0].setAlphaBlended(true);
    	 tris[1].setAlphaBlended(true);
    	 m.addTriangles(tris);
    	 m.finalizeModel();
    	 for(i=0; i<redLasers.length; i++){
     	    redLasers[i]=new LaserProjectile(tr,m);}
	 }
	catch(Exception e){e.printStackTrace();}
       }
    public LaserProjectile triggerRedLaser(Vector3D location,Vector3D heading){
	redLaserIndex++;redLaserIndex%=redLasers.length;
	final LaserProjectile result = redLasers[redLaserIndex];
	result.destroy();
	result.reset(location, heading.scalarMultiply(SKL_SPEED));
	tr.getWorld().add(result);
	return result;
	}
}//end ProjectileFactory
