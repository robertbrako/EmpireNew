package com.Empire.EmpireNew;

import java.util.ArrayList;

import com.Empire.EmpireNew.Objects.BasicGrid;
import com.Empire.EmpireNew.Objects.GameTile;
import com.Empire.EmpireNew.API.Player;
import com.Empire.EmpireNew.Objects.TerminalPlayer;
import com.Empire.EmpireNew.Objects.Terrain;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;


public class BasicGridViewOld extends View {

	public static final int X0 = 20; // eventually change to non-final gettable
	public static final int Y0 = 20;
	public BasicGrid basicGrid;
	private TerminalPlayer player;
	
	private int rows = 10;
	private int columns = 10;
	
	private boolean alreadyDrawn = false;
	private boolean onlyFillIn = false;
	private int mode = 0;
	
	private float hexSize = 25f; // determines size of pretty much everything and affects many calculations.
	private float h;
	private float r;
	private float fac;
	// consider also bringing variables x,y here instead of in Hexagon
	
	private Canvas cacheCanvas;
	private Bitmap cacheBitmap;
	private Paint paint1, paint2;
	private Path path1, path2;
	private ArrayList<Hexagon> wireframe;
	
	public BasicGridViewOld(Context context, AttributeSet attrs) { // remember: other constructors possible
		super(context, attrs);
		paint1 = new Paint();
		paint2 = new Paint();
		paint1.setAlpha(0);
		// paint1.setStyle(Style.FILL);
		paint2.setStyle(Style.FILL);

		path1 = new Path(); // for main grid
		path2 = new Path(); // for coloring in individual Hexagons

		wireframe = new ArrayList<Hexagon>();
		basicGrid = new BasicGrid((rows-2), (columns-2));
		
		setupEqs();
		repopulateWireFrame();
	}
	
	void setPlayer(Player player) { // this might be wack
		this.player = (TerminalPlayer) player;
	}
	
	public void draw(Canvas c) {
		super.draw(c);
		
		if (true) {		// wireframe should be already populated
			cacheBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
			cacheCanvas = new Canvas(cacheBitmap);	// guess: as I do things to canvas, I do things to cacheBmp
				paint1.setStyle(Style.FILL);
				
				// ------------------IMPORTANT FOR LOOP------------------
				for (GameTile gameTile : basicGrid.contents) {
					path1.reset();
					setupPath(path1, basicGrid.contents.indexOf(gameTile));
					if (!gameTile.isBlank()) {
						if (gameTile.visibleTo(player)) {	// wait, does visibleTo imply not blank?
							if (gameTile.tileTerrain(player) == Terrain.LAND) {
								paint1.setColor(Color.rgb(125, 80, 80));
							}
							else if (gameTile.tileTerrain(player) == Terrain.SEA) {
								paint1.setColor(Color.CYAN);
							}
							else { paint1.setColor(Color.BLACK); }
						}
						else {
							paint1.setColor(Color.LTGRAY);
						}
					}
					else if (gameTile.isBlank()) {
						paint1.setColor(Color.LTGRAY);
					}
					
					cacheCanvas.drawPath(path1, paint1);
					System.out.println("Drawing something at column #" + wireframe.get(basicGrid.contents.indexOf(gameTile)).getHexPoints()[0]);
					/**
					if (!gameTile.isBlank() && gameTile.visibleTo(player)) {	// second pass, to take care of units
						
						if (gameTile.gContents(player) != null) {
							
							if (gameTile.gContents(player).type() == "Army") {
								paint1.setColor(Color.RED);
							}
							else if (gameTile.gContents(player).type() == "Bomber") {
								paint1.setColor(Color.BLUE);
							}
							float[] temp = wireframe.get(basicGrid.contents.indexOf(gameTile)).getHexPoints();
							
							//resource below does not exist at present
							//Bitmap testo = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
							//Bitmap testo1 = Bitmap.createScaledBitmap(testo, (int)(r+r), (int)hexSize, false);
							Paint xPaint = new Paint();
							xPaint.setColor(Color.BLACK);
							cacheCanvas.drawRoundRect(new RectF(temp[0],temp[1],temp[10],temp[11]), (r+r), hexSize, xPaint);
							//xPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
							//cacheCanvas.drawBitmap(testo1, temp[0], temp[1], null);
							// cacheCanvas.drawBitmap(Bitmap.createBitmap((int)(r+r), 15, Bitmap.Config.ARGB_8888), temp[0], temp[1], xPaint);
						}
					}
					**/
				}
			// -------end for loop: next add on wireframe------------------------------------
			paint1.setColor(Color.BLACK);
			paint1.setStyle(Style.STROKE);
			paint1.setAntiAlias(true);
			
			for (int i=0; i<wireframe.size(); i++) {
				cacheCanvas.drawLines(wireframe.get(i).getHexPoints(), paint1);
			}
			paint1.setAntiAlias(false);
			
			c.drawBitmap(cacheBitmap,  0, 0, paint1);
				//alreadyDrawn = true;
				//paint1.reset(); // ??
		}
		/**
		if (onlyFillIn == true) {
			c.drawBitmap(cacheBitmap,  0, 0, new Paint());
			if (mode == 2) {
				paint2.setColor(Color.BLUE);
			}
			else if (mode == 3) {
				paint2.setColor(Color.WHITE);
			}
			else {
				paint2.setColor(Color.BLACK);
			}

			c.drawPath(path2, paint2);

			onlyFillIn = false;
		}
		**/
	}

