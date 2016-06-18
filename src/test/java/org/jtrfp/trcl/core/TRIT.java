/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.core;

import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.gui.SwingMenuSystemFactory;
import org.junit.Before;
import org.junit.Test;

public class TRIT {
    
    @Before
    public void before(){
	Features.resetImpl();
    }

    @Test
    public void testRegisterFeature() {
	Features.registerFeature(new SwingMenuSystemFactory());
	Features.get(Features.getSingleton(), MenuSystem.class);
    }

}//end TRIT
