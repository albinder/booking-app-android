package com.tdispatch.passenger.model;

import java.util.ArrayList;

import org.json.JSONObject;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.db.VehicleDbAdapter;
import com.webnetmobile.tools.JsonTools;

/*
 ******************************************************************************
 *
 * Copyright (C) 2013 T Dispatch Ltd
 *
 * Licensed under the GPL License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.gnu.org/licenses/gpl-3.0.html
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
public class VehicleData implements Parcelable
{
	protected long mLocalId;
	protected String mPk;
	protected String mName;
	protected Boolean mDefault;

	public VehicleData() {
		// dummy
	}
	public VehicleData(Cursor c) {
		setLocalId( c.getLong( c.getColumnIndex( VehicleDbAdapter.KEY_LOCAL_ID )));
		setPk( c.getString( c.getColumnIndex( VehicleDbAdapter.KEY_PK )));
		setName( c.getString( c.getColumnIndex( VehicleDbAdapter.KEY_NAME )));
		setDefault(c.getInt( c.getColumnIndex(VehicleDbAdapter.KEY_DEFAULT)));
	}
	public VehicleData( JSONObject json ) {
		set( json );
	}

	public VehicleData setLocalId(long localId) {
		mLocalId = localId;
		return this;
	}
	public long getLocalId() {
		return mLocalId;
	}

	public VehicleData setPk( String pk ) {
		mPk = pk;
		return this;
	}
	public String getPk() {
		return mPk;
	}

	public VehicleData setName(String name) {
		mName = name;
		return this;
	}
	public String getName() {
		return mName;
	}
	public VehicleData setDefault(Boolean isDefault) {
		mDefault = isDefault;
		return this;
	}
	public VehicleData setDefault(int isDefault) {
		mDefault = (isDefault==1);
		return this;
	}
	public Boolean isDefault() {
		return mDefault;
	}


	protected void set( JSONObject json ) {
		setPk( JsonTools.getString(json, "pk") );
		setName( JsonTools.getString(json, "name") );
	}

	/* db */

	public long insert() {
		VehicleDbAdapter db = new VehicleDbAdapter( TDApplication.getAppContext() );
		long id = db.insert(this);
		setLocalId(id);
		return id;
	}

	public static VehicleData getDefault() {
		VehicleDbAdapter db = new VehicleDbAdapter(TDApplication.getAppContext());
		return db.getDefault();
	}
	public static VehicleData getByPk(String pk) {
		VehicleDbAdapter db = new VehicleDbAdapter(TDApplication.getAppContext());
		return db.getByPk(pk);
	}

	public static ArrayList<VehicleData> getAll() {
		VehicleDbAdapter db = new VehicleDbAdapter(TDApplication.getAppContext());
		return db.getAll();
	}

	public static boolean removeAll() {
		VehicleDbAdapter db = new VehicleDbAdapter( TDApplication.getAppContext() );
		return db.removeAll();
	}



	/* Parcelable implementation */

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeLong( getLocalId() );
		out.writeString( getPk() );
		out.writeString( getName() );
		out.writeInt( isDefault() ? 1 : 0 );
	}

	private VehicleData(Parcel in) {
		setLocalId( in.readLong() );
		setPk( in.readString() );
		setName( in.readString() );
		setDefault( in.readInt() );
	}

	public static final Parcelable.Creator<VehicleData> CREATOR = new Parcelable.Creator<VehicleData>() {
		@Override
		public VehicleData createFromParcel(Parcel in) {
			return new VehicleData(in);
		}

		@Override
		public VehicleData[] newArray(int size) {
			return new VehicleData[size];
		}
	};

}
