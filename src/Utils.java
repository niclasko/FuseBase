/*
 * Copyright (c) 2018 "Niclas Kjall-Ohlsson, Bjornar Fjoren"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.lang.System;
import java.util.Calendar;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.TimeZone;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class Utils {
	
	private static boolean[] reservedCharacters = new boolean[256];
	
	static {
		Arrays.fill(Utils.reservedCharacters, false);
		
		Utils.reservedCharacters[(int)'+'] = true;
		Utils.reservedCharacters[(int)'-'] = true;
		Utils.reservedCharacters[(int)'*'] = true;
		Utils.reservedCharacters[(int)'/'] = true;
		Utils.reservedCharacters[(int)'<'] = true;
		Utils.reservedCharacters[(int)'>'] = true;
		Utils.reservedCharacters[(int)'='] = true;
		Utils.reservedCharacters[(int)'^'] = true;
		Utils.reservedCharacters[(int)'('] = true;
		Utils.reservedCharacters[(int)')'] = true;
		Utils.reservedCharacters[(int)' '] = true;
		
	}
	
	public static double round(double value, int places) {
		try {
			BigDecimal bd = new BigDecimal(value);
			bd = bd.setScale(places, RoundingMode.HALF_UP);
			return bd.doubleValue();
		} catch(Exception e) {
			return Double.NaN;
		}
	}
	
	public static double parseDouble(String value) {
		try {
			return Double.parseDouble(value);
		} catch(Exception e) {
			return 0.0;
		}
	}
	
	public static int parseInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch(Exception e) {
			return 0;
		}
	}

	public static String readInputStreamToString(InputStream inputStream) throws Exception {
		StringBuilder contents = new StringBuilder();
		char[] buffer = new char[4096];
		int bytesRead = 0;

		InputStreamReader inputStreamReader =
			new InputStreamReader(inputStream, StandardCharsets.UTF_8);

		while((bytesRead = inputStreamReader.read(buffer, 0, buffer.length)) > 0) {
			contents.append(buffer, 0, bytesRead);
		}

		return contents.toString();
	}
	
	public static String getCurrentPath() {
		return ".";
	}
	
	public static int getIntegerByteSize() {
		return (int) Integer.SIZE / Byte.SIZE;
	}
	
	public static int getDoubleByteSize() {
		return (int) Double.SIZE / Byte.SIZE;
	}
	
	public static String toUpperCase(String string) {
		boolean reservedCharacterFound = false;
		
		for(int i=0; i<string.length(); i++) {
			if(Utils.reservedCharacters[(int)string.charAt(i)]) {
				reservedCharacterFound = true;
				break;
			}
		}
		
		if(!reservedCharacterFound) {
			return string.toUpperCase();
		}
		
		return string;
	}
	
	public static long timeInMs() {
		return System.currentTimeMillis();
	}
	
	public static String convertTimeInMsToDate(long timeInMs) {
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMs);
		
		return (new java.sql.Timestamp(calendar.getTime().getTime())).toString();
		
	}
	
	public static String getCurrentTimeStamp() {
		return (new java.sql.Timestamp(Calendar.getInstance().getTime().getTime())).toString();
	}
	
	public static String getCurrentTimeStampFileNameFriendly() {
		return getCurrentTimeStamp().replaceAll("(\\-)|(\\:)|(\\.)|(\\s+)", "");
	}
	
	public static void createDirectoryIfNotExists(String path) {
		
		File f = new File(path);
		
		if(!f.exists()) {
			
			f.getParentFile().mkdirs();
		
		}
		
	}
	
	public static String listOfEnumValuesForReSTAPI(Class<? extends Enum<?>> e) {
		
		StringBuilder sb = new StringBuilder();
		int i=0;
		
		for(Object value : e.getEnumConstants()) {
			
			if(i++ > 0) {
				
				sb.append("|");
				
			}
			
			sb.append(value.toString());
			
		}
		
		return sb.toString();
		
	}
	
	public static String exceptionToString(Exception e) {
		
		StringWriter sw =	
			new StringWriter();
			
		e.printStackTrace(
			new PrintWriter(sw)
		);
		
		return sw.toString();
		
	}
	
	public static class Base64Encode {
		
		private final static String base64chars =
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

		public static String encode(String s) {

			// the result/encoded string, the padding string, and the pad count
			String r = "", p = "";
			int c = s.length() % 3;
	
			// add a right zero pad to make this string a multiple of 3 characters
			if (c > 0) {
	    		for (; c < 3; c++) {
					p += "=";
					s += "\0";
	    		}
			}
	
			// increment over the length of the string, three characters at a time
			for (c = 0; c < s.length(); c += 3) {
	
	    		// we add newlines after every 76 output characters, according to
	    		// the MIME specs
	    		if (c > 0 && (c / 3 * 4) % 76 == 0)
					r += "\r\n";
	
	    		// these three 8-bit (ASCII) characters become one 24-bit number
	    		int n = (s.charAt(c) << 16) + (s.charAt(c + 1) << 8)
		    		+ (s.charAt(c + 2));
	
	    		// this 24-bit number gets separated into four 6-bit numbers
	    		int n1 = (n >> 18) & 63, n2 = (n >> 12) & 63, n3 = (n >> 6) & 63, n4 = n & 63;
	
	    		// those four 6-bit numbers are used as indices into the base64
	    		// character list
	    		r += "" + base64chars.charAt(n1) + base64chars.charAt(n2)
		    			+ base64chars.charAt(n3) + base64chars.charAt(n4);
			}
	
			return r.substring(0, r.length() - p.length()) + p;
		}
		
		public static String decode(String s) {
			
			if(s == null || s.length() == 0 || s.length() % 4 != 0) {
				
				return null;
				
			}

			// remove/ignore any characters not in the base64 characters list
			// or the pad character -- particularly newlines
			s = s.replaceAll("[^" + base64chars + "=]", "");

			// replace any incoming padding with a zero pad (the 'A' character is
			// zero)
			String p = (s.charAt(s.length() - 1) == '=' ? 
						(s.charAt(s.length() - 2) == '=' ? "AA" : "A") : "");
			String r = "";
			s = s.substring(0, s.length() - p.length()) + p;

			// increment over the length of this encoded string, four characters
			// at a time
			for (int c = 0; c < s.length(); c += 4) {

				// each of these four characters represents a 6-bit index in the
				// base64 characters list which, when concatenated, will give the
				// 24-bit number for the original 3 characters
				int n = (base64chars.indexOf(s.charAt(c)) << 18)
						+ (base64chars.indexOf(s.charAt(c + 1)) << 12)
						+ (base64chars.indexOf(s.charAt(c + 2)) << 6)
						+ base64chars.indexOf(s.charAt(c + 3));

				// split the 24-bit number into the original three 8-bit (ASCII)
				// characters
				r += "" + (char) ((n >>> 16) & 0xFF) + (char) ((n >>> 8) & 0xFF)
						+ (char) (n & 0xFF);
			}

			// remove any zero pad that was added to make this a multiple of 24 bits
			return r.substring(0, r.length() - p.length());
			
		}
		
	}
	
	public static class Encryptor {
		
		public static byte[] encrypt(byte[] key, String text) throws Exception {
			
			// Create key and cipher
			Key aesKey = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			// encrypt the text
			cipher.init(Cipher.ENCRYPT_MODE, aesKey);
			byte[] encrypted = cipher.doFinal(text.getBytes());
			
			return encrypted;
			
		}
		
		public static String decrypt(byte[] key, byte[] encrypted) throws Exception {
			
			// Create key and cipher
			Key aesKey = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			// decrypt the text
			cipher.init(Cipher.DECRYPT_MODE, aesKey);
			return new String(cipher.doFinal(encrypted));
			
		}
		
	}
	
	public static String getSystemTime() {
		Calendar calendar =
			Calendar.getInstance();
		SimpleDateFormat dateFormat =
			new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US
			);
		dateFormat.setTimeZone(
			TimeZone.getTimeZone("GMT")
		);
		return dateFormat.format(calendar.getTime());
	}
	
	public static void main(String args[]) {
		
		;
		
	}
}