package Actions;

import org.json.JSONObject;

public class ToggleContentVisible {
	public ToggleContentVisible() {
	}
	
	public boolean isEnabled(JSONObject context) {
		// has to have both contents and something to toggle
		if (!context.has("contents") || !context.has("contentVisible"))
			return false;
		
		System.out.println("Hello is ENABLED !!");
		return true;
	}
	
	public boolean doAction(JSONObject context) {
		boolean contentVisible = context.optBoolean("contentVisible");
		if (contentVisible) {
			// Hide the content
		}
		System.out.println("Hello World !!");
		return true;
	}
}
