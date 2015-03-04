/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2014 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.snd;

import java.nio.IntBuffer;
import java.util.HashMap;

import org.jtrfp.trcl.core.TR;

import de.quippy.javamod.multimedia.mod.loader.Module;
import de.quippy.javamod.multimedia.mod.loader.instrument.Sample;
import de.quippy.javamod.multimedia.mod.loader.pattern.Pattern;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternElement;
import de.quippy.javamod.multimedia.mod.loader.pattern.PatternRow;
import de.quippy.javamod.system.Helpers;

public class GPUResidentMOD {
    private final TR tr;
    private final Module module;
    private SoundTexture [] samples;
    private double realtimeSecondsPerRow=-1;
    private int speed=6;
    private int bpm=125;
    double []panStates = new double[32];// [-1,1]
    double []volumeStates = new double[32]; // [0,1]
    private double songLengthInSeconds=-1;
    private static final double MUSIC_VOLUME = 2; 
    private final HashMap<PatternElement,Integer> durationInRows = new HashMap<PatternElement,Integer>();
    
    public GPUResidentMOD(TR tr, Module module){
	this.tr=tr;
	this.module=module;
	Sample [] modSamples = module.getInstrumentContainer().getSamples();
	for(int i=0; i<volumeStates.length; i++)
	    volumeStates[i]=MUSIC_VOLUME*(double)module.getChannelVolume(i);
	for(int i=0; i<panStates.length; i++){
	    panStates[i]=((double)module.getPanningValue(i)-128.)/128.;
	    }
	this.samples=new SoundTexture[modSamples.length];
	for(int i=0; i<modSamples.length; i++){
	    final Sample thisModSample = modSamples[i];
	    if(thisModSample.sample!=null){
		this.samples[i]=tr.soundSystem.get().newSoundTexture(IntBuffer.wrap(thisModSample.sample),thisModSample.baseFrequency);
	    }//end if(!null)
	}//end for(i)
	calculateNoteLengths();
    }//end constructor
    
    private void calculateNoteLengths() {
	final Pattern []    patterns = module.getPatternContainer().getPattern();
	final int []        arrangements = module.getArrangement();
	final int []	    rowOfPreviousNote = new int[32];
	int 		    rowCounter=0;
	setTempo(module.getBPMSpeed());
	setInterruptLockedSpeed(module.getTempo());
	try{
	 for(int arrIdx=0; arrIdx<module.getSongLength(); arrIdx++){
	    int arrangement = arrangements[arrIdx];
	    final Pattern pattern = patterns[arrangement];
	    for(PatternRow row:pattern.getPatternRow()){
		rowCounter++;
		for(PatternElement element:row.getPatternElement()){
		   if(element.getEffekt()==0x0B)
		       throw new EndOfSongException();
		   final int lengthInRows = rowCounter-rowOfPreviousNote[element.getChannel()];
		   durationInRows.put(element, lengthInRows);
		   rowOfPreviousNote[element.getChannel()]=rowCounter;
		}//end for(patternElements)
	    }//end for(rows)
	 }//end for(arrangements)
	}catch(EndOfSongException e){}
    }//end calculateNoteLength()

    public void apply(final double startOffsetInSeconds, SoundEvent parent, double stereoWidth){
	final Pattern []    patterns = module.getPatternContainer().getPattern();
	final int []        arrangements = module.getArrangement();
	double 		    timeOffsetCounter=0;
	setTempo(module.getBPMSpeed());
	setInterruptLockedSpeed(module.getTempo());
	try{
	 for(int arrIdx=0; arrIdx<module.getSongLength(); arrIdx++){
	    int arrangement = arrangements[arrIdx];
	    final Pattern pattern = patterns[arrangement];
	    for(PatternRow row:pattern.getPatternRow()){
		timeOffsetCounter+=realtimeSecondsPerRow;
		for(PatternElement element:row.getPatternElement()){
		   final int instID = element.getInstrument()-1;
		   final int fx = element.getEffekt();
		   final int op = element.getEffektOp();
		   switch (fx){
		    case 0x0C:
		       volumeStates[element.getChannel()]=MUSIC_VOLUME*(double)op;
		       break;
		    case 0x0F:
		       setSpeedOrTempo(op);
		       break;
		    case 0x0B:
		       throw new EndOfSongException();
		    default:
		   }//end switch()
		   if(instID>-1&&element.getNoteIndex()>0){
		 	final SoundTexture texture = this.samples[instID];
			if(texture!=null){
			  final double ps = (panStates[element.getChannel()]*stereoWidth+1)/2.; 
			  final double []panState = new double[2];
			  panState[0]= (1-ps)*volumeStates[element.getChannel()];
			  panState[1]= ps*volumeStates[element.getChannel()];
			  final double playbackRatio = 428./Helpers.noteValues[element.getNoteIndex()-1];
			  final SoundEvent evt = tr.soundSystem.get().
				  getPlaybackFactory().
				  create(texture,
					  timeOffsetCounter+startOffsetInSeconds, 
					  panState,
					  parent,
					  playbackRatio);
			  tr.soundSystem.get().enqueuePlaybackEvent(evt);
			}//end if(null)
		   }//end if(>-1)
		}//end for(patternElements)
	    }//end for(rows)
	 }//end for(arrangements)
	}catch(EndOfSongException e){}
	songLengthInSeconds=timeOffsetCounter;
    }//end startSequence()

    
    private static class EndOfSongException extends Exception{}
    
    private void setSpeedOrTempo(int op) {//UNTESTED
	if(op >31){
	    //Tempo
	    setTempo(op);
	}else{
	    //Interrupt-based "Speed"
	    setInterruptLockedSpeed(op);
	}
	
    }//end setSpeedOrTempo()
    
    private void setInterruptLockedSpeed(int speed){
	this.speed=speed;
	recalculateRealtimeSecondsPerRow();
    }
    
    private void setTempo(int tempo){
	this.bpm=tempo;
	recalculateRealtimeSecondsPerRow();
    }

    private void recalculateRealtimeSecondsPerRow() {
	realtimeSecondsPerRow = ((60. / (double)bpm)/4.);// 4 rows per beat (assuming 16th notes)
	//TODO: 'speed'
    }

    /**
     * WARNING: This value is only available after apply(...) is invoked.
     * @return Song length in buffer frames, or -1 if apply() was not first invoked.
     * @since Nov 2, 2014
     */
    public double getSongLengthInRealtimeSeconds() {
	return songLengthInSeconds;
    }
}//end MusicPlayer
