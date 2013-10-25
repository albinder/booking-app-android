package com.tdispatch.passenger.fragment.dialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog.OnDateSetListener;
import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog.OnTimeSetListener;
import com.tdispatch.passenger.R;
import com.tdispatch.passenger.api.ApiHelper;
import com.tdispatch.passenger.api.ApiResponse;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.common.Office;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDDialogFragment;
import com.tdispatch.passenger.model.CardData;
import com.tdispatch.passenger.model.LocationData;
import com.tdispatch.passenger.model.VehicleData;
import com.webnetmobile.tools.JsonTools;
import com.webnetmobile.tools.WebnetTools;

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
public class BookingConfirmationDialogFragment extends TDDialogFragment
{
	public interface BookingConfirmationDialogClickListener
	{
		public void bookingConfirmed(LocationData pickup, LocationData dropoff, Long pickupMillis,
											String vehiclePk, String cardToken);
	}


	// **************************************

	public static final int DIALOG_TYPE_OK 		= 0;
	public static final int DIALOG_TYPE_ERROR		= 1;

	protected static final String KEY_PICKUP 		= "pickup";
	protected static final String KEY_DROPOFF	 	= "dropoff";

	public static BookingConfirmationDialogFragment newInstance(LocationData pickup, LocationData dropoff) {

		BookingConfirmationDialogFragment frag = new BookingConfirmationDialogFragment();

		if( pickup == null ) {
			throw new NullPointerException("Pickup location cannot be null");
		}

		Bundle args = new Bundle();
		args.putParcelable(KEY_PICKUP, pickup);
		args.putParcelable(KEY_DROPOFF, dropoff);
		frag.setArguments(args);

		return frag;
	}

	protected BookingConfirmationDialogClickListener mHostFragment;

	protected LocationData mPickup;
	protected LocationData mDropoff;
	protected Long mPickupMillis = 0L;			// 0 == now

	protected int mShortestPickupTimeOffset = Office.getMinimumAllowedPickupTimeOffset();

	protected Boolean mBookButtonEnabled = false;
	protected Boolean mInitialFeeCalcDone = false;

	protected String mVehiclePk = null;
	protected ArrayList<VehicleData> mVehicles = new ArrayList<VehicleData>();

