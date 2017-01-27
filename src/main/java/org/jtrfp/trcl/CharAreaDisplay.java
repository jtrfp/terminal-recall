/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl;

import java.util.ArrayList;
import java.util.Scanner;

import org.jtrfp.trcl.core.TRFactory.TR;

public class CharAreaDisplay extends RenderableSpacePartitioningGrid {
    private final int 		heightChars,widthChars;
    private final CharLineDisplay[]lines;
    private double 		fontSize=.07;
    private ArrayList<String>	lineStrings = new ArrayList<String>();
    private double 		startString;
    private double		positionX,positionY,positionZ;
    
    public CharAreaDisplay(double fontSize, int widthChars, int heightChars, TR tr, GLFont font) {
	super();
	this.heightChars=heightChars;
	this.widthChars=widthChars;
	lines = new CharLineDisplay[heightChars];
	for(int i=0; i<heightChars; i++){
	    lines[i]=new CharLineDisplay(this, fontSize, widthChars, font);
	}//end for(i)
	this.fontSize=fontSize;
	updatePositions();
    }//end constructor
    
    public void setFontSize(double glSize){
	this.fontSize=glSize;
	for(CharLineDisplay line:lines){
	    line.setFontSize(fontSize);
	}
	updatePositions();
    }//end setFontSize(...)
    
    public CharAreaDisplay setContent(String content){
	content = wordWrap(content,widthChars);
	final Scanner s = new Scanner(content);
	lineStrings.clear();
	while(s.hasNext()){
	    final String line = s.nextLine();
	    lineStrings.add(line);
	}
	s.close();
	updateStrings();
	return this;
    }//end setContent()
    
    private static String wordWrap(final String content, int widthChars) {
	final StringBuilder result 		= new StringBuilder();
	for(String line:content.split("\n")){
	    if(line.length()>=widthChars){
		 int lineLengthSoFar=0;
		 for(String word:line.split(" ")){
		     while(word.length()>widthChars){
			 result.append(word.substring(0, widthChars));
			 result.append(' ');
			 result.append("\n");
			 lineLengthSoFar=0;
			 word=word.substring(widthChars);
			 }//end while(word is longer than line)
		     if(lineLengthSoFar+word.length()>=widthChars){
			result.append("\n");
		        lineLengthSoFar=0;
		     }//end if(word wrap)
		     lineLengthSoFar+=word.length()+1;
		     result.append(word);
		     result.append(' ');
		 }//end while(hasMoreElements)
	    }//end if(length>widthChars)
	    else{result.append(line);result.append("\n");}
	}//end while(hasNextLine)
	return result.toString();
    }//end wordWrap

    public void setScrollPosition(double newScrollPos){
	startString=newScrollPos;
	updatePositions();
	updateStrings();
    }//end setScrollPosition(...)
    
    public void setPosition(double x, double y, double z){
	positionX=x;
	positionY=y;
	positionZ=z;
	updatePositions();
    }//end setPosition(...)

    private void updateStrings() {
	for (int i = 0; i < heightChars; i++) {
	    final int index = (int) Math.floor(startString) + i - heightChars;
	    if (lineStrings.size() > index&&index>=0) {
		final String line = lineStrings.get(index);
		if (line != null)
		    lines[i].setContent(line);
	    }// end if(lineStrings.size())
	    else lines[i].setContent("");
	}//end end(i)
    }// end updateStrings()
    
    private void updatePositions(){
	for(int i=0; i<heightChars; i++){
	    lines[i].setPosition(positionX, positionY-(i-(startString%1.))*fontSize, positionZ);
	}//end for(i)
    }//end updatePositions()
    
    public int getNumActiveLines(){
	return lineStrings.size();
    }
}//end CharAreaDisplay
