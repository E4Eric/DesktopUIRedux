package UIModel;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONException;
import org.json.JSONObject;

public class UIEventHandler {
	Shell shell;
	Styler styler;
	JSONObject windowModel;

	JSONObject elementUnderMouse = null;

	Map<String, JSONObject> actions = new HashMap<String, JSONObject>();

	public UIEventHandler(Shell theShell, JSONObject theWindowModel, Styler theStyler) {
		shell = theShell;
		windowModel = theWindowModel;
		styler = theStyler;

		if (shell != null) {
			// Hook up generic listeners
			shell.addKeyListener(new KeyListener() {
				@Override
				public void keyReleased(KeyEvent ke) {
				}

				@Override
				public void keyPressed(KeyEvent ke) {
					handleKeyDown(ke);
				}
			});

			shell.addMouseMoveListener(new MouseMoveListener() {
				@Override
				public void mouseMove(MouseEvent me) {
					JSONObject curElement = styler.pick(windowModel, me.x, me.y);
					handleMouseMove(me, curElement);
				}
			});

			shell.addPaintListener(new PaintListener() {
				@Override
				public void paintControl(PaintEvent ev) {
					try {
						Rectangle ca = shell.getClientArea();

						styler.layout(windowModel, ca, true);
						styler.draw(ev, windowModel);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});

		}

		registerActions();
	}

	private void registerActions() {
		JSONObject actionSpec = new JSONObject();
		try {
			actionSpec.putOpt("classSpec", "Actions.Hello");
			registerAction("Hello Action", actionSpec);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void registerAction(String actionName, JSONObject actionSpec) {
		actions.put(actionName, actionSpec);
	}

	public void removeAction(String actionName) {
		actions.remove(actionName);
	}

	@SuppressWarnings("deprecation")
	public boolean runAction(String actionName, JSONObject context) {
		try {
			JSONObject actionSpec = actions.get(actionName);
			Object implementation = actionSpec.opt("implementation");
			if (implementation == null && actionSpec.has("classSpec")) {
				String classSpec = actionSpec.optString("classSpec");
				Class<?> classDefinition = Class.forName(classSpec);
				implementation = classDefinition.newInstance();
				actionSpec.putOpt("implementation", implementation);
			}

			if (implementation != null) {
				Method isEnabled = implementation.getClass().getMethod("isEnabled", JSONObject.class);
				boolean enabled = (boolean) isEnabled.invoke(implementation, context);
				if (enabled) {
					Method doAction = implementation.getClass().getMethod("doAction", JSONObject.class);
					boolean success = (boolean) doAction.invoke(implementation, context);
					return success;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public void handleHover(JSONObject curElement) {
		String hs = curElement != null ? curElement.toString() : " ---";
		System.out.println(" --> HandleHover, Element = " + hs);
	}

	JSONObject timerElement = null;
	Runnable timer = new Runnable() {
		public void run() {
			System.out.println("*** TIME OUT !! ***");
			handleHover(timerElement);
		}
	};

	int hoverTimeout = 300;

	public String adornPath(File currentFile, String adornment) {
		String fullPath = currentFile.getAbsolutePath();
		String parentPath = fullPath.substring(0, fullPath.lastIndexOf("\\"));
		String name = currentFile.getName();
		String[] parts = styler.splitNameIntoParts(name);
		String newName = parts[0] + adornment + "." + parts[1];
		String newFullPath = parentPath + "\\" + newName;

		return newFullPath;
	}

	public void adornStyle(JSONObject element, String adornment, boolean add) {
		if (element == null)
			return;

		Style style = styler.getStyle(element);
		if (style != null) {
			if (add) {
				String adornedImagePath = adornPath(new File(style.imagePath), adornment);
				File adornedImageFile = new File(adornedImagePath);

				String adornedJSONPath = adornPath(new File(style.jsonPath), adornment);
				File adornedJSONFile = new File(adornedJSONPath);
				if (adornedImageFile.exists() && adornedJSONFile.exists()) {
					System.out.println("OK To Adorn..." + adornment);
					String curStyle = element.optString("style");
					try {
						element.putOpt("style", curStyle + adornment);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			} else {
				String curStyle = element.optString("style");
				try {
					String newStyle = curStyle.replace(adornment, "");
					element.putOpt("style", newStyle);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void handleMouseMove(MouseEvent me, JSONObject curElement) {
		// Reset the hover timer
		timerElement = curElement;
		me.display.timerExec(hoverTimeout, timer);

		if (curElement != elementUnderMouse) {
			// HACK!! Fake a Leave and Enter event
			if (elementUnderMouse != null) {
				adornStyle(elementUnderMouse, "Over", false);
				Rectangle er = (Rectangle) elementUnderMouse.opt("drawRect");
				shell.redraw(er.x, er.y, er.width, er.height, false);
			}

			elementUnderMouse = curElement;

			if (elementUnderMouse != null) {
				adornStyle(curElement, "Over", true);
				Rectangle er = (Rectangle) elementUnderMouse.opt("drawRect");
				shell.redraw(er.x, er.y, er.width, er.height, false);
			}
		}
	}

	public void handleKeyDown(KeyEvent ke) {
		Shell shell = (Shell) ke.widget;
		if (ke.character == ' ') {
			shell.redraw();
		}
		if (ke.character == 'b') {
			styler.setCurrentSkin("Brass");
			shell.redraw();
		}
		if (ke.character == 't') {
			styler.setCurrentSkin("Testing");
			shell.redraw();
		}
		if (ke.character == 'e') {
			styler.setCurrentSkin("Eclipse");
			shell.redraw();
		}
		if (ke.character == 'E') {
			styler.setCurrentSkin("EclipseDark");
			shell.redraw();
		}
		if (ke.character == 's') {
			long start = System.currentTimeMillis();
			long count = 0;
			while (System.currentTimeMillis() - start < 1000) {
				shell.redraw();
				count++;
			}
			System.out.println("Draws " + count + " Frames a second");
		}
	}

	public void handleMouseDown(MouseEvent me) {
		if (elementUnderMouse != null) {
			if (elementUnderMouse.has("clickAction")) {
				JSONObject rc = new JSONObject();
				try {
					rc.putOpt("styler", styler);
					rc.putOpt("element", elementUnderMouse);
					runAction(elementUnderMouse.optString("clickAction"), rc);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
