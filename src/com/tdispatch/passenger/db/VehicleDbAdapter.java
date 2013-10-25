package com.tdispatch.passenger.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.model.VehicleData;

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
public class VehicleDbAdapter extends DbAdapter
{
	public static final String DB_TABLE		= "vehicle";

	public static final String KEY_LOCAL_ID 	= "_id";
	public static final String KEY_PK			= "pk";
	public static final String KEY_NAME		= "name";
	public static final String KEY_DEFAULT	= "is_default";

	protected String[] mMapping = { KEY_LOCAL_ID, KEY_PK, KEY_NAME, KEY_DEFAULT };

	public VehicleDbAdapter( TDApplication context ) {
		super( context );
	}

	@Override
	public String getTableName() {
		return DB_TABLE;
	}

	public static void createDbTable(SQLiteDatabase db) {

		String query = "CREATE TABLE " + DB_TABLE + "("
				+ KEY_LOCAL_ID + " INTEGER PRIMARY KEY"
					+ "," + KEY_PK + " TEXT"
					+ "," + KEY_NAME + " TEXT"
					+ "," + KEY_DEFAULT + " INTEGER KEY"
				+ ")";

		db.execSQL( query );
	}

	public static void deleteDbTable( SQLiteDatabase db ) {
		String query = "DROP TABLE " + DB_TABLE;
		db.execSQL( query );
	}

	public static void upgradeDbTable( SQLiteDatabase db, int oldVersion, int newVersion ) {
		deleteDbTable( db );
		createDbTable( db );
	}

	public long insert( VehicleData vehicle ) {

		ContentValues values = new ContentValues();
		values.put( KEY_PK, vehicle.getPk() );
		values.put( KEY_NAME, vehicle.getName() );
		values.put( KEY_DEFAULT, vehicle.isDefault() );

		open();
		long id = mDb.insert( DB_TABLE, null, values );
		close();

		return id;
	}

	public boolean removeAll() {

		boolean result = false;

		open();
		result = (mDb.delete( DB_TABLE, null, null ) > 0);
		close();

		return result;
	}

	public VehicleData getDefault() {
		VehicleData vehicle = null;

		String orderBy = KEY_DEFAULT + " DESC, " + KEY_NAME + " ASC";

		open();
		Cursor c = mDb.query(DB_TABLE, mMapping, null, null, null, null, orderBy, "1");
		if ( c!=null ) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				vehicle = new VehicleData(c);
			}
			c.close();
		}
		close();


		return vehicle;
	}
	public VehicleData getByPk(String pk) {
		VehicleData vehicle = null;

		String whereClause = KEY_PK + "=?";
		String[] whereArgs = { pk };
		open();
		Cursor c = mDb.query(DB_TABLE, mMapping, whereClause, whereArgs, null, null, null, "1");
		if ( c!=null ) {
			if ( c.moveToFirst() ) {
				vehicle = new VehicleData(c);
			}
			c.close();
		}
		close();

		return vehicle;
	}

	public ArrayList<VehicleData> getAll() {
		ArrayList<VehicleData> result = new ArrayList<VehicleData>();

		String orderBy = KEY_NAME + " ASC";

		open();
		Cursor c = mDb.query(DB_TABLE, mMapping, null, null, null, null, orderBy);
		if ( c!=null ) {
			if (c.getCount() > 0) {
				while( c.moveToNext() ) {
					result.add( new VehicleData(c) );
				}
			}
			c.close();
		}
		close();

		return result;
	}


	protected Cursor getByWhereClause( String whereClause, String[] whereArgs ) {
		open();
		Cursor result = mDb.query(DB_TABLE, mMapping, whereClause, whereArgs, null, null, null );
		close();

		return result;
	}


} // end of class