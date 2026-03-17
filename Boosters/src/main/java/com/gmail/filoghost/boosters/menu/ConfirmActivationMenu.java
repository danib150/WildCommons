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

import org.bukkit.ChatColor;
import org.bukkit.Material;

import com.gmail.filoghost.boosters.sql.BoosterImpl;
import com.gmail.filoghost.boosters.sql.SQLManager;
import com.gmail.filoghost.boosters.sql.SQLTask;

import wild.api.menu.IconBuilder;
import wild.api.menu.IconMenu;

public class ConfirmActivationMenu extends IconMenu {
	
	
	public ConfirmActivationMenu(BoosterImpl booster) {
		super("Conferma", 3);
		setIcon(2, 2, new IconBuilder(Material.STAINED_GLASS_PANE).dataValue(5).name(ChatColor.GREEN + "Conferma attivazione").closeOnClick(true).clickHandler(clicker -> {
			
			SQLTask.submitAsync(() -> {
				// Anche se per caso venisse attivato 2 volte, non succede niente (qualche ms di differenza sul timestamp dell'attivazione)
				if (SQLManager.activateBooster(clicker, booster.getId())) {
					clicker.sendMessage(ChatColor.GREEN + "Booster attivato con successo!");
				}
			}, clicker);
			
		}).build());
		
		setIcon(8, 2, new IconBuilder(Material.STAINED_GLASS_PANE).dataValue(14).name(ChatColor.RED + "Annulla attivazione").closeOnClick(true).build());
		refresh();
	}

}
