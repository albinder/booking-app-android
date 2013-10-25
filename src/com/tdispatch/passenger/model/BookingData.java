package com.tdispatch.passenger.model;

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

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;

import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.db.BookingDbAdapter;
import com.webnetmobile.tools.JsonTools;
import com.webnetmobile.tools.WebnetLog;

public class BookingData implements Parcelable
{
	public static final int PAYMENT_METHOD_UNKNOWN 				= 0;
	public static final int PAYMENT_METHOD_CASH 				= 1;
	public static final String PAYMENT_METHOD_CASH_STRING 		= "cash";
	public static final int PAYMENT_METHOD_ACCOUNT 				= 2;
	public static final String PAYMENT_METHOD_ACCOUNT_STRING 	= "account";
	public static final int PAYMENT_METHOD_CARD					= 3;
	public static final String PAYMENT_METHOD_CARD_STRING		= "credit-card";

	// type of booking entry
	public static final int TYPE_UNKNOWN			=   0;
	public static final int TYPE_QUOTING			=   1;
	public static final int TYPE_INCOMING			=   2;
	public static final String TYPE_INCOMING_STRING	= "incoming";
	public static final int TYPE_FROM_PARTNER		=   4;
	public static final int TYPE_DISPATCHED			=   8;
	public static final int TYPE_CONFIRMED			=  16;
	public static final int TYPE_ACTIVE				=  32;
	public static final int TYPE_COMPLETED			=  64;
	public static final int TYPE_REJECTED			= 128;
	public static final int TYPE_CANCELLED			= 256;
	public static final int TYPE_DRAFT				= 512;
	public static final String TYPE_DRAFT_STRING	= "draft";


	protected long mLocalId;

	protected String mPk;
	protected String mBookingKey;

	protected int mType = TYPE_UNKNOWN;

	protected String mDriverPk;

	protected String mPickupDateString;
	protected Date mPickupDate;
	protected LocationData mPickupLocation;
	protected LocationData mDropoffLocation;

	protected String mCabOfficeName;
	protected String mCabOfficeSlug;

	protected int mPassengerCount = 0;
	protected int mLuggageCount = 0;
	protected String mFlightNumber;

	protected ArrayList<LocationData> mWayPoints = new ArrayList<LocationData>();

	protected Double mDistanceMiles;
	protected Double mDistanceKm;

	protected String mCustomerName;
	protected String mCustomerPhone;
	protected String mExtraInfo;

	protected int mPaymentMethod;
	protected boolean mIsPaid;
	protected String mPaymentReference;
	protected Double mPaidValue;
	protected String mPriceRulePk;
	protected Double mPriceCorrection;

	protected Double mCostValue;
	protected String mCostCurrency;
	protected Double mTotalCostValue;
	protected String mTotalCostCurrency;

	protected String mVehiclePk;
	protected String mVehicleName;

	protected String mReceiptUrl;

	protected JSONObject mJson;


	public BookingData() {
		// dummy
	}
	public BookingData( String jsonString ) {
		try {
			JSONObject json = new JSONObject(jsonString);
			set( json );
		} catch( Exception e ) {
			WebnetLog.e("Failed to create Booking from JSON string: '" + jsonString + "'");
			e.printStackTrace();
		}
	}
	public BookingData( JSONObject json ) {
		set( json );
	}
	public BookingData( Cursor cursor ) {
		try {
			JSONObject json = new JSONObject( cursor.getString( cursor.getColumnIndex( BookingDbAdapter.KEY_JSON) ) );
			set( json );
		} catch( Exception e ) {
			WebnetLog.e("Failed to create Booking from Cursor");
			e.printStackTrace();
		}
	}

	public BookingData setLocalId(long localId) {
		mLocalId = localId;
		return this;
	}
	public long getLocalId() {
		return mLocalId;
	}


	public BookingData setPk( String pk ) {
		mPk = pk;
		return this;
	}
	public String getPk() {
		return mPk;
	}


	public BookingData setBookingKey(String bookingKey) {
		mBookingKey = bookingKey;
		return this;
	}
	public String getBookingKey() {
		return mBookingKey;
	}

	public BookingData setDriverPk( String data ) {
		mDriverPk = data;
		return this;
	}
	public String getDriverPk() {
		return mDriverPk;
	}

