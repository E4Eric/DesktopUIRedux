package UIModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Styler {
	Map<String, Style> styles = new HashMap<String, Style>();
	Map<String, Image> icons = new HashMap<String, Image>();

	private String assetDir;
	private String curSkin;

	private Shell shell;

	public Styler() {
	}

	public String[] splitNameIntoParts(String filename) {
		String[] parts = { "", "" };
		int lastDot = filename.lastIndexOf('.');
		if (lastDot >= 0) {
			parts[0] = filename.substring(0, lastDot);
			parts[1] = filename.substring(lastDot + 1);
		}
		return parts;
	}

	public Styler(String assetPath, String skinName, Shell shell) {
		this.shell = shell;
		assetDir = assetPath;
		curSkin = skinName;

		loadIcons();
	}

	public void setCurrentSkin(String newSkin) {
		curSkin = newSkin;

		clearStyles();
		loadIcons();
	}

	private void clearIcons() {
		for (Iterator<String> it = icons.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			Image image = icons.get(key);
			if (!image.isDisposed())
				image.dispose();
		}
		icons = new HashMap<String, Image>();
	}

	private void loadIcons() {
		clearIcons();

		File assets = new File(assetDir + "\\Skins\\" + curSkin + "\\icons");
		File[] iconFiles = assets.listFiles();
		for (int i = 0; i < iconFiles.length; i++) {
			ImageData data = new ImageData(iconFiles[i].getAbsolutePath());
			Image image = new Image(shell.getDisplay(), data);
			String[] parts = splitNameIntoParts(iconFiles[i].getName());
			icons.put(parts[0], image);
		}
	}

	public Image getIcon(String iconName) {
		Image icon = icons.get(iconName);
		return icon;
	}

	public void clearStyles() {
		styles = new HashMap<String, Style>();
	}

	public void addStyle(String styleName, Style style) {
		styles.put(styleName, style);
	}

	public void removeStyle(String styleName) {
		styles.remove(styleName);
	}

	public void checkForStyleModifications() {
		List<String> modifiedStyles = new ArrayList<String>();
		for (Iterator<String> iterator = styles.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Style style = styles.get(key);
			if (style.hasBeenModified()) {
				modifiedStyles.add(key);
			}
		}

		if (modifiedStyles.size() > 0) {
			for (Iterator<String> it = modifiedStyles.iterator(); it.hasNext();) {
				String styleName = (String) it.next();
				removeStyle(styleName);
			}
			shell.redraw();
		}
	}

	public Style getStyle(JSONObject element) {
		if (!element.has("style"))
			return null;

		String styleName = element.optString("style");
		String styleJSONPath = assetDir + "\\Skins\\" + curSkin + "\\Frames\\" + styleName + ".txt";
		String imagePath = assetDir + "\\Skins\\" + curSkin + "\\Frames\\" + styleName + ".png";
		Style style = styles.get(styleName);
		if (style == null) {
			Style newStyle = new Style(imagePath, styleJSONPath);
			styles.put(styleName, newStyle);
			return newStyle;
		}
		return style;
	}

	public Point offsetForStyle(Point size, JSONObject element) {
		Style style = getStyle(element);
		if (style != null) {
			int lw = style.jsonData.optInt("lw");
			int th = style.jsonData.optInt("th");
			int lm = style.jsonData.optInt("lm");
			int tm = style.jsonData.optInt("tm");

			size.x += lw + lm;
			size.y += th + tm;
		}

		return size;
	}

	public Point inflateForStyle(Point size, JSONObject element) {
		Style style = getStyle(element);
		if (style != null) {
			int lw = style.jsonData.optInt("lw");
			int th = style.jsonData.optInt("th");
			int rw = style.jsonData.optInt("rw");
			int bh = style.jsonData.optInt("bh");
			int lm = style.jsonData.optInt("lm");
			int rm = style.jsonData.optInt("rm");
			int tm = style.jsonData.optInt("tm");
			int bm = style.jsonData.optInt("bm");
			int styleWidth = lw + lm + rw + rm;
			int styleHeight = th + tm + bh + bm;

			size.x += styleWidth;
			size.y += styleHeight;
		}

		return size;
	}

	private void offsetRecursive(JSONObject element, int offsetX, int offsetY) throws JSONException {
		Rectangle dr = (Rectangle) element.opt("drawRect");
		dr.x += offsetX;
		dr.y += offsetY;

		if (element.has("contents")) {
			JSONArray kids = element.optJSONArray("contents");
			for (int i = 0, size = kids.length(); i < size; i++) {
				JSONObject kid = (JSONObject) kids.get(i);
				offsetRecursive(kid, offsetX, offsetY);
			}
		}
	}

	private Rectangle panel(JSONObject element, Rectangle available, boolean horizontal) throws JSONException {
		Rectangle drawRect = new Rectangle(available.x, available.y, available.width, available.height);
		element.putOpt("drawRect", drawRect);

		available = AdjustForStyle(element, available);
		JSONArray kids = element.optJSONArray("contents");
		for (int i = 0, size = kids.length(); i < size; i++) {
			JSONObject kid = (JSONObject) kids.get(i);
			Rectangle kidRect;
			switch (kid.optString("side")) {
			case "top":
				kidRect = layout(kid, available, true);
				available.y += kidRect.height;
				available.height -= kidRect.height;
				break;
			case "left":
				kidRect = layout(kid, available, false);
				available.x += kidRect.width;
				available.width -= kidRect.width;
				break;
			case "bottom":
				kidRect = layout(kid, available, true);

				// Adjust the y pos
				int offsetY = ((available.y + available.height) - kidRect.height) - kidRect.y;
				offsetRecursive(kid, 0, offsetY);

				available.height -= kidRect.height;
				break;
			case "right":
				kidRect = layout(kid, available, false);
				// Adjust the x pos
				kidRect.x = (available.x + available.width) - kidRect.width;
				kid.putOpt("drawRect", kidRect);
				available.width -= kidRect.width;
				break;
			case "center":
				kidRect = new Rectangle(available.x,available.y,available.width,available.height);
				kid.putOpt("drawRect", kidRect);
				available = new Rectangle(0,0,0,0);
				break;
			}
		}

		return available;
	}

	private Rectangle tile(JSONObject element, Rectangle available, boolean horizontal) throws JSONException {
		return pack(element, available, horizontal);
	}

	private Rectangle pack(JSONObject element, Rectangle available, boolean horizontal) throws JSONException {
		Rectangle packRect = new Rectangle(available.x, available.y, available.width, available.height);
		Rectangle innerRect = AdjustForStyle(element, packRect);

		int maxHeight = 0;
		JSONArray kids = element.optJSONArray("contents");

		// Pass 1: pack all the children and get the maximum height
		Rectangle kidAvailable = new Rectangle(innerRect.x, innerRect.y, innerRect.width, innerRect.height);
		for (int i = 0, size = kids.length(); i < size; i++) {
			JSONObject kid = (JSONObject) kids.get(i);
			Rectangle kidRect = layout(kid, kidAvailable, horizontal);
			maxHeight = Math.max(maxHeight, kidRect.height);
			kidAvailable.x += kidRect.width;
		}

		// Pass 2: normalize the child heights
		Point bounds = new Point(0, maxHeight);
		for (int i = 0, size = kids.length(); i < size; i++) {
			JSONObject kid = (JSONObject) kids.get(i);
			Rectangle kidRect = (Rectangle) kid.opt("drawRect");
			kidRect.height = maxHeight;
			bounds.x += kidRect.width;
		}

		// Finally calculate the packRect
		bounds = inflateForStyle(bounds, element);
		packRect.height = bounds.y;
		element.putOpt("drawRect", new Rectangle(packRect.x, packRect.y, packRect.width, packRect.height));

		return packRect;
	}

	private Rectangle label(JSONObject element, Rectangle available, boolean horizontal) throws JSONException {
		// "label" and/or "icon"
		Point labelSize = new Point(0, 0);
		if (element.has("label")) {
			GC gc = new GC(Display.getCurrent());
			labelSize = gc.textExtent(element.optString("label"));
		}
		Point iconSize = new Point(0, 0);
		if (element.has("icon")) {
			Image icon = getIcon(element.optString("icon"));
			iconSize = new Point(icon.getImageData().width, icon.getImageData().height);
		}

		Point elementSize = new Point(0, 0);
		if (horizontal) {
			elementSize.x = labelSize.x + iconSize.x;
			elementSize.x += (iconSize.x > 0 && labelSize.x > 0) ? 3 : 0; // HACK '3' should be part of the style
			elementSize.y = Math.max(labelSize.y, iconSize.y);
		} else {
			elementSize.y = labelSize.x + iconSize.x;
			elementSize.y += (iconSize.y > 0 && labelSize.y > 0) ? 3 : 0; // HACK '3' should be part of the style
			elementSize.x = Math.max(labelSize.x, iconSize.x);
		}
		elementSize = inflateForStyle(elementSize, element);
		Rectangle labelRect = new Rectangle(available.x, available.y, elementSize.x, elementSize.y);
		element.putOpt("drawRect", labelRect);

		return labelRect;
	}

	public Rectangle AdjustForStyle(JSONObject element, Rectangle available) {
		Style style = getStyle(element);
		if (style != null) {
			int lw = style.jsonData.optInt("lw");
			int th = style.jsonData.optInt("th");
			int rw = style.jsonData.optInt("rw");
			int bh = style.jsonData.optInt("bh");
			int lm = style.jsonData.optInt("lm");
			int rm = style.jsonData.optInt("rm");
			int tm = style.jsonData.optInt("tm");
			int bm = style.jsonData.optInt("bm");

			return new Rectangle(available.x + lw + lm, available.y + th + tm, available.width - (lw + lm + rw + rm),
					available.height - (th + tm + bh + bm));
		}

		return available;
	}

	public Rectangle layout(JSONObject element, Rectangle available, boolean horizontal) throws JSONException {
		String layout = element.optString("layout");
		layout = layout.length() == 0 ? "label" : layout;

		switch (layout) {
		case "panel": // Each child eats part of the 'available'
			return panel(element, available, horizontal);
		case "tile":
			return tile(element, available, horizontal);
		case "pack":
			return pack(element, available, horizontal);
		case "label":
			return label(element, available, horizontal);
		}

		return available;
	}

	private void drawStyledCell(GC gc, JSONObject element) {
		Rectangle drawRect = (Rectangle) element.opt("drawRect");

		int curX = drawRect.x;
		int curY = drawRect.y;
		int originalX = drawRect.x;
		int originalY = drawRect.y;

		Style style = getStyle(element);
		if (style != null) {
			int lw = style.jsonData.optInt("lw");
			int th = style.jsonData.optInt("th");
			int rw = style.jsonData.optInt("rw");
			int bh = style.jsonData.optInt("bh");
			int lm = style.jsonData.optInt("lm");
			int tm = style.jsonData.optInt("tm");
			
			int elementWidth = drawRect.width;
			int elementHeight = drawRect.height;

			// Invariants
			Image styleImage = new Image(gc.getDevice(), style.imageData);
			int srcLenH = styleImage.getImageData().width - (lw + rw);
			int dstLenH = elementWidth - (lw + rw);
			int srcLenV = styleImage.getImageData().height - (th + bh);
			int dstLenV = elementHeight - (th + bh);

			// Top
			gc.drawImage(styleImage, 0, 0, lw, th, curX, curY, lw, th);
			curX += lw;
			gc.drawImage(styleImage, rw, 0, srcLenH, th, curX, curY, dstLenH, th);
			curX += dstLenH;
			gc.drawImage(styleImage, rw + srcLenH, 0, rw, th, curX, curY, rw, th);
			curX += lw;

			// Middle
			curX = originalX;
			curY += th;

			gc.drawImage(styleImage, 0, th, lw, srcLenV, curX, curY, lw, dstLenV);
			curX += lw;
			gc.drawImage(styleImage, rw, th, srcLenH, srcLenV, curX, curY, dstLenH, dstLenV);
			curX += dstLenH;
			gc.drawImage(styleImage, rw + srcLenH, th, rw, srcLenV, curX, curY, rw, dstLenV);
			curX += rw;

			// Bottom
			curX = originalX;
			curY = originalY + elementHeight - bh;

			gc.drawImage(styleImage, 0, th + srcLenV, lw, bh, curX, curY, lw, bh);
			curX += lw;
			gc.drawImage(styleImage, rw, th + srcLenV, srcLenH, bh, curX, curY, dstLenH, bh);
			curX += dstLenH;
			gc.drawImage(styleImage, rw + srcLenH, th + srcLenV, rw, bh, curX, curY, rw, bh);
			curX += rw;

			styleImage.dispose();

			curX = originalX + lw + lm;
			curY = originalY + th + tm;
		}

		// Finally draw any icon / label
		if (element.has("icon")) {
			String iconName = element.optString("icon");
			Image iconImage = getIcon(iconName);
			gc.drawImage(iconImage, curX, curY);
			curX += iconImage.getImageData().width;
			if (element.has("label")) {
				curX += 3;
			}
		}
		if (element.has("label")) {
			String fontColor = style.jsonData.optString("font-spec");
			switch (fontColor) {
			case "Black":
				gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
				break;
			case "White":
				gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
				break;
			}
			gc.drawText(element.optString("label"), curX, curY, true);
		}
	}

	public void draw(PaintEvent ev, JSONObject element) throws JSONException {
		drawStyledCell(ev.gc, element);
		if (element.has("contents")) {
			JSONArray kids = element.optJSONArray("contents");
			for (int i = 0, size = kids.length(); i < size; i++) {
				draw(ev, (JSONObject) kids.get(i));
			}
		}
	}

	public JSONObject pick(JSONObject element, int mouseX, int mouseY) {
		Point mouse = new Point(mouseX, mouseY);
		Rectangle elementRect = (Rectangle) element.opt("drawRect");
		if (elementRect.contains(mouse)) {
			if (element.has("contents")) {
				JSONArray kids = element.optJSONArray("contents");
				for (int i = 0, size = kids.length(); i < size; i++) {
					JSONObject found;
					try {
						found = pick((JSONObject) kids.get(i), mouseX, mouseY);
						if (found != null) {
							return found;
						}
					} catch (JSONException e) {
						e.printStackTrace();
						return null;
					}
				}
			} else {
				return element;
			}
		}
		return null;
	}
}
