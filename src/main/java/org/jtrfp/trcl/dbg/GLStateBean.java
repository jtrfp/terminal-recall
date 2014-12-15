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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jtrfp.trcl.dbg.PropertyDumpSupport.PropertyDumpElement;

public class GLStateBean {
    private final StackTracingPropertyChangeSupport pcSupport = 
	    new StackTracingPropertyChangeSupport(new PropertyChangeSupport(this));
    private final HashSet<GLSanityChecker> sanityCheckers = new HashSet<GLSanityChecker>();
    private final PropertyDumpSupport pdSupport = new PropertyDumpSupport(this);
    
    private int texture2DBinding,
    		texture2DMSBinding,
    		texture1DBinding,
    		texture2DArrayBinding,
    		textureBufferTextureBinding,
    		
    		arrayBufferBinding,
    		copyReadBufferBinding,
    		elementArrayBufferBinding,
    		textureBufferBufferBinding,
    		transformFeedbackBufferBinding,
    		uniformBufferBinding,
    		
    		readRenderBufferBinding,
    		drawRenderBufferBinding;
    
    public GLStateBean addSanityChecker(GLSanityChecker sc){
	sanityCheckers.add(sc);
	sc.applyTo(this);
	return this;
    }
    
    public GLStateBean removeSanityChecker(GLSanityChecker sc){
	if(sanityCheckers.remove(sc))
	    sc.stripFrom(this);
	return this;
    }
    
    public GLStateBean dumpHumanReadableStateReport(PrintStream ps){
	dumpHumanReadableStateReport(ps,0);
	return this;
    }//end dumpHumanReadableStateReport(...)
    
    private void dumpHumanReadableStateReport(final PrintStream ps, final int indentation){
	dumpProperties(new AbstractMap<String,PropertyDumpElement>(){
	    @Override
	    public PropertyDumpElement put(String key, PropertyDumpElement entry) {
		if (Map.class.isAssignableFrom(entry.getClass()))
		    dumpHumanReadableStateReport(ps, indentation + 1);
		else {
		    for (int i = 0; i < indentation; i++)
			ps.print("\t");
		    ps.println("P: " + key + "\t" + entry.getIntrinsic());
		    //Print stack trace
		    StackTraceElement[] trace = entry
			    .getStackTraceOfLastModification();
		    if (trace != null) {
			for (int i = 0; i < indentation; i++)
				ps.print("\t");
			ps.println("Trace of last modification:");
			for (StackTraceElement elm : entry
				.getStackTraceOfLastModification()) {
			    for (int i = 0; i < indentation; i++)
				ps.print("\t");
			    ps.println(" " + elm.getClassName()+"."+ elm.getMethodName() + "("
				    + elm.getFileName() + ":"
				    + elm.getLineNumber() + ")");
			}// end for(stack trace elements)
		    }//end if(trace!=null)
		}// end else{}
		return entry;
	    }//end put(...)

	    @Override
	    public Set<java.util.Map.Entry<String, PropertyDumpElement>> entrySet() {
		return null;
	    }});
    }//end dumpHumanReadableStateReport(...)
    
    /**
     * @param arg0
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener arg0) {
	pcSupport.addPropertyChangeListener(arg0);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	pcSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
	return pcSupport.getPropertyChangeListeners();
    }

    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
     */
    public PropertyChangeListener[] getPropertyChangeListeners(
	    String propertyName) {
	return pcSupport.getPropertyChangeListeners(propertyName);
    }

    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#hasListeners(java.lang.String)
     */
    public boolean hasListeners(String propertyName) {
	return pcSupport.hasListeners(propertyName);
    }

    /**
     * @param arg0
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener arg0) {
	pcSupport.removePropertyChangeListener(arg0);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	pcSupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * @return the texture2DBinding
     */
    public int getTexture2DBinding() {
        return texture2DBinding;
    }

    /**
     * @param texture2dBinding the texture2DBinding to set
     */
    public void setTexture2DBinding(int texture2dBinding) {
	pcSupport.firePropertyChange("texture2DBinding", this.texture2DBinding, texture2dBinding);
        texture2DBinding = texture2dBinding;
    }

    /**
     * @return the texture2DMSBinding
     */
    public int getTexture2DMSBinding() {
        return texture2DMSBinding;
    }

    /**
     * @param texture2dmsBinding the texture2DMSBinding to set
     */
    public void setTexture2DMSBinding(int texture2dmsBinding) {
	pcSupport.firePropertyChange("texture2DMSBinding", this.texture2DMSBinding, texture2dmsBinding);
        texture2DMSBinding = texture2dmsBinding;
    }

    /**
     * @return the texture1DBinding
     */
    public int getTexture1DBinding() {
        return texture1DBinding;
    }

    /**
     * @param texture1dBinding the texture1DBinding to set
     */
    public void setTexture1DBinding(int texture1dBinding) {
	pcSupport.firePropertyChange("texture1DBinding", this.texture1DBinding, texture1dBinding);
        texture1DBinding = texture1dBinding;
    }

    /**
     * @return the texture2DArrayBinding
     */
    public int getTexture2DArrayBinding() {
        return texture2DArrayBinding;
    }

