package UIModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.stream.Stream;

import org.json.JSONObject;

public class JSONUtils {
	public static JSONObject readFileAsJSON(String jsonPath) {
		File json = new File(jsonPath);
		BufferedReader reader = null;
		JSONObject newObj = null;
		try {
			reader = new BufferedReader(new FileReader(json));
			Stream<String> lines = reader.lines();
			Iterator<String> iter = lines.iterator();
			String content = "";
			while (iter.hasNext()) {
				content += iter.next();
			}
				newObj = new JSONObject(content);
				return newObj;
		} catch (Exception e1) {
			return null;
		}
	}
}
