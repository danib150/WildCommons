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
package wild.api.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;

public class CustomSkullAdapter {

	private static Field profileField = null;
	
	private GameProfile profile;
	
	private CustomSkullAdapter(String encodedTexture, UUID uuid) {
		profile = new GameProfile(uuid != null ? uuid : UUID.randomUUID(), null);
	    profile.getProperties().put("textures", new Property("textures", encodedTexture));
	}
	
	public static wild.api.item.CustomSkullAdapter fromEncodedTexture(String encodedTexture) {
		return fromEncodedTexture(encodedTexture, null);
	}
	
	public static wild.api.item.CustomSkullAdapter fromEncodedTexture(String encodedTexture, UUID uuid) {
		return new wild.api.item.CustomSkullAdapter(encodedTexture, uuid);
	}
	
	public static wild.api.item.CustomSkullAdapter fromURL(String url) {
		return fromURL(url, null);
	}
	
	public static wild.api.item.CustomSkullAdapter fromURL(String url, UUID uuid) {
		byte[] encodedData = Base64.getEncoder().encode(("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes());
		return new wild.api.item.CustomSkullAdapter(new String(encodedData), uuid);
	}
	
	public void apply(SkullMeta skullMeta) {
		try {
			if (profileField == null) {
				profileField = skullMeta.getClass().getDeclaredField("profile");
				profileField.setAccessible(true);
			}
			
			profileField.set(skullMeta, profile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
