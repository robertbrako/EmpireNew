package com.rmbcorp.empire.Views;

import com.Empire.EmpireNew.RuleManager;
import com.Empire.EmpireNew.util.ClientUtils;
import com.Empire.EmpireNew.util.Numbers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Visual indicator of progress in game, subclassing View<br>
 * Capabilities:<br>
 * * drawing hexagon grid (filled hexagons + outlines)<br>
 * * panning up, down, left, and right<br>
 * * zooming in & out (re-centering algorithm in-progress)<br>
 * * highlighting selected hexagon (using extensive help from Locator class)<br>
 * * color-coding tiles based on LAND/SEA type and based on visibility<br>
 *  <br>
 * IMPORTANT: Convention is mention rows FIRST, then columns SECOND in all functions.  rows = 1st, cols = 2nd !!!<br>
 * --If it ever comes up, treat 10 rows and 0 columns as 10 rows and 0 columns.  Treat 0 rows and 10 columns as nothing.<br>
 * --need consistent system concerning where invalidate() is called
 * @author robertbrako
 *
 */
public final class BasicGridView extends View {
	
	private float[] optionsPoints;
	private int X0 = 0;
	private int Y0 = 20;
	
	private HexagonContainer allHexagons;
	
	private int rows, cols;
	private float hexSize = 25f;
	private float[] yesX;
	private float[] noX;
	private float[] yesNoY;
	
	private Bitmap cacheBitmap;
	private Canvas cacheCanvas;
	private Paint paint0, paint1, paint2, paint3, paint4, paint5;
	private int outputColorG, outputColorA;
	private Path path;
	
	private byte[][][] mapData; 
	//private byte[][][] playerUnitData; // be aware that byte+byte=(byte) int, not byte
	
	private boolean yesNoClickable = false;
	private boolean showGood = false;
	private int[] moveables;
	
	//private boolean troll = false; // I don't know

	public String mandatoryDialogText = "Welcome to Empire! Hello World!"; // factor out Strings like these
	
	private int[] selection = { Numbers.NULL, Numbers.NULL }; // eventually tiles may be added with negative coordinates, so -1 won't do.

	public BasicGridView(Context context, AttributeSet attrs) { // attrs from activity_main.xml's BasicGridView
		super(context, attrs);
		
		paint0 = new Paint(); // for overall bitmap use
		paint1 = new Paint(); // highlight fills hexagon tiles
		paint2 = new Paint(); // fills hexagon tiles
		paint3 = new Paint(); // for showing unit type
		paint4 = new Paint(); // paints popup/urgent dialog
		paint5 = new Paint(); // paints options dialog
		// setupPaints() instead...
		path = new Path();
	}
	/**prepares hexagon tiles for drawing.  Hexagon objects will be fully initialized, 
	 * so make sure that X0 and Y0 are non-null/non-negative beforehand.
	 * @param rows
	 * @param cols
	 */
	public void init(int rows, int cols) { // receives grid data from MainActivity (may delegate to ViewController later)
		allHexagons = new HexagonContainer(rows, cols);
		for (int i=0; i<rows; i++) {
			for (int j=0; j<cols; j++) {
				allHexagons.add(i, j);
			}
		}
		this.rows = rows;
		this.cols = cols;
	}

