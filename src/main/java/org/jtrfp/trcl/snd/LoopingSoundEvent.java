package org.jtrfp.trcl.snd;

import java.util.Collection;

import javax.media.opengl.GL2ES2;

import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.ext.tr.SoundSystemFactory.SoundSystemFeature;
import org.jtrfp.trcl.obj.RelevantEverywhere;

public class LoopingSoundEvent extends AbstractSoundEvent implements RelevantEverywhere {
    private TRFutureTask<Void> lastApply;
    private double nextLoopTimeSeconds;
    private SoundTexture soundTexture;
    private double [] pan;
    private SamplePlaybackEvent mostRecentEvent;
    
    private LoopingSoundEvent(
	    Factory origin, SoundTexture texture, double [] pan) {
	super(0L, 0L, origin, null);
	setSoundTexture(texture);
	nextLoopTimeSeconds=Features.get(getOrigin().getTR(),SoundSystemFeature.class).getCurrentFrameBufferTimeCounter();
	setPan(pan);
	activate();
    }//end constructor
    
    @Override
    public void destroy(){
	if(mostRecentEvent!=null)
	 mostRecentEvent.destroy();
	super.destroy();
    }
    
    @Override
    public void deactivate(){
	if(mostRecentEvent!=null)
	    mostRecentEvent.deactivate();
	super.deactivate();
    }
    /*
    public void play(){
	if(!isPlaying){
	    activate();
	    firstRun.set(true);
	    nextLoopTimeSeconds=getOrigin().getTR().soundSystem.get().getCurrentFrameBufferTimeCounter();
	    isPlaying=true;}
    }
    
    public void stop(){
	destroy();
	isPlaying=false;
    }//end stop()
*/
    @Override
    public void apply(GL2ES2 gl, final double bufferStartTimeSeconds) {// Non-blocking.
	if(lastApply!=null)
	    if(!lastApply.isDone())
		return;
	  final TR                   tr  = getOrigin().getTR();
	  final SoundSystem soundSystem  = Features.get(tr,SoundSystemFeature.class);
	  final double bufferSizeSeconds = soundSystem.getBufferSizeSeconds();
	  //Need to fill to twice the end because it cannot be guranteed that these changes will take effect immediately or on the next buffer.
	  final double bufferEndTimeSecondsTwice = bufferStartTimeSeconds+bufferSizeSeconds*2;
	  while(nextLoopTimeSeconds <= bufferEndTimeSecondsTwice ){
	     //lastApply = getOrigin().getTR().getThreadManager().submitToThreadPool(new Callable<Void>(){
	     //@Override
	     //public Void call() throws Exception {
		// Apply
		final SamplePlaybackEvent.Factory playbackFactory = soundSystem.getPlaybackFactory();
		mostRecentEvent = playbackFactory.create(
			soundTexture, 
			nextLoopTimeSeconds,
			new double []{
			 SoundSystem.DEFAULT_SFX_VOLUME, 
			 SoundSystem.DEFAULT_SFX_VOLUME});
		soundSystem.enqueuePlaybackEvent(mostRecentEvent);
		
		LoopingSoundEvent.this.nextLoopTimeSeconds+=soundTexture.getLengthInRealtimeSeconds();
		}//end while(need more)
		//return null;
	     //}//end call()
	    //});//end submit()
    }//end apply()
    
    public static class Factory extends AbstractSoundEvent.Factory{

	protected Factory(TR tr) {
	    super(tr);
	}

	@Override
	public void apply(GL2ES2 gl, Collection<SoundEvent> events,
		double bufferStartTimeSeconds) {
	    for(SoundEvent event:events)
		event.apply(gl, bufferStartTimeSeconds);
	}//end apply(...)
	
	
	public LoopingSoundEvent create(SoundTexture tex, double [] pan){
	    return new LoopingSoundEvent(this,tex, pan);
	}//end create(...)
    }//end Factory

    public SoundTexture getSoundTexture() {
        return soundTexture;
    }
    public void setSoundTexture(SoundTexture soundTexture) {
        this.soundTexture = soundTexture;
    }
    public double[] getPan() {
        return pan;
    }
    public void setPan(double[] pan) {
        this.pan = pan;
    }

}//end LoopingSoundEvent
