/*******************************************************************************
 * This file is part of the JAVA FILE DESCRIPTION TOOLKIT (JFDT)
 * A library for parsing files and mapping their data to/from java Beans.
 * ...which is now part of the JAVA TERMINAL REALITY FILE PARSERS project.
 * Copyright (c) 2017 Chuck Ritola and any contributors to these files.
 * 
 *     JFDT is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     JDFT is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with jTRFP.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.jtrfp.jfdt.v2;

/**
 * Provides support for creating new objects programmatically. This is typically used by the parser
 * when creating new objects to populate a target bean. In such a case, the target beam is checked if 
 * it is an instance of Factory then uses those Factory methods. This provides the advantage of parent-aware
 * sub-objects.
 * @author Chuck Ritola
 *
 */

public interface Factory {
    /**
     * 
     * @param objectClass
     * @return
     * @throws ClassNotFoundException if the Factory does not support creating an instance of provided class.
     * @since May 1, 2017
     */
 public <T> T newObject(Class<T> objectClass) throws ClassNotFoundException;
}
