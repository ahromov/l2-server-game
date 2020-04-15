/*
 * Copyright © 2004-2020 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.scripting;

import static com.l2jserver.gameserver.config.Configuration.general;
import static com.l2jserver.gameserver.config.Configuration.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptException;

import org.mdkt.compiler.InMemoryJavaCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Script engine manager.
 * @author KenM
 * @author Zoey76
 */
public final class ScriptEngineManager {
	private static final Logger LOG = LoggerFactory.getLogger(ScriptEngineManager.class);
	
	private static final String CLASS_PATH = server().getScriptRoot().getAbsolutePath() + System.getProperty("path.separator") + System.getProperty("java.class.path");
	
	private static final String MAIN = "main";
	
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	
	private static final Class<?>[] ARG_MAIN = new Class[] {
		String[].class
	};
	
	private static final InMemoryJavaCompiler COMPILER = InMemoryJavaCompiler.newInstance() //
		.useOptions("-classpath", CLASS_PATH, "-g") //
		.ignoreWarnings();
	
	public void executeScriptList(File list) throws Exception {
		if (general().noQuests()) {
			if (!general().noHandlers()) {
				addSource(new File(server().getScriptRoot(), "com/l2jserver/datapack/handlers/MasterHandler.java"));
				LOG.info("Handlers loaded, all other scripts skipped!");
			}
			return;
		}
		
		if (list.isFile()) {
			try (FileInputStream fis = new FileInputStream(list);
				InputStreamReader isr = new InputStreamReader(fis);
				LineNumberReader lnr = new LineNumberReader(isr)) {
				String line;
				while ((line = lnr.readLine()) != null) {
					if (general().noHandlers() && line.contains("MasterHandler.java")) {
						continue;
					}
					
					String[] parts = line.trim().split("#");
					
					if ((parts.length > 0) && !parts[0].isEmpty() && (parts[0].charAt(0) != '#')) {
						line = parts[0];
						
						if (line.endsWith("/**")) {
							line = line.substring(0, line.length() - 3);
						} else if (line.endsWith("/*")) {
							line = line.substring(0, line.length() - 2);
						}
						
						final File file = new File(server().getScriptRoot(), line);
						if (file.isDirectory() && parts[0].endsWith("/**")) {
							executeAllScriptsInDirectory(file, true);
						} else if (file.isDirectory() && parts[0].endsWith("/*")) {
							executeAllScriptsInDirectory(file, false);
						} else if (file.isFile()) {
							addSource(file);
						} else {
							LOG.warn("Failed loading: ({}) @ {}:{} - Reason: doesnt exists or is not a file.", file.getCanonicalPath(), list.getName(), lnr.getLineNumber());
						}
					}
				}
			}
		} else {
			throw new IllegalArgumentException("Argument must be an file containing a list of scripts to be loaded");
		}
		
		final Map<String, Class<?>> classes = COMPILER.compileAll();
		for (Entry<String, Class<?>> e : classes.entrySet()) {
			runMain(e.getValue());
		}
	}
	
	private void executeAllScriptsInDirectory(File dir, boolean recurseDown) {
		if (dir.isDirectory()) {
			final File[] files = dir.listFiles();
			if (files == null) {
				return;
			}
			
			for (File file : files) {
				if (file.isDirectory() && recurseDown) {
					if (general().debug()) {
						LOG.info("Entering folder: {}", file.getName());
					}
					executeAllScriptsInDirectory(file, recurseDown);
				} else if (file.isFile()) {
					addSource(file);
				}
			}
		} else {
			throw new IllegalArgumentException("The argument directory either doesnt exists or is not an directory.");
		}
	}
	
	public Class<?> compileScript(File file) {
		try (FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader reader = new BufferedReader(isr)) {
			return COMPILER.compile(getClassForFile(file), readerToString(reader));
		} catch (Exception ex) {
			LOG.warn("Error executing script!", ex);
		}
		return null;
	}
	
	public void executeScript(File file) throws Exception {
		final Class<?> clazz = compileScript(file);
		
		runMain(clazz);
	}
	
	public void executeScript(String file) throws Exception {
		executeScript(new File(server().getScriptRoot(), file));
	}
	
	public void addSource(File file) {
		if (general().debug()) {
			LOG.info("Loading Script: {}", file.getAbsolutePath());
		}
		
		try (FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader reader = new BufferedReader(isr)) {
			COMPILER.addSource(getClassForFile(file), readerToString(reader));
		} catch (Exception ex) {
			LOG.warn("Error executing script!", ex);
		}
	}
	
	private static String getClassForFile(File script) {
		final String path = script.getAbsolutePath();
		final String scpPath = server().getScriptRoot().getAbsolutePath();
		if (path.startsWith(scpPath)) {
			final int idx = path.lastIndexOf('.');
			return path.substring(scpPath.length() + 1, idx).replace('/', '.').replace('\\', '.');
		}
		return null;
	}
	
	private static void runMain(Class<?> clazz) throws Exception {
		final boolean isPublicClazz = Modifier.isPublic(clazz.getModifiers());
		final Method mainMethod = findMethod(clazz, MAIN, ARG_MAIN);
		if (mainMethod != null) {
			if (!isPublicClazz) {
				mainMethod.setAccessible(true);
			}
			
			mainMethod.invoke(null, new Object[] {
				EMPTY_STRING_ARRAY
			});
		}
	}
	
	private static String readerToString(Reader reader) throws ScriptException {
		try (BufferedReader in = new BufferedReader(reader)) {
			final StringBuilder result = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				result.append(line).append(System.lineSeparator());
			}
			return result.toString();
		} catch (IOException ex) {
			throw new ScriptException(ex);
		}
	}
	
	private static Method findMethod(Class<?> clazz, String methodName, Class<?>[] args) {
		try {
			final Method mainMethod = clazz.getMethod(methodName, args);
			final int modifiers = mainMethod.getModifiers();
			if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
				return mainMethod;
			}
		} catch (NoSuchMethodException ignored) {
		}
		return null;
	}
	
	public static ScriptEngineManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final ScriptEngineManager INSTANCE = new ScriptEngineManager();
	}
}
