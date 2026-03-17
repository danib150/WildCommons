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
package com.gmail.filoghost.boosters;

import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.boosters.command.BoosterAdminCommand;
import com.gmail.filoghost.boosters.command.BoosterCommand;
import com.gmail.filoghost.boosters.sql.BoosterImpl;
import com.gmail.filoghost.boosters.sql.SQLManager;
import com.gmail.filoghost.boosters.sql.SQLTask;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.cubespace.yamler.YamlerConfigurationException;

public class BoostersPlugin extends JavaPlugin {

	public static BoostersPlugin instance;
	
	public Set<String> registeredPluginsIDs = Sets.newConcurrentHashSet();
	public Map<String, BoosterImpl> activeBoostersByPluginID = Maps.newConcurrentMap();
	
	
	@Override
	public void onEnable() {
		instance = this;
		
		if (!Bukkit.getPluginManager().isPluginEnabled("WildCommons")) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + this.getName() + "] Richiesto WildCommons!");
			setEnabled(false);
			return;
		}
		
		Settings settings;
		
		try {
			settings = new Settings(this);
			settings.init();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			setEnabled(false);
			return;
		}
		
		// Database MySQL + lettura iniziale booster attivi
		try {
			SQLManager.connect(settings.mysql_host, settings.mysql_port, settings.mysql_database, settings.mysql_user, settings.mysql_pass);
			SQLManager.createTable();
			for (BoosterImpl activeBooster : SQLManager.getAllActiveBoosters()) {
				activeBoostersByPluginID.put(activeBooster.getPluginID(), activeBooster);
			}
					
		} catch (Exception ex) {
			ex.printStackTrace();
			setEnabled(false);
			return;
		}
		
		// Task per aggiornare periodicamente i booster attivi
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new ActiveBoosterCheckTask(), 30 * 20, 30 * 20);
		
		// Task per tenere SQL attivo
		Bukkit.getScheduler().runTaskTimer(this, () -> {
			SQLTask.submitAsync(() -> {
				SQLManager.checkConnection();
			}, null);
		}, 20 * 60 * 20, 20 * 60 * 20);
		
		new BoosterAdminCommand();
		new BoosterCommand();
	}


	@Override
	public void onDisable() {
		SQLManager.close();
	}
	
	

}
