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
package com.l2jserver.gameserver.script.faenor;

import static com.l2jserver.gameserver.config.Configuration.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.script.ScriptContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.script.Parser;
import com.l2jserver.gameserver.script.ParserNotCreatedException;
import com.l2jserver.gameserver.script.ScriptDocument;
import com.l2jserver.gameserver.script.ScriptEngine;
import com.l2jserver.gameserver.util.file.filter.XMLFilter;

/**
 * @author Luis Arias
 */
public class FaenorScriptEngine extends ScriptEngine {
	
	private static final Logger LOG = LoggerFactory.getLogger(FaenorScriptEngine.class);
	
	public static final String PACKAGE_DIRECTORY = "data/faenor/";
	
	protected FaenorScriptEngine() {
		final File packDirectory = new File(server().getDatapackRoot(), PACKAGE_DIRECTORY);
		final File[] files = packDirectory.listFiles(new XMLFilter());
		if (files != null) {
			for (File file : files) {
				try (InputStream in = new FileInputStream(file)) {
					parseScript(new ScriptDocument(file.getName(), in), null);
				} catch (IOException ex) {
					LOG.warn("There has been an error parsing Faenor XMLs!", ex);
				}
			}
		}
	}
	
	public void parseScript(ScriptDocument script, ScriptContext context) {
		Node node = script.getDocument().getFirstChild();
		String parserClass = "faenor.Faenor" + node.getNodeName() + "Parser";
		
		Parser parser = null;
		try {
			parser = createParser(parserClass);
		} catch (ParserNotCreatedException ex) {
			LOG.warn("No parser registered for script {}!", parserClass, ex);
		}
		
		if (parser == null) {
			LOG.warn("Unknown script type {}!", script.getName());
			return;
		}
		
		try {
			parser.parseScript(node, context);
			LOG.info("Loaded {} successfully.", script.getName());
		} catch (Exception ex) {
			LOG.warn("Script parsing failed!", ex);
		}
	}
	
	public static FaenorScriptEngine getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final FaenorScriptEngine INSTANCE = new FaenorScriptEngine();
	}
}
