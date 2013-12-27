package org.jtrfp.trcl.obj;

import java.awt.Dimension;
import java.io.IOException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.AnimatedTexture;
import org.jtrfp.trcl.GammaCorrectingColorProcessor;
import org.jtrfp.trcl.Sequencer;
import org.jtrfp.trcl.Texture;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.core.TR;

public class Explosion extends BillboardSprite {
    private final Sequencer sequencer;
    private static final int NUM_FRAMES=16;
    private final ExplosionType type;
    public Explosion(TR tr, ExplosionType type) {
	super(tr);
	this.type=type;
	setBillboardSize(type.getBillboardSize());
	addBehavior(new ExplosionBehavior());
	String [] aniFiles = type.getAnimationFiles();
	Texture [] frames = new Texture[aniFiles.length];
	try{for(int i=0; i<aniFiles.length;i++){
	        frames[i]=frame(aniFiles[i]);
	    }
	}//end try{}
	catch(Exception e){e.printStackTrace();}
	setTexture(new AnimatedTexture(sequencer=new Sequencer(type.getMillisPerFrame(), frames.length, false),frames),true);
    }//end constructor
    
    @Override
    public void setPosition(Vector3D pos){
	super.setPosition(pos.subtract(type.getOrigin().scalarMultiply(getBillboardSize().getHeight()/2.)));
    }
    
    public static enum ExplosionType{
	Blast(new String[]{
		"BLAST1.RAW",
		"BLAST2.RAW",
		"BLAST3.RAW",
		"BLAST4.RAW",
		"BLAST5.RAW",
		"BLAST6.RAW",
		"BLAST7.RAW",
		"BLAST8.RAW",
		"BLAST9.RAW",
		"BLAST10.RAW",
		"BLAST11.RAW",
		"BLAST12.RAW",
		"BLAST13.RAW",
		"BLAST14.RAW",
		"BLAST15.RAW",
		"BLAST16.RAW"
	},new Vector3D(0,0,0),new Dimension(20000,20000),100),
	Billow(new String[]{
		"BILLOW1.RAW",
		"BILLOW2.RAW",
		"BILLOW3.RAW",
		"BILLOW4.RAW",
		"BILLOW5.RAW",
		"BILLOW6.RAW",
		"BILLOW7.RAW",
		"BILLOW8.RAW",
		"BILLOW9.RAW",
		"BILLOW10.RAW",
		"BILLOW11.RAW",
		"BILLOW12.RAW",
		"BILLOW13.RAW",
		"BILLOW14.RAW",
		"BILLOW15.RAW",
		"BILLOW16.RAW",
	},new Vector3D(0,0,0),new Dimension(40000,40000),100),
	BigExplosion(new String[]{
		"BIGEX1.RAW",
		"BIGEX2.RAW",
		"BIGEX3.RAW",
		"BIGEX4.RAW",
		"BIGEX5.RAW",
		"BIGEX6.RAW",
		"BIGEX7.RAW",
		"BIGEX8.RAW",
		"BIGEX9.RAW",
		"BIGEX10.RAW",
		"BIGEX11.RAW",
		"BIGEX12.RAW",
		"BIGEX13.RAW",
		"BIGEX14.RAW",
		"BIGEX15.RAW",
		"BIGEX16.RAW",
	},new Vector3D(0,-1,0), new Dimension(100000,100000),120);
	
	private final String [] animationFiles;
	private final Vector3D origin;
	private final Dimension billboardSize;
	private final int millisPerFrame;
	ExplosionType(String [] animationFiles,Vector3D origin, Dimension billboardSize, int millisPerFrame){
	    this.animationFiles=animationFiles; this.origin=origin;this.billboardSize=billboardSize;this.millisPerFrame=millisPerFrame;
	}
	public int getMillisPerFrame() {
	    return millisPerFrame;
	}
	public Dimension getBillboardSize() {
	    return billboardSize;
	}
	public String [] getAnimationFiles(){
	    return animationFiles;
	}
	public Vector3D getOrigin(){
	    return origin;}
    }//end ExplosionType
    
    private Texture frame(String name) throws IllegalAccessException, IOException, FileLoadException
	{return (Texture)getTr().getResourceManager().getRAWAsTexture(name, getTr().getGlobalPalette(), GammaCorrectingColorProcessor.singleton, getTr().getGPU().takeGL());}

    public void resetExplosion() {
	getBehavior().probeForBehavior(ExplosionBehavior.class).reset();
	setVisible(true);
	sequencer.reset();
    }
    
    private class ExplosionBehavior extends Behavior{
	private long timeoutTimeInMillis=0;
	@Override
	public void _tick(long tickTimeMillis){
	    if(tickTimeMillis>timeoutTimeInMillis){
		destroy();
	    }//end if(timeout)
	}//end _tick(...)
	public void reset(){
	    timeoutTimeInMillis=System.currentTimeMillis()+type.getMillisPerFrame()*NUM_FRAMES;
	}//end reset()
    }//end ExplosionBehavior

}
