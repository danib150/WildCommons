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

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import com.gmail.filoghost.boosters.sql.BoosterImpl;
import com.gmail.filoghost.boosters.sql.DBCache;
import com.gmail.filoghost.boosters.sql.SQLManager;

public class ActiveBoosterCheckTask implements Runnable {
	
	@Override
	public void run() {
		try {
			List<BoosterImpl> activeBoosters = SQLManager.getAllActiveBoosters();
			
			for (BoosterImpl activeBooster : activeBoosters) {
				BoosterImpl previousBooster = BoostersPlugin.instance.activeBoostersByPluginID.put(activeBooster.getPluginID(), activeBooster);
				if (previousBooster == null || activeBooster.getId() != previousBooster.getId()) {
					// Allora è stato caricato qualcosa di nuovo direttamente dal database
					// In questo modo il giocatore vede la lista dei booster correttamente aggiornata
					DBCache.invalidate(activeBooster.getPlayerName());
				}
			}
			
			// Cancella i booster non più trovati (magari sono stati annullati tramite database per qualche motivo)
			Iterator<String> keysIterator = BoostersPlugin.instance.activeBoostersByPluginID.keySet().iterator();
			mainLoop: while (keysIterator.hasNext()) {
				String oldPluginID = keysIterator.next();
				
				for (BoosterImpl activeBooster : activeBoosters) {
					if (activeBooster.getPluginID().equals(oldPluginID)) {
						continue mainLoop;
					}
				}
				
				// Non è stato trovato un booster con lo stesso plugin ID tra quelli attivi: quindi non è più attivo
				keysIterator.remove();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
