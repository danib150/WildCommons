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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import wild.api.util.CaseInsensitiveMap;

public class TimeUtils {
	
	private static final int DEFAULT_TIME_UNITS_SHOWN = 2;
	private static final Pattern TIME_FORMAT_PATTERN = Pattern.compile("([0-9]+)([a-zA-Z]+)");
	
	public static final long
			SECOND_MILLIS = 1000,
			MINUTE_MILLIS = SECOND_MILLIS * 60,
			HOUR_MILLIS = MINUTE_MILLIS * 60,
			DAY_MILLIS = HOUR_MILLIS * 24,
			WEEK_MILLIS = DAY_MILLIS * 7,
			MONTH_MILLIS = DAY_MILLIS * 30,
			YEAR_MILLIS = DAY_MILLIS * 365;
	
	
	private static Map<String, Long> unitsMillisMap;
	static {
		unitsMillisMap = new CaseInsensitiveMap<String, Long>();
		multiPut(unitsMillisMap, YEAR_MILLIS, "y", "year", "years");
		multiPut(unitsMillisMap, MONTH_MILLIS, "mo", "month", "months");
		multiPut(unitsMillisMap, WEEK_MILLIS, "w", "week", "weeks");
		multiPut(unitsMillisMap, DAY_MILLIS, "d", "day", "days");
		multiPut(unitsMillisMap, HOUR_MILLIS, "h", "hour", "hours");
		multiPut(unitsMillisMap, MINUTE_MILLIS, "m", "minute", "minutes");
		multiPut(unitsMillisMap, SECOND_MILLIS, "s", "second", "seconds");
	}
	
	
	private static void multiPut(Map<String, Long> map, long value, String... keys) {
		for (String key : keys) {
			map.put(key, value);
		}
	}
	
	
	public static long readTimespan(String input) throws InvalidTimeFormatException {
		Matcher matcher = TIME_FORMAT_PATTERN.matcher(input);
		if (!matcher.matches()) {
			throw new InvalidTimeFormatException("Il formato del tempo non è valido (deve essere numero + unità di tempo): " + input);
		}
		
		String valueString = matcher.group(1);
		String unitString = matcher.group(2);
		
		int unitValue;
		
		try {
			unitValue = Integer.parseInt(valueString);
		} catch (NumberFormatException e) {
			throw new InvalidTimeFormatException("Numero non valido: " + input);
		}
		
		if (unitValue <= 0) {
			throw new InvalidTimeFormatException("Il numero deve essere maggiore di zero: " + input);
		}
		
		if (unitValue > 1000) {
			throw new InvalidTimeFormatException("Il numero è troppo grande (usa un'unità di tempo più grande): " + input);
		}
		
		if (!unitsMillisMap.containsKey(unitString)) {
			throw new InvalidTimeFormatException("Unità di tempo non riconosciuta: " + input);
		}
		
		return unitsMillisMap.get(unitString) * unitValue;
	}
	
	public static String formatTimespan(long diff) {
		return formatTimespan(diff, DEFAULT_TIME_UNITS_SHOWN);
	}
	
	public static String formatTimespan(long diff, int maxTimeUnits) {
		if (diff % 1000 != 0) {
			// Arrotondamento per eccesso
			diff += (1000 - (diff % 1000));
		}
		
		long seconds, minutes, hours, days, weeks, months, years;
		seconds = minutes = hours = days = weeks = months = years = 0;
		
		if (diff >= YEAR_MILLIS) {
			years = diff / YEAR_MILLIS;
			diff %= YEAR_MILLIS;
		}
		
		if (diff >= MONTH_MILLIS) {
			months = diff / MONTH_MILLIS;
			diff %= MONTH_MILLIS;
		}
		
		if (diff >= WEEK_MILLIS) {
			weeks = diff / WEEK_MILLIS;
			diff %= WEEK_MILLIS;
		}
		
		if (diff >= DAY_MILLIS) {
			days = diff / DAY_MILLIS;
			diff %= DAY_MILLIS;
		}
		
		if (diff >= HOUR_MILLIS) {
			hours = diff / HOUR_MILLIS;
			diff %= HOUR_MILLIS;
		}
		
		if (diff >= MINUTE_MILLIS) {
			minutes = diff / MINUTE_MILLIS;
			diff %= MINUTE_MILLIS;
		}
		
		if (diff >= SECOND_MILLIS) {
			seconds = diff / SECOND_MILLIS;
			diff %= SECOND_MILLIS;
		}
		
		return formatTimeUnits(
				maxTimeUnits,
				new NamedTimeUnit(years, "anni", "anno"),
				new NamedTimeUnit(months, "mesi", "mese"),
				new NamedTimeUnit(weeks, "settimane", "settimana"),
				new NamedTimeUnit(days, "giorni", "giorno"),
				new NamedTimeUnit(hours, "ore", "ora"),
				new NamedTimeUnit(minutes, "minuti", "minuto"),
				new NamedTimeUnit(seconds, "secondi", "secondo"));
	}
	
	private static String formatTimeUnits(int maxTimeUnits, NamedTimeUnit... namedTimeUnits) {
		StringBuilder output = new StringBuilder();
		
		int count = 0;
		for (NamedTimeUnit unit : namedTimeUnits) {
			
			if (count >= maxTimeUnits) break;
			
			if (unit.getValue() != 0) {
				count++;
				
				if (output.length() > 0) {
					output.append(" ");
				}
				
				output.append(unit.getValue());
				output.append(" ");
				
				if (unit.getValue() == 1) {
					output.append(unit.getSingular());
				} else {
					output.append(unit.getPlural());
				}
			}
		}
		
		if (count == 0) {
			return formatTimeUnits(1, new NamedTimeUnit(0, "secondi", "secondo"));
		}
		
		return output.toString();
	}

	
	@AllArgsConstructor
	@Getter
	private static class NamedTimeUnit {
		
		private long value;
		private String plural, singular;
		
	}


}
