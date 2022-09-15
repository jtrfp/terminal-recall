/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017-2022 Chuck Ritola
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

import java.util.Properties;

public class MavenBuildInformation implements BuildInformation {
    private static final String NOT_AVAILABLE_BUILD_ID      = "${buildNumber}";
    private static final String NOT_AVAILABLE_BRANCH_NAME   = "${scmBranch}";
    private static final String PROPERTIES_RESOURCE_PATH    = "buildNumber.properties";
    
    private Properties properties;

    @Override
    public String getUniqueBuildId() {
	final String result = getProperties().getProperty("git-sha-1");
	if( result == null || result.contentEquals(NOT_AVAILABLE_BUILD_ID))
	    return "[build ID not available]";
	else
	    return result;
    }//end getUniqueBuildId()

    @Override
    public String getBranch() {
	final String result = getProperties().getProperty("branch");
	if( result == null || result.contentEquals(NOT_AVAILABLE_BRANCH_NAME))
	    return "[branch ID not available]";
	else
	    return result;
    }//end getBranch()
    
    protected Properties getProperties() {
	java.io.InputStream inputStream = null;
	if( properties == null ) {
	    inputStream = 
		    Thread.currentThread().
		    getContextClassLoader().
		    getResourceAsStream(PROPERTIES_RESOURCE_PATH);
	    try {
		if(inputStream == null)
		    throw new NullPointerException("Build properties InputStream intolerably null.");
		properties = new Properties();
		properties.load(inputStream);
	    } catch(Exception e){e.printStackTrace();}
	    if(inputStream != null) try{inputStream.close();}catch(Exception e){e.printStackTrace();}
	}//end if(null)
	return properties;
    }//end Properties

}//end MavenBuildInformation