	public BookingData setPickupDate( String stamp ) {
		// date is RFC 3339
		Time time = new Time();
		time.parse3339( stamp );

		mPickupDateString = stamp;
		mPickupDate = new Date(time.toMillis(false));

		return this;
	}
	public Date getPickupDate() {
		return mPickupDate;
	}
	public String getPickupDateString() {
		return mPickupDateString;
	}

	public BookingData setPickupLocation(LocationData pickupLocation) {
		mPickupLocation = pickupLocation;

		return this;
	}
	public LocationData getPickupLocation() {
		return mPickupLocation;
	}

	public BookingData setDropoffLocation(LocationData dropoffLocation) {
		mDropoffLocation = dropoffLocation;

		return this;
	}
	public LocationData getDropoffLocation() {
		return mDropoffLocation;
	}


	public BookingData setLuggageCount( int count ) {
		mLuggageCount = count;

		return this;
	}
	public int getLuggageCount() {
		return mLuggageCount;
	}

	public BookingData setPassengerCount( int count ) {
		mPassengerCount = count;

		return this;
	}
	public int getPassengerCount() {
		return mPassengerCount;
	}

	public BookingData setFlightNumber( String data ) {
		mFlightNumber = data;

		return this;
	}
	public String getFlightNumber() {
		return mFlightNumber;
	}


	public BookingData setWayPoints( ArrayList<LocationData> data ) {
		mWayPoints = data;

		return this;
	}
	public ArrayList<LocationData> getWayPoints() {
		return mWayPoints;
	}


	public BookingData setDistanceKm( Double data ) {
		mDistanceKm = data;

		return this;
	}
	public Double getDistanceKm() {
		return mDistanceKm;
	}

	public BookingData setDistanceMiles( Double data ) {
		mDistanceMiles = data;

		return this;
	}
	public Double getDistanceMiles() {
		return mDistanceMiles;
	}


	public BookingData setCustomerName( String data ) {
		mCustomerName = data;

		return this;
	}
	public String getCustomerName() {
		return mCustomerName;
	}

	public BookingData setCustomerPhone( String data ) {
		mCustomerPhone = data;

		return this;
	}
	public String getCustomerPhone() {
		return mCustomerPhone;
	}
	public Boolean hasCustomerPhone() {
		Boolean result = true;

		if( (mCustomerPhone == null) || ( mCustomerPhone.equals("") ) ) {
			result = false;
		}

		return result;
	}

	public BookingData setExtraInfo( String data ) {
		mExtraInfo = data;

		return this;
	}
	public String getExtraInfo() {
		return mExtraInfo;
	}


	public BookingData setIsPaid( Boolean data ) {
		mIsPaid = data;

		return this;
	}
	public Boolean isPaid() {
		return mIsPaid;
	}

	public BookingData setPaymentMethod( String data ) {
		int method = PAYMENT_METHOD_UNKNOWN;
		if( PAYMENT_METHOD_CARD_STRING.equals(data) ) {
			method = PAYMENT_METHOD_ACCOUNT;
		} else if( PAYMENT_METHOD_CARD_STRING.equals(data)) {
			method = PAYMENT_METHOD_CARD;
		} else  if( PAYMENT_METHOD_CASH_STRING.equals(data)) {
			method = PAYMENT_METHOD_CASH;
		}

		setPaymentMethod( method );

		return this;
	}
	public BookingData setPaymentMethod( int data ) {
		mPaymentMethod = data;

		return this;
	}
	public int getPaymentMethod() {
		return mPaymentMethod;
	}
	public String getPaymentMethodString() {
		String method = null;

		switch( mPaymentMethod ) {
			case PAYMENT_METHOD_CASH:
				method = PAYMENT_METHOD_CASH_STRING;
				break;
			case PAYMENT_METHOD_ACCOUNT:
				method = PAYMENT_METHOD_ACCOUNT_STRING;
				break;
			case PAYMENT_METHOD_CARD:
				method = PAYMENT_METHOD_CARD_STRING;
				break;
		}

		return method;
	}

	public BookingData setPaymentReference( String reference ) {
		mPaymentReference = reference;
		return this;
	}
	public String getPaymentReference() {
		return mPaymentReference;
	}

	public BookingData setPaidValue( Double value ) {
		mPaidValue = value;
		return this;
	}
	public Double getPaidValue() {
		return mPaidValue;
	}

	public BookingData setPriceRulePk(String pk) {
		mPriceRulePk = pk;
		return this;
	}
	public String getPriceRulePk() {
		return mPriceRulePk;
	}

	public BookingData setPriceCorrection(Double correction) {
		mPriceCorrection = correction;
		return this;
	}
	public Double getPriceCorrection() {
		return mPriceCorrection;
	}

	public BookingData setCostCurrency( String data ) {
		mCostCurrency = data;

		return this;
	}
	public String getCostCurrency() {
		return mCostCurrency;
	}
	public BookingData setCostValue( Double val ) {
		mCostValue = val;
		return this;
	}
	public Double getCostValue() {
		return mCostValue;
	}

	public BookingData setTotalCostCurrency( String data ) {
		mTotalCostCurrency = data;

		return this;
	}
	public String getTotalCostCurrency() {
		return mTotalCostCurrency;
	}
	public BookingData setTotalCostValue( Double val ) {
		mTotalCostValue = val;
		return this;
	}
	public Double getTotalCostValue() {
		return mTotalCostValue;
	}


	public BookingData setCabOfficeName(String data) {
		mCabOfficeName = data;

		return this;
	}
	public String getCabOfficeName() {
		return mCabOfficeName;
	}
	public BookingData setCabOfficeSlug(String data) {
		mCabOfficeSlug = data;

		return this;
	}
	public String getCabOfficeSlug() {
		return mCabOfficeSlug;
	}


	public BookingData setType( String status ) {

		if( status.equals("quoting") ) {
			mType = TYPE_QUOTING;
		} else if (status.equals("from_partner")) {
			mType = TYPE_FROM_PARTNER;
		} else if (TYPE_DRAFT_STRING.equals(status)) {
			mType = TYPE_DRAFT;
		} else if (TYPE_INCOMING_STRING.equals(status)) {
			mType = TYPE_INCOMING;
		} else if (status.equals("dispatched")) {
			mType = TYPE_DISPATCHED;
		} else if (status.equals("rejected")) {
			mType = TYPE_REJECTED;
		} else if (status.equals("completed")) {
			mType = TYPE_COMPLETED;
		} else if (status.equals("confirmed")) {
			mType = TYPE_CONFIRMED;
		} else if (status.equals("active")) {
			mType = TYPE_ACTIVE;
		} else if (status.equals("cancelled")) {
			mType = TYPE_CANCELLED;
		} else {
			WebnetLog.e("Unknown booking type: '" + status + "'");
			mType = TYPE_UNKNOWN;
		}

		return this;
	}
	public BookingData setType( int status ) {
		mType = status;

		return this;
	}
	public int getType() {
		return mType;
	}


	public BookingData setReceiptUrl( String url ) {
		mReceiptUrl = url;
		return this;
	}
	public String getReceiptUrl() {
		return mReceiptUrl;
	}


	public BookingData setVehiclePk( String pk ) {
		mVehiclePk = pk;
		return this;
	}
	public String getVehiclePk() {
		return mVehiclePk;
	}

	public BookingData setVehicleName( String name ) {
		mVehicleName = name;
		return this;
	}
	public String getVehicleName() {
		return mVehicleName;
	}

	// ********************************************

