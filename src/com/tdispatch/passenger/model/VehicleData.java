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

public class VehicleData implements Parcelable
{
	protected long mLocalId;
	protected String mPk;
	protected String mName;
	protected Boolean mDefault;
	protected int mPassengerCapacity;
	protected int mLuggageCapacity;

	public VehicleData() {
		// dummy
	}
	public VehicleData(Cursor c) {
		setLocalId( c.getLong( c.getColumnIndex( VehicleDbAdapter.KEY_LOCAL_ID )));
		setPk( c.getString( c.getColumnIndex( VehicleDbAdapter.KEY_PK )));
		setName( c.getString( c.getColumnIndex( VehicleDbAdapter.KEY_NAME )));
		setDefault(c.getInt( c.getColumnIndex(VehicleDbAdapter.KEY_DEFAULT)));
		setLuggageCapacity(c.getInt( c.getColumnIndex(VehicleDbAdapter.KEY_LUGGAGE_COUNT)));
		setPassengerCapacity(c.getInt( c.getColumnIndex(VehicleDbAdapter.KEY_PASSENGER_COUNT)));
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


	public VehicleData setPassengerCapacity(int count) {
		mPassengerCapacity = count;
		return this;
	}
	public int getPassengerCapacity() {
		return mPassengerCapacity;
	}

	public VehicleData setLuggageCapacity(int count) {
		mLuggageCapacity = count;
		return this;
	}
	public int getLuggageCapacity() {
		return mLuggageCapacity;
	}

	protected void set( JSONObject json ) {
		setPk( JsonTools.getString(json, "pk") );
		setName( JsonTools.getString(json, "name") );
		setDefault( JsonTools.getBoolean(json, "default", false));
		setLuggageCapacity( JsonTools.getInt(json, "maximum_luggage", 3));
		setPassengerCapacity( JsonTools.getInt(json, "maximum_passengers", 1));
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
	public static VehicleData getByLocalId(long id) {
		VehicleDbAdapter db = new VehicleDbAdapter(TDApplication.getAppContext());
		return db.getByLocalId(id);
	}

	public static VehicleData getByPk(String pk) {
		VehicleDbAdapter db = new VehicleDbAdapter(TDApplication.getAppContext());
		return db.getByPk(pk);
	}

	public static ArrayList<VehicleData> getAll() {
		VehicleDbAdapter db = new VehicleDbAdapter(TDApplication.getAppContext());
		return db.getAll();
	}

	public static Cursor getAllAsCursor() {
		return getAllAsCursor(null, 0, 0);
	}
	public static Cursor getAllAsCursor(VehicleData currentVehicle, int requiredSeats, int requiredLuggage) {
		VehicleDbAdapter db = new VehicleDbAdapter(TDApplication.getAppContext());
		return db.getAllAsCursor(currentVehicle, requiredSeats, requiredLuggage);
	}

	public static boolean removeAll() {
		VehicleDbAdapter db = new VehicleDbAdapter( TDApplication.getAppContext() );
		return db.removeAll();
	}



	// ** helpers

	protected static Boolean mMaxValuesSet = false;
	protected static int mMaxPassengerCount = 0;
	protected static int mMaxLuggageCount = 0;
	public static int getMaxPassengerCount() {
		getMaxValues();
		return mMaxPassengerCount;
	}
	public static int getMaxLuggageCount() {
		getMaxValues();
		return mMaxLuggageCount;
	}

	protected static void getMaxValues() {
		if( mMaxValuesSet == false ) {
			ArrayList<VehicleData> vehicles = getAll();

			for (VehicleData vehicle : vehicles) {
				int tmp = vehicle.getLuggageCapacity();
				if(tmp > mMaxLuggageCount) {
					mMaxLuggageCount = tmp;
				}

				tmp = vehicle.getPassengerCapacity();
				if( tmp > mMaxPassengerCount) {
					mMaxPassengerCount = tmp;
				}
			}

			mMaxValuesSet = true;
		}
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
		out.writeInt( getLuggageCapacity());
		out.writeInt( getPassengerCapacity());
	}

	private VehicleData(Parcel in) {
		setLocalId( in.readLong() );
		setPk( in.readString() );
		setName( in.readString() );
		setDefault( in.readInt() );
		setLuggageCapacity(in.readInt());
		setPassengerCapacity(in.readInt());
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
