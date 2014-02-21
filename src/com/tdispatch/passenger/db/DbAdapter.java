package com.tdispatch.passenger.db;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

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

public abstract class DbAdapter
{
	abstract public String getTableName();

	public static final String DB_TABLE = null;
	public static final String KEY_LOCAL_ID = "_id";

	protected TDApplication mContext;
	protected SQLiteDatabase mDb;
	protected DbOpenHelper mDbHelper;

	public DbAdapter(TDApplication context) {
		mContext = context;
	}

	protected int openCount = 0;

	protected DbAdapter open() throws SQLException {
		if( openCount == 0 ) {
			if( mDb == null ) {
				mDbHelper = DbOpenHelper.getInstance(mContext);
				mDb = mDbHelper.getWritableDatabase();
			}
		}

		openCount++;

		return this;
	}

	protected void close() {

		if( openCount == 0 ) {
			if( mDb != null ) {
				mDb.close();
				mDb = null;
			}
			if( mDbHelper != null ) {
				mDbHelper.close();
				mDbHelper = null;
			}
		}

		if( openCount > 0 ) {
			openCount--;
		}
	}

	public int delete( long id ) {
		open();
		int count = mDb.delete(getTableName(), KEY_LOCAL_ID + "=?", new String[] { String.valueOf(id) });
		close();
		return count;
	}

	public void deleteAll() {
		open();
		mDb.delete(getTableName(), null, null);
		close();
	}


}