	protected String mCardToken = null;
	protected ArrayList<CardData> mCards = new ArrayList<CardData>();

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);

		if( mShortestPickupTimeOffset < 0 ) {
			throw new IllegalArgumentException("caboffice_minimum_allowed_pickup_time_offset_in_minutes must cannot be negative value");
		}


		try {
			mHostFragment = (BookingConfirmationDialogClickListener)getTargetFragment();
		} catch( ClassCastException e ) {
			throw new ClassCastException("Host Fragment needs to implement BookingConfirmationDialogClickListener");
		}

		Bundle args = getArguments();
		mPickup = args.getParcelable(KEY_PICKUP);
		mDropoff = args.getParcelable(KEY_DROPOFF);

		if( mShortestPickupTimeOffset > 0 ) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis( c.getTimeInMillis() + (mShortestPickupTimeOffset * (WebnetTools.MILLIS_PER_MINUTE)) );

			mPickupYear = c.get(Calendar.YEAR);
			mPickupMonth = c.get(Calendar.MONTH);
			mPickupDay = c.get(Calendar.DAY_OF_MONTH);

			mPickupHour = c.get(Calendar.HOUR_OF_DAY);
			mPickupMinute = c.get(Calendar.MINUTE);

			mTimePickerInitialized = true;
			mDatePickerInitialized = true;
		}

	}

	@Override
	protected int getLayoutId() {
		return R.layout.booking_confirmation_dialog_fragment;
	}

	@Override
	protected void onPostCreateView() {

		if( Office.isBraintreeEnabled() ) {
			mCards = CardData.getAll();
			if ( mCards.size() > 0 ) {
				mCardToken = mCards.get(0).getToken();

				Spinner sp = (Spinner)mFragmentView.findViewById(R.id.spinner_card);
				CardSpinnerAdapter mCardAdapter = new CardSpinnerAdapter( mParentActivity, R.layout.card_spinner_entry, mCards );
				sp.setAdapter( mCardAdapter );
				sp.setOnItemSelectedListener( mCardSelectedListener );
				sp.setSelection(0);

				WebnetTools.setVisibility(mFragmentView, R.id.card_container, View.VISIBLE );
			} else {
				throw new RuntimeException("No card defined");
			}
		} else {
			WebnetTools.setVisibility(mFragmentView, R.id.card_container, View.GONE );
		}


		mVehicles = VehicleData.getAll();
		if( mVehicles.size() > 0 ) {
			mVehiclePk = VehicleData.getDefault().getPk();

			int defaultVehiclePos = 0;
			for( VehicleData v : mVehicles ) {
				if( v.getPk().equals(mVehiclePk) ) {
					break;
				}
				defaultVehiclePos++;
			}

			Spinner sp = (Spinner)mFragmentView.findViewById(R.id.spinner_vehicle);
			VehicleSpinnerAdapter mVehicleAdapter = new VehicleSpinnerAdapter( mParentActivity, R.layout.vehicle_spinner_entry, mVehicles );
			sp.setAdapter( mVehicleAdapter );
			sp.setOnItemSelectedListener( mVehicleSelectedListener );
			sp.setSelection(defaultVehiclePos);

			WebnetTools.setVisibility(mFragmentView, R.id.vehicle_container, View.VISIBLE );
		} else {
			WebnetTools.setVisibility(mFragmentView, R.id.vehicle_container, View.GONE );
		}


		int[] ids = { R.id.button_ok, R.id.button_retry, R.id.button_cancel, R.id.button_pickup_date, R.id.button_pickup_time, R.id.button_now, R.id.button_later};
		for( int id : ids ) {
			View button = mFragmentView.findViewById( id );
			button.setOnClickListener( mOnClickListener );
		}

		if (mDropoff != null ) {
			recalculateFee();
		} else {
			mBookButtonEnabled = true;
			unlockUI();
		}

		updateDisplay();
	}

	protected View.OnClickListener mOnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {

			switch( v.getId() ) {

				case R.id.button_pickup_date: {
					showDatePicker(mPickupYear, mPickupMonth, mPickupDay);
				}
				break;

				case R.id.button_later:
				case R.id.button_pickup_time: {
					showTimePicker(mPickupHour, mPickupMinute);
				}
				break;

				case R.id.button_ok: {
					if( validatePickupTimeAndShowMessage() ){
						mHostFragment.bookingConfirmed(mPickup, mDropoff, getPickupTimeMillis(), mVehiclePk, mCardToken);
						dismiss();
					}
				}
				break;

				case R.id.button_cancel: {
					dismiss();
				}
				break;

				case R.id.button_retry: {
					recalculateFee();
				}
				break;
			}
		}
	};



	protected Long getPickupTimeMillis() {
		Long pickupMillis = null;

		if( mTimePickerInitialized && mDatePickerInitialized ) {
			Calendar c = Calendar.getInstance();
			c.set(mPickupYear, mPickupMonth, mPickupDay, mPickupHour, mPickupMinute );
			pickupMillis = c.getTimeInMillis();
		}

		return pickupMillis;
	}


	protected Boolean validatePickupTimeAndShowMessage() {

		Long pickupMillis = getPickupTimeMillis();

		Boolean result = true;

		if( result ) {
			int maxDaysAhead = Office.getNewBookingMaxDaysAhead();

			String pickupMillisBody = "";

			if( pickupMillis != null ) {
				Long diff = (pickupMillis - System.currentTimeMillis());

				if( diff > 0 ) {
					if( diff > (WebnetTools.MILLIS_PER_MINUTE * 5) ) {
						if( diff > (WebnetTools.MILLIS_PER_DAY * maxDaysAhead) ) {
							pickupMillisBody = getString(R.string.new_booking_pickup_date_too_ahead_body_fmt, maxDaysAhead);
							result = false;
						}
					}
				} else {
					pickupMillisBody = getString(R.string.new_booking_pickup_date_already_passed);
					result = false;
				}

				if (!result ) {
					showDialog( GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), pickupMillisBody );
				}
			}
		}


		if( result ) {
			if( (mTimePickerInitialized==false) && (mDatePickerInitialized==false) ) {
				pickupMillis = null;
			} else {
				Calendar c = Calendar.getInstance();
				c.set(mPickupYear, mPickupMonth, mPickupDay, mPickupHour, mPickupMinute );
				pickupMillis = c.getTimeInMillis();
			}

			Boolean pickupTimeTooEarly = false;
			if( mShortestPickupTimeOffset > 0 ) {
				if( pickupMillis != null ) {
					if( (pickupMillis - System.currentTimeMillis()) < (mShortestPickupTimeOffset*WebnetTools.MILLIS_PER_MINUTE) ) {
						pickupTimeTooEarly = true;
					}
				}
			}

			if( pickupTimeTooEarly ) {
				String tooEarly = getString(R.string.new_booking_pickup_date_too_early_fmt, mShortestPickupTimeOffset);
				showDialog( GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), tooEarly );

				result = false;
			}
		}

		return result;
	}


	protected void updateDisplay() {
		WebnetTools.setText(mFragmentView, R.id.pickup_address, mPickup.getAddress());
		if( mDropoff != null ) {
			WebnetTools.setText(mFragmentView, R.id.dropoff_address, mDropoff.getAddress());
		} else {
			WebnetTools.setVisibility(mFragmentView, R.id.dropoff_container, View.GONE);
			WebnetTools.setVisibility(mFragmentView, R.id.fee_container, View.GONE);
		}

		String pickupTime = getString(R.string.new_booking_dialog_pickup_time_now);
		Boolean dateTimePickersVisibile = false;
		if( mTimePickerInitialized ) {
			pickupTime = String.format(Locale.US, "%02d:%02d", mPickupHour, mPickupMinute);		// FIXME am/pm
			dateTimePickersVisibile = true;
		}
		WebnetTools.setText(mFragmentView, R.id.button_pickup_time, pickupTime);
		WebnetTools.setVisibility( mFragmentView, R.id.pickup_pickers_container,  dateTimePickersVisibile ? View.VISIBLE : View.GONE );
		WebnetTools.setVisibility( mFragmentView, R.id.picker_buttons_container, !dateTimePickersVisibile ? View.VISIBLE : View.GONE );

		String pickupDate = "";
		if( mDatePickerInitialized ) {
			SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.US);
			sdf.setTimeZone(TimeZone.getDefault());
			pickupDate = sdf.format( new Date(mPickupYear-1900, mPickupMonth, mPickupDay, mPickupHour, mPickupMinute) );
		}
		WebnetTools.setText(mFragmentView, R.id.button_pickup_date, pickupDate );

		WebnetTools.setText(mFragmentView, R.id.fee, mBookingFeePriceFormatted);

		if( mDropoff != null ) {
			if( mInitialFeeCalcDone ) {
				WebnetTools.setVisibility(mFragmentView, R.id.button_ok, (mBookButtonEnabled) ? View.VISIBLE : View.INVISIBLE);
				WebnetTools.setVisibility(mFragmentView, R.id.button_retry, (mBookButtonEnabled) ? View.INVISIBLE : View.VISIBLE);
			} else {
				WebnetTools.setVisibility(mFragmentView, R.id.button_ok, View.INVISIBLE);
				WebnetTools.setVisibility(mFragmentView, R.id.button_retry, View.INVISIBLE);
			}
		} else {
			WebnetTools.setVisibility(mFragmentView, R.id.button_ok, View.VISIBLE);
		}
	}



	protected Boolean mTimePickerInitialized = false;
	protected int mPickupHour = 0;
	protected int mPickupMinute = 0;

	protected void showTimePicker( int hour, int minute ) {
		if( (hour == 0) && (minute == 0)) {
			Calendar c = Calendar.getInstance();

			c.add(Calendar.MINUTE, 10);
			hour = c.get(Calendar.HOUR_OF_DAY);
			minute = c.get(Calendar.MINUTE);
		}

		RadialTimePickerDialog timePicker = RadialTimePickerDialog.newInstance( mOnTimeSetListener,
							hour, minute, DateFormat.is24HourFormat(TDApplication.getAppContext()));
		timePicker.show( mFragmentManager, "radialtimepicker");
	}

	protected OnTimeSetListener mOnTimeSetListener = new OnTimeSetListener() {
		@Override
		public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
			mPickupHour = hourOfDay;
			mPickupMinute = minute;
			mTimePickerInitialized = true;

			initPickupDateOnce();
			updateDisplay();

			WebnetTools.setText( mFragmentView, R.id.fee, "---" );
			if ( validatePickupTimeAndShowMessage() ) {
				recalculateFee();
			}
		}
	};



	protected Boolean mDatePickerInitialized = false;
	protected int mPickupYear = 0;
	protected int mPickupMonth = 0;
	protected int mPickupDay = 0;
	protected void initPickupDateOnce() {
		if(mDatePickerInitialized == false) {
			Calendar c = Calendar.getInstance();
			mPickupYear = c.get(Calendar.YEAR);
			mPickupMonth = c.get(Calendar.MONTH);
			mPickupDay = c.get(Calendar.DAY_OF_MONTH);

			mDatePickerInitialized = true;
		}
	}

	protected void showDatePicker(int year, int month, int day ) {
		if( (year+month+day) == 0 ) {
			Calendar c = Calendar.getInstance();
			year = c.get(Calendar.YEAR);
			month = c.get(Calendar.MONTH);
			day = c.get(Calendar.DAY_OF_MONTH);
		}

		CalendarDatePickerDialog picker = CalendarDatePickerDialog.newInstance(mOnSetDateListener, year, month, day);
		picker.show(mFragmentManager, "radialdatepicker");
	}

	protected CalendarDatePickerDialog.OnDateSetListener mOnSetDateListener = new OnDateSetListener() {

		@Override
		public void onDateSet(CalendarDatePickerDialog dialog, int year, int month, int day) {
			mPickupYear = year;
			mPickupMonth = month;
			mPickupDay = day;

			updateDisplay();

			WebnetTools.setText( mFragmentView, R.id.fee, "---" );
			if ( validatePickupTimeAndShowMessage() ) {
				recalculateFee();
			}
		}
	};


	protected void recalculateFee() {
		if (mDropoff != null) {
			WebnetTools.executeAsyncTask( new GetFeeAsyncTask() );
		}
	}

	protected String mBookingFeePriceFormatted 	= "";
	protected String mBookingFeeDistanceFormatted 	= "";

	protected class GetFeeAsyncTask extends AsyncTask<Void, Void, ApiResponse> {

		@Override
		protected void onPreExecute() {
			lockUI();
			WebnetTools.setText( mFragmentView, R.id.fee, R.string.tdfragment_please_wait );
		}

		@Override
		protected ApiResponse doInBackground( Void ... params ) {

			ApiHelper api = ApiHelper.getInstance(TDApplication.getAppContext());
			ApiResponse response = new ApiResponse();

			try {
				Long  millis = (getPickupTimeMillis() != null) ? getPickupTimeMillis() : System.currentTimeMillis();

				response = api.locationFare( mPickup, mDropoff, millis, mVehiclePk );
				if( response.getErrorCode() == Const.ErrorCode.OK ) {

					JSONObject feeJson = JsonTools.getJSONObject( response.getJSONObject(), "fare");
					mBookingFeePriceFormatted = JsonTools.getString(feeJson, "formatted_total_cost");

					JSONObject distJson = JsonTools.getJSONObject(feeJson, "distance");
					if( WebnetTools.useMetricUnits() ) {
						mBookingFeeDistanceFormatted = String.format( getString(R.string.journey_distance_metrics_fmt), JsonTools.getString(distJson, "km") );
					} else {
						mBookingFeeDistanceFormatted = String.format( getString(R.string.journey_distance_imperial_fmt), JsonTools.getString(distJson, "miles") );
					}
				}

			} catch ( Exception e ) {
				e.printStackTrace();
			}

			return response;
		}

		@Override
		protected void onPostExecute( ApiResponse response ) {
			unlockUI();

			mInitialFeeCalcDone = true;

			if( response.getErrorCode() == Const.ErrorCode.OK ) {
				mBookButtonEnabled = true;
			} else {
				mBookButtonEnabled = false;
				mBookingFeePriceFormatted = "???";
				showDialog(GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), response.getErrorMessage());
			}

			updateDisplay();
		}
	}

	protected void lockUI() {
		doHandleBusy(true);
	}
	protected void unlockUI() {
		doHandleBusy(false);
	}
	protected void doHandleBusy(Boolean show) {

		View v = mFragmentView.findViewById( R.id.busy_container );
		AnimationDrawable busyAnim = (AnimationDrawable)((ImageView)mFragmentView.findViewById(R.id.busy)).getBackground();
		if (show) {
			busyAnim.start();
			v.setVisibility(View.VISIBLE);
		} else {
			v.setVisibility(View.GONE);
			busyAnim.stop();
		}
	}


	/**[ spinner listeners ]*************************************************************************************************/

	protected Boolean mSkippedVehicleSelectedInitEvent = false;
	protected OnItemSelectedListener mVehicleSelectedListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
			mVehiclePk = mVehicles.get(position).getPk();
			if( mSkippedVehicleSelectedInitEvent ) {
				recalculateFee();
			} else {
				mSkippedVehicleSelectedInitEvent = true;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// dummy
		}
	};


	protected OnItemSelectedListener mCardSelectedListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
			mCardToken = mCards.get(position).getToken();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// dummy
		}
	};

	/**[ vehicle spinner adapter ]*******************************************************************************************/

	protected class VehicleSpinnerAdapter extends ArrayAdapter<VehicleData>
	{
		protected TDApplication mApp;
		protected Context mContext;

		public VehicleSpinnerAdapter(Activity activity, int textViewResourceId, ArrayList<VehicleData> objects) {
			super(activity, textViewResourceId, objects);

			mContext = activity;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			return buildView(position, convertView, parent);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return buildView(position, convertView, parent);
		}


		protected View buildView(int position, View convertView, ViewGroup parent) {
			VehicleData item = mVehicles.get(position);

			View view = convertView;
			if ( view == null) {
				LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = li.inflate(R.layout.vehicle_list_entry, null);

				WebnetTools.setCustomFonts( TDApplication.getAppContext(), (ViewGroup)view );
			}

			WebnetTools.setText(view, R.id.name, item.getName());

			return( view );
		}
	}




	/**[ cards spinner adapter ]*********************************************************************************************/

	protected class CardSpinnerAdapter extends ArrayAdapter<CardData>
	{
		protected TDApplication mApp;
		protected Context mContext;

		public CardSpinnerAdapter(Activity activity, int textViewResourceId, ArrayList<CardData> objects) {
			super(activity, textViewResourceId, objects);

			mContext = activity;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			return buildView(position, convertView, parent);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return buildView(position, convertView, parent);
		}


		protected View buildView(int position, View convertView, ViewGroup parent) {
			CardData item = mCards.get(position);

			View view = convertView;
			if ( view == null) {
				LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = li.inflate(R.layout.card_spinner_list_entry, null);

				WebnetTools.setCustomFonts( TDApplication.getAppContext(), (ViewGroup)view );
			}

			WebnetTools.setText(view, R.id.label, item.getDisplayLabel());
			WebnetTools.setText(view, R.id.number, item.getNumber());

			return( view );
		}
	}


}	// end of class
