package com.tdispatch.passenger.db;

import org.json.JSONArray;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.model.BookingData;
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

public class BookingDbAdapter extends DbAdapter
{
	public static final String DB_TABLE					= "booking";

	public static final String KEY_LOCAL_ID 			= "_id";

	public static final String KEY_BOOKING_PK			= "booking_pk";
	public static final String KEY_PICKUP_ADDRESS		= "pickup_address";
	public static final String KEY_DROPOFF_ADDRESS		= "dropoff_address";
	public static final String KEY_PICKUP_DATE			= "date";
	public static final String KEY_TYPE					= "type";

	public static final String KEY_JSON					= "json";

	protected String[] mMapping = { KEY_LOCAL_ID, KEY_JSON, KEY_BOOKING_PK, KEY_TYPE, KEY_PICKUP_DATE,
											KEY_PICKUP_ADDRESS, KEY_DROPOFF_ADDRESS };

	public BookingDbAdapter( TDApplication context ) {
		super( context );
	}

	@Override
	public String getTableName() {
		return DB_TABLE;
	}

	public static void createDbTable(SQLiteDatabase db) {

		String query = "CREATE TABLE " + DB_TABLE + "("
				+ KEY_LOCAL_ID + " INTEGER PRIMARY KEY"

				+ "," + KEY_BOOKING_PK + " TEXT"

				+ "," + KEY_PICKUP_ADDRESS + " TEXT"
				+ "," + KEY_DROPOFF_ADDRESS + " TEXT"
				+ "," + KEY_PICKUP_DATE + " DATE KEY"

				+ "," + KEY_TYPE + " INTEGER KEY"

				+ "," + KEY_JSON + " TEXT"
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

	protected int mOpenCount = 0;
	@Override
	public BookingDbAdapter open() throws SQLException {
		if( mDb == null ) {
			mDbHelper = DbOpenHelper.getInstance( mContext );
			mDb = mDbHelper.getReadableDatabase();
		}

		mOpenCount++;

		return this;
	}
	@Override
	public void close() {
		if( mOpenCount == 0 ) {

			if( mDb != null ) {
				mDb.close();
				mDb = null;
			}
			if( mDbHelper != null ) {
				mDbHelper.close();
				mDbHelper = null;
			}
		} else {
			if( mOpenCount > 0 ) {
				mOpenCount--;
			}
		}
	}

	public long insert( BookingData booking ) {

		ContentValues values = new ContentValues();
		values.put( KEY_BOOKING_PK, booking.getPk() );
		values.put( KEY_PICKUP_ADDRESS, booking.getPickupLocation().getAddress() );
		values.put( KEY_PICKUP_DATE, booking.getPickupDate().getTime() / WebnetTools.MILLIS_PER_SECOND );
		values.put( KEY_DROPOFF_ADDRESS, booking.getPickupLocation().getAddress() );
		values.put( KEY_TYPE, booking.getType());
		values.put( KEY_JSON, booking.getJson().toString() );

		open();
		long id = mDb.insert( DB_TABLE, null, values );
		close();

		return id;
	}

	public boolean update( BookingData booking ) {

		ContentValues values = new ContentValues();
		values.put( KEY_TYPE, booking.getType() );

		String whereClause = BookingDbAdapter.KEY_LOCAL_ID + "=?";
		String[] whereArgs = { String.valueOf( booking.getPk() ) };

		open();
		int affectedRows = mDb.update( BookingDbAdapter.DB_TABLE, values, whereClause, whereArgs);
		close();

		return (affectedRows == 1) ? true :  false;
	}


	public boolean remove( BookingData booking ) {
		return remove( booking.getPk() );
	}
	public boolean remove( String bookingPk ) {
		String whereClause = String.format( "%s=?", KEY_BOOKING_PK );
		String whereArgs[] = { String.valueOf( bookingPk ) };

		return removeByWhereClause(whereClause, whereArgs);
	}

	protected boolean removeByWhereClause( String whereClause, String[] whereArgs ) {
		open();
		boolean result = (mDb.delete( DB_TABLE, whereClause, whereArgs) > 0);
		close();

		return result;
	}

	public boolean removeAll() {
		return removeAllByType( null );
	}
	public boolean removeAllByType( Integer type ) {

		boolean result = false;

		open();

		if( type == null ) {
			result = (mDb.delete( DB_TABLE, null, null ) > 0);
		} else {
			String whereClause = String.format( "%s=?", KEY_TYPE );
			String[] whereArgs = { String.valueOf(type) };
			result = (mDb.delete( DB_TABLE, whereClause, whereArgs) > 0);
		}

		close();

		return result;
	}

	public BookingData getByPk( String bookingPk ) {
		BookingData booking = null;

		String whereClause = KEY_BOOKING_PK + "=?";
		String[] whereArgs = { bookingPk };

		open();
		Cursor c = mDb.query(DB_TABLE, mMapping, whereClause, whereArgs, null, null, null, "1" );
		if( c != null ) {
			if( c.getCount() > 0 ) {
				c.moveToFirst();
				booking = new BookingData(c);
			}
			c.close();
		}
		close();

		return booking;
	}


	protected Cursor getByWhereClause( String whereClause, String[] whereArgs ) {
		open();
		Cursor result = mDb.query(DB_TABLE, mMapping, whereClause, whereArgs, null, null, null );
		close();

		return result;
	}

	public Cursor getAll() {

		open();
		String orderBy = KEY_PICKUP_DATE + " DESC";
		Cursor result = mDb.query(DB_TABLE, mMapping, null, null, null, null, orderBy);
		close();

		return result;
	}
	public JSONArray getAllTrackable() {

		JSONArray result = new JSONArray();

		String whereClause = KEY_TYPE + "=? OR " + KEY_TYPE + "=? OR " + KEY_TYPE + "=? OR " + KEY_TYPE + "=?" ;
		String[] whereArgs = { String.valueOf(BookingData.TYPE_ACTIVE), String.valueOf(BookingData.TYPE_CONFIRMED),
								String.valueOf(BookingData.TYPE_DISPATCHED), String.valueOf(BookingData.TYPE_INCOMING) };
		String orderBy = KEY_PICKUP_DATE + " DESC";

		open();
		Cursor c = mDb.query(DB_TABLE, mMapping, whereClause, whereArgs, null, null, orderBy);
		if ( c!=null ) {
			if (c.getCount() > 0) {
				while( c.moveToNext() ) {
					result.put( c.getString(c.getColumnIndex(KEY_BOOKING_PK)));
				}
			}
			c.close();
		}
		close();

		return result;
	}


} // end of class