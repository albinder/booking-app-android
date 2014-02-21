package com.tdispatch.passenger.model;

import org.json.JSONObject;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
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

public class BookingTrackingData implements Parcelable
{
	protected String mDriverPk;
	protected String mDriverName;

	protected double mLatitude;
	protected double mLongitude;

	/**[ constructors ]***********************************************************************************************/

	public BookingTrackingData( JSONObject json ) {
		set( json );
	}

	/**[ setters / getters ]******************************************************************************************/

	public BookingTrackingData setDriverPk( String pk ) {
		mDriverPk = pk;
		return this;
	}
	public String getDriverPk() {
		return mDriverPk;
	}
	public BookingTrackingData setDriverName( String name ) {
		mDriverName = name;
		return this;
	}
	public String getDriverName() {
		return mDriverName;
	}

	public BookingTrackingData setLatitude(double latitude) {
		mLatitude = latitude;
		return this;
	}
	public double getLatitude() {
		return mLatitude;
	}
	public BookingTrackingData setLongitude(double longitude) {
		mLongitude = longitude;
		return this;
	}
	public double getLongitude() {
		return mLongitude;
	}


	public BookingTrackingData setLocation( Location loc ) {
		setLatitude( loc.getLatitude() );
		setLongitude( loc.getLongitude() );

		return this;
	}
	public BookingTrackingData setLocation( LatLng loc ) {
		setLatitude( loc.latitude );
		setLongitude( loc.longitude );

		return this;
	}

	/**[ helpers ]****************************************************************************************************/

	public BookingTrackingData set( JSONObject json ) {
		if( json != null ) {
			setDriverPk( JsonTools.getString(json, "pk"));
			setDriverName( JsonTools.getString(json, "name"));

			JSONObject locationObj = JsonTools.getJSONObject(json, "location");
				setLatitude( JsonTools.getDouble(locationObj, "lat"));
				setLongitude( JsonTools.getDouble(locationObj, "lng"));
		}

		return this;
	}

	public LatLng getLatLng() {
		return new LatLng( getLatitude(), getLongitude() );
	}


	/**[ parcelable ]*************************************************************************************************/

    @Override
	public int describeContents() {
        return 0;
    }

    @Override
	public void writeToParcel(Parcel out, int flags) {
    	out.writeString( getDriverPk() );
    	out.writeString( getDriverName() );
    	out.writeDouble( getLatitude() );
    	out.writeDouble( getLongitude() );
    }

    private BookingTrackingData(Parcel in) {
    	setDriverPk(in.readString());
    	setDriverName(in.readString());
    	setLatitude(in.readDouble());
    	setLongitude(in.readDouble());
    }


    public static final Parcelable.Creator<BookingTrackingData> CREATOR = new Parcelable.Creator<BookingTrackingData>() {
        @Override
		public BookingTrackingData createFromParcel(Parcel in) {
            return new BookingTrackingData(in);
        }

        @Override
		public BookingTrackingData[] newArray(int size) {
            return new BookingTrackingData[size];
        }
    };



	/**[ end of class ]***********************************************************************************************/
}
