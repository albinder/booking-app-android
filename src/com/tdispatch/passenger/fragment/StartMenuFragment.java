package com.tdispatch.passenger.fragment;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.iface.host.MainMenuHostInterface;
import com.tdispatch.passenger.tools.Office;
import com.webnetmobile.tools.WebnetTools;

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

public class StartMenuFragment extends TDFragment
{
	protected int mLogoTapCount = 0;


	@Override
	protected int getLayoutId() {
		return R.layout.start_menu_fragment;
	}

	@Override
	protected void onPostCreateView() {

		TextView version = (TextView)mFragmentView.findViewById(R.id.version);
		version.setText( TDApplication.getAppVersion() + " (" + getString(R.string.source_code_signature) + ")" );
		version.setVisibility(View.GONE);

		WebnetTools.setVisibility(mFragmentView, R.id.demo_warning_container, Office.isDemoWarningDisabled() ? View.GONE : View.VISIBLE );


		int ids[] = { R.id.button_login, R.id.button_register, R.id.logo };

		for( int id : ids ) {
			View v = mFragmentView.findViewById( id );
			v.setOnClickListener(mOnClickListener);
		}
	}

	protected MainMenuHostInterface mHostActivity;
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mHostActivity = (MainMenuHostInterface)activity;
		} catch( ClassCastException e ) {
			throw new ClassCastException("Host Activity needs to implement MainMenuHostInterface");
		}
	}


	protected View.OnClickListener mOnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {

			switch( v.getId() ) {

				case R.id.logo: {
					mLogoTapCount++;
					if( mLogoTapCount >= 5 ) {
						WebnetTools.setVisibility(mFragmentView, R.id.version, View.VISIBLE);
					}
				}
				break;



				case R.id.button_login: {
					mHostActivity.showOAuth();
				}
				break;

				case R.id.button_register: {
					mHostActivity.showRegister();
				}
				break;
			}
		}
	};

// end of class
}