    /**
     * @param texture2dArrayBinding the texture2DArrayBinding to set
     */
    public void setTexture2DArrayBinding(int texture2dArrayBinding) {
	pcSupport.firePropertyChange("texture2DArrayBinding", this.texture2DArrayBinding, texture2dArrayBinding);
        texture2DArrayBinding = texture2dArrayBinding;
    }

    /**
     * @return the textureBufferBinding
     */
    public int getTextureBufferTextureBinding() {
        return textureBufferTextureBinding;
    }

    /**
     * @param textureBufferBinding the textureBufferBinding to set
     */
    public void setTextureBufferTextureBinding(int textureBufferTextureBinding) {
	pcSupport.firePropertyChange("textureBufferTextureBinding", this.textureBufferTextureBinding, textureBufferTextureBinding);
        this.textureBufferTextureBinding = textureBufferTextureBinding;
    }

    /**
     * @param dest
     * @see org.jtrfp.trcl.dbg.PropertyDumpSupport#dumpProperties(java.util.Map)
     */
    public void dumpProperties(Map<String, PropertyDumpElement> dest) {
	pdSupport.dumpProperties(dest);
    }

    /**
     * @return the arrayBufferBinding
     */
    public int getArrayBufferBinding() {
        return arrayBufferBinding;
    }

    /**
     * @param arrayBufferBinding the arrayBufferBinding to set
     */
    public void setArrayBufferBinding(int arrayBufferBinding) {
	pcSupport.firePropertyChange("arrayBufferBinding", this.arrayBufferBinding, arrayBufferBinding);
        this.arrayBufferBinding = arrayBufferBinding;
    }

    /**
     * @return the copyReadBufferBinding
     */
    public int getCopyReadBufferBinding() {
        return copyReadBufferBinding;
    }

    /**
     * @param copyReadBufferBinding the copyReadBufferBinding to set
     */
    public void setCopyReadBufferBinding(int copyReadBufferBinding) {
	pcSupport.firePropertyChange("copyReadBufferBinding", this.copyReadBufferBinding, copyReadBufferBinding);
        this.copyReadBufferBinding = copyReadBufferBinding;
    }

    /**
     * @return the elementArrayBufferBinding
     */
    public int getElementArrayBufferBinding() {
        return elementArrayBufferBinding;
    }

    /**
     * @param elementArrayBufferBinding the elementArrayBufferBinding to set
     */
    public void setElementArrayBufferBinding(int elementArrayBufferBinding) {
	pcSupport.firePropertyChange("elementArrayBufferBinding", this.elementArrayBufferBinding, elementArrayBufferBinding);
        this.elementArrayBufferBinding = elementArrayBufferBinding;
    }

    /**
     * @return the textureBufferBufferBinding
     */
    public int getTextureBufferBufferBinding() {
        return textureBufferBufferBinding;
    }

    /**
     * @param textureBufferBufferBinding the textureBufferBufferBinding to set
     */
    public void setTextureBufferBufferBinding(int textureBufferBufferBinding) {
	pcSupport.firePropertyChange("textureBufferBufferBinding", this.textureBufferBufferBinding, textureBufferBufferBinding);
        this.textureBufferBufferBinding = textureBufferBufferBinding;
    }

    /**
     * @return the transformFeedbackBufferBinding
     */
    public int getTransformFeedbackBufferBinding() {
        return transformFeedbackBufferBinding;
    }

    /**
     * @param transformFeedbackBufferBinding the transformFeedbackBufferBinding to set
     */
    public void setTransformFeedbackBufferBinding(int transformFeedbackBufferBinding) {
	pcSupport.firePropertyChange("transformFeedbackBufferBinding", this.transformFeedbackBufferBinding, transformFeedbackBufferBinding);
        this.transformFeedbackBufferBinding = transformFeedbackBufferBinding;
    }

    /**
     * @return the uniformBufferBinding
     */
    public int getUniformBufferBinding() {
        return uniformBufferBinding;
    }

    /**
     * @param uniformBufferBinding the uniformBufferBinding to set
     */
    public void setUniformBufferBinding(int uniformBufferBinding) {
	pcSupport.firePropertyChange("uniformBufferBinding", this.textureBufferBufferBinding, textureBufferBufferBinding);
        this.uniformBufferBinding = uniformBufferBinding;
    }
    
    protected StackTracingPropertyChangeSupport getStackTracingPropertyChangeSupport(){
	return pcSupport;
    }

    /**
     * @return the readRenderBufferBinding
     */
    public int getReadRenderBufferBinding() {
        return readRenderBufferBinding;
    }

    /**
     * @param readRenderBufferBinding the readRenderBufferBinding to set
     */
    public void setReadRenderBufferBinding(int readRenderBufferBinding) {
	pcSupport.firePropertyChange("readRenderBufferBinding", this.readRenderBufferBinding, readRenderBufferBinding);
        this.readRenderBufferBinding = readRenderBufferBinding;
    }

    /**
     * @return the drawRenderBufferBinding
     */
    public int getDrawRenderBufferBinding() {
        return drawRenderBufferBinding;
    }

    /**
     * @param drawRenderBufferBinding the drawRenderBufferBinding to set
     */
    public void setDrawRenderBufferBinding(int drawRenderBufferBinding) {
	pcSupport.firePropertyChange("drawRenderBufferBinding", this.drawRenderBufferBinding, drawRenderBufferBinding);
        this.drawRenderBufferBinding = drawRenderBufferBinding;
    }

}
