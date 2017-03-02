package com.Empire.EmpireNew.Objects;

import java.util.List;
import java.util.ArrayList;

import com.Empire.EmpireNew.API.BackendTile;
import com.Empire.EmpireNew.API.BasicPlayer;
//import com.Empire.EmpireNew.API.GameExceptions;
import com.Empire.EmpireNew.API.Order;
import com.Empire.EmpireNew.API.Piece;
import com.Empire.EmpireNew.API.PieceID;
import com.Empire.EmpireNew.API.Player;
import com.Empire.EmpireNew.API.Tile;
import com.Empire.EmpireNew.API.GameExceptions.InvalidMoveException;

public class GamePiece implements Piece {

	protected BackendTile location;
	protected GamePieceID id;
	protected byte type;
	protected Player owner;
	protected List<Order> orders;
	private int productionAccumulated;
	
	/**Game pieces will be created statically... soon **/
	public static boolean createPiece(Tile startLocation, Player ownerP) {
		Piece piece = new GamePiece((BackendTile) startLocation, ownerP);
		ownerP.givePiece(piece);
		return true;
	}
	
	public GamePiece() {
		
	}
	
	public GamePiece(BackendTile startLocation, Player ownerP) {
		location = startLocation;
		owner = ownerP;
		type = PieceType.CITY;
		id = new GamePieceID();
		startLocation.addCity(this);
		orders = new ArrayList<Order>();
	}
	
	@Override
	public PieceID id() {
		return id;
	}

	@Override
	public boolean equals(Piece that) {
		if(that==null) {
			return false;
		} else {
			return this.id().equals(that.id());
		}
	}

	@Override
	public Player owner() {
		return owner;
	}

	@Override
	public boolean isMobile() {
		return false;
	}

	@Override
	public boolean isCity() {
		if (type == PieceType.CITY) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isUnit() {
		return false;
	}

	@Override
	public List<Piece> contents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tile location() {
		return location;
	}

	@Override
	public boolean visibleTo(Player player) {
		return owner.equals(player);
	}

	@Override
	public void giveOrder(Player player, Order order) {
		if(owner.equals(player)){
			orders.add(order);
		} else {
			// fail silently
		}
	}

	@Override
	public List<Order> orders(Player requester) {
		if(owner.equals(requester)) {
			return orders;
		} else {
			return null;
		}
	}
	
	void giveToPlayer(BasicPlayer p) {
		p.givePiece(this);
		owner = p;
	}

	@Override
	public byte type() {
		return type;
	}
	
	public GamePiece executeNextOrder() {
		if(this.isCity()) {
			productionAccumulated++; // regardless of what orders are, cities get closer to building unit
		}
		if(orders.isEmpty()) { // to avoid out of range exceptions
			return null;
		}
		if(orders.get(0).action(owner) == 9 && isMobile()) { // need code for action = "PRODUCE"
			int cost = 0;
			try {
				GameUnit K = new GameUnit(location, owner, orders.get(0).action(owner));
				cost = GameUnit.getIntData(orders.get(0).action(owner), "productionCost");
				if(cost <= productionAccumulated) {
					orders.remove(0);
					return K;
				}
			} catch (InvalidMoveException e) { }
			//int cost = GameUnit.typeData.get(K.type()).productionCost;
			
		} else {
			orders.remove(0); // pop the order if it's not meaningful 
		}
		// orders.remove(0); // pop the next order off whether it's meaningful or not
		return null;
	}
	
	@Override
	public String toString() {
		return id().toString();
	}
}
