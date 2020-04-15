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
package com.l2jserver.gameserver.idfactory;

import static com.l2jserver.gameserver.config.Configuration.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;

/**
 * Stack ID Factory.
 * @version 2.6.1.0
 */
public class StackIDFactory extends IdFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(IdFactory.class);
	
	private int _curOID;
	
	private int _tempOID;
	
	private final Stack<Integer> _freeOIDStack = new Stack<>();
	
	protected StackIDFactory() {
		super();
		_curOID = FIRST_OID;
		_tempOID = FIRST_OID;
		
		try (var con = ConnectionFactory.getInstance().getConnection()) {
			Integer[] tmp_obj_ids = extractUsedObjectIDTable();
			if (tmp_obj_ids.length > 0) {
				_curOID = tmp_obj_ids[tmp_obj_ids.length - 1];
			}
			LOG.info("Max Id = {}.", _curOID);
			
			int N = tmp_obj_ids.length;
			for (int idx = 0; idx < N; idx++) {
				N = insertUntil(tmp_obj_ids, idx, N, con);
			}
			
			_curOID++;
			LOG.info("Next usable Object Id is {}.", _curOID);
			_initialized = true;
		} catch (Exception ex) {
			LOG.error("Could not be initialized properly!", ex);
		}
	}
	
	private int insertUntil(Integer[] tmp_obj_ids, int idx, int N, Connection con) throws SQLException {
		int id = tmp_obj_ids[idx];
		if (id == _tempOID) {
			_tempOID++;
			return N;
		}
		// check these IDs not present in DB
		if (server().badIdChecking()) {
			for (String check : ID_CHECKS) {
				try (var ps = con.prepareStatement(check)) {
					ps.setInt(1, _tempOID);
					// ps.setInt(1, _curOID);
					ps.setInt(2, id);
					try (var rs = ps.executeQuery()) {
						while (rs.next()) {
							int badId = rs.getInt(1);
							LOG.error("Bad Id {} in DB found by {}!", badId, check);
							throw new RuntimeException();
						}
					}
				}
			}
		}
		
		// int hole = id - _curOID;
		int hole = id - _tempOID;
		if (hole > (N - idx)) {
			hole = N - idx;
		}
		for (int i = 1; i <= hole; i++) {
			// log.info("Free ID added " + (_tempOID));
			_freeOIDStack.push(_tempOID);
			_tempOID++;
			// _curOID++;
		}
		if (hole < (N - idx)) {
			_tempOID++;
		}
		return N - hole;
	}
	
	public static IdFactory getInstance() {
		return _instance;
	}
	
	@Override
	public synchronized int getNextId() {
		int id;
		if (!_freeOIDStack.empty()) {
			id = _freeOIDStack.pop();
		} else {
			id = _curOID;
			_curOID = _curOID + 1;
		}
		return id;
	}
	
	/**
	 * return a used Object ID back to the pool
	 * @param id
	 */
	@Override
	public synchronized void releaseId(int id) {
		_freeOIDStack.push(id);
	}
	
	@Override
	public int size() {
		return (FREE_OBJECT_ID_SIZE - _curOID) + FIRST_OID + _freeOIDStack.size();
	}
}