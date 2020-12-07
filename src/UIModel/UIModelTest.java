package UIModel;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONObject;

class UIModelTest {
	public static void main(String[] args) {
//		Map<String, JSONObject> fonts = new HashMap<String, JSONObject>();
//		Map<String, JSONObject> actions = new HashMap<String, JSONObject>();
		
		JSONObject windowModel = JSONUtils
				.readFileAsJSON("Models\\appModel2.json");

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText(windowModel.optString("shellTitle"));

		Styler styler = new Styler(windowModel.optString("assetPath"), windowModel.optString("curSkin"), shell);
		
		shell.setBounds(windowModel.optInt("shellX"), windowModel.optInt("shellY"), windowModel.optInt("shellWidth"),
				windowModel.optInt("shellHeight"));
		
		// Hook up UI listeners
		new UIEventHandler(shell, windowModel, styler);

		// Watch the style files for changes
		Runnable styleChecker = new Runnable() {
			public void run() {
				styler.checkForStyleModifications();
				Display.getDefault().timerExec(200, this);
			}
		};
		Display.getDefault().timerExec(200, styleChecker);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
