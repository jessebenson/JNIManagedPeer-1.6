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

import java.util.ArrayList;
import java.util.List;

import com.sun.tools.javah.Util;

public class Main {

	public static void main(String[] args) {

		if (args.length == 0) {
			Util.usage(1);
		}

		List<String> javadocargsList = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			final int index = i;
			
			if (args[i].equals("-d")) {
				i++;
				if (i >= args.length) {
					Util.usage(1);
				} else if (args[i].charAt(0) == '-') {
					Util.error("Missing output directory in -d commandline parameter.");
				} else if ((i+1) >= args.length) {
					Util.error("No classes specified on the commandline.");
				}
			} else if (args[i].equals("-v") || args[i].equals("-verbose")) {
				if ((i+1) >= args.length) {
					Util.error("No classes specified on the commandline.");
				}
				args[i] = "-verbose";
			} else if ((args[i].equals("-help")) || (args[i].equals("--help")) || (args[i].equals("-?")) || (args[i].equals("-h"))) {
				Util.usage(0);
			} else if (args[i].equals("-version")) {
				if ((i+1) >= args.length) {
					Util.version();
				}
			} else if (args[i].equals("-pch")) {
				i++;
				if ((i+1) >= args.length) {
					Util.error("No classes specified on the commandline.");
				}
				MainDoclet.pch = args[i];
				continue;
			} else if (args[i].equals("-namespace")) {
				i++;
				if ((i+1) >= args.length) {
					Util.error("No classes specified on the commandline.");
				}
				MainDoclet.namespace = args[i];
				continue;
			} else if (args[i].equals("-force")) {
				if ((i+1) >= args.length) {
					Util.error("No classes specified on the commandline.");
				}
			} else if (args[i].equals("-classpath")) {
				i++;
				if (i >= args.length) {
					Util.usage(1);
				} else if (args[i].charAt(0) == '-') {
					Util.error("Missing classpath arguments in the -classpath commandline parameter.");
				} else if ((i+1) >= args.length) {
					Util.error("No classes specified on the commandline.");
				}
			} else if (args[i].equals("-bootclasspath")) {
				i++;
				if (i >= args.length) {
					Util.usage(1);
				} else if (args[i].charAt(0) == '-') {
					Util.error("Missing classpath arguments in the -bootclasspath commandline parameter.");
				} else if ((i+1) >= args.length) {
					Util.error("No classes specified on the commandline.");
				}
			} else if (args[i].charAt(0) == '-') {
				Util.error("Unknown option: %s", args[i], null, true);
			} else {
				//break; /* The rest must be classes. */
			}
			
			javadocargsList.add(args[index]);
			if (i != index)
				javadocargsList.add(args[i]);
		}

		/* Invoke javadoc */
		javadocargsList.add("-private");
		javadocargsList.add("-Xclasses");
		
		String[] javadocargs = new String[javadocargsList.size()];
		for (int i = 0; i < javadocargs.length; i++) {
			javadocargs[i] = javadocargsList.get(i);
		}

		int rc = com.sun.tools.javadoc.Main.execute("javadoc", "com.jni.tools.MainDoclet", javadocargs);
		System.exit(rc);
	}
}
