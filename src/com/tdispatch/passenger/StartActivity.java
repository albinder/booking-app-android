package com.tdispatch.passenger;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.rampo.updatechecker.UpdateChecker;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDActivity;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.fragment.AccountLoginFragment;
import com.tdispatch.passenger.fragment.AccountRegisterFragment;
import com.tdispatch.passenger.fragment.StartMenuFragment;
import com.tdispatch.passenger.fragment.TourFragment;
import com.tdispatch.passenger.host.MainMenuHostInterface;
import com.tdispatch.passenger.host.OAuthHostInterface;
import com.tdispatch.passenger.host.RegisterHostInterface;
import com.tdispatch.passenger.host.TourHostInterface;
import com.tdispatch.passenger.model.AccountData;
import com.webnetmobile.tools.Redirector;

/*
 ******************************************************************************
 *
 * Copyright (C) 2013 T Dispatch Ltd
 *
 * Licensed under the GPL License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 *
 * @author Marcin Orlowski <marcin.orlowski@webnet.pl>
 *
 ******************************************************************************
*/
public class StartActivity extends TDActivity implements TourHostInterface, OAuthHostInterface, RegisterHostInterface, MainMenuHostInterface
{
	public static final int MODE_NORMAL 	= 0;
	public static final int MODE_TOUR		= 1;

	protected int mMode = MODE_NORMAL;

	protected Handler mHandler = new Handler();

	protected String PREFS_SEEN_TOUR = "tour_seen_it_already";
	protected Boolean mSeenTheTourAlready = false;
	protected Boolean mTourOnUserRequest = false;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();

		if( extras != null ) {
			mMode = extras.getInt(Const.Bundle.MODE, MODE_NORMAL);
		}

		setContentView( R.layout.start_activity );


		mSeenTheTourAlready = mPrefs.getBoolean(PREFS_SEEN_TOUR, false);


		switch( mMode ) {
			case MODE_TOUR: {
				mTourOnUserRequest = true;
				showTour();
			}
			break;

			case MODE_NORMAL: {
				int errorCode = TDApplication.getGoogleServicesCheckReturnCode();
				Dialog errorDialog;
				if( errorCode != ConnectionResult.SUCCESS ) {
					if( GooglePlayServicesUtil.isUserRecoverableError( errorCode ) ) {
						errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, 26354);
						if( errorDialog != null ) {
							errorDialog.show();
						}
					}
				} else {

					AccountData userProfile = TDApplication.getSessionManager().getAccountData();
					if( userProfile == null ) {
						if( mSeenTheTourAlready ) {
							showStart();
						} else {
							showTour();
						}
					} else {
						Long expires = TDApplication.getSessionManager().getAccessTokenExpirationMillis();
						if( expires > 0 ) {
							showMapView();
						} else {
							TDApplication.getSessionManager().doLogout();
							showStart();
						}
					}
				}
			}
			break;
		}
	}

	@Override
	public void showBooking() {
		Redirector.showActivity(mContext, MainActivity.class);
		finish();
	}
	public void showStart() {
		StartMenuFragment frag = new StartMenuFragment();
		setFragment( frag, false );

		UpdateChecker.checkForDialog(this);
	}

	@Override
	protected void setFragment( TDFragment fragment ) {
		setFragment( fragment, true );
	}
	@Override
	protected void setFragment( TDFragment fragment, Boolean addToBackStack ) {

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();

		if( addToBackStack ) {
			ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
		}

		ft.replace( R.id.fragment_container, fragment, Const.Tag.FRAGMENT );

		if( addToBackStack ) {
			ft.addToBackStack(null);
		}

		ft.commit();

		fm.executePendingTransactions();
	}


	/**[ tour host interface ]*************************************************************************************/

	@Override
	public void tourCompleted() {

		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putBoolean(PREFS_SEEN_TOUR, true);
		editor.commit();

		if( mTourOnUserRequest ) {
			finish();
		} else {
			showStart();
		}
	}

	/**[ oauth host interface ]************************************************************************************/

	@Override
	public void oAuthCancelled() {
		showStart();
	}
	@Override
	public void oAuthAuthenticated() {
		showBooking();
	}

	/**[ register host interface ]*********************************************************************************/

	@Override
	public void registerCompleted() {
		showBooking();
	}


	/**[ menu host interface ]*************************************************************************************/

	@Override
	public void showRegister() {
		AccountRegisterFragment frag = new AccountRegisterFragment();
		setFragment( frag );
	}

	@Override
	public void showOAuth() {
		AccountLoginFragment frag = new AccountLoginFragment();
		setFragment( frag );
	}

	@Override
	public void showTour() {
		TourFragment frag = new TourFragment();
		setFragment(frag, false);
	}


	// helper, not interface memeber
	protected void showMapView() {
		Redirector.showActivity(mContext, MainActivity.class);
		finish();
	}


} // end of class
