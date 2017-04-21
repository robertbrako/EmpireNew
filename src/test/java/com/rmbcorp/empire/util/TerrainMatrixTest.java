package com.rmbcorp.empire.util;

import com.rmbcorp.empire.Objects.Terrain;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class TerrainMatrixTest {

    private Terrain[][] terrainMock;

    @Before public void setUp() {
        terrainMock = new Terrain[10][10];
    }

    @Test /** Simply copy-pasted from TerrainMatrix for now; can be made more proper later **/
    public void stressTest() {
        Random r = new Random();
        //first assume the quad function works:
        int x0 = 3, y0 = 3; // assuming grid of 16x16, let's just use first quadrant
        float seedRow, seedCol;
        System.out.println("Initial Condition: Row = " + x0 + ", Col = " + y0);
        for (int i=0; i<100; i++) {
            seedRow = x0+(r.nextFloat()-0.5f)*(x0- TerrainMatrix.seedDist);
            seedCol = y0+(r.nextFloat()-0.5f)*(y0- TerrainMatrix.seedDist);
            System.out.println("Try " + i + ": Row = " + seedRow + ", Col = " + seedCol);
        }
        System.out.println("Now for other quadrant: Row = 11, Col = 11");
        x0=12;
        y0=12;
        for (int i=0; i<100; i++) {
            seedRow = x0+(r.nextFloat()-0.5f)*(terrainMock.length/2-x0);
            seedCol = y0+(r.nextFloat()-0.5f)*(terrainMock.length/2-y0);
            System.out.println("Try " + i + ": Row = " + seedRow + ", Col = " + seedCol);
        }
        System.out.println("End of stress test");
    }
}
