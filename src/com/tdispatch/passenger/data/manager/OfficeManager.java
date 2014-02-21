package com.tdispatch.passenger.data.manager;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.define.FleetData;
import com.tdispatch.passenger.model.OfficeData;

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

final public class OfficeManager
{
	protected TDApplication mContext;
	protected OfficeManager mOfficeManager;
	protected SharedPreferences mPrefs;

	protected OfficeData mOfficeData;

	protected static OfficeManager _instance = null;
	public static OfficeManager getInstance(TDApplication context) {
		if( _instance == null ) {
			_instance = new OfficeManager(context);
		}

		return _instance;
	}

	public OfficeManager( TDApplication context ) {
		mContext = context;

		load();
	}

	public OfficeData get() {
		return mOfficeData;
	}

	public OfficeManager put( OfficeData data ) {
		mOfficeData = data;
		save();

		return this;
	}
	public OfficeManager load() {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		mOfficeData = new OfficeData(	mPrefs.getString( FleetData.NAME, "---" ),
										mPrefs.getString( FleetData.PHONE, "---" ),
										mPrefs.getString( FleetData.EMAIL, "---" )
									);

		return this;
	}
	public OfficeManager save() {

		if( mOfficeData != null ) {
			SharedPreferences.Editor editor = mPrefs.edit();

			editor.putString( FleetData.NAME, mOfficeData.getName() );
			editor.putString( FleetData.PHONE, mOfficeData.getPhone() );
			editor.putString( FleetData.EMAIL, mOfficeData.getEmail() );

			editor.commit();
		}

		return this;
	}


} // end of class
