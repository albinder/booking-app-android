package com.tdispatch.passenger.fragment;

import uk.co.jasonfry.android.tools.ui.PageControl;
import uk.co.jasonfry.android.tools.ui.SwipeView;
import android.app.Activity;
import android.view.View;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.iface.host.TourHostInterface;

/*
 *********************************************************************************
 *
 * Copyright (C) 2013-2014 T Dispatch Ltd
 *
 * See the LICENSE for terms and conditions of use, modification and distribution
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *********************************************************************************
 *
 * @author Marcin Orlowski <marcin.orlowski@webnet.pl>
 *
 *********************************************************************************
*/

public class TourFragment extends TDFragment
{
	@Override
	protected int getLayoutId() {

		int layoutId = R.layout.tour_fragment;

		if( TDApplication.isTablet() ) {
			layoutId = R.layout.tour_fragment_tablet;
		}
		return layoutId;
	}

	@Override
	protected void onPostCreateView() {

		SwipeView swipeView = (SwipeView)mFragmentView.findViewById(R.id.swipe_view);
		swipeView.setPageControl((PageControl)mFragmentView.findViewById(R.id.page_control));

		int ids[] = { R.id.button_ok };
		for( int id : ids ) {
			View v = mFragmentView.findViewById(id);
			v.setOnClickListener( mOnClickListener );
		}
	}

	protected TourHostInterface mHostActivity;
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mHostActivity = (TourHostInterface)activity;
		} catch( ClassCastException e ) {
			throw new ClassCastException("Host Activity needs to implement TourHostInterface");
		}
	}

	protected View.OnClickListener mOnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {

			switch( v.getId() ) {

				case R.id.button_ok: {
					mHostActivity.tourCompleted();
				}
				break;
			}

		}
	};


}	// end of class
