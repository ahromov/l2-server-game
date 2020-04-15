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
package com.l2jserver.gameserver.config.converter;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.aeonbits.owner.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pattern Converter.
 * @author Zoey76
 * @version 2.6.1.0
 */
public class PatternConverter implements Converter<Pattern> {
	
	private static final Logger LOG = LoggerFactory.getLogger(PatternConverter.class);
	
	private static final Pattern DEFAULT_PATTERN = Pattern.compile("[a-zA-Z0-9]*");
	
	@Override
	public Pattern convert(Method method, String input) {
		try {
			return Pattern.compile(input);
		} catch (Exception ex) {
			LOG.error("Error creating pattern {}!", input, ex);
			return DEFAULT_PATTERN;
		}
	}
}
