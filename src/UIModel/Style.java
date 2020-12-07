package UIModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import org.eclipse.swt.graphics.ImageData;
import org.json.JSONObject;

public class Style {
	public String imagePath;
	public ImageData imageData;
	public FileTime imageModified;
	public String jsonPath;
	public JSONObject jsonData;
	public FileTime jsonModified;

	public Style(String imageFilePath, String jsonFilePath) {
		imagePath = imageFilePath;
		imageData = new ImageData(imagePath);
		imageModified = getLastModified(imagePath);

		jsonPath = jsonFilePath;
		jsonData = JSONUtils.readFileAsJSON(jsonPath);
		jsonModified = getLastModified(jsonPath);
	}
	
	public boolean hasBeenModified() {
		FileTime jsonTime = getLastModified(jsonPath);
		if (jsonTime.compareTo(jsonModified) > 0)
			return true;
		
		FileTime imageTime = getLastModified(imagePath);
		if (imageTime.compareTo(imageModified) > 0)
			return true;		
		
		return false;
	}
	
	private FileTime getLastModified(String filePath) {
		Path path = Paths.get(filePath);
		try {
			BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
			return attributes.lastModifiedTime();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
