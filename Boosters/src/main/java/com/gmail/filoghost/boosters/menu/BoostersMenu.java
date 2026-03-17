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
package com.gmail.filoghost.boosters.menu;

import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.filoghost.boosters.BoostersPlugin;
import com.gmail.filoghost.boosters.TimeUtils;
import com.gmail.filoghost.boosters.sql.BoosterImpl;

import wild.api.menu.IconBuilder;
import wild.api.menu.IconMenu;

public class BoostersMenu extends IconMenu {

	public BoostersMenu(List<BoosterImpl> boosters) {
		super("Lista Booster", divideRoundUp(boosters.size() + 1, 7) + 2); // +1 per lasciare sempre lo store nell'angolo spaziato, +2 per padding
		
		// Mostra prima quelli attivi, poi quelli da attivare
		int index = 0;
		for (BoosterImpl booster : boosters) {
			if (booster.wasActivated()) {
				displayBooster(index, booster);
				index++;
			}
		}
		for (BoosterImpl booster : boosters) {
			if (!booster.wasActivated()) {
				displayBooster(index, booster);
				index++;
			}
		}
		
		setIcon(9, getRows(), new IconBuilder(Material.WRITTEN_BOOK)
				.closeOnClick(true)
				.name(ChatColor.GOLD + "Store")
				.lore(ChatColor.GRAY + "Clicca per accedere allo store,",
					  ChatColor.GRAY + "dove è possibile comprare i booster.",
					  "",
					  ChatColor.AQUA + "store.WildAdventure.it")
				.clickHandler(clicker -> {
					clicker.sendMessage("");
					clicker.sendMessage(ChatColor.WHITE + "Clicca il link: " + ChatColor.AQUA + ChatColor.UNDERLINE + "store.WildAdventure.it");
					clicker.sendMessage("");
				})
				.build());
		
		refresh();
	}
	
	private void displayBooster(int index, BoosterImpl booster) {
		int positionX = (index % 7) + 2;
		int positionY = (index / 7) + 2;
		
		IconBuilder iconBuilder = new IconBuilder(Material.POTION);
		iconBuilder.name(ChatColor.BLUE + "Booster " + getUserFriendlyName(booster.getPluginID()));
		
		String activationLine;
		PotionEffectType potionType;
		
		if (booster.wasActivated()) {
			potionType = PotionEffectType.JUMP;
			iconBuilder.glow();
			activationLine = ChatColor.GREEN + "ATTIVO" + ChatColor.GRAY + " (Tempo rimasto: " + TimeUtils.formatTimespan(booster.getActivatedAt() + booster.getDurationMillis() - System.currentTimeMillis()) + ")";
			
		} else if (BoostersPlugin.instance.registeredPluginsIDs.contains(booster.getPluginID())) {
			potionType = PotionEffectType.WATER_BREATHING;
			activationLine = ChatColor.YELLOW + "CLICCA PER ATTIVARE";
			iconBuilder.clickHandler(clicker -> {
				new ConfirmActivationMenu(booster).open(clicker);
			});
			
		} else {
			potionType = null;
			activationLine = ChatColor.DARK_GRAY + "NON ATTIVABILE IN QUESTA MODALITA'";
		}
		
		iconBuilder.lore(
			ChatColor.GRAY + "Moltiplicatore " + getMultipliedUnit(booster.getPluginID()) + ": " + ChatColor.GOLD + "x" + booster.getMultiplier(),
			ChatColor.GRAY + "Durata: " + ChatColor.LIGHT_PURPLE + TimeUtils.formatTimespan(booster.getDurationMillis()),
			"",
			activationLine
		);
		
		iconBuilder.metaModifier(meta -> {
			if (potionType != null) {
				PotionMeta potionMeta = (PotionMeta) meta;
				potionMeta.setMainEffect(potionType);
				potionMeta.addCustomEffect(new PotionEffect(potionType, 1, 1), true); // Altrimenti non si vede graficamente
			}
		});
		
		setIcon(positionX, positionY, iconBuilder.build());
	}
	
	private String getMultipliedUnit(String pluginID) {
		switch (pluginID) {
			case "sky_wars": 	return "punti";
			default: 			return "coins";
		}
	}
	
	private String getUserFriendlyName(String pluginID) {
		switch (pluginID) {
			default: return WordUtils.capitalize(pluginID.replace("_", " "));
		}
	}

	private static int divideRoundUp(int n, int divisor) {
		return n % divisor == 0 ? (n / divisor) : ((n / divisor) + 1);
	}

}
