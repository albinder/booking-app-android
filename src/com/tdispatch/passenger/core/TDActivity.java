package com.tdispatch.passenger.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.define.Tag;
import com.tdispatch.passenger.fragment.dialog.GenericDialogFragment;
import com.tdispatch.passenger.iface.host.CommonHostInterface;
import com.tdispatch.passenger.iface.host.RedirectorInterface;
import com.webnetmobile.tools.WebnetLog;
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

public abstract class TDActivity extends android.support.v4.app.FragmentActivity implements RedirectorInterface, CommonHostInterface
{
	protected SharedPreferences mPrefs;

	protected Context mContext;
	protected TDActivity mMe;
	protected TDApplication mApp;
	protected FragmentManager mFragmentManager = null;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {

		super.onCreate(savedInstanceState);

		//		// Crittercism 4.1.0+
		//		String critAppId = getString(R.string.crittercism_app_id);
		//		if( (critAppId != null) && (critAppId.length() == 24) ) {
		//			CrittercismConfig crittercismConfig = new CrittercismConfig();
		//				crittercismConfig.setDelaySendingAppLoad(false);
		//				crittercismConfig.setLogcatReportingEnabled(true);
		//				crittercismConfig.setVersionCodeToBeIncludedInVersionString(true);
		//				crittercismConfig.setCustomVersionName( TDApplication.getAppVersion() + " (" + TDApplication.getAppVersionCode() + ")" );
		//			Crittercism.initialize(getApplicationContext(), critAppId, crittercismConfig);
		//			Crittercism.setMetadata( TDApplication.getEnvInfoAsJson() );
		//		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		mApp = TDApplication.getAppContext();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mApp);
		mFragmentManager = getSupportFragmentManager();

		mContext = this;
		mMe = this;
	}



	/**[ ui lock ]***********************************************************************************************************/

	protected Boolean useContainerWithLockUIOverlay() {
		return true;
	}

	@Override
	public void setContentView( int layoutId ) {

		if( useContainerWithLockUIOverlay() == true ) {
			LayoutInflater inflater = getLayoutInflater();

			ViewGroup activityLayout = (ViewGroup)inflater.inflate( R.layout.tdactivity, null );
			ViewGroup contentContainer = (ViewGroup)activityLayout.findViewById(R.id.tdactivity_content_container);

			inflater.inflate( layoutId, contentContainer );

			super.setContentView( activityLayout );

			doHandleUiLock(0);
		} else {
			super.setContentView( layoutId );
		}

		// set custom fonts
		setCustomFonts();
	}


	/**[ common host interface ]*********************************************************************************************/

	@Override
	public void lockUI() {
		doHandleUiLock(+1);
	}
	@Override
	public void unlockUI() {
		doHandleUiLock(-1);
	}

	protected int mUiLockCount = 0;
	protected void doHandleUiLock(int step) {

		View v = findViewById( R.id.tdactivity_busy_overlay_container );
		if( v != null ) {
			mUiLockCount += step;
			if( (mUiLockCount > 0) ) {
				InputMethodManager imm = (InputMethodManager)getSystemService( Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
			v.setVisibility( (mUiLockCount > 0 ) ? View.VISIBLE : View.GONE);
		}

	}


	/**[ custom fonts ]******************************************************************************************************/

	protected void setCustomFonts() {
		setCustomFonts((ViewGroup)(((ViewGroup)findViewById(android.R.id.content)).getChildAt(0)));
	}

	protected void setCustomFonts( ViewGroup viewGroup ) {
		WebnetTools.setCustomFonts(mApp, viewGroup);
	}




	// Redirector Interface - FIXME should not be here...
	@Override
	public void showLogin() {
		WebnetLog.e("Not implemented!");
	}

	@Override
	public void showRegister() {
		WebnetLog.e("Not implemented!");
	}

	@Override
	public void showBooking() {
		WebnetLog.e("Not implemented!");
	}

	@Override
	public void showOAuth() {
		WebnetLog.e("Not implemented!");
	}



	protected void setFragment( TDFragment fragment ) {
		setFragment( fragment, true );
	}
	protected void setFragment( TDFragment fragment, Boolean addToBackStack ) {
		setFragment(fragment, addToBackStack, R.id.fragment_container, Tag.FRAGMENT);
	}

	protected void setFragment( TDFragment fragment, Boolean addToBackStack, int fragmentContainer, String tag ) {

		FragmentTransaction ft = mFragmentManager.beginTransaction();

		ft.replace( fragmentContainer, fragment, tag );

		if( addToBackStack ) {
			ft.addToBackStack(null);
		}

		ft.commit();

		mFragmentManager.executePendingTransactions();
	}


	/**[ dialogs ]*************************************************************************************************************/


	/**[ dialog helpers ]********************************************************************************************/

	protected void showDialog( int type, int titleId, int messageId ) {
		showDialog( type, getString(titleId), getString(messageId));
	}
	protected void showDialog( int type, int titleId, int messageId, int buttonId ) {
		showDialog( type, getString(titleId), getString(messageId), getString(buttonId));
	}
	protected void showDialog( int type, String title, String message ) {
		showDialog(type, title, message, null);
	}
	protected void showDialog( int type, String title, String message, String button ) {
		GenericDialogFragment frag = GenericDialogFragment.newInstance( type, title, message );
		frag.show(getSupportFragmentManager(), "genericdialog");
	}


} // end of class
