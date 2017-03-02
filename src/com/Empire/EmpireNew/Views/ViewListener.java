package com.Empire.EmpireNew.Views;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/** Listens for onTouch events in both BasicGridView and MainBarView
 * Results are sent to ViewController or MainBarView for handling (MBV will need its own handler soon).
 * @author robertbrako
 *
 */
public final class ViewListener implements OnTouchListener {
	
	private BasicGridView bgv;
	private MainBarView mbv;
	
	/** Constructor which links listener to the client Views (BasicGridView and MainBarView) **/
	public ViewListener(BasicGridView bgv, MainBarView mbv) {
		this.bgv = bgv;
		this.mbv = mbv;	
	}
	
	/** onTouch override; identifies which view was clicked; lets ViewController handle BGV and lets MBV handle MBV (for now)**/
	@Override
	public boolean onTouch(View view, MotionEvent e) { // sometimes returns false because I'm not paying attention to that right now
		if (view.getClass() == bgv.getClass()) {
			if (e.getActionMasked() == MotionEvent.ACTION_UP) {
				//CAUTION: remember to set flags before changing to next status
				bgv.performClick();
				ViewController.getViewController().bgvActionUp(e.getX(), e.getY());

			} // end if (ACTION_UP)
		}
		else if (view.getClass() == mbv.getClass()) { // otherwise it's just MainBarView
			int[] mbvOrigin = {32, 16}; // these should probably be moved
			int[] mbvDimensions = {12, 12};
			if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
				int[] mbvResult = Locator.locate(mbvOrigin, mbvDimensions, 20, e.getX(), e.getY());
				int[] displacement = ((MainBarView) view).analyzeClick(mbvResult, false);
				if (displacement[0] != -1)
					((MainBarView) view).highlight(displacement);
				return true;
			}
			if (e.getActionMasked() == MotionEvent.ACTION_UP) {
				
				int[] mbvResult = Locator.locate(mbvOrigin, mbvDimensions, 20, e.getX(), e.getY());
				int[] displacement = ((MainBarView) view).analyzeClick(mbvResult, true);
				((MainBarView) view).highlight(null);
				if (displacement[0] != -1)
					bgv.panZoom(displacement[0], displacement[1]);
				return true;
			}
			
		}
		else {
			view.performClick(); // so that lint quits complaining...
		}
		return true;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException { // don't clone me!
		throw new CloneNotSupportedException();
	}
	
	@Override
	public String toString() {
		return "ViewListener";
	}
}
