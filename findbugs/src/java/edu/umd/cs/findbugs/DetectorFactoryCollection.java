/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2003, University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.findbugs;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * The DetectorFactoryCollection stores all of the DetectorFactory objects
 * used to create the Detectors which implement the various analyses.
 * It is a singleton class.
 *
 * @see DetectorFactory
 * @author David Hovemeyer
 */
public class DetectorFactoryCollection {
	private ArrayList<DetectorFactory> factoryList = new ArrayList<DetectorFactory>();
	private HashMap<String, DetectorFactory> factoriesByName = new HashMap<String, DetectorFactory>();

	private static DetectorFactoryCollection theInstance;
	private static final Object lock = new Object();

	/**
	 * Constructor.
	 */
	private DetectorFactoryCollection() {
		loadPlugins();
	}

	/**
	 * Get the single instance of DetectorFactoryCollection.
	 */
	public static DetectorFactoryCollection instance() {
		synchronized (lock) {
			if (theInstance == null)
				theInstance = new DetectorFactoryCollection();
			return theInstance;
		}
	}

	/**
	 * Return an Iterator over the DetectorFactory objects for all
	 * registered Detectors.
	 */
	public Iterator<DetectorFactory> factoryIterator() {
		return factoryList.iterator();
	}

	/**
	 * Look up a DetectorFactory by its short name.
	 * @param name the short name
	 * @return the DetectorFactory, or null if there is no factory with that short name
	 */
	public DetectorFactory getFactory(String name) {
		return factoriesByName.get(name);
	}

	/**
	 * Register a DetectorFactory.
	 */
	private void registerDetector(DetectorFactory factory)  {
		String detectorName = factory.getShortName();
		factoryList.add(factory);
		factoriesByName.put(detectorName, factory);
	}

	/**
	 * Load all plugins.
	 * Uses the "findbugs.home" property to determine where
	 * FindBugs is installed.
	 */
	private void loadPlugins() {
		// Load all detector plugins.
	
		String homeDir = System.getProperty("findbugs.home");
		if (homeDir == null) {
			System.err.println("Error: The findbugs.home property is not set!");
			System.exit(1);
		}
	
		File pluginDir = new File(homeDir + File.separator + "plugin");
		File[] contentList = pluginDir.listFiles();
		if (contentList == null) {
			System.err.println("Error: The path " + pluginDir.getPath() + " does not seem to be a directory!");
			System.exit(1);
		}
	
		int numLoaded = 0;
		for (int i = 0; i < contentList.length; ++i) {
			File file = contentList[i];
			if (file.getName().endsWith(".jar")) {
				try {
					URL url = file.toURL();
					PluginLoader pluginLoader = new PluginLoader(url);
	
					// Register all of the detectors that this plugin contains
					DetectorFactory[] detectorFactoryList = pluginLoader.getDetectorFactoryList();
					for (int j = 0; j < detectorFactoryList.length; ++j)
						registerDetector(detectorFactoryList[j]);
	
					I18N i18n = I18N.instance();
	
					// Register the BugPatterns
					BugPattern[] bugPatternList = pluginLoader.getBugPatternList();
					for (int j = 0; j < bugPatternList.length; ++j)
						i18n.registerBugPattern(bugPatternList[j]);
	
					// Register the BugCodes
					BugCode[] bugCodeList = pluginLoader.getBugCodeList();
					for (int j = 0; j < bugCodeList.length; ++j)
						i18n.registerBugCode(bugCodeList[j]);
	
					++numLoaded;
				} catch (Exception e) {
					System.err.println("Warning: could not load plugin " + file.getPath() + ": " + e.toString());
				}
			}
		}
	
		//System.out.println("Loaded " + numLoaded + " plugins");
	}

}

// vim:ts=4
