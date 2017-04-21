package com.rmbcorp.empire.Objects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.Empire.EmpireNew.API.BasicPlayer;
import com.Empire.EmpireNew.API.BasicPlayerID;
import com.Empire.EmpireNew.API.Piece;
import com.Empire.EmpireNew.API.Tile;
import com.Empire.EmpireNew.API.Unit;
import com.Empire.EmpireNew.util.SaveData;

public class TerminalPlayer extends BasicPlayer {
	
	private List<Piece> pieces;
	BufferedReader in;
	
	public TerminalPlayer () {
		id = new BasicPlayerID();
		in = new BufferedReader (new InputStreamReader(System.in));
		pieces = new ArrayList<Piece>();
	}
	
	@Override
	public void sendMsg(String msg) {
		System.out.println(msg);
		return;
	}
	
	@Override
	public void conductTurn() {
		String command;
		Tile current = pieces.get(0).location(); //should catch IndexOutOfBoundsException and treat it as player having lost the game if caught, just to be safe, but this will be handled elsewhere
		Piece curPiece = pieces.get(0); //ditto with above
		command = new String("0");
		
		do {
			sendMsg("Currently at tile: " + current.id());
			sendMsg("Tile blank? " + current.isBlank());
			
			if(current.visibleTo(this)) {
				sendMsg("This tile's terrain is " + current.tileTerrain(this));
				sendMsg("On the ground in this tile is Piece " + current.gContents(this) + " which is a(n) "
						+ (current.gContents(this)==null ? "null" : current.gContents(this).type()) + ".");
				sendMsg("In the air in this tile is:");
				if(current.airContents(this).isEmpty()) {
					sendMsg("  Nothing.");
				} else {
					for(Unit p : current.airContents(this)) {
						sendMsg("  Unit " + p.id() + " which is a " + p.type()
								+ " owned by Player " + p.owner().id() + ".");
					}
				}
			} else {
				sendMsg("This tile is not visible to you at this time.");
			}
			
			for(int d = 0; d<= 5; d++) {
				sendMsg("Next tile in dir " + d + " is " + (current.next(d)!=null ? current.next(d).id().toString() : "null"));
			}
			
			sendMsg("Your currently selected piece is Piece " + curPiece.id() +" which is " + (curPiece.location().equals(current)?"":"not ") + "at this location.");
			
			sendMsg("Enter command (h for help, done to end turn)");
			try {
				command = in.readLine();
			} catch(IOException ioe) { 
				//fail silently
			}
			
			if(command.equals("m")) {
				sendMsg("Move in which dir [0-5]?");
				try {
					current = current.next(Integer.valueOf(in.readLine()));
					// hexDrawGUI.status.setText("Moved successfully, I think");
				} catch(IOException ioe) {
						// fail silently
				}
			}
			/**
			if(command.equals("h")) {
				sendMsg("Command summary:");
				sendMsg("m - move");
				sendMsg("h - see this summary");
				sendMsg("done - end your turn");
				sendMsg("n - move to next piece");
				sendMsg("o - give orders to your selected piece");
				hexDrawPanel.showHelp();
			}
			**/
			if(command.equals("n")) {
				int curIndex = pieces.indexOf(curPiece);
				if(pieces.size()<=curIndex+1) {
					curPiece = pieces.get(0); //wrap around if already at player's last piece
				} else {
					curPiece = pieces.get(curIndex+1);
				}
				sendMsg("This piece is a(n) " + curPiece.type() + ".");
				sendMsg("Its piece ID is " + curPiece.id().toString() + ".");
				sendMsg("The piece " + ((curPiece.isMobile())? "is" : "is not") + " mobile.");
				sendMsg("The piece " + ((curPiece.isUnit())? "is" : "is not") + " a unit.");
				sendMsg("The piece " + ((curPiece.isCity())? "is" : "is not") + " a city.");
				sendMsg("The piece is at tile " + curPiece.location().id().toString() + ".");
				sendMsg("");
				// hexDrawGUI.status.setText("Console displaying info about next piece");
				
				sendMsg("Move to this piece's location? (Y/N)");
				String booleanAnswer;
				try {
					booleanAnswer = in.readLine();
				} catch(IOException ioe) { 
					booleanAnswer = new String("N");//fail silently
				}
				if(booleanAnswer.equals("Y")||booleanAnswer.equals("y")) {
					current = curPiece.location();
					// hexDrawGUI.status.setText("Moved to next piece's location");
				}
				
			}
			
			if(command.equals("o")) {
				sendMsg("What order? (MoveDir, Produce, and Goto supported)");
				String action;
				try {
					action = in.readLine();
				} catch(IOException ex) {
					action = "MoveDir";
				}
				String target = "";
				if(action.equals("MoveDir")) {
					sendMsg("In which direction (0-5)?");
					try {
						target = in.readLine();	
					} catch(IOException ex) {
						target = "0";
					}
					System.out.println("Refusing to mopve in direction " + target);
				}
				if(action.equals("Produce")) {
					sendMsg("Produce what type of unit?");
					try {
						target = in.readLine();	
					} catch(IOException ex) {
						target = "Army";
					}
				}
				//curPiece.giveOrder(this, new GameOrder("", action, target));
			}
		} while(!command.equals("done"));
		
		
		List<Piece> toRemove = new ArrayList<Piece>();
		for(Piece p : pieces) {
			if(p.owner()!=this) {
				toRemove.add(p);
			}
		}// weed out any that no longer have this owner
		for(Piece p : toRemove) {
			pieces.remove(p);
		}// (requires 2 loops to avoid breaking pieces.iterator() )
	}
	
	@Override
	public void givePiece(Piece p) {
		givePiece((GamePiece) p);
	}
	
	@Override
	public void givePiece(GamePiece p) { // p must be properly initialized before giving to player
		if(p==null) {
			return;
		}
		
		if(!pieces.contains(p)) { //avoiding duplicates, which would make life complicated
			pieces.add(p);
		}
	}

	@Override
	public SaveData save() {
		// TODO Auto-generated method stub
		return new SaveData();
	}
}
