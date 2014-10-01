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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import com.jni.annotation.JNIClass;
import com.jni.annotation.JNIMethod;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;
import com.sun.tools.javah.Gen;
import com.sun.tools.javah.Mangle;
import com.sun.tools.javah.TypeSignature;
import com.sun.tools.javah.Util;

public class JNIGenerator extends Gen {

	public JNIGenerator(RootDoc root) {
		super(root);
	}

	@Override
	public String getIncludes() {
		return "#include <JNIManagedPeer.h>" + lineSeparator +
				"#include <jni.h>";
	}

	@Override
	protected String baseFileName(ClassDoc clazz) {
		return super.baseFileName(clazz) + "ManagedPeer";
	}
	
	@Override
	public void writeDeclaration(OutputStream o, ClassDoc clazz) {
		try {
			String cname = baseFileName(clazz);
			PrintWriter pw = wrapWriter(o);

			/* Get the desired namespace for this peer class */
			String[] namespace = getNamespace(clazz);
			pw.println(cppNamespaceBegin(namespace));
			pw.println();

			/* All ManagedPeer classes derive from the base JNI::ManagedPeer class */
			pw.println("class " + cname + " : public ::JNI::ManagedPeer");
			pw.println("{");
			pw.println("public:");
			pw.println("\t" + "explicit " + cname + "(jobject object);");
			pw.println("\t~" + cname + "()");
			pw.println();
			pw.println("\t" + "static jclass GetClass();");
			pw.println();

			/* Write declarations for methods marked with the JNIMethod annotation. */
			MethodDoc[] classmethods = clazz.methods();
			for (MethodDoc method : classmethods) {
				Annotation jniMethod = getAnnotation(clazz, method, JNIMethod.class);
				if (jniMethod != null) {
					String modifiers = (isStatic(method) ? "static " : "");
					String returnType = getReturnType(method);
					String methodName = getMethodName(method);
					String argumentSignature = getArgumentsSignature(method, /*includeTypes:*/ true);
					
					pw.println("\t" + modifiers + returnType + " " + methodName + "(" + argumentSignature + ");");
				}
			}

			pw.println("};");
			pw.println();

			/* Close the namespace */
			pw.println(cppNamespaceEnd(namespace));
		} catch (ClassNotFoundException e) {
			Util.error("jni.sigerror", e.getMessage());
		}
	}

	@Override
	public void writeDefinition(OutputStream o, ClassDoc clazz) {
		try {
			String cname = baseFileName(clazz);
			PrintWriter pw = wrapWriter(o);
			TypeSignature typeSignature = new TypeSignature(root);

			/* Get the desired namespace for this peer class */
			String[] namespace = getNamespace(clazz);
			pw.println(cppNamespaceBegin(namespace));
			pw.println();

			/* Constructor with Java object */
			pw.println(cname + "::" + cname + "(jobject object)");
			pw.println("\t" + ": ::JNI::ManagedPeer(object)");
			pw.println("{");
			pw.println("}");
			pw.println();

			/* Destructor */
			pw.println(cname + "::~" + cname + "()");
			pw.println("{");
			pw.println("}");
			pw.println();

			/* static GetClass method - uses a static "ref counted" JClass variable to read the Java class once */
			pw.println("jclass " + cname + "::GetClass()");
			pw.println("{");
			pw.println("\t" + "static ::JNI::JClass clazz(\"" + typeSignature.getTypeSignature(clazz) + "\");");
			pw.println("\t" + "return clazz;");
			pw.println("}");
			pw.println();

			/* Write definitions for methods marked with the JNIMethod annotation. */
			MethodDoc[] classmethods = clazz.methods();
			for (MethodDoc method : classmethods) {
				Annotation jniMethod = getAnnotation(clazz, method, JNIMethod.class);
				if (jniMethod != null) {
					String returnType = getReturnType(method);
					String methodName = getMethodName(method);
					String argumentSignature = getArgumentsSignature(method, /*includeTypes:*/ true);

					String methodSimpleName = method.name();
					String methodSignature = typeSignature.getTypeSignature(method.signature(), method.returnType());

					/* Method signature */
					pw.println(returnType + " " + cname + "::" + methodName + "(" + argumentSignature + ")");
					pw.println("{");

					/* Static variable to compute the jmethodID once on first use */
					pw.println("\t" + "static jmethodID methodID(GetMethodID(GetClass(), \"" + methodSimpleName + "\", \"" + methodSignature + "\"));");

					/* Generate the code to call the Java method. */
					pw.print("\t");
					pw.print(getCallSignature(method));
					pw.print("(");

					/* If the method is not static, we need a Java instance to invoke */
					if (!isStatic(method))
						pw.print("Object(), ");
					pw.print("methodID");

					/* If the method has parameters, we need to forward the parameters */
					String arguments = getArgumentsSignature(method, /*includeTypes:*/ false);
					if (arguments != null && !arguments.isEmpty())
						pw.print(", " + arguments);
					pw.println(");");

					pw.println("}");
					pw.println();
				}
			}

			/* Close the namespace */
			pw.println(cppNamespaceEnd(namespace));
		} catch (ClassNotFoundException e) {
			Util.error("jni.sigerror", e.getMessage());
		}
	}

	protected final Annotation getAnnotation(ClassDoc clazz, Class annotation) throws ClassNotFoundException {
		Class runtimeClazz = Class.forName(clazz.qualifiedName());
		return runtimeClazz.getAnnotation(annotation);
	}
	
