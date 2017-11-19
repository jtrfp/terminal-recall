/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017 Chuck Ritola
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
    
    private String     uniqueBuildId = null;
    private String     branch        = null;
    private Properties properties;

    @Override
    public String getUniqueBuildId() {
	if( uniqueBuildId == null ) {
	    final Properties properties = getProperties();
	    uniqueBuildId = properties.getProperty("git-sha-1");
	    if( uniqueBuildId != null )
		if( uniqueBuildId.equals(NOT_AVAILABLE_BUILD_ID) )
		    uniqueBuildId = null;
	}//end if(null)
	return uniqueBuildId;
    }//end getUniqueBuildId()

    @Override
    public String getBranch() {
	if( branch == null ) {
	    final Properties properties = getProperties();
	    branch = properties.getProperty("branch");
	    if( branch != null )
		if( branch.equals(NOT_AVAILABLE_BRANCH_NAME) )
		    branch = null;
	}//end if(null)
	return branch;
    }//end getBranch()
    
    protected Properties getProperties() {
	if( properties == null ) {
	    java.io.InputStream inputStream = 
		    Thread.currentThread().
		    getContextClassLoader().
		    getResourceAsStream(PROPERTIES_RESOURCE_PATH);
	    try {
		if(inputStream == null)
		    throw new NullPointerException("Build properties InputStream intolerably null.");
		properties = new Properties();
		properties.load(inputStream);
	    } catch(Exception e){e.printStackTrace();}
	}//end if(null)
	return properties;
    }//end Properties

}//end MavenBuildInformation
