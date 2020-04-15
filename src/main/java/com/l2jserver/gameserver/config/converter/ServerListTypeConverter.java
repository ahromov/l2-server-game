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

import org.aeonbits.owner.Converter;
import org.apache.logging.log4j.util.Strings;

/**
 * Server List Type Converter.
 * @author Zoey76
 * @version 2.6.1.0
 */
public class ServerListTypeConverter implements Converter<Integer> {
	
	@Override
	public Integer convert(Method method, String input) {
		if (Strings.isBlank(input)) {
			return 1;
		}
		return getServerTypeId(input.split(","));
	}
	
	public static int getServerTypeId(String[] serverTypes) {
		int typeId = 0;
		for (String serverType : serverTypes) {
			switch (serverType.trim().toLowerCase()) {
				default:
				case "normal": {
					typeId |= 0x01;
					break;
				}
				case "relax": {
					typeId |= 0x02;
					break;
				}
				case "test": {
					typeId |= 0x04;
					break;
				}
				case "nolabel": {
					typeId |= 0x08;
					break;
				}
				case "restricted": {
					typeId |= 0x10;
					break;
				}
				case "event": {
					typeId |= 0x20;
					break;
				}
				case "free": {
					typeId |= 0x40;
					break;
				}
			}
		}
		return typeId;
	}
}
