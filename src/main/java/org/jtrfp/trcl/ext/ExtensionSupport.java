/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.ext;

import java.util.HashSet;

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.jtrfp.trcl.coll.BidiReferenceMap;
import org.jtrfp.trcl.coll.CachedAdapter;

public class ExtensionSupport<PARENT> {
    private final PARENT parent;
    private final HashSet<Extension> applied = new HashSet<Extension>();
    
    private final CachedAdapter<Class<? extends Extension<?>>,Extension<?>> extensionFactory = 
	    new CachedAdapter<Class<? extends Extension<?>>,Extension<?>>(
	     new BidiReferenceMap<Class<? extends Extension<?>>,Extension<?>>
              (ReferenceStrength.HARD,ReferenceStrength.HARD, 64,.75f,true)){
	@Override
	protected Extension<?> _adapt(
		Class<? extends Extension<?>> value)
			throws UnsupportedOperationException {
	    try{Extension<PARENT> result = (Extension<PARENT>)value.newInstance();
	    final Class eClass = result.getExtendedClass();
	    final Class pClass = parent.getClass();
	    if(!eClass.equals(pClass))
		throw new UnsupportedOperationException(
			"Class mismatch. Extension: "+result.getExtendedClass().getName()+
			" Expected: "+pClass.getName());
	    System.out.println("Initializing Extension: "+result.getHumanReadableName());
	    result.init(parent);
	    return result;
	    }
	    catch(Exception e){throw new UnsupportedOperationException(e);}
	}//end _adapt()

	@Override
	protected Class<? extends Extension<?>> _reAdapt(
		Extension<?> value)
			throws UnsupportedOperationException {
	    throw new UnsupportedOperationException();
	}//end _reAdapt
    };
		
 //// C O N S T R U C T O R ////
 public ExtensionSupport(PARENT parent){
     this.parent=parent;
 }
 
 public<CLASS extends Extension<?>> Extension<?> peekExtension(Class<CLASS> extensionClass){
     return extensionFactory.adapt(extensionClass);
 }
 
 public<CLASS extends Extension<?>> Extension<?> getExtension(Class<CLASS> extensionClass){
     final Extension result = extensionFactory.adapt(extensionClass);
     if(applied.add(result))
	 result.apply(parent);
     return result;
 }//end getExtension(...)
 
 public void loadBuiltInExtensions(){
     for(Class<? extends Extension> eClass:Extensions.builtInExtensions){
	 try{if(parent.getClass().isAssignableFrom(((Extension)(eClass.newInstance())).getExtendedClass()) || eClass.equals(parent.getClass()))
	     getExtension(eClass);}catch(Exception e){e.printStackTrace();}
     }
 }
}//end ExtensionSupport
