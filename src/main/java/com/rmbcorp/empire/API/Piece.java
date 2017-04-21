package com.rmbcorp.empire.API;
import java.util.List;

public interface Piece {

	public PieceID id ();
	
	/** Description of equals(Piece that)
	 * @return			1 iff this.pieceId().equals(that.pieceId())
	 * 					0 otherwise
	 */
	public boolean equals(Piece that);
	
	public Player owner();
	
	public boolean isMobile();
	public boolean isCity();
	public boolean isUnit();

	/**
	 * 
	 * @return			a list of units within the city, carrier, or transport
	 * 					null if not a city, carrier, or transport
	 */
	public List<Piece> contents(); //only applicable to carriers and transports

	public Tile location();
	
	/**	Description of visibleTo(Player)
	 * 
	 * This function tells whether a given player has permission to see a given piece.
	 * This will generally be determined by whether the player has permission to see a given tile,
	 * but is returned separately for the piece 
	 * 
	 * @param player	the player whose visibility you want to know about
	 * @return			1 if visible to player, 0 if not
	 */
	
	public boolean visibleTo(Player player);
	public void giveOrder(Player player, Order order);
	public List<Order> orders(Player requester);
	public byte type(); // enum is probably a better implementation for this
}