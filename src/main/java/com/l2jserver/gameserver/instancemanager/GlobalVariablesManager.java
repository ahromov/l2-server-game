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
package com.l2jserver.gameserver.instancemanager;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.model.variables.AbstractVariables;

/**
 * Global Variables Manager.
 * @author xban1x
 */
public final class GlobalVariablesManager extends AbstractVariables {
	
	private static final Logger LOG = LoggerFactory.getLogger(GlobalVariablesManager.class);
	
	private static final String SELECT_QUERY = "SELECT * FROM global_variables";
	
	private static final String DELETE_QUERY = "DELETE FROM global_variables";
	
	private static final String INSERT_QUERY = "INSERT INTO global_variables (var, value) VALUES (?, ?)";
	
	protected GlobalVariablesManager() {
		restoreMe();
	}
	
	@Override
	public boolean restoreMe() {
		// Restore previous variables.
		try (var con = ConnectionFactory.getInstance().getConnection();
			var st = con.createStatement();
			var rset = st.executeQuery(SELECT_QUERY)) {
			while (rset.next()) {
				set(rset.getString("var"), rset.getString("value"));
			}
		} catch (Exception ex) {
			LOG.warn("Couldn't restore global variables!", ex);
			return false;
		} finally {
			compareAndSetChanges(true, false);
		}
		LOG.info("Loaded {} variables.", getSet().size());
		return true;
	}
	
	@Override
	public boolean storeMe() {
		// No changes, nothing to store.
		if (!hasChanges()) {
			return false;
		}
		
		try (var con = ConnectionFactory.getInstance().getConnection();
			var del = con.createStatement();
			var st = con.prepareStatement(INSERT_QUERY)) {
			// Clear previous entries.
			del.execute(DELETE_QUERY);
			
			// Insert all variables.
			for (Entry<String, Object> entry : getSet().entrySet()) {
				st.setString(1, entry.getKey());
				st.setString(2, String.valueOf(entry.getValue()));
				st.addBatch();
			}
			st.executeBatch();
		} catch (Exception ex) {
			LOG.warn("Couldn't save global variables to database!", ex);
			return false;
		} finally {
			compareAndSetChanges(true, false);
		}
		LOG.info("Stored {} variables.", getSet().size());
		return true;
	}
	
	public static final GlobalVariablesManager getInstance() {
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder {
		protected static final GlobalVariablesManager _instance = new GlobalVariablesManager();
	}
}