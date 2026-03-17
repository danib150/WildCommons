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
package com.gmail.filoghost.boosters.api;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.filoghost.boosters.BoostersPlugin;
import com.gmail.filoghost.boosters.menu.BoostersMenu;
import com.gmail.filoghost.boosters.sql.BoosterImpl;
import com.gmail.filoghost.boosters.sql.DBCache;

import lombok.NonNull;

public class BoostersAPI {
	
	
	/**
	 * Permette ai giocatori di attivare tutti i booster per un determinato plugin (da usare in onEnable).
	 */
	public static void registerPluginID(@NonNull String pluginID) {
		checkPluginInitialized();
		BoostersPlugin.instance.registeredPluginsIDs.add(pluginID);
	}


	/**
	 * Rimuove il permesso di attivare i booster per un determinato plugin (da usare in onDisable).
	 */
	public static void unregisterPluginID(@NonNull String pluginID) {
		checkPluginInitialized();
		BoostersPlugin.instance.registeredPluginsIDs.remove(pluginID);
	}
	
	
	/**
	 * Apre il menu dei booster per un giocatore.
	 */
	public static void openBoostersMenu(@NonNull Player player) {
		DBCache.getNonExpiredBoosters(player,
			() -> {
				player.sendMessage(ChatColor.GRAY + "Caricamento in corso...");
			},
			(List<BoosterImpl> boostersList) -> {
				new BoostersMenu(boostersList).open(player);
			},
			(Exception error) -> {
				player.sendMessage(ChatColor.RED + "Si è verificato un errore durante il caricamento dei booster. Se persiste, contatta lo staff.");
				error.printStackTrace();
			}
		);
	}
	
	
	/**
	 * Restituisce il booster attivo per il determinato plugin oppure null.
	 * Il booster restituito non è scaduto al momento della chiamata alla funzione.
	 */
	public static Booster getActiveBooster(@NonNull String pluginID) {
		checkPluginInitialized();
		BoosterImpl booster = BoostersPlugin.instance.activeBoostersByPluginID.get(pluginID);
		long now = System.currentTimeMillis();
		
		if (booster != null && !booster.isExpired(now)) {
			return booster;
		} else {
			return null;
		}
	}
	
	
	/**
	 * Il messaggio da aggiungere in chat dopo "+10 Coins" per esempio.
	 * Diventa "+10 Coins (Booster ...)"
	 */
	public static String getBoosterMessageSuffix(Booster booster) {
		return booster != null ? " (Booster x" + booster.getMultiplier() + " di " + booster.getPlayerName() + ")" : "";
	}
	
	
	
	private static void checkPluginInitialized() {
		if (BoostersPlugin.instance == null) {
			throw new IllegalStateException("Plugin not yet initialized");
		}
	}

}
