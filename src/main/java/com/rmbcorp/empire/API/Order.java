package com.rmbcorp.empire.API;

public interface Order {
	
	public String condition(Player requester);
	public byte action(Player requester);
	public String target(Player requester);
	public ID orderID();
	public boolean completed();
	
}
