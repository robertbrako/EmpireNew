package com.Empire.EmpireNew.Views;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**MainBarView displays tile data and provides buttons for panning and zooming.
 * 
 * @author Aspire
 *
 */
public final class MainBarView extends View {
	
	private int X0 = 32;
	private int Y0 = 16;
	private int hexSize = 20;
	private int turnNumberCopy = 0;
	
	private ArrayList<Hexagon> allHexagons;
	private int[] highlight = { 0, 0, 0, 0, 0, 0};
	
	private Bitmap cacheBitmap;
	private Canvas cacheCanvas;
	private Paint paint0, paint1, paint2, paint3;
	private Path path;
	private float[] hexPoints = new float[12];
	
	private String[] infoText;

	/** Constructor follows View subclass constructor and initializes some variables. **/
	public MainBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint0 = new Paint();
		paint1 = new Paint();
		paint2 = new Paint();
		paint3 = new Paint();
		setupPaints(2);
		path = new Path();
		allHexagons = new ArrayList<Hexagon>();
		setupHexagons();
	}
	/** Override of draw in order to display custom content **/
	@Override
	public void draw(Canvas c) {
		super.draw(c);
		cacheBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
		cacheCanvas = new Canvas(cacheBitmap);
		
		// using hexagons for the buttons
		for (Hexagon hex : allHexagons) {
			//float[] hexPoints = hex.setHexPoints(X0,Y0,hexSize); // these vars are fixed for MBV; feel free to delete this line
			hexPoints = hex.getHexPoints();
			paint1.setStyle(Style.FILL);
			paint1.setColor(Color.RED);
			
			setupPath(path, hexPoints);
			cacheCanvas.drawPath(path, paint1);
			
			paint1.setAntiAlias(true);
			paint1.setStyle(Style.STROKE);
			paint1.setColor(Color.BLACK);
			paint1.setStrokeWidth((float) (hexSize*.2));
			
			cacheCanvas.drawPath(path, paint1);
			
			//let's use hexPoints to draw lines indicating function
			float[] lines = setupLines(hexPoints, hex.getRow(), hex.getCol());
			setupPath2(path, lines);
			paint1.setStrokeWidth((float) (hexSize*.04));
			
			cacheCanvas.drawPath(path, paint1);
			cacheCanvas.drawText("T: " + turnNumberCopy, getWidth()*.90f, getHeight()*.5f, paint1);
		}
		
		for (int i = 0; i < highlight.length; i++) {
			if (highlight[i] == 1) {
				setupPath(path, allHexagons.get(i).getHexPoints());
				cacheCanvas.drawPath(path, paint2);
			}
		}
		
		setupPaints(3);
		if (infoText != null) {
			for (int i = 0; i < infoText.length; i++) {
				c.drawText(infoText[i], 16, 32+24*i, paint3);
			}
		}

		c.drawBitmap(cacheBitmap,  0, 0, paint0);
	}
	/** Implementation of perform click... returning false (not anymore) for test purposes **/
	@Override
	public boolean performClick() {
		super.performClick();
		return true;
	}
	/** Applies logic to click; should be factored out and done in a way similar to BGV
	 * @param data is row,col data figured out in Locator.  Kind of a hack.
	 * @param mode is just flow control: true for mouseUp, false for mouseDown
	 * @return codes for what to do next: if mouseDown, 0-5 indicates which MBV button to highlight; if mouseUp, return codes to zoom or pan BGV
	 **/
	public int[] analyzeClick(int[] data, boolean mode) {
		int[] result = {-1, -1};
		int[] result2 = {-1};
		
		if (data[0] == 0 && data[1] == 11) {
			result[0] = 20/10;
			result[1] = 0;
			result2[0] = 0;
		}
		else if (data[0] == 1 && data[1] == 10) {
			result[0] = 0;
			result[1] = 20/10;
			result2[0] = 1;
		}
		else if (data[0] == 1 && data[1] == 11) {
			result[0] = 0;
			result[1] = -20/10;
			result2[0] = 2;
		}
		else if (data[0] == 2 && data[1] == 11) {
			result[0] = -20/10;
			result[1] = 0;
			result2[0] = 3;
		}
		else if (data[0] == 0 && data[1] == 9) {
			result[0] = -999000;
			result[1] = 0;
			result2[0] = 4;
		}
		else if (data[0] == 2 && data[1] == 9) {
			result[0] = -999001;
			result[1] = 1;
			result2[0] = 5;
		}
		if (mode == true)
			return result; // for mouseUp
		else
			return result2; // for mouseDown
	}
	/** Set message text for user to see.  Do up to 3 lines of text
	 * @param text a String[] of up to 3 entries.  Nothing happens for String arrays with 4 or more entries.
	 */
	public void setText(String[] text) {
		if (text.length <= 3)
			infoText = text;
		invalidate();
	}
	/** Modifies highlight matrix, which draw() looks at in order to highlight button onMouseDown
	 * @param selection either { x } : x = 0, 1, ..., 5 (indicating which button to highlight), or null to dehighlight
	 */
	public void highlight(int[] selection) {
		if (selection == null) {
			for (int i = 0; i < highlight.length; i++) {
				highlight[i] = 0;
			}
		}
		else { // in this case, selection is { x }, for x = 0, 1, .., 5
			highlight[selection[0]] = 1;
		}
		invalidate();
	}
	/** update local copy of turn number **/
	public void updateTurnNumber(int number) {
		turnNumberCopy = number;
	}
	
	private void setupHexagons() {
		allHexagons.add(new Hexagon(0,11, X0, Y0, hexSize)); // U
		allHexagons.add(new Hexagon(1,10, X0, Y0, hexSize)); // L
		allHexagons.add(new Hexagon(1,11, X0, Y0, hexSize)); // R
		allHexagons.add(new Hexagon(2,11, X0, Y0, hexSize)); // D
		allHexagons.add(new Hexagon(0,9, X0, Y0, hexSize));  // +
		allHexagons.add(new Hexagon(2,9, X0, Y0, hexSize));  // -
	}
	
	private void setupPath(Path tempPath, float[] hexPoints) {
		tempPath.reset();
		tempPath.moveTo(hexPoints[0], hexPoints[3]);
		tempPath.lineTo(hexPoints[1], hexPoints[4]);
		tempPath.lineTo(hexPoints[2], hexPoints[3]);
		tempPath.lineTo(hexPoints[2], hexPoints[5]);
		tempPath.lineTo(hexPoints[1], hexPoints[6]);
		tempPath.lineTo(hexPoints[0], hexPoints[5]);
		tempPath.close();
		/* Old algorithm in case you want to use 6 pt array again
		for (int i = 2; i < hexPoints.length; i+=2) {
			tempPath.lineTo(hexPoints[i], hexPoints[i+1]);
		}
		tempPath.lineTo(hexPoints[0],hexPoints[1]);
		*/
	}
	
	private void setupPath2(Path tempPath, float[] hexPoints) {
		tempPath.reset();
		tempPath.moveTo(hexPoints[0],hexPoints[1]);
		for (int i = 2; i < hexPoints.length; i+=2) {
			tempPath.lineTo(hexPoints[i], hexPoints[i+1]);
		}
		tempPath.lineTo(hexPoints[hexPoints.length-2],hexPoints[hexPoints.length-1]);
	}
	
	private float[] setupLines(float[] hexPoints, int thisRow, int thisCol) {
		float[] lines = new float[6];
		float xBasis = hexPoints[0];
		float yBasis = hexPoints[1];
		float r = Hexagon.CalculateR(hexSize);

		if ((thisRow == 0 || thisRow == 2) && thisCol == 11) {
			lines[0] = xBasis+r/2;
			lines[2] = xBasis+r;
			lines[4] = xBasis+1.5f*r;
			lines[1] = yBasis+hexSize/2;
			lines[3] = yBasis+(hexSize*(1-(thisRow+1)%3)); // add hexSize*0 for row 0, hexSize*1 for row 2
			lines[5] = yBasis+hexSize/2;
		}
		else if (thisRow == 1) { // col is either 10(L) or 11(R)
			lines[0] = xBasis+r;
			lines[2] = xBasis+r+(0.7f*r*(2*(thisCol%2-0.5f)));
			lines[4] = xBasis+r;
			lines[1] = yBasis;
			lines[3] = yBasis+hexSize/2;
			lines[5] = yBasis+hexSize;
		}
		else if (thisCol == 9) {
			lines[0] = xBasis+r/2;
			lines[2] = xBasis+1.5f*r;
			lines[1] = yBasis+hexSize/2;
			lines[3] = lines[1];
			if (thisRow == 0) {
				lines[4] = xBasis+r;
				lines[5] = yBasis;
			}
			else { // awkward - use separate functions for the lines
				lines[4] = xBasis+r/2;
				lines[5] = yBasis+hexSize/2;
			}
		}
		
		return lines;
	}
	
	private void setupPaints(int code) {
		if (code == 2) {
			paint2.setStyle(Style.FILL);
			paint2.setARGB(128, 0, 255, 255);
		}
		if (code == 3) {
			paint3.setARGB(255, 64, 0, 0);
			paint3.setAntiAlias(true);
			paint3.setTextSize(24);
		}
	}
}
