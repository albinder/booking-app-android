package com.tdispatch.passenger.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.model.CardData;

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

public class CardDbAdapter extends DbAdapter
{
	public static final String DB_TABLE				= "card";

	public static final String KEY_LOCAL_ID 			= "_id";

	public static final String KEY_DEFAULT			= "is_default";
	public static final String KEY_TOKEN 				= "token";
	public static final String KEY_NUMBER				= "number";
	public static final String KEY_EXPIRED			= "is_expired";
	public static final String KEY_CARD_TYPE			= "card_type";
	public static final String KEY_EXPIRATION_DATE	= "expiration_date";
	public static final String KEY_HOLDER_NAME		= "holder_name";

	protected Long 	mLocalId;
	protected int		mHolderName;


	protected String[] mMapping = {"\"" + KEY_LOCAL_ID  + "\"",
									"\"" + KEY_DEFAULT + "\"", "\"" + KEY_TOKEN + "\"",
									"\"" + KEY_NUMBER + "\"",
									"\"" + KEY_EXPIRED + "\"", "\"" + KEY_CARD_TYPE + "\"",
									"\"" + KEY_EXPIRATION_DATE + "\"",
									"\"" + KEY_HOLDER_NAME + "\""
								};

	public CardDbAdapter( TDApplication context ) {
		super( context );
	}

	@Override
	public String getTableName() {
		return DB_TABLE;
	}

	public static void createDbTable(SQLiteDatabase db) {
		String query = "CREATE TABLE " + DB_TABLE + "("
				+ KEY_LOCAL_ID + " INTEGER PRIMARY KEY"
				+ ", \"" + KEY_TOKEN  + "\" TEXT"
				+ ", \"" + KEY_NUMBER + "\" TEXT"
				+ ", \"" + KEY_DEFAULT + "\" INTEGER KEY"
				+ ", \"" + KEY_EXPIRED + "\" INTEGER KEY"
				+ ", \"" + KEY_CARD_TYPE + "\" TEXT"
				+ ", \"" + KEY_HOLDER_NAME + "\" TEXT"
				+ ", \"" + KEY_EXPIRATION_DATE + "\" TEXT"
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


	//***********************************************************

	public long insert( CardData data ) {

		ContentValues values = new ContentValues();

		values.put( KEY_TOKEN, data.getToken() );
		values.put( KEY_NUMBER, data.getNumber() );
		values.put( KEY_DEFAULT, data.isDefault() );
		values.put( KEY_EXPIRED, data.isExpired());
		values.put( KEY_CARD_TYPE, data.getCardTypeString());
		values.put( KEY_EXPIRATION_DATE, data.getExpirationDate());
		values.put( KEY_HOLDER_NAME, data.getHolderName());

		open();
		long id = mDb.insert( getTableName(), null, values );
		close();

		return id;
	}

	public CardData getByToken(String token) {

		CardData result = null;

		open();

		String whereClause = KEY_TOKEN + "=?";
		String[] whereArgs = { token };

		Cursor cursor = mDb.query(getTableName(), mMapping, whereClause, whereArgs, null, null, null);
		if( cursor != null ) {
			while( cursor.moveToNext() ) {
				result = new CardData( cursor );
			}
			cursor.close();
		}
		close();

		return result;
	}

	public ArrayList<CardData> getAll() {

		ArrayList<CardData> result = new ArrayList<CardData>();

		String orderBy =    "\"" + KEY_DEFAULT + "\" DESC"
						+ ", \"" + KEY_HOLDER_NAME + "\" COLLATE NOCASE ASC"
						+ ", \"" + KEY_NUMBER + "\" ASC";

		open();
		Cursor cursor = mDb.query(getTableName(), mMapping, null, null, null, null, orderBy);
		if( cursor != null ) {
			while( cursor.moveToNext() ) {
				result.add( new CardData( cursor ) );
			}
			cursor.close();
		}
		close();

		return result;
	}


	public int count() {
		int count = 0;

		open();

		Cursor mCount= mDb.rawQuery("select count(*) from " + getTableName(), null);
		if( mCount != null ) {
			if( mCount.moveToFirst() ) {
				count= mCount.getInt(0);
			}

			mCount.close();
		}

		close();

		return count;
	}

} // end of class