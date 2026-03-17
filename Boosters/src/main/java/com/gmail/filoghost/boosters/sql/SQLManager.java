/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.boosters.sql;

import java.sql.SQLException;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.filoghost.boosters.BoostersPlugin;
import com.google.common.collect.Lists;
import lombok.Cleanup;
import wild.api.mysql.MySQL;
import wild.api.mysql.SQLResult;

public class SQLManager {
	
	private static MySQL mysql;

	public static void connect(String host, int port, String database, String user, String pass) throws SQLException, ClassNotFoundException {
		mysql = new MySQL(host, port, database, user, pass);
		mysql.connect();
	}
	
	public static void close() {
		if (mysql != null) {
			mysql.close();
		}
	}
	
	public static void createTable() throws SQLException {
		mysql.update("CREATE TABLE IF NOT EXISTS " + SQLColumns.TABLE + " ("
				+ SQLColumns.ID + " INT NOT NULL AUTO_INCREMENT, "
				+ SQLColumns.PLAYER + " varchar(20) NOT NULL, "
				+ SQLColumns.PLUGIN_ID + " varchar(30) NOT NULL, "
				+ SQLColumns.MULTIPLIER + " TINYINT NOT NULL, "
				+ SQLColumns.DURATION + " INT NOT NULL, "
				+ SQLColumns.ACTIVATED_AT + " BIGINT, "
				+ "PRIMARY KEY (" + SQLColumns.ID + ")"
				+ ") ENGINE = InnoDB DEFAULT CHARSET = UTF8;");
	}
	
	/**
	 * Tutti i booster ancora da attivare o che sono attivi (e non scaduti) al momento.
	 */
	public static List<BoosterImpl> getNonExpiredBoosters(String playerName) throws SQLException {
		long now = System.currentTimeMillis();
		@Cleanup SQLResult result = mysql.preparedQuery("SELECT * FROM " + SQLColumns.TABLE + " "
				+ "WHERE " + SQLColumns.PLAYER + " = ? "
				+ "AND ("
					+ SQLColumns.ACTIVATED_AT + " IS NULL "
					+ "OR (? < " + SQLColumns.ACTIVATED_AT + " + " + SQLColumns.DURATION + ")"
				+ ");", playerName, now);
		
		List<BoosterImpl> boostersList = Lists.newArrayList();
		
		while (result.next()) {
			boostersList.add(new BoosterImpl(result));
		}

		return boostersList;
	}
	
	
	public static BoosterImpl getActiveBooster(String pluginID) throws SQLException {
		long now = System.currentTimeMillis();
		@Cleanup SQLResult result = mysql.preparedQuery("SELECT * FROM " + SQLColumns.TABLE + " "
				+ "WHERE " + SQLColumns.PLUGIN_ID + " = ? "
				+ "AND " + SQLColumns.ACTIVATED_AT + " IS NOT NULL "
				+ "AND (? < " + SQLColumns.ACTIVATED_AT + " + " + SQLColumns.DURATION + ");", pluginID, now);
		
		if (result.next()) {
			return new BoosterImpl(result);
		} else {
			return null;
		}
	}
	
	
	public static List<BoosterImpl> getAllActiveBoosters() throws SQLException {
		long now = System.currentTimeMillis();
		@Cleanup SQLResult result = mysql.preparedQuery("SELECT * FROM " + SQLColumns.TABLE + " "
				+ "WHERE " + SQLColumns.ACTIVATED_AT + " IS NOT NULL "
				+ "AND (? < " + SQLColumns.ACTIVATED_AT + " + " + SQLColumns.DURATION + ");", now);
		
		List<BoosterImpl> activeBoosters = Lists.newArrayList();
		
		mainLoop: while (result.next()) {
			BoosterImpl booster = new BoosterImpl(result);
			
			for (BoosterImpl alreadyInsertedBooster : activeBoosters) {
				if (alreadyInsertedBooster.getPluginID().equals(booster.getPluginID())) {
					try {
						throw new IllegalStateException("Multiple boosters active at the same time for plugin ID " + booster.getPluginID());
					} catch (Exception e) {
						e.printStackTrace();
					}
					continue mainLoop;
				}
			}
			
			activeBoosters.add(booster);
		}
		
		return activeBoosters;
	}
	
	
	public static void createBooster(String playerName, String pluginID, int multiplier, long durationMillis) throws SQLException {
		mysql.preparedUpdate("INSERT INTO " + SQLColumns.TABLE + " "
				+ "(" + SQLColumns.PLAYER + ", " + SQLColumns.PLUGIN_ID + ", " + SQLColumns.MULTIPLIER + ", " + SQLColumns.DURATION + ") "
				+ "VALUES (?, ?, ?, ?);",
				playerName, pluginID, multiplier, durationMillis);
		
		DBCache.invalidate(playerName);
	}
	

	public static boolean activateBooster(Player player, int id) throws SQLException {
		@Cleanup SQLResult result = mysql.preparedQuery("SELECT * FROM " + SQLColumns.TABLE + " WHERE " + SQLColumns.ID + " = ?;", id);
		if (!result.next()) {
			player.sendMessage(ChatColor.RED + "Errore: ID booster non trovato.");
			return false;
		}
		
		BoosterImpl booster = new BoosterImpl(result);
		if (!booster.getPlayerName().equalsIgnoreCase(player.getName())) {
			player.sendMessage(ChatColor.RED + "Errore: questo booster non ti appartiene.");
			return false;
		}
		if (booster.wasActivated()) {
			player.sendMessage(ChatColor.RED + "Errore: questo booster è già stato attivato.");
			return false;
		}
		
		if (!BoostersPlugin.instance.registeredPluginsIDs.contains(booster.getPluginID())) {
			player.sendMessage(ChatColor.RED + "Errore: non puoi attivare il booster in questa modalità.");
			return false;
		}
		
		if (getActiveBooster(booster.getPluginID()) != null) {
			player.sendMessage(ChatColor.RED + "Errore: c'è già un booster attivo in questa modalità.");
			return false;
		}
		
		long now = System.currentTimeMillis();
		mysql.preparedUpdate("UPDATE " + SQLColumns.TABLE + " SET " + SQLColumns.ACTIVATED_AT + " = ? WHERE " + SQLColumns.ID + " = ?;", now, id);
		booster.setActivatedAt(now);
		
		DBCache.invalidate(player.getName());
		
		BoostersPlugin.instance.activeBoostersByPluginID.put(booster.getPluginID(), booster);
		return true;
	}
	

	public static void checkConnection() {
		mysql.isConnectionValid();
	}

}
