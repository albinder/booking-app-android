package com.tdispatch.passenger.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tdispatch.passenger.core.TDApplication;

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

public class DbOpenHelper extends SQLiteOpenHelper
{
	private static final String DATABASE_NAME = "tdispatch_passenger";
	private static final int DATABASE_VERSION = 4;

	private static DbOpenHelper mSystemDbOpenHelper = null;

	public static DbOpenHelper getInstance( TDApplication context ) {
		if( mSystemDbOpenHelper == null ) {
			mSystemDbOpenHelper = new DbOpenHelper(context);
		}

		return mSystemDbOpenHelper;
	}

	public DbOpenHelper(TDApplication context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate( SQLiteDatabase db ) {
		createAllTables(db);
	}

	@Override
	public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
		recreateAllTables(db);
	}


	public void createAllTables( SQLiteDatabase db ) {
		BookingDbAdapter.createDbTable(db);
		VehicleDbAdapter.createDbTable(db);
		CardDbAdapter.createDbTable(db);
	}

	public void deleteAllTables(SQLiteDatabase db) {
		BookingDbAdapter.deleteDbTable(db);
		VehicleDbAdapter.deleteDbTable(db);
		CardDbAdapter.deleteDbTable(db);
	}

	public void recreateAllTables(SQLiteDatabase db) {
		deleteAllTables(db);
		createAllTables(db);
	}
}