	public BookingData set( JSONObject json ) {

		setJson( json );

		// type of booking
		setType( JsonTools.getString(json, "status") );

		// generic (should be always present, no matter of action
		setDriverPk( JsonTools.getString( json, "driver_pk"));
		setBookingKey( JsonTools.getStringOrNull(json, "booking_key") );
		setPk( JsonTools.getString(json, "pk") );

		setExtraInfo( JsonTools.getString(json, "extra_instructions") );

		JSONObject distObj = JsonTools.getJSONObject(json, "distance");
			setDistanceKm( JsonTools.getDouble( distObj, "km") );
			setDistanceMiles( JsonTools.getDouble(distObj, "miles") );

		setPickupDate( JsonTools.getString(json, "pickup_time" ) );
		setIsPaid( JsonTools.getBoolean(json, "is_paid", false));
		setLuggageCount( JsonTools.getInt(json, "luggage", 0));

		setPaymentMethod( JsonTools.getString(json, "payment_method"));
		setPaymentReference( JsonTools.getString(json, "payment_ref"));
		setPaidValue( JsonTools.getDouble(json, "paid_value") );
		setPriceRulePk( JsonTools.getString(json, "price_rule"));
		setPriceCorrection( JsonTools.getDouble(json, "price_correction"));

		setCustomerName( JsonTools.getString(json, "customer_name"));
		setCustomerPhone( JsonTools.getString(json, "customer_phone"));

		JSONObject dropoffLocation = JsonTools.getJSONObject( json, "dropoff_location");
		if( dropoffLocation != null ) {
			setDropoffLocation( new LocationData( dropoffLocation ) );
		}

		setFlightNumber( JsonTools.getStringOrNull(json, "flight_number") );

		JSONObject costObj = JsonTools.getJSONObject(json, "cost");
			setCostCurrency( JsonTools.getString(costObj, "currency"));
			setCostValue( JsonTools.getDouble(costObj, "value"));

		JSONObject totalCostObj = JsonTools.getJSONObject(json, "total_cost");
			setTotalCostCurrency( JsonTools.getString(totalCostObj, "currency"));
			setTotalCostValue( JsonTools.getDouble(totalCostObj, "value"));

		setPickupLocation( new LocationData( JsonTools.getJSONObject(json, "pickup_location")));

		setPassengerCount( JsonTools.getInt(json, "passengers", 0));

		JSONObject officeObj = JsonTools.getJSONObject(json, "office");
			setCabOfficeName( JsonTools.getString( officeObj, "name") );
			setCabOfficeSlug( JsonTools.getString(officeObj, "slug") );

		// waypoints
		mWayPoints = new ArrayList<LocationData>();
		JSONArray wpObj = JsonTools.getJSONArray(json, "way_points");
		for( int i=0; i< wpObj.length(); i++ ) {
			try {
				LocationData ld = new LocationData( wpObj.getJSONObject( i ) );
				mWayPoints.add( ld );
			} catch( Exception e ) {
				e.printStackTrace();
			}
		}

		setReceiptUrl( JsonTools.getString(json, "receipt_url"));

		return this;
	}

