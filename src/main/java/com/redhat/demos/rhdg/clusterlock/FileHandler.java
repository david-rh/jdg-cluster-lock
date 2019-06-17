package com.redhat.demos.rhdg.clusterlock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileHandler {
	
	@Value("${BASE_PATH:'./'}")
	private String basePath;
	
	public FileHandler() {
	}

	public String readFile(String filename)
	{
		StringBuilder fileContent = new StringBuilder();
		Path path = Paths.get(basePath, filename);
		try (BufferedReader bReader = Files.newBufferedReader(path)) {
			String line;
			while ((line = bReader.readLine()) != null) {
				fileContent.append(line).append("\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileContent.toString();
	}
	
	public void writeFile(String filename, String message)
	{
		Path path = Paths.get(basePath, filename);
		try (BufferedWriter bWriter = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND )) {
			
			bWriter.write(message+"\n");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void deleteFile(String filename) {
		Path path = Paths.get(basePath, filename);
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
