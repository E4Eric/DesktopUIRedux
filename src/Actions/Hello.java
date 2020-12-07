package Actions;

import org.json.JSONObject;

public class Hello {
	public Hello() {
	}
	
	public boolean isEnabled(JSONObject context) {
		System.out.println("Hello is ENABLED !!");
		return true;
	}
	
	public boolean doAction(JSONObject context) {
		System.out.println("Hello World !!");
		return true;
	}
}
