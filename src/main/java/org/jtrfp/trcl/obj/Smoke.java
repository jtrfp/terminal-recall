package org.jtrfp.trcl.obj;

import java.awt.Dimension;
import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.AnimatedTexture;
import org.jtrfp.trcl.DummyFuture;
import org.jtrfp.trcl.GammaCorrectingColorProcessor;
import org.jtrfp.trcl.Sequencer;
import org.jtrfp.trcl.Texture;
import org.jtrfp.trcl.TextureDescription;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.math.Vect3D;

public class Smoke extends BillboardSprite {
	    private final Sequencer sequencer;
	    private static final int NUM_FRAMES=4;
	    private final SmokeType type;
	    public Smoke(TR tr, SmokeType type) {
		super(tr);
		this.type=type;
		setBillboardSize(type.getBillboardSize());
		if(type.isRandomRotate())setRotation(2*Math.PI*Math.random());
		addBehavior(new SmokeBehavior());
		String [] aniFiles = type.getAnimationFiles();
		Future<Texture> [] frames = new Future[aniFiles.length];
		try{for(int i=0; i<aniFiles.length;i++){
		        frames[i]=frame(aniFiles[i]);
		    }
		}//end try{}
		catch(Exception e){e.printStackTrace();}
		setTexture(new DummyFuture<TextureDescription>(new AnimatedTexture(sequencer=new Sequencer(type.getMillisPerFrame(), frames.length, false,false),frames)),true);
	    }//end constructor
	    
	    @Override
	    public Smoke setPosition(double [] pos){
		final double [] superPos = super.getPosition();
		Vect3D.subtract(pos,type.getOrigin().scalarMultiply(getBillboardSize().getHeight()/2.).toArray(),superPos);
		super.notifyPositionChange();
		//super.setPosition(pos.subtract(type.getOrigin().scalarMultiply(getBillboardSize().getHeight()/2.)));
		return this;
	    }
	    
	    public static enum SmokeType{
		Puff(new String[]{
			"PUFF1.RAW",
			"PUFF2.RAW",
			"PUFF3.RAW",
			"PUFF4.RAW",
		},new Vector3D(0,0,0),new Dimension(14000,14000),200,true);
		
		private final String [] animationFiles;
		private final Vector3D origin;
		private final Dimension billboardSize;
		private final int millisPerFrame;
		private final boolean randomRotate;
		SmokeType(String [] animationFiles,Vector3D origin, Dimension billboardSize, int millisPerFrame, boolean randomRotate){
		    this.animationFiles=animationFiles; 
		    this.origin=origin;
		    this.billboardSize=billboardSize;
		    this.millisPerFrame=millisPerFrame;
		    this.randomRotate=randomRotate;
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
		/**
		 * @return the randomRotate
		 */
		public boolean isRandomRotate() {
		    return randomRotate;
		}
	    }//end SmokeType
	    
	    private Future<Texture> frame(String name) throws IllegalAccessException, IOException, FileLoadException
		{return (Future)getTr().getResourceManager().getRAWAsTexture(name, getTr().getDarkIsClearPalette(), GammaCorrectingColorProcessor.singleton, getTr().getGPU().takeGL());}

	    public void resetSmoke() {
		getBehavior().probeForBehavior(SmokeBehavior.class).reset();
		setVisible(true);
		setActive(true);
		sequencer.reset();
	    }
	    
	    private class SmokeBehavior extends Behavior{
		private long timeoutTimeInMillis=0;
		@Override
		public void _tick(long tickTimeMillis){
		    if(tickTimeMillis>=timeoutTimeInMillis){
			destroy();
		    }//end if(timeout)
		}//end _tick(...)
		public void reset(){
		    timeoutTimeInMillis=System.currentTimeMillis()+type.getMillisPerFrame()*(NUM_FRAMES);
		}//end reset()
	    }//end SmokeBehavior
}
