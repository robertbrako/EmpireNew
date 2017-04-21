package com.rmbcorp.empire;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import com.rmbcorp.empire.Objects.BasicGrid;
import com.rmbcorp.empire.Objects.Controller;
import com.rmbcorp.empire.Objects.GameTile;
import com.rmbcorp.empire.Objects.GenericPlayer;
import com.rmbcorp.empire.Objects.PieceManager;
import com.rmbcorp.empire.Views.BasicGridView;
import com.rmbcorp.empire.Views.MainBarView;
import com.rmbcorp.empire.Views.ViewController;
import com.rmbcorp.empire.Views.ViewListener;

import static com.rmbcorp.empire.DataBridge.StringKey.PLAYER_COUNT;

public class MainActivity extends Activity {

	// first vars: Model, Views, and Controller
	private BasicGrid basicGrid;
	private BasicGridView bgv;
	private MainBarView mbv;
	private Controller c;
	private ViewController vc;
	private ViewListener vL;
	
	private final int rows = 19; // will need to un-final in expandable-grid version
	private final int cols = 19;
	
	//second vars: Secondary variables/helpers to first vars
	private static GenericPlayer[] players = new GenericPlayer[2]; 
	private GUIPlayer p1;
	private AIPlayer p2;
	
	// third vars: Miscellaneous
	private LinearLayout linearLayout; // should this be local?
	
	/** Setup the program for client... "create" if you will. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // if (findViewById(R.id.linearLayout) != null) {
            if (savedInstanceState != null) {
            	System.out.println("Save exists somehow...");
                return;  // if entering from save, no need to redo things
            }
           
            linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL); // below: change to fill parent if below api8
            linearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            linearLayout.setGravity(Gravity.CENTER);
            
            LayoutInflater inflater = LayoutInflater.from(this);
			View tempView = inflater.inflate(R.layout.activity_main, linearLayout);
			//View temp = getLayoutInflater().inflate(R.layout.activity_main, linearLayout); // probably same thing; keep for reference
			DataBridge.put(PLAYER_COUNT, players.length);
			basicGrid = new BasicGrid(rows, cols);
			
			createComponents(tempView);
			
			setupListeners();

			setContentView(linearLayout);
			setVisible(true);
        // }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       //getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	// bgv.onResumeBasicGridView();  << need to define
    	// mbv.onResumeMainGridView();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	// bgv.onPauseBasicGridView();
    	// mbv.onPauseMainGridView();
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	//can reduce some resource usage/purge some memory, maybe even unregister listeners
    }
    
    private void createComponents(View temp) {
		//these should be things that will be done for each player 
		p1 = new GUIPlayer();
		p2 = new AIPlayer();
		players[0] = p1;
		players[1] = p2;
		
		c = Controller.getController();
		c.init(basicGrid, players, rows, cols);
		//p1.connectToPlayers();
		p2.connectToPlayers(); // move this down after pieces are created, and then both link and do initial grid survey.
		//actually, maybe let C link all players upon init... yeah, that's good
		// do the following for each player
		mbv = (MainBarView) temp.findViewById(R.id.mainBarView1);
		bgv = (BasicGridView) temp.findViewById(R.id.basicGridView1);
		vc = ViewController.getViewController();
		vc.init(bgv, mbv, (GUIPlayer) players[0]);
		vL = new ViewListener(bgv, mbv);
		
		PieceManager.create(basicGrid.getStartLocation(0), p1); // Piece creation needs to allow for multiple P's
		PieceManager.create((GameTile) basicGrid.getStartLocation(0).nextG(1), p1, RuleManager.ARMY);
		PieceManager.create((GameTile) basicGrid.getStartLocation(0).nextG(2), p1, RuleManager.BOMBER); // seed algo ensures this is land tile
		// ready to add AI!
		PieceManager.create(basicGrid.getStartLocation(1), p2); // Piece creation needs to allow for multiple P's
		PieceManager.create((GameTile) basicGrid.getStartLocation(1).nextG(1), p2, RuleManager.ARMY);
		PieceManager.create((GameTile) basicGrid.getStartLocation(1).nextG(2), p2, RuleManager.ARMY); // seed algo ensures this is land tile
		//*/
		bgv.init(rows, cols);
		vc.updateTileVisibility();
		
		bgv.invalidate();
    }
    
    private void setupListeners() {
    	bgv.setOnTouchListener(vL);
    	mbv.setOnTouchListener(vL);
    }
}