	/** Custom implementation of draw() **/
	@Override
	public void draw(Canvas c) { // requisite draw implementation, the main purpose of this class
		super.draw(c);
		cacheBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
		cacheCanvas = new Canvas(cacheBitmap);
		
		// Routine for main grid; ignore flag check until necessary
		Hexagon hex;
		for (int i=0; i<rows; i++) {
			for (int j=0; j<cols; j++) {
				hex = allHexagons.get(i,j);
				float[] hexPoints = hex.setHexPoints(X0, Y0, hexSize);
				paint2.setAntiAlias(false);
				paint2.setStyle(Style.FILL);
				if (mapData != null) {
					switch (mapData[i][j][0]) {
						case 0:
							paint2.setColor(Color.LTGRAY); // UNKNOWN
							break;
						case 1:
							paint2.setColor(Color.BLUE); // SEA
							break;
						case 2:
							paint2.setColor(Color.rgb(170,40,10)); // LAND
							break;
					}
				}
				else {
					paint2.setColor(Color.DKGRAY);
				}
				
				setupPath(path, hexPoints);
				cacheCanvas.drawPath(path, paint2);
				
				paint2.setAntiAlias(true);
				paint2.setStyle(Style.STROKE);
				paint2.setColor(Color.BLACK);
				paint2.setStrokeWidth(hexSize*.16f);
				
				cacheCanvas.drawPath(path, paint2);
			}
		}
		
		//Routine for units; ignore flag check until necessary
		if (mapData != null) {
			byte[] code;
			String outputType;
			for (int i=0; i<rows; i++) {
				for (int j=0; j<cols; j++) {
					code = mapData[i][j];
					//ground unit routine
					outputColorG = Color.argb(255, 255, 0, 255*(code[1]/32/4));
					outputColorA = Color.argb(255, 255, 128, 255*(code[2]/32/4));
					outputType = ClientUtils.interpret((byte) (code[1]%32));
					float y_pos = allHexagons.get(i,j).getHexPoints()[3]+hexSize*.7f;
					float x_pos = allHexagons.get(i,j).getHexPoints()[0]+hexSize*.4f;
					paint3.setColor(outputColorG);
					paint3.setTextSize(hexSize*.7f);
					paint3.setFakeBoldText(true);
					if (code[1] != -1)
						cacheCanvas.drawText(outputType, x_pos, y_pos, paint3);
					/// air units next
					outputType = ClientUtils.interpret((byte) (code[2]%32));
					paint3.setColor(outputColorA);
					if (code[2] != -1)
						cacheCanvas.drawText(outputType, x_pos, y_pos, paint3);
				} // end for (cols)
			} // end for (rows)
		} // end if (unit data is/isn't null)
		
		//Routine if a cell is highlighted
		if (ViewController.checkFlag(ClientUtils.Flag.HIGHLIGHT)) { // selection should be tended to already
			float[] hexPoints = allHexagons.get(selection[0],selection[1]).getHexPoints();
			paint1.setStyle(Style.FILL);
			paint1.setARGB(128, 0, 255, 255);
			setupPath(path, hexPoints);
			cacheCanvas.drawPath(path, paint1);
		}
		
		//moveable tile highlight routine
		if (showGood) {
			paint1.setStyle(Style.FILL);
			paint1.setARGB(128, 20, 255, 255);
			if (moveables != null) {
				for (int i=0; i<moveables.length; i+=2) {
					float[] hltPoints = new float[6];
					hltPoints = allHexagons.get(moveables[i],moveables[i+1]).getHexPoints();
					setupPath(path, hltPoints);
					cacheCanvas.drawPath(path, paint1);
					//below was for testing; delete when you feel like it
					//cacheCanvas.drawText("i:"+i, allHexagons.get(moveables[i],moveables[i+1]).getHexPoints()[0], allHexagons.get(moveables[i],moveables[i+1]).getHexPoints()[3]+hexSize, paint3);
					paint1.setARGB(128, 21+i, 255, 255);
				}
			}
		}
		
		//Options Dialog
		if (ViewController.checkFlag(ClientUtils.Flag.OPTIONS)) {
			paint5.setARGB(240, 240, 240, 255);
			optionsPoints = new float[] { this.getWidth()*.15f, this.getHeight()*.6f, this.getWidth(), this.getHeight() };
			cacheCanvas.drawRect(optionsPoints[0], optionsPoints[1], optionsPoints[2], optionsPoints[3], paint5);
			paint5.setARGB(255, 0, 0, 0);
			paint5.setTextSize(getHeight()*.05f);
			String[] optionText1 = { "SENTRY", "MOVE", "[SAVE]" };
			String[] optionText2 = { "MOVE TO", "EXEC", "CLOSE" }; // change EXEC back to PATROL!!!
			//have fun adjusting the proportions for different form factors...
			for (int i=0; i<optionText1.length; i++) {
				cacheCanvas.drawText(optionText1[i], getWidth()*.2f, getHeight()*(.65f+(i+1)*.1f), paint5);
			}
			for (int i=0; i<optionText2.length; i++) {
				cacheCanvas.drawText(optionText2[i], getWidth()*.6f, getHeight()*(.65f+(i+1)*.1f), paint5);
			}
		}
		
		//Mandatory Dialog / Intro Dialog
		if (ViewController.checkFlag(ClientUtils.Flag.POPUP)) {
			paint4.setARGB(128, 0, 255, 255);
			if (yesNoClickable) {
				cacheCanvas.drawRect(new RectF(this.getWidth()*.15f, this.getHeight()*.2f, this.getWidth()*.85f, this.getHeight()*.6f), paint4);
				yesX = new float[] { this.getWidth()*.25f, this.getWidth()*.45f};
				noX = new float[] { this.getWidth()*.55f, this.getWidth()*.75f };
				yesNoY = new float[] { this.getHeight()*.4f, this.getHeight()*.5f };
				paint4.setARGB(255, 0, 255, 0);
				cacheCanvas.drawRect(new RectF(yesX[0], yesNoY[0], yesX[1], yesNoY[1]), paint4);
				paint4.setARGB(255, 255, 0, 0);
				cacheCanvas.drawRect(new RectF(noX[0], yesNoY[0], noX[1], yesNoY[1]), paint4);
			}
			else {
				cacheCanvas.drawRect(new RectF(this.getWidth()*.15f, this.getHeight()*.2f, this.getWidth()*.85f, this.getHeight()*.4f), paint4);
			}
			
			paint4.setARGB(255, 0, 0, 0);
			paint4.setTextSize(this.getHeight()*.03f); // caution: need to account for resolution
			paint4.setFakeBoldText(true);
			cacheCanvas.drawText(mandatoryDialogText, this.getWidth()*.20f, this.getHeight()*.3f, paint4); //Q: can drawText() be a hacking entry point? Probably not, but...
		}
		//showGood = false;
		c.drawBitmap(cacheBitmap,  0, 0, paint0);
	}
	/**Note that ViewController controls app flow logic<br>
	 * To perform click, we get status of things from VC and then respond accordingly.
	 * Response is to give VC current data about grid position & zoom, if needed.
	 * @return true if event was processed here without issue
	 */
	@Override
	public boolean performClick() { // Handles clicks; stuff can go awry if you falsely return false, I've learned
		super.performClick();
		int[] origin = {X0, Y0};
		int[] dimensions = {rows, cols};

		if (ViewController.getStatus() == 0) { // Scenario: close initial dialogue and proceed to tutorial mode query
			//flags stay same, though status and yes is changed by VC
			return true;
		}
		if (ViewController.getStatus() == 1) { // Scenario: yes/no up and user clicked
			//CAUTION: may need to check if window was resized
			ViewController.updateYN(yesX, noX, yesNoY);
			return true;
		}
		if (ViewController.getStatus() == 2) { // Scenario: Neutral
			ViewController.updateBoard(origin, dimensions, hexSize);
			return true;
		}
		if (ViewController.getStatus() == 3) { // Scenario: options menu up
			ViewController.updateWH(getWidth(), getHeight());
			ViewController.updateBoard(origin, dimensions, hexSize);
		}
		if (ViewController.getStatus() == 4) { // Scenario: options menu closed, awaiting tile selection
			ViewController.updateBoard(origin, dimensions, hexSize);
		}
		else {  }
		return true;
	}
	/** Facilitates panning by changing X0 and Y0; zoom by passing special code in first parameter.<br>
	 * It's possible to pan left/right and up/down at same time if desired.
	 * @param up_down is usually amount to pan up/down by, but:<br>
	 * Passing -999000 here is code for zoom in, and -999001 is code for zoom out.
	 * @param left_right is always amount to pan left/right by.  May be 0, of course.
	 */
	public void panZoom(int up_down, int left_right) { // one stop zoom-in/out and pan u/d/l/r
		if (up_down == -999000) { // code for zoom in
			hexSize*=2;
			displace(2f);
		}
		else if (up_down == -999001) { // code for zoom out
			hexSize*=.5;
			displace(.5f);
		}
		else {
			Y0 += up_down*1.5*hexSize;
			X0 += left_right*1.5*hexSize;
		}
		invalidate();
	}
	/** On Click, ViewController may tell us that a location was selected.<br>
	 * This method notifies BGV in case we need to draw a highlight or other response.
	 * @param arg0 row number of selection
	 * @param arg1 column number of selection
	 */
	public void select(int arg0, int arg1) {
		selection[0] = arg0;
		selection[1] = arg1;
	}
	/** Part of flow logic, this indicates whether a yes/no box should be drawn **/
	public void setYesNo(boolean value) {
		yesNoClickable = value;
	}
	/** Used to set dialog box message to a given String prior to drawing **/
	public void setMsg(String message) {
		mandatoryDialogText = message;
	}
	/** Updates BGV with matrix of bytes; each byte indicates what units are present
	 * @param dataFromController a a rows*cols*unitData byte array, where unitData is<br>
	 * a 4-array with slots for ground unit type (code), no. of fighters, no. of bombers, & no. of satellites
	 */
	public void setMapData(byte[][][] dataFromController) {
		mapData = dataFromController;
	}
	/** Used to highlight all tiles that a piece can move to
	 * @param row row of selected piece
	 * @param col column of selected piece
	 * @param movesPerTurn is... maybe not needed here.
	 */
	public void showMoveable(int row, int col, int movesPerTurn) {
		if (movesPerTurn == -1) {
			showGood = false;
			return;
		}
		showGood = true;
		selection[0] = row;
		selection[1] = col;
		moveables = RuleManager.getValidTiles(selection[0], selection[1], RuleManager.getIntData((byte) (mapData[selection[0]][selection[1]][1]%32), (byte) 1));
		// special check if piece is bomber; assume there is some kind of piece if this method is called
		if (moveables.length < 12) // minimum length 12 = 6 points = 6 valid tiles for piece that can move 1-per-turn
			moveables = RuleManager.getValidTiles(selection[0], selection[1], RuleManager.getIntData((byte) (mapData[selection[0]][selection[1]][2]%32), (byte) 1));
			// altered search to include bombers, which are in index [2] instead of [0]
		for (int i=0; i<moveables.length; i+=2) {
			if (moveables[i]<0 || moveables[i+1]<0 || moveables[i]>=rows || moveables[i+1]>=cols) {
				//prepare array for clean drawing
				moveables[i]=row;
				moveables[i+1]=col;
			}
		}
		invalidate();
	}

	//////////////////////////////////////////////////////////
	private void setupPath(Path tempPath, float[] hexPoints) {
		tempPath.reset();
		allHexagons.get(0, 0).getHexPoints();
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
	
	private void displace(float amt) { // re-center based on zoom... I'll need to sketch out the math and revise
		float w = this.getWidth();
		float h = this.getHeight();
		float r = Hexagon.CalculateR(hexSize);
		
		if (amt == 2f) {
			X0-=w/(2*r);
			Y0-=h/(2*r);
		}
		else if (amt == .5f) {
			X0+=w/(16*r);
			Y0+=h/(16*r);
		}
	}

///////////////////////////////////////
	private class HexagonContainer {
		
		Hexagon[][] hexagonContainer;
		
		HexagonContainer(int rows, int cols) { // fixed size upon construction; may need linked-list implementation later
			hexagonContainer = new Hexagon[rows][cols];
		}
		
		@SuppressWarnings("synthetic-access")
		void add(int row, int col) {
			hexagonContainer[row][col] = new Hexagon(row, col, X0, Y0, hexSize);
		}
		
		Hexagon get(int localRow, int localCol) { // no bounds checking
			return hexagonContainer[localRow][localCol];
		}
	}
}