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
import java.util.HashMap;
import java.io.Serializable;
import java.util.jar.JarFile;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.net.URL;

public class FileManager implements Serializable {
	
	private HashMap<String, WebServerFile> outputFiles;
	
	public FileManager() {
		
		this.outputFiles =
			new HashMap<String, WebServerFile>();
			
	}
	
	/*
	** Checks whether jarFile contains fileName entry
	*/
	public boolean isResourceFile(JarFile jarFile, String fileName) throws Exception {
		
		String jarEntryFileName = fileName;
		
		if(jarFile.getEntry(fileName) != null) {
			
			return true;
			
		}
		
		if(jarEntryFileName.length() >= 1) {
			if(jarEntryFileName.substring(0,1).equals("/".intern())) {
				jarEntryFileName = "..".intern() + jarEntryFileName.intern();
			} else if(!jarEntryFileName.substring(0,1).equals("/".intern())) {
				jarEntryFileName = "../".intern() + jarEntryFileName.intern();
			}
		}
		
		return (jarFile.getEntry(jarEntryFileName) != null);
		
	}
	
	/*
	** Read a file from a jar and write to DataOutputStream
	*/
	public void serveResourceFile(DataOutputStream output, JarFile jarFile, String fileName, byte[] resourceFileBytes) throws Exception {
		
		String jarEntryFileName = fileName;
		
		if(jarFile.getEntry(fileName) == null) {
			
			if(jarEntryFileName.length() >= 1) {
				if(jarEntryFileName.substring(0,1).equals("/")) {
					jarEntryFileName = ".." + jarEntryFileName;
				} else if(!jarEntryFileName.substring(0,1).equals("/")) {
					jarEntryFileName = "../" + jarEntryFileName;
				}
			}
			
		}
		
		InputStream is =
			jarFile.getInputStream(
				jarFile.getEntry(
					jarEntryFileName
				)
			);
		
		BufferedInputStream bis = null;
		int bytesRead = 0, totalBytesRead = 0;
		
		bis = new BufferedInputStream(is);
		
		while((bytesRead = bis.read(resourceFileBytes, 0, resourceFileBytes.length)) > -1) {
			totalBytesRead += bytesRead;
			if(totalBytesRead % resourceFileBytes.length == 0) {
				output.write(resourceFileBytes, 0, resourceFileBytes.length);
			}
		}
		
		if(totalBytesRead % resourceFileBytes.length > 0) {
			output.write(resourceFileBytes, 0, totalBytesRead % resourceFileBytes.length);
		}
	    
		bis.close();
		
	}
	
	/*
	** Open a InputStream for a file from a jar file
	*/
	public InputStream getFileInputStreamFromJarFile(JarFile jarFile, String fileName) throws Exception {
		
		return jarFile.getInputStream(
			jarFile.getEntry(
				fileName
			)
		);
		
	}
	
	/*
	** List contents of a directory as a JSONBuilder object
	*/
	public JSONBuilder listFullDirectory(String path) throws Exception {
		
		int i = 0;
		JSONBuilder jb = JSONBuilder.f();
		String _path = (path == null ? "" : path);
		
		java.io.File f = null;
		
		f =
			new java.io.File(
				(
					new java.io.File(_path)
				).getCanonicalPath()
			);
			
		jb.$('[');
		
		if(f.isDirectory()) {

			File[] files = f.listFiles();

			for(int j=0; j<files.length; j++) {
				
				jb.
					$('{').
						k("fileName").v(files[j].getName()).
						k("fullPath").v(files[j].getCanonicalPath()).
						k("isDirectory").v(files[j].isDirectory()).
					$('}');
				
			}
			
		} else {
			
			jb.
				$('{').
					k("fileName").v(f.getName()).
					k("fullPath").v(f.getCanonicalPath()).
					k("isDirectory").v(f.isDirectory()).
				$('}');
			
		}
		
		jb.$(']');
		
		return jb;
		
	}
	
	/*
	** Read the first N rows of a file and return as String
	*/
	public String sampleFile(String fileName, int firstNRows) throws Exception {
		
		StringBuilder sample = new StringBuilder();
		String line;
		int linesRead = 0;
		BufferedReader br = null;
		
		char c = '\0', pc = '\0';
		int n;
		
		br = new BufferedReader(new FileReader(fileName));
		
		StringBuilder sb = new StringBuilder();
		
		while((n = br.read()) != -1 && linesRead <= firstNRows) {
			
			pc = c;
			c = (char)n;
			
			if(c == '\n' && pc != '\r') {
				linesRead++;
			} else if(c == '\n' && pc == '\r') {
				linesRead++;
			} else if(c == '\r') {
				linesRead++;
			}
			
			sample.append(c);
			
		}
		
		br.close();
		
		return sample.toString();
		
	}
	
	/*
	** Write fileData to fileName, append if fileName is in outputFiles, otherwise create new file
	*/
	public void writeToFile(String fileName, String fileData) throws Exception {
		
		WebServerFile webServerFile = null;
		
		if(this.outputFiles.containsKey(fileName)) {
			
			webServerFile = this.outputFiles.get(fileName);
			
		} else {
			
			(new java.io.File(fileName.substring(0,fileName.lastIndexOf('/')))).mkdirs();
			
			webServerFile = new WebServerFile(
				fileName, 
				new PrintWriter(new BufferedWriter(new FileWriter(fileName, false)))
			);
			
			this.outputFiles.put(
				fileName, 
				webServerFile
			);
		}
		
		if(!fileData.equals("") && !fileData.equals(Config.CLOSEFILE)) {
			
			webServerFile.print(fileData, true);
			
		} else if(fileData.equals(Config.CLOSEFILE)) {
			
			webServerFile.close();
			this.outputFiles.remove(fileName);
			
		}
		
	}
	
	/*
	** Write binary fileData to fileName
	*/
	public void writeToBinaryFile(String fileName, byte[] fileData) throws Exception {
		
		// Create directories recursively if they don't exist
		(new File(fileName.substring(0,fileName.lastIndexOf('/')))).mkdirs();
		
		FileOutputStream fos =
			new FileOutputStream(fileName, false);
			
		fos.write(fileData);
		
		fos.close();
		
	}
	
	/*
	** Read contents from URL and write to DataOutputStream
	*/
	public void printURL(DataOutputStream output, String urlAddress) throws Exception {
		
		URL url;
		InputStream is = null;
		int bytesRead;
		byte[] buffer = new byte[4000];

		url = new URL(urlAddress);
		is = url.openStream();
		
		while ((bytesRead = is.read(buffer, 0, buffer.length)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
		
		output.flush();
		
		is.close();
		
	}
	
}