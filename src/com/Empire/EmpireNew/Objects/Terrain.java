package com.Empire.EmpireNew.Objects;

public enum Terrain {
	UNKNOWN,
	SEA,
	LAND,
	AIR,
	// note that air should never be used for the terrain of a tile, only
	// within an (air) unit to show the type of terrain that unit travels
}
