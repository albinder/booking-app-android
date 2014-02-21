package com.tdispatch.passenger.data.manager;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.model.AccountData;
import com.tdispatch.passenger.model.BookingData;
import com.tdispatch.passenger.model.CardData;
import com.tdispatch.passenger.model.VehicleData;
import com.tdispatch.passenger.tools.Office;

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

final public class SessionManager
{
	protected TDApplication mContext;
	protected SharedPreferences mPrefs;

	protected static final int VERSION	= 3;

	protected final String KEY_VERSION						= "session_data_version";
	protected final String KEY_ACCESS_TOKEN					= "access_token";
	protected final String KEY_ACCESS_TOKEN_EXPIRATION_MILLIS	= "access_token_expiration_millis";
	protected final String KEY_REFRESH_TOKEN					= "refresh_token";
	protected final String KEY_ACCOUNT_DATA					= "account_data";
	protected final String KEY_CABOFFICE_CLIENT_ID			= "oauth_client_id";

	public SessionManager( TDApplication context ) {
		mContext = context;

		mPrefs = mContext.getSharedPreferences("session_manager", Context.MODE_PRIVATE);;

		if( mPrefs.getInt(KEY_VERSION, 0) < VERSION ) {
			doLogout();

			SharedPreferences.Editor editor = mPrefs.edit();
			editor.putInt( KEY_VERSION, VERSION );
			editor.commit();
		}
	}

	protected static SessionManager mInstance = null;
	public static SessionManager getInstance( TDApplication context ) {
		if( mInstance == null ) {
			mInstance = new SessionManager(context);
		}

		return mInstance;
	}



	public String getAccessToken() {
		return mPrefs.getString(KEY_ACCESS_TOKEN, null);
	}


	public SessionManager setAccessToken( String accessToken ) {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putString( KEY_ACCESS_TOKEN, accessToken );
		editor.commit();

		return this;
	}

	public SessionManager setAccessTokenExpirationMillis( long millis ) {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putLong( KEY_ACCESS_TOKEN_EXPIRATION_MILLIS, millis );
		editor.commit();

		return this;
	}
	public long getAccessTokenExpirationMillis() {
		return mPrefs.getLong(KEY_ACCESS_TOKEN_EXPIRATION_MILLIS, 0);
	}


	public String getRefreshToken() {
		return mPrefs.getString(KEY_REFRESH_TOKEN, null);
	}
	public SessionManager setRefreshToken( String refreshToken ) {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putString( KEY_REFRESH_TOKEN, refreshToken );
		editor.commit();

		return this;
	}

	public Boolean isAccessTokenValid() {
		return ((getAccessTokenExpirationMillis() - System.currentTimeMillis()) > 0);
	}


	public SessionManager putAccountData( AccountData data ) {
		SharedPreferences.Editor editor = mPrefs.edit();
		if( data != null ) {
			editor.putString( KEY_ACCOUNT_DATA, data.toJSON().toString() );
			editor.putString( KEY_CABOFFICE_CLIENT_ID, Office.getOAuthClientId());
		} else {
			editor.putString( KEY_ACCOUNT_DATA, null );
		}
		editor.commit();

		return this;
	}


	public AccountData getAccountData() {
		AccountData profile = null;

		String jsonStr = mPrefs.getString(KEY_ACCOUNT_DATA, null);
		if( jsonStr != null ) {

			if( Office.getOAuthClientId().equals( mPrefs.getString(KEY_CABOFFICE_CLIENT_ID, null)) ) {
				try {
					profile = new AccountData( new JSONObject( jsonStr ) );
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
		}

		return profile;
	}


	public SessionManager doLogout() {
		setAccessToken( null );
		setRefreshToken( null );
		putAccountData( null );

		CardData.deleteAll();
		VehicleData.removeAll();
		BookingData.removeAll();

		return this;
	}

	// end of class
}