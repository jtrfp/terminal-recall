/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola and contributors.
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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.core.TriangleVertexWindow;

public interface TextureBehavior {
 public void apply(TriangleList triangleList, int gpuTVIndex, int numFrames, Triangle thisTriangle, Vector3D pos, TriangleVertexWindow vw);
 public void init(TextureDescription parent);
 public static abstract class Abstract implements TextureBehavior{
	 private TextureDescription parent;
	 public void init(TextureDescription parent){
	     this.parent=parent;
	 }//end init()
	 protected TextureDescription getParent(){
	     return parent;
	 }
 }//end Abstract
 public static class Support{
     private final ArrayList<TextureBehavior> behaviors = new ArrayList<TextureBehavior>();
     
     public void addBehavior(TextureBehavior beh){
	 if(!behaviors.contains(beh)) behaviors.add(beh);
     }

     public void removeBehavior(TextureBehavior beh){
	 behaviors.remove(beh);
     }

     public void apply(TriangleList triangleList, int gpuTVIndex, int numFrames, Triangle thisTriangle, Vector3D pos, TriangleVertexWindow vw){
	 for(TextureBehavior beh:behaviors)
	     beh.apply(triangleList, gpuTVIndex, numFrames, thisTriangle, pos, vw);
     }//end apply()
 }//end Support
}
