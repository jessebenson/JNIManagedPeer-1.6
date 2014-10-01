/*
 * Copyright 2014 Jesse Benson
 * 
 * This code is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this work. If not, see http://www.gnu.org/licenses/.
 */

package com.jni.tools;

import com.sun.javadoc.*;
import com.sun.tools.javah.Gen;
import com.sun.tools.javah.JNI;
import com.sun.tools.javah.Util;

import java.io.*;

/**
 * A doclet to parse and execute commandline options.
 */
public class MainDoclet {

	public static String odir;
	public static boolean force = false;

	/**
	 * Entry point.
	 */
	public static boolean start(RootDoc root) {

		//Command line options.
		String [][] cmdoptions = root.options();
		// Classes specified on command line.
		ClassDoc[] classes = root.classes();
		Gen g = new JNI(root);

		validateOptions(cmdoptions);

		if (odir == null) {
			Util.error("err.no.dir.specified");
		}

		/*
		 * Arrange for output destination.
		 */
		g.setOutDir(odir);

		/*
		 * Force set to false will turn off smarts about checking file
		 * content before writing.
		 */
		g.setForce(force);

		if (classes.length == 0) {
			Util.error("no.classes.specified");
		}

		/*
		 * Set classes.
		 */
		g.setClasses(classes);

		try {
			g.run();
		} catch (ClassNotFoundException cnfe) {
			Util.error("class.not.found", cnfe.getMessage());
		} catch (IOException ioe) {
			Util.error("io.exception", ioe.getMessage());
		}

		return true;
	}

	/**
	 * Required doclet method.
	 */
	public static int optionLength(String option) {
		if (option.equals("-d")) {
			return 2;
		} else if (option.equals("-help")) {
			return 1;
		} else if (option.equals("--help")) {
			return 1;
		} else if (option.equals("-?")) {
			return 1;
		} else if (option.equals("-h")) {
			return 1;
		} else if (option.equals("-version")) {
			return 1;
		} else if (option.equals("-force")) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * Parse the command line options.
	 */
	public static void validateOptions(String cmdoptions[][]) {
		/* Default values for options, overridden by user options. */
		String bootcp = System.getProperty("sun.boot.class.path");
		String usercp = System.getProperty("env.class.path");

		for (int p = 0; p < cmdoptions.length; p++) {
			if (cmdoptions[p][0].equals("-d")) {
				odir = cmdoptions[p][1];
			} else if (cmdoptions[p][0].equals("-verbose")) {
				Util.verbose = true;
			} else if ((cmdoptions[p][0].equals("-help"))
					|| (cmdoptions[p][0].equals("--help"))
					|| (cmdoptions[p][0].equals("-?"))
					|| (cmdoptions[p][0].equals("-h"))) {
				Util.usage(0);
			} else if (cmdoptions[p][0].equals("-version")) {
				Util.version();
			} else if (cmdoptions[p][0].equals("-force")) {
				force = true;
			} else if (cmdoptions[p][0].equals("-classpath")) {
				usercp = cmdoptions[p][1];
			} else if (cmdoptions[p][0].equals("-bootclasspath")) {
				bootcp = cmdoptions[p][1];
			} else if ((cmdoptions[p][0].charAt(0) == '-') && (!cmdoptions[p][0].equals("-private"))) {
				Util.error("unknown.option", cmdoptions[p][0], null, true);
			} else {
				break; /* The rest must be classes. */
			}
		}


		if (Util.verbose) {
			System.err.println("[ Search Path: " + bootcp + System.getProperty("file.separator") + usercp + " ]");
		}
	}
}
