/*
 * Copyright (c) 2002, 2004, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


package com.sun.tools.javah;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.MissingResourceException;

/**
 * Messages, verbose and error handling support.
 *
 * For errors, the failure modes are:
 *      error -- User did something wrong
 *      bug   -- Bug has occurred in javah
 *      fatal -- We can't even find resources, so bail fast, don't localize
 *
 */
public class Util {

    /*
     * Help for verbosity.
     */
    public static boolean verbose = false;

    public static void log(String s) {
        System.out.println(s);
    }

    public static String getText(String key) {
        return getText(key, null, null);
    }

    private static String getText(String message, String a1, String a2){
        return MessageFormat.format(message, new Object[] { a1, a2 });
    }

    /*
     * Usage message.
     */
    public static void usage(int exitValue) {
    	PrintStream out = (exitValue == 0 ? System.out : System.err);
    	
    	out.println("JNIManagedPeer 1.6");
    	out.println();
    	out.println("Usage: java -jar JNIManagedPeer.jar [options] <classes>");
    	out.println();
    	out.println("where [options] include:");
    	out.println();
    	out.println("\t-help                 Print this help message and exit");
    	out.println("\t-classpath <path>     Path from which to load classes");
    	out.println("\t-bootclasspath <path> Path from which to load bootstrap classes");
    	out.println("\t-pch <file>           Precompiled header file to include in .cpp files (#include <file>)");
    	out.println("\t-namespace <ns>       Namespace to put the C++ managed peers in (ex: My.Namespace)");
    	out.println("\t-d <dir>              Output directory");
    	out.println("\t-version              Print version information");
    	out.println("\t-verbose              Enable verbose output");
    	out.println("\t-force                Always write output files");
    	
        System.exit(exitValue);
    }

    public static void version() {
        System.out.println("JNIManagedPeer 1.6");
        System.out.println("Java " + System.getProperty("java.version"));
        System.exit(0);
    }

    /*
     * Failure modes.
     */
    public static void bug(String key) {
        bug(key, null);
    }

    public static void bug(String key, Exception e) {
        if (e != null)
            e.printStackTrace();
        System.err.println(getText(key));
        System.exit(11);
    }

    public static void error(String key) {
        error(key, null);
    }

    public static void error(String key, String a1) {
        error(key, a1, null);
    }

    public static void error(String key, String a1, String a2) {
        error(key, a1, a2, false);
    }

    public static void error(String key, String a1, String a2, boolean showUsage) {
        System.err.println("Error: " + getText(key, a1, a2));
        if (showUsage)
            usage(15);
        System.exit(15);
    }

    private static void fatal(String msg) {
        fatal(msg, null);
    }

    private static void fatal(String msg, Exception e) {
        if (e != null) {
            e.printStackTrace();
        }
        System.err.println(msg);
        System.exit(10);
    }
}