	protected final Annotation getAnnotation(ClassDoc clazz, MethodDoc method, Class annotation) throws ClassNotFoundException {
		Class runtimeClazz = Class.forName(clazz.qualifiedName());
		for (Method runtimeMethod : runtimeClazz.getMethods()) {
			if (runtimeMethod.getName() == method.name()) {
				return runtimeMethod.getAnnotation(annotation);
			}
		}
		return null;
	}
	
	protected final String[] getNamespace(ClassDoc clazz) throws ClassNotFoundException {
		JNIClass jniClass = (JNIClass) getAnnotation(clazz, JNIClass.class);
		if (jniClass == null)
			Util.bug("tried.to.define.non.annotated.class");
		
		String namespace = jniClass.value();
		if (namespace == null)
			Util.error("JNIClass.does.not.define.namespace", clazz.qualifiedName());
		
		return namespace.split("\\.");
	}
	
	protected final String cppNamespaceBegin(String[] namespace) {
		StringBuffer buffer = new StringBuffer();
		for (String ns : namespace) {
			buffer.append("namespace " + ns + " { ");
		}
		return buffer.toString();
	}
	
	protected final String cppNamespaceEnd(String[] namespace) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < namespace.length; i++) {
			buffer.append("}");
		}
		buffer.append(" // namespace ");
		for (int i = 0; i < namespace.length; i++) {
			buffer.append(namespace[i]);
			if (i+1 < namespace.length)
				buffer.append(".");
		}
		return buffer.toString();
	}

	protected final boolean isVoid(MethodDoc method) {
		Type returnType = method.returnType();
		return (jniType(returnType) == "void");
	}
	
	protected final boolean isStatic(MethodDoc method) {
		return method.isStatic();
	}
	
	protected final String getReturnType(MethodDoc method) {
		Type returnType = method.returnType();
		return jniType(returnType);
	}
	
	protected final String getMethodName(MethodDoc method) {
		return Mangle.mangle(method.name(), Mangle.Type.FIELDSTUB);
	}
	
	protected final String getArgumentsSignature(MethodDoc method, boolean includeTypes) {
		StringBuffer signature = new StringBuffer();

		/* Write out the method parameters */
		Parameter[] paramArgs = method.parameters();
		for (int i = 0; i < paramArgs.length; i++) {
			Parameter param = paramArgs[i];
			if (includeTypes)
			{
				Type paramType = param.type();
				signature.append(jniType(paramType) + " ");
			}
			signature.append(param.name());
			if (i+1 < paramArgs.length)
				signature.append(", ");
		}

		return signature.toString();
	}
	
	private final String getCallSignature(MethodDoc method, String baseSignature) {
		return String.format("Env().Call%s%sMethod", isStatic(method) ? "Static" : "", baseSignature);
	}
	
	protected final String getCallSignature(MethodDoc method) {
		String returnType = getReturnType(method);
		
		String baseSignature = null;
		boolean needsCast = false;
		boolean needsReturn = true;

		if (returnType == "void") {
			baseSignature = "Void";
			needsReturn = false;
		} else if (returnType == "jboolean") {
			baseSignature = "Boolean";
		} else if (returnType == "jbyte") {
			baseSignature = "Byte";
		} else if (returnType == "jchar") {
			baseSignature = "Char";
		} else if (returnType == "jshort") {
			baseSignature = "Short";
		} else if (returnType == "jint") {
			baseSignature = "Int";
		} else if (returnType == "jlong") {
			baseSignature = "Long";
		} else if (returnType == "jfloat") {
			baseSignature = "Float";
		} else if (returnType == "jdouble") {
			baseSignature = "Double";
		} else if (returnType == "jobject") {
			baseSignature = "Object";
		} else { // jclass, jstring, jthrowable, or j*Array-types
			baseSignature = "Object";
			needsCast = true;
		}

		StringBuffer signature = new StringBuffer();
		if (needsReturn)
			signature.append("return ");
		if (needsCast)
			signature.append("(" + returnType + ")");
		signature.append(getCallSignature(method, baseSignature));

		return signature.toString();
	}

	protected final String jniType(Type t) {
		String elmT = t.typeName();
		ClassDoc throwable = root.classNamed("java.lang.Throwable");
		ClassDoc jClass = root.classNamed("java.lang.Class");
		ClassDoc tclassDoc = t.asClassDoc();

		if ((t.dimension()).indexOf("[]") != -1) {
			if ((t.dimension().indexOf("[][]") != -1) || (tclassDoc != null)) return "jobjectArray";
			else if (elmT.equals("boolean")) return "jbooleanArray";
			else if (elmT.equals("byte")) return "jbyteArray";
			else if (elmT.equals("char")) return "jcharArray";
			else if (elmT.equals("short")) return "jshortArray";
			else if (elmT.equals("int")) return "jintArray";
			else if (elmT.equals("long")) return "jlongArray";
			else if (elmT.equals("float")) return "jfloatArray";
			else if (elmT.equals("double")) return "jdoubleArray";
		} else {
			if (elmT.equals("void")) return "void";
			else if (elmT.equals("String")) return "jstring";
			else if (elmT.equals("boolean")) return "jboolean";
			else if (elmT.equals("byte")) return "jbyte";
			else if (elmT.equals("char")) return "jchar";
			else if (elmT.equals("short")) return "jshort";
			else if (elmT.equals("int")) return "jint";
			else if (elmT.equals("long")) return "jlong";
			else if (elmT.equals("float")) return "jfloat";
			else if (elmT.equals("double")) return "jdouble";
			else if (tclassDoc != null) {
				if (tclassDoc.subclassOf(throwable)) return "jthrowable";
				else if (tclassDoc.subclassOf(jClass)) return "jclass";
				else return "jobject";
			}
		}

		Util.bug("jni.unknown.type");
		return null; /* dead code. */
	}
}
