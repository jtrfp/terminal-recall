package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.GammaCorrectingColorProcessor;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.TextureDescription;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.Location3D;

public class NavArrow extends WorldObject2DVisibleEverywhere {
private static final double WIDTH=.08;
private static final double HEIGHT=.08;
private static final double Z=.0001;
private final NAVSystem nav;
    public NavArrow(TR tr, NAVSystem navSystem) {
	super(tr);
	this.nav=navSystem;
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
	setTop(Vector3D.PLUS_J);
	setActive(true);
	setVisible(true);
	addBehavior(new NavArrowBehavior());
	}//end try{}
	catch(Exception e){e.printStackTrace();}
    }//end constructor
    
    private class NavArrowBehavior extends Behavior{
	@Override
	public void _tick(long time){
	    final WorldObject player = getTr().getPlayer();
	    final Vector3D playerPos = player.getPosition();
	    final Vector3D playerPosXY = new Vector3D(playerPos.getX(),playerPos.getZ(),0);
	    final Vector3D playerHeading = player.getHeading();
	    final Vector3D playerHeadingXY = new Vector3D(playerHeading.getX(),playerHeading.getZ(),0);
	    final Location3D loc =nav.currentNAVTarget().getLocationOnMap();
	    final Vector3D navLoc = new Vector3D(
		    TR.legacy2Modern(loc.getZ()),
		    TR.legacy2Modern(loc.getY()),
		    TR.legacy2Modern(loc.getX()));
	    final Vector3D navLocXY = new Vector3D(navLoc.getX(),navLoc.getZ(),0);
	    final Vector3D player2NavVectorXY = TR.twosComplimentSubtract(navLocXY, playerPosXY);
	    final double modernDistance = player2NavVectorXY.getNorm();
	    final Vector3D normPlayer2NavVector = player2NavVectorXY.normalize();
	    //Kludge to correct negative X bug in engine. (mirrored world)
	    final Vector3D correctedNormPlayer2NavVector = new Vector3D(-normPlayer2NavVector.getX(),normPlayer2NavVector.getY(),0);
	    final Rotation rot = new Rotation(Vector3D.PLUS_J,playerHeadingXY);
	    setTop(rot.applyTo(correctedNormPlayer2NavVector));
	    //System.out.println("TICK");
	}//_ticks(...)
    }//end NavArrowBehavior
}//end NavArrow