	public int[] update(View v, MotionEvent e) { // ultimately change to Hexagon instead of float

		try {
			
			Hexagon hgwells = findHexByTouchEvent(e);
			if (hgwells.getSize() == 3.14159f) { // clicked in the in-between area
				if (hexSize != 25f) {
					reSize(25f);
				}
				else {
					reSize(40f);
				}
				return new int[] { -1, -1 };
			}
			else if (hgwells.getSize() == 1.414f) {
				return new int[] { -1, -1 };
			}
			// now, select cell if above don't apply
			path2.reset();
			setupPath(path2, hgwells.getRow()*columns+hgwells.getCol());
			//onlyFillIn = true;
			invalidate();
			if (basicGrid.contents.get(wireframe.indexOf(hgwells)).gContents(player) != null) {
				return new int[] { hgwells.getRow(), hgwells.getCol(),  1};
			}
			else {
				return new int[] { hgwells.getRow(), hgwells.getCol(),  0};
			}
		}
		catch (Exception ex) { // for clicking out of bounds
			return new int[] { -1, -1 };
		}

	}
	
	private Hexagon findHexByTouchEvent(MotionEvent e) {
		float x = e.getX();
		float y = e.getY();
		float rY = (y-fac);
		float rX = (x-X0);
		int intY = (int) FloatMath.floor(rY);
		// int intX = (int) FloatMath.floor(rX);
		int side = (int) hexSize;	// don't worry about rounding: hexSize is only float b/c of calcs
		int row;
		int col;

		if (intY % (2*(side+h)) >= 0 && intY % (2*(side+h)) < (h)) {
			// Region hard: need more complex test
			row = 0;
			col = 0;
			return new Hexagon(row, col, 3.14159f);
		}
		else if (intY % (2*(side+h)) >= (h) && intY % (2*(side+h)) < (h+side)) {
			// Region easy: simple test for which Hexagon
			
			row = 2 * (int) FloatMath.floor(rY / (float) (2*(side+h))); // later, accommodate negatives
			
			// now, what column are we in?
			col = (int) FloatMath.floor(rX / (float) (r+r));
			
			// Dependency: panning grid right meansHexagon.x--, graphically.  col and row should not change,
			//  but here col is based on a static canvas.  fix: if we pan, then add or remove that amount
			//  to e.getX(), and that should fix the as-yet-non-existent problem
			return pullHexagon(row, col);
		}
		else if (intY % (2*(side+h)) >= (h+side) && intY % (2*(side+h)) < (h+side+h)) {
			col = 0;
			row = 0;
			return new Hexagon(row, col, 3.14159f);
		}
		else if (intY % (2*(side+h)) >= (h+side+h) && intY % (2*(side+h)) < (h+side+h+side)) {
			// also easy case
			row = (2 * (int) FloatMath.floor((float) rY / (float) (2*(side+h)) ) + 1);
			col = (int) FloatMath.floor((rX-r) / (float) (r+r));
			return pullHexagon(row,col); // make error obvious enough
		}
		else {  // above cases should be -almost- exhaustive
			col = 0;
			row = 0;
			return new Hexagon(row,col,1.414f); // make error obvious enough
		}
		// finally, we should have a col and row to pull a Hexagon out of the array
	}
	
	private void setupPath(Path path, int index) {
		float[] temp = wireframe.get(index).getHexPoints();  // could do multiple hex's at once if needed
		path.moveTo(temp[0], temp[1]);
		for (int i=2; i<temp.length/2; i+=2) {	// this is a pretty sweet for loop
			path.lineTo(temp[i], temp[i+1]);
		}
		path.close();
	}
	
	public void reSize(float newSize) { // this may eventually become private
		if (newSize > 0) {
			
			hexSize = newSize;
			setupEqs();
			repopulateWireFrame(); // at this point, the abstract Hexagons become different sizes
			alreadyDrawn = false;  // now it's appropriate to redraw everything
			invalidate(); // still need to draw the visual Hexagons
			
		}
	}
	
	private void setupEqs() {
		h = Hexagon.CalculateH(hexSize);
		r = Hexagon.CalculateR(hexSize);
		fac = Y0 - h;
	}
	
	private void repopulateWireFrame() {  // presently resizes but doesn't change total count of hexagons
		wireframe.clear();
		for (int i=0; i<rows; i++) {
			for (int j=0; j<columns; j++) {
				wireframe.add(new Hexagon(i, j, hexSize));  // make sure hex's are being added to correct index
			}
		}
	}
	
	public Hexagon pullHexagon(int id) {
		return wireframe.get(id);
	}
	
	public Hexagon pullHexagon(int row, int col) {
		int id = columns*row + col;
		return wireframe.get(id);
	}
}