	public BookingData setJson( String jsonString ) {
		try {
			mJson = new JSONObject(jsonString);
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		return this;
	}
	public BookingData setJson( JSONObject json ) {
		mJson = json;

		return this;
	}
	public JSONObject getJson() {
		return mJson;
	}


	// ****** HELPERS *****************************

	public String getTypeName() {

		String type = "unknown";

		switch( getType() ) {
			case TYPE_UNKNOWN:
				type = "unknown";
				break;

			case TYPE_QUOTING:
				type = "quoting";
				break;

			case TYPE_INCOMING:
				type = TYPE_INCOMING_STRING;
				break;

			case TYPE_FROM_PARTNER:
				type = "from partner";
				break;

			case TYPE_DISPATCHED:
				type = "dispatched";
				break;

			case TYPE_CONFIRMED:
				type = "confirmed";
				break;

			case TYPE_ACTIVE:
				type = "active";
				break;

			case TYPE_COMPLETED:
				type = "completed";
				break;

			case TYPE_REJECTED:
				type = "rejected";
				break;

			case TYPE_CANCELLED:
				type = "cancelled";
				break;

			case TYPE_DRAFT:
				type = TYPE_DRAFT_STRING;
				break;
		}

		return type;
	}


	// ****** ACTIVE RECORD ***********************

	public long insert() {
		BookingDbAdapter db = new BookingDbAdapter( TDApplication.getAppContext() );
		long id = db.insert(this);
		setLocalId(id);
		return id;
	}

	public Boolean remove() {
		BookingDbAdapter db = new BookingDbAdapter( TDApplication.getAppContext() );
		return db.remove( this );
	}

	public static boolean removeAll() {
		BookingDbAdapter db = new BookingDbAdapter( TDApplication.getAppContext() );
		return db.removeAll();
	}

	public static boolean removeAllByType( Integer type ) {
		BookingDbAdapter db = new BookingDbAdapter( TDApplication.getAppContext() );
		return db.removeAllByType( type );
	}

	public Boolean update() {
		Boolean result = false;

		if( getLocalId() != 0 ) {
			BookingDbAdapter db = new BookingDbAdapter(TDApplication.getAppContext());
			result = db.update(this);
		}

		return result;
	}

	public static BookingData getByPk( String bookingPk ) {
		BookingDbAdapter db = new BookingDbAdapter( TDApplication.getAppContext() );
		return db.getByPk(bookingPk);
	}
	public static Cursor getAllAsCursor() {
		BookingDbAdapter db = new BookingDbAdapter( TDApplication.getAppContext() );
		return db.getAll();
	}

	public static JSONArray getAllTrackable() {
		BookingDbAdapter db = new BookingDbAdapter( TDApplication.getAppContext() );
		return db.getAllTrackable();
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
		out.writeString( getBookingKey() );
		out.writeString( getDriverPk() );
		out.writeString( getPickupDateString() );
		out.writeParcelable( getPickupLocation(), 0);
		out.writeParcelable( getDropoffLocation(), 0);
		out.writeInt( getPassengerCount() );
		out.writeInt( getLuggageCount() );
		out.writeString( getFlightNumber() );
		out.writeDouble( getDistanceKm() );
		out.writeDouble( getDistanceMiles() );
		out.writeString( getCustomerName() );
		out.writeString( getCustomerPhone() );
		out.writeString( getExtraInfo() );
		out.writeInt( getPaymentMethod() );
		out.writeString( getPaymentReference() );
		out.writeDouble( getPaidValue() );
		out.writeByte( (byte)(isPaid() ? 1 : 0) );
		out.writeString( getCostCurrency() );
		out.writeDouble( getCostValue() );
		out.writeString( getTotalCostCurrency() );
		out.writeDouble( getTotalCostValue() );
		out.writeString( getPriceRulePk() );
		out.writeDouble(getPriceCorrection());
		out.writeString( getCabOfficeName() );
		out.writeString( getCabOfficeSlug() );
		out.writeInt( getType() );
		out.writeString( getReceiptUrl() );
		out.writeString( getVehiclePk() );
		out.writeString( getVehicleName() );

		out.writeString( getJson().toString() );

		out.writeInt( mWayPoints.size() );
		for( LocationData item : mWayPoints ) {
			out.writeParcelable( item, 0);
		}
	}

	private BookingData(Parcel in) {
		setLocalId( in.readLong() );
		setPk( in.readString() );
		setBookingKey( in.readString() );
		setDriverPk( in.readString() );
		setPickupDate( in.readString() );
		setPickupLocation( (LocationData)in.readParcelable(LocationData.class.getClassLoader()));
		setDropoffLocation( (LocationData)in.readParcelable(LocationData.class.getClassLoader()));
		setPassengerCount(in.readInt());
		setLuggageCount(in.readInt());
		setFlightNumber( in.readString() );
		setDistanceKm( in.readDouble() );
		setDistanceMiles( in.readDouble() );
		setCustomerName( in.readString() );
		setCustomerPhone( in.readString() );
		setExtraInfo( in.readString() );
		setPaymentMethod( in.readInt() );
		setPaymentReference( in.readString() );
		setPaidValue( in.readDouble() );
		setIsPaid( (in.readByte() == 1) );
		setCostCurrency( in.readString() );
		setCostValue( in.readDouble() );
		setTotalCostCurrency( in.readString() );
		setTotalCostValue( in.readDouble() );
		setTotalCostCurrency( in.readString() );
		setPriceRulePk( in.readString() );
		setPriceCorrection(in.readDouble());
		setCabOfficeName( in.readString() );
		setCabOfficeSlug( in.readString() );
		setType( in.readInt() );
		setReceiptUrl( in.readString() );
		setVehiclePk( in.readString() );
		setVehicleName( in.readString());

		setJson( in.readString() );

		int wpCount = in.readInt();
		for( int i=0; i<wpCount; i++ ) {
			LocationData ld = (LocationData)in.readParcelable(LocationData.class.getClassLoader());
			mWayPoints.add( ld );
		}
	}

	public static final Parcelable.Creator<BookingData> CREATOR = new Parcelable.Creator<BookingData>() {
		@Override
		public BookingData createFromParcel(Parcel in) {
			return new BookingData(in);
		}

		@Override
		public BookingData[] newArray(int size) {
			return new BookingData[size];
		}
	};

}
