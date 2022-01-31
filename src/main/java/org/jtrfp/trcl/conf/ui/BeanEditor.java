/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2022 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.conf.ui;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jtrfp.trcl.gui.TRBeanUtils;

public class BeanEditor implements ObjectEditorUI<Object> {
    private final List<ObjectEditorUI<?>> objectEditors = new ArrayList<>();
    private final JPanel rootPanel = new JPanel();
    private Supplier<Object> targetGetter;
    private Set<Annotation> inheritedAnnotations;
    
    public BeanEditor() {}

    @SuppressWarnings("unchecked")
    private void initialize() throws NoApplicablePropertiesException {
	try {
	    rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
	    final Object target = targetGetter.get();
	    final BeanInfo bi = Introspector.getBeanInfo(target.getClass());
	    
	    for(PropertyDescriptor pd : bi.getPropertyDescriptors()) {
		ConfigByUI conf = null;

		final Method readMethod = pd.getReadMethod();
		final Method writeMethod = pd.getWriteMethod();

		if(readMethod != null)
		    for(Annotation ann : readMethod.getAnnotations())
			if(ann instanceof ConfigByUI)
			    conf = (ConfigByUI)ann;
		if(writeMethod != null)
		    for(Annotation ann : writeMethod.getAnnotations())
			if(ann instanceof ConfigByUI)
			    conf = (ConfigByUI)ann;
		if(conf != null) {
		    final Object unidentifiedEditor = conf.editorClass().getConstructor().newInstance();
		    if( unidentifiedEditor instanceof ObjectEditorUI) {
			final ObjectEditorUI<?> objectEditor = (ObjectEditorUI<?>)unidentifiedEditor;
			//Exception-handling makes these a bit messy. ):
			@SuppressWarnings("rawtypes")
			final Supplier pGet = ()->{
			    try {
				if(readMethod == null)
				    throw new IllegalAccessException("No read method found for "+pd.getDisplayName());
				return readMethod.invoke(targetGetter.get());
			    } catch (IllegalAccessException | IllegalArgumentException
				    | InvocationTargetException e1) {
				e1.printStackTrace();
				return null;
			    }
			};
			@SuppressWarnings("rawtypes")
			final Consumer pSet = (x)->{
			    try {
				if(writeMethod == null)
				    throw new IllegalAccessException("No write method found for "+pd.getDisplayName());
				writeMethod.invoke(targetGetter.get(),x);
			    } catch (IllegalAccessException | IllegalArgumentException
				    | InvocationTargetException e) {
				e.printStackTrace();
			    }
			};
			
			final Set<Annotation> annotations = new HashSet<>();
			
			if(readMethod != null)
			    annotations.addAll(Arrays.asList(readMethod.getAnnotations()));
			if(writeMethod != null)
			    annotations.addAll(Arrays.asList(writeMethod.getAnnotations()));
			if(inheritedAnnotations != null)
			    annotations.addAll(inheritedAnnotations);
			objectEditors.add(objectEditor);
			final JComponent ui = objectEditor.getUIComponent();
			ui.setAlignmentX(0);
			annotations.stream().filter(x->x instanceof ToolTip).findAny().ifPresent(x->ui.setToolTipText(((ToolTip)x).text()));
			rootPanel.add(ui);
			objectEditor.configure(pSet, pGet, Collections.unmodifiableSet(annotations), TRBeanUtils.camelCaseToSentence(pd.getName()));
		    } else System.err.println("Unidentified editor class: "+conf.editorClass().getName());
		}//end if(conf)
	    }
	} catch(Exception e) {e.printStackTrace();}
	if(objectEditors.isEmpty())
	    throw new NoApplicablePropertiesException();
    }//end initialize()

    @Override
    public JComponent getUIComponent() {
	return rootPanel;
    }

    @Override
    public boolean isNeedingRestart() {//TODO: Add support to use annotation to denote if restart is needed
	boolean result = false;
	for(ObjectEditorUI<?> oe : objectEditors)
	    result |= oe.isNeedingRestart();

	return result;
    }

    @Override
    public void proposeApplySettings() {
	for(ObjectEditorUI<?> beui : objectEditors)
	    beui.proposeApplySettings();
    }

    @Override
    public void proposeRevertSettings(long revertTimeMillis) {
	for(ObjectEditorUI<?> beui : objectEditors)
	    beui.proposeRevertSettings(revertTimeMillis);
    }

    @Override
    public void configure(Consumer<Object> setter,
	    Supplier<Object> getter, Set<Annotation> annotations, String humanReadablePropertyName) {
	targetGetter = getter;
	this.inheritedAnnotations = annotations;
	initialize();
    }

}//end BeanEditor
