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
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.flow.Mission;

public class NavArrow extends WorldObject2DVisibleEverywhere {
private static final double WIDTH=.08;
private static final double HEIGHT=.08;
private static final double Z=.0001;
private static final int TEXT_UPDATE_INTERVAL_MS=150;
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
	private int counter=0;
	@Override
	public void _tick(long time){
	    final TR tr=getTr();
	    final Mission mission = tr.getCurrentMission();
	    final WorldObject player = tr.getPlayer();
	    final Vector3D playerPos = player.getPosition();
	    final Vector3D playerPosXY = new Vector3D(playerPos.getX(),playerPos.getZ(),0);
	    final Vector3D playerHeading = player.getHeading();
	    final Vector3D playerHeadingXY = new Vector3D(playerHeading.getX(),playerHeading.getZ(),0);
	    if(mission.currentNAVTarget()==null){setVisible(false);return;}
	    if(mission.currentNAVTarget().getTarget()==null){setVisible(false);return;}
	    	else setVisible(true);
	    final Vector3D loc =mission.currentNAVTarget().getTarget().getPosition();
	    final Vector3D navLocXY = new Vector3D(loc.getX(),loc.getZ(),0);
	    final Vector3D player2NavVectorXY = TR.twosComplimentSubtract(navLocXY, playerPosXY);
	    final double modernDistance = player2NavVectorXY.getNorm();
	    counter++;counter%=TEXT_UPDATE_INTERVAL_MS/(1000/ThreadManager.GAMEPLAY_FPS);
	    //This need only be done occasionally
	    if(counter==0){
		getTr().getHudSystem().getDistance().setContent(""+(int)(modernDistance/TR.mapSquareSize));
		getTr().getHudSystem().getSector().setContent(((int)((playerPos.getX()+TR.mapCartOffset)/TR.mapSquareSize))+"."+
		    ((int)((playerPos.getZ()+TR.mapCartOffset)/TR.mapSquareSize)));
	    }
	    final Vector3D normPlayer2NavVector = player2NavVectorXY.normalize();
	    //Kludge to correct negative X bug in engine. (mirrored world)
	    final Vector3D correctedNormPlayer2NavVector = new Vector3D(-normPlayer2NavVector.getX(),normPlayer2NavVector.getY(),0);
	    final Rotation rot = new Rotation(Vector3D.PLUS_J,playerHeadingXY.getNorm()!=0?playerHeadingXY:Vector3D.PLUS_I);
	    setTop(rot.applyTo(correctedNormPlayer2NavVector));
	}//_ticks(...)
    }//end NavArrowBehavior
}//end NavArrow
