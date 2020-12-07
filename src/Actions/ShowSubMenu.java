package Actions;

import org.eclipse.swt.graphics.Rectangle;
import org.json.JSONException;
import org.json.JSONObject;

import UIModel.Styler;

public class ShowSubMenu {
	public ShowSubMenu() {
	}
	
	public boolean isEnabled(JSONObject context) {
		return context.has("submenu");
	}
	
	public boolean doAction(JSONObject context) {
		try {
			JSONObject element = (JSONObject) context.opt("element");
			Styler styler = (Styler) context.opt("element");
			
			boolean showMenu = element.getBoolean("showmenu");
			Rectangle drawRect = (Rectangle) element.get("drawRect");
			
			if (!showMenu) {
				showMenu = !showMenu;
				element.putOpt("showmenu", showMenu);
				// HACK!! should be a reaction to the above model change
				String openType = "DropDown"; //context.getString("openstyle")
				Rectangle available = new Rectangle(drawRect.x, drawRect.y + drawRect.height, 800,800);
				Rectangle menuRect = styler.layout(element, available, false);
				System.out.println(menuRect);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Hello World !!");
		return true;
	}
}
