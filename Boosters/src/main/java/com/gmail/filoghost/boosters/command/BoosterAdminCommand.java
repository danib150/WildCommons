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
package com.gmail.filoghost.boosters.command;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.command.CommandSender;

import com.gmail.filoghost.boosters.BoostersPlugin;
import com.gmail.filoghost.boosters.InvalidTimeFormatException;
import com.gmail.filoghost.boosters.TimeUtils;
import com.gmail.filoghost.boosters.sql.BoosterImpl;
import com.gmail.filoghost.boosters.sql.SQLManager;
import com.gmail.filoghost.boosters.sql.SQLTask;
import com.google.common.base.Joiner;

import net.md_5.bungee.api.ChatColor;
import wild.api.command.CommandFramework.Permission;
import wild.api.command.SubCommandFramework;

@Permission("boosters.admin")
public class BoosterAdminCommand extends SubCommandFramework {

	public BoosterAdminCommand() {
		super(BoostersPlugin.instance, "boosteradmin");
	}

	@Override
	public void noArgs(CommandSender sender) {
		sender.sendMessage(ChatColor.DARK_GREEN + "Lista comandi Booster Admin:");
		for (SubCommandDetails sub : this.getAccessibleSubCommands(sender)) {
			sender.sendMessage(ChatColor.GREEN + "/" + this.label + " " + sub.getName() + (sub.getUsage() != null ?  " " + sub.getUsage() : ""));
		}
	}
	
	@SubCommand("ids")
	public void ids(CommandSender sender, String label, String[] args) {
		sender.sendMessage(ChatColor.DARK_GREEN + "Plugin IDs disponibili in questa modalità: " + ChatColor.GREEN + Joiner.on(" ").join(BoostersPlugin.instance.registeredPluginsIDs));
	}

	@SubCommand("add")
	@SubCommandUsage("<player> <pluginID> <multiplier> <duration>")
	@SubCommandMinArgs(4)
	public void add(CommandSender sender, String label, String[] args) {
		String playerName = args[0];
		String pluginID = args[1];
		int multiplier = CommandValidate.getInteger(args[2]);
		CommandValidate.isTrue(2 <= multiplier && multiplier <= 100, "Il moltiplicatore deve essere tra 2 e 100");
		
		long durationMillis;
		try {
			durationMillis = TimeUtils.readTimespan(args[3]);
		} catch (InvalidTimeFormatException e) {
			throw new ExecuteException(e.getMessage());
		}
		CommandValidate.isTrue(durationMillis <= TimeUnit.DAYS.toMillis(30), "La durata non può essere maggiore di 30 giorni.");
		
		SQLTask.submitAsync(() -> {
			SQLManager.createBooster(playerName, pluginID, multiplier, durationMillis);
			sender.sendMessage(ChatColor.GREEN + "Aggiunto booster a " + playerName + ", per il plugin " + pluginID + ", moltiplicatore x" + multiplier + ", durata " + TimeUtils.formatTimespan(durationMillis) + ".");
		}, sender);
		
	}
	
	@SubCommand("view")
	@SubCommandUsage("<player>")
	@SubCommandMinArgs(1)
	public void view(CommandSender sender, String label, String[] args) {
		String playerName = args[0];
		
		SQLTask.submitAsync(() -> {
			List<BoosterImpl> boosters = SQLManager.getNonExpiredBoosters(playerName);
			if (boosters.size() > 0) {
				sender.sendMessage(ChatColor.DARK_GREEN + "Booster di " + playerName + " (" + boosters.size() + "):");
				for (BoosterImpl booster : boosters) {
					sender.sendMessage(ChatColor.GREEN + "- " + (booster.wasActivated() ? "(ATTIVO) " : "") + "Plugin " + booster.getPluginID() + ", x" + booster.getMultiplier() + ", durata " + TimeUtils.formatTimespan(booster.getDurationMillis()));
				}
			} else {
				sender.sendMessage(ChatColor.RED + playerName + " non ha nessun booster attivabile.");
			}
		}, sender);
	}
	
}
