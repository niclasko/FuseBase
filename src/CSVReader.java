/*
 * Copyright (c) 2018 "Niclas Kjäll-Ohlsson, Bjørnar Fjøren"
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
import java.util.Arrays;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.io.Serializable;

public class CSVReader implements Serializable {
	
	private String sLine;
	private String fields[];
	private String header[];
	private int recordNumber = 0;
	private int numFields = 0;
	private char fieldSeparator;
	private String field = "";
	private char c, pc;
	
	private int recordCount = 0;
	
	private int error = 0;
	private String errorText = "";
	
	private File file;
	private transient FileInputStream fs;
	private transient DataInputStream in;
	private transient BufferedReader br;
	
	private char fieldBuffer[] = new char[2000];
	
	private boolean fileIsOpen = false;
	
	private int loadToDBprogress = 0;
	
	private String fileName = "";
	
	private String outputPath;
	
	private String fieldSpec;
	
	private String possiblyPartialLine;
	
	private long bytesRead;
	
	private long fileSize;
	
	private boolean isLoading;
	
	private int fieldCount;
	
	private JSONBuilder jsonBuilder;
	
	private boolean writeToJSON = false;
	
	/*
	** Open local file
	**
	*/
	public CSVReader(String sPath, char fieldSeparator, String outputPath, String fieldSpec) {
		try {
			file = new java.io.File(sPath);
			fs = new FileInputStream(file);
			in = new DataInputStream(fs);
			br = new BufferedReader(new InputStreamReader(in));
			this.fieldSeparator = fieldSeparator;
			this.outputPath = outputPath;
			this.fieldSpec = fieldSpec;
			
			this.fileSize = this.file.length();
			
			//this.compressedOut = new PrintWriter(new BufferedWriter(new FileWriter(sOutputPath + "compressed.csv", false)));
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		
		this.fieldCount = fieldSpec.length();
		
		this.fileName = sPath;
		
		this.readHeader();
		
		this.fileIsOpen = true;
		
		this.bytesRead = 0;
		
		this.isLoading = true;
	}
	
	/*
	** Read chunks from remote file
	**
	*/
	public CSVReader(String fileName, char fieldSeparator, String outputPath, String fieldSpec, String chunk, long fileSize) {
		file = null;
		fs = null;
		in = null;
		br = null;
		this.fieldSeparator = fieldSeparator;
		this.outputPath = outputPath;
		this.fieldSpec = fieldSpec;
		
		this.fieldCount = fieldSpec.length();
		
		this.fileName = fileName;
		
		this.readHeaderLineFromChunk(chunk);
		
		this.fileIsOpen = true;
		
		this.possiblyPartialLine = "";
		
		this.bytesRead = 0;
		
		this.fileSize = fileSize;
		
		this.isLoading = true;
		
		this.readChunk(chunk, 1);
		
		this.finishLoad();
		
	}
	
	/*
	** Convert CSV chunk to JSON
	**
	*/
	public CSVReader(String chunk, char fieldSeparator, JSONBuilder jsonBuilder) {
		
		file = null;
		fs = null;
		in = null;
		br = null;
		
		this.fieldCount = 0;
		
		this.fieldSeparator = fieldSeparator;
		
		this.readHeaderLineFromChunk(chunk);
		
		this.fileIsOpen = true;
		
		this.possiblyPartialLine = "";
		
		this.bytesRead = 0;
		
		this.fileSize = fileSize;
		
		this.isLoading = true;
		
		this.jsonBuilder = jsonBuilder;
		
		this.writeToJSON = true;
		
		this.jsonBuilder.$('[');
		
		this.readChunk(chunk, 1);
		
		this.finishLoad();
		
		this.jsonBuilder.$(']');
	}
	
	public static String csvToJSON(String chunk, char fieldSeparator) {
		
		CSVReader csvReader =
			new CSVReader(
				chunk,
				fieldSeparator,
				new JSONBuilder()
			);
			
		return csvReader.getJSON();
		
	}
	
	private String getJSON() {
		
		if(this.jsonBuilder != null) {
			
			return this.jsonBuilder.getJSON();
			
		}
		
		return "";
		
	}
	
	public boolean isLoading() {
		return this.isLoading;
	}
	
	public double getProgress() {
		try {
			return (this.bytesRead*1.0)/(this.fileSize*1.0)*100.0;
		} catch(Exception e) {
			return Double.NaN;
		}
	}
	
	public long bytesRead() {
		return this.bytesRead;
	}
	
	public long fileLength() {
		return this.file.length();
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public static String getDBFriendlyFileName(String longFileName) {
		String fileNameWithoutPath = (new java.io.File(longFileName)).getName();
		int positionOfDot = fileNameWithoutPath.indexOf(".");
		
		return fileNameWithoutPath.substring(
			0,
			(positionOfDot > -1 ? positionOfDot : fileNameWithoutPath.length())
		);
	}
	
	public String getDBFriendlyFileName() {
		return CSVReader.getDBFriendlyFileName(fileName);
	}
	
	private String readLineFromFile() {
		
		StringBuilder sb = new StringBuilder();
		int n;
		char c = '\0', pc = '\0';
		
		boolean inQuotes = false;
		
		try {
			while((n = br.read()) != -1) {
				pc = c;
				c = (char)n;
				this.bytesRead++;
				
				if(c == '"' && pc != '"') {
					inQuotes = !inQuotes;
				}
				
				if(c == '\n' && pc != '\r' && !inQuotes) {
					break;
				} else if(c == '\n' && pc == '\r' && !inQuotes) {
					break;
				} else if(c == '\r') {
					continue;
				} else {
					sb.append(c);
				}
			}
		} catch(Exception e) {
			;
		}
		
		if(sb.length() == 0) {
			return null;
		}
		
		return sb.toString();
	}
	
	public boolean readLine() {
		try {
			if((sLine = this.readLineFromFile()) == null) {
				return false;
			}
			this.parseLine(sLine, fields);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public void readLine(String sLine) {
		this.recordCount++;
		this.parseLine(sLine, fields);
	}
	
	private void readLine(String sLine, boolean hasNextLine, boolean chunkEndsWithNewLine) {
		if(hasNextLine) {
			this.readLine(sLine);
		} else if(!hasNextLine && !chunkEndsWithNewLine) {
			this.possiblyPartialLine = sLine;
		} else if(!hasNextLine && chunkEndsWithNewLine) {
			this.readLine(sLine);
		}
	}
	
	private void parseLine(String lineData, String destination[]) {
		
		this.error = 0;
		int fieldIdx = 0, bytesRead = 0;
		boolean newField = true, inQuotes = false;
		field = "";
		c = '\0';
		char nc;
		int quoteCount = 0;
		
		for(int i=0; i<lineData.length(); i++) {
			pc = c;
			c = lineData.charAt(i);
			nc = (i<lineData.length()-1 ? lineData.charAt(i+1) : '\0');
			
			if(c == '"' && !inQuotes) {
				inQuotes = true;
				continue;
			} else if(c == '"' && inQuotes) {
				quoteCount++;
				if(quoteCount % 2 == 1 && nc != '"') {
					inQuotes = false;
					quoteCount = 0;
					if(i < lineData.length()-1) {
						continue;
					}
				} else if(quoteCount % 2 == 1 && nc == '"') {
					continue;
				}
			}
			
			if(c == fieldSeparator && !inQuotes && fieldIdx < this.fieldCount) {
				destination[fieldIdx++] = new String(fieldBuffer, 0, bytesRead);
				newField = true;
			} else if(i == (lineData.length() - 1)) {
				
				if(c != fieldSeparator && c != '"') {
					fieldBuffer[bytesRead++] = c;
				}

				destination[fieldIdx++] = new String(fieldBuffer, 0, bytesRead);
				newField = true;
				
			} else {
				fieldBuffer[bytesRead++] = c;
				newField = false;
				
			}
			
			if(newField) {
				
				bytesRead = 0;
				
			}
			
			if(fieldIdx > destination.length) {
				this.error = 1;
				this.errorText = "Too many fields";
				return;
			}
		}
		if(fieldIdx < destination.length-1) {
			this.error = 2;
			this.errorText = "Too few fields";
		}
		
		if(this.writeToJSON && this.header != destination) {
			
			this.jsonBuilder.$('{');
			
			for(int i=0; i<this.header.length; i++) {
				
				this.jsonBuilder.k(this.header[i]).v(this.fields[i]);
				
			}
			
			this.jsonBuilder.$('}');
			
		}
		
		//System.out.println(Arrays.toString(destination));
	}
	
	private int getError() {
		return this.error;
	}
	
	private String getErrorText() {
		return this.errorText;
	}
	
	private void readHeader() {
		try {
			sLine = this.readLineFromFile();
			
			this.readHeader(sLine);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void readHeader(String sLine) {
		for(int i=0;i<sLine.length(); i++) {
			c = sLine.charAt(i);
			if(c == fieldSeparator || i == (sLine.length() - 1)) {
				numFields++;
			}
		}
		
		if(this.fieldCount == 0) {
			
			this.fieldCount = numFields;
			
		}
		
		header = new String[numFields];
		fields = new String[numFields];
		
		this.parseLine(sLine, this.header);
	}
	
	public int getFieldCount() {
		return this.fields.length;
	}
	
	public String[] getFields() {
		return this.fields;
	}
	
	public String[] getHeader() {
		return this.header;
	}
	
	public boolean fileIsOpen() {
		return this.fileIsOpen;
	}
	
	public int getRecordCount() {
		return this.recordCount;
	}
	
	public void readAll() {
		if(this.fileIsOpen()) {

			while(this.readLine()) {
				this.recordCount++;
			}
			this.finishLoad();
		}
	}
	
	public void finishLoad() {
		if(this.possiblyPartialLine != null) {
			if(this.possiblyPartialLine.length() > 0) {
				this.readLine(this.possiblyPartialLine);
			}
		}
		this.isLoading = false;
		this.bytesRead = this.fileSize;
	}
	
	public void readHeaderLineFromChunk(String chunk) {
		Scanner scanner = new Scanner(chunk);
		if(scanner.hasNextLine()) {
			this.readHeader(scanner.nextLine());
		}
		scanner.close();
	}
	
	private boolean chunkEndsWithNewLine(String chunk) {
		if(chunk.length() > 1) {
			if(chunk.charAt(chunk.length()-1) == '\n') {
				return true;
			}
		} else if(chunk.length() == 1) {
			if(chunk.charAt(0) == '\n') {
				return true;
			}
		}
		return false;
	}
	
	public void readChunk(String chunk, int skipN) {
		Scanner scanner = new Scanner(chunk);
		int lineNo = 0;
		boolean chunkEndsWithNewLine = chunkEndsWithNewLine(chunk);
		while(scanner.hasNextLine() && lineNo++ < skipN) {
			scanner.nextLine();
		}
		while(scanner.hasNextLine()) {
			this.readLine(scanner.nextLine(), scanner.hasNextLine(), chunkEndsWithNewLine);
		}
		
		this.possiblyPartialLine =
			new String(this.possiblyPartialLine);
		
		scanner.close();
	}
	
	public void readChunk(String chunk) {
		
		String assembledChunk = this.possiblyPartialLine + chunk;
		boolean chunkEndsWithNewLine = chunkEndsWithNewLine(chunk);
		this.possiblyPartialLine = "";
		Scanner scanner = new Scanner(assembledChunk);
		
		while(scanner.hasNextLine()) {
			this.readLine(scanner.nextLine(), scanner.hasNextLine(), chunkEndsWithNewLine);
		}
		scanner.close();
	}
	
}