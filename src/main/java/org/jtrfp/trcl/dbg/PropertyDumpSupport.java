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
package org.jtrfp.trcl.dbg;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

public class PropertyDumpSupport {
    private final Object delegator;
    public PropertyDumpSupport(Object delegator){
	this.delegator=delegator;
    }
    public void dumpProperties(Map<String,PropertyDumpElement> dest){
	PropertyDescriptor [] props = PropertyUtils.getPropertyDescriptors(delegator.getClass());
	// Try to get a stacktrace.
	final HashMap<String, StackTraceElement[]> stackTraces = new HashMap<String, StackTraceElement[]>();
	try {final Method sm = delegator.getClass().getDeclaredMethod(
		    "getStackTracingPropertyChangeSupport");
	    ((StackTracingPropertyChangeSupport) sm.invoke(delegator))
		    .getStackTraces(stackTraces);
	} catch (NoSuchMethodException e) {e.printStackTrace();
	} catch (IllegalAccessException e) {e.printStackTrace();
	} catch (IllegalArgumentException e) {e.printStackTrace();
	} catch (InvocationTargetException e) {e.printStackTrace();
	}
	for(PropertyDescriptor pd:props){
	    final Method rm = pd.getReadMethod();
	    if(rm!=null){
		final String name = pd.getName();
		    final boolean irrelevantProperty = 
			    rm.getDeclaringClass()==Object.class ||
			    name.contentEquals("class")  ||
			    name.contentEquals("propertyChangeListeners");
		    if(!irrelevantProperty){
			try{Object val = rm.invoke(delegator);
			    try{final Method sm = rm.getReturnType().getMethod("dumpProperties", Map.class);
				final HashMap<String,Object> subDest = new HashMap<String,Object>();
				sm.invoke(val, subDest);
				val = subDest;}
			    catch(NoSuchMethodException e){}
			    StackTraceElement [] stackTrace = stackTraces.get(pd.getName());
			    PropertyDumpElement gsp = new PropertyDumpElement(val,stackTrace);
			    dest.put(pd.getName(),gsp);}
			     catch(Exception e){e.printStackTrace();}
		    }//end if(!irrelevant)
	    }//end if(readMethod)
	}//end for(props)
    }//end dumpProperties(...)
    
    public class PropertyDumpElement{
	private final Object 		  intrinsic;
	private final StackTraceElement []stackTraceOfLastModification;
	
	public PropertyDumpElement(Object intrinsic, StackTraceElement [] stackTraceOfLastModification){
	    this.intrinsic=intrinsic;
	    this.stackTraceOfLastModification=stackTraceOfLastModification;
	}

	/**
	 * @return the intrinsic
	 */
	public Object getIntrinsic() {
	    return intrinsic;
	}

	/**
	 * @return the stackTraceOfLastModification
	 */
	public StackTraceElement[] getStackTraceOfLastModification() {
	    return stackTraceOfLastModification;
	}
    }//end GLStateProperty
}//end PropertyDumpSupport()
