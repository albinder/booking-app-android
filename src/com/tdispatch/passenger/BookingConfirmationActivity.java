package com.tdispatch.passenger;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog.OnDateSetListener;
import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog.OnTimeSetListener;
import com.tdispatch.passenger.api.ApiHelper;
import com.tdispatch.passenger.api.ApiResponse;
import com.tdispatch.passenger.core.TDActivity;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.define.BundleKey;
import com.tdispatch.passenger.define.ErrorCode;
import com.tdispatch.passenger.define.PaymentMethod;
import com.tdispatch.passenger.define.RequestCode;
import com.tdispatch.passenger.fragment.dialog.GenericDialogFragment;
import com.tdispatch.passenger.model.CardData;
import com.tdispatch.passenger.model.LocationData;
import com.tdispatch.passenger.model.VehicleData;
import com.tdispatch.passenger.tools.Office;
import com.webnetmobile.tools.JsonTools;
import com.webnetmobile.tools.WebnetTools;

public class BookingConfirmationActivity extends TDActivity
{
	protected LocationData mPickupLocation;
	protected LocationData mDropoffLocation;

	protected int mShortestPickupTimeOffset = Office.getMinimumAllowedPickupTimeOffset();

	protected Boolean mBookButtonEnabled = false;
	protected Boolean mInitialFeeCalcDone = false;

	protected int mRequiredSeats = 1;
	protected int mRequiredLuggage = 0;
    protected int mMaxPossibleSeats = (VehicleData.getMaxPassengerCount() - 1);
    protected int mMaxPossibleLuggage = VehicleData.getMaxLuggageCount();

	protected VehicleData mVehicle = VehicleData.getDefault();
	protected ArrayList<VehicleData> mVehicles = VehicleData.getAll();

	protected String mCardToken = null;
	protected ArrayList<CardData> mCards = new ArrayList<CardData>();
	protected int mPaymentMethod = PaymentMethod.UNKNOWN;

	protected String mNotes = null;

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);

		if( mShortestPickupTimeOffset < 0 ) {
			throw new IllegalArgumentException("caboffice_minimum_allowed_pickup_time_offset_in_minutes cannot be negative value");
		}

		setContentView( R.layout.booking_confirmation_activity );

		Bundle extras = getIntent().getExtras();

		mPickupLocation = extras.getParcelable(BundleKey.PICKUP_LOCATION);
		mDropoffLocation = extras.getParcelable(BundleKey.DROPOFF_LOCATION);

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



		WebnetTools.setVisibility(this, R.id.payment_method_container, View.GONE);
		WebnetTools.setVisibility(mMe, R.id.card_container, View.GONE );
		if( Office.isBraintreeEnabled() ) {
			mCards = CardData.getAll();
			if ( mCards.size() > 0 ) {
				mCardToken = mCards.get(0).getToken();

				Spinner sp = (Spinner)findViewById(R.id.spinner_card);
				CardSpinnerAdapter mCardAdapter = new CardSpinnerAdapter( mMe, R.layout.card_spinner_entry, mCards );
				sp.setAdapter( mCardAdapter );
				sp.setOnItemSelectedListener( mCardSelectedListener );
				sp.setSelection(0);

				WebnetTools.setVisibility(mMe, R.id.card_container, View.VISIBLE );
				setPaymentMethod( PaymentMethod.CARD );

				if( Office.isCashPaymentDisabled() == false ) {
					WebnetTools.setVisibility(this, R.id.payment_method_container, View.VISIBLE);
				}
			} else {
				if( Office.isCashPaymentDisabled() ) {
					throw new RuntimeException("No card defined");
				} else {
					WebnetTools.setVisibility(this, R.id.button_card, View.GONE);
					WebnetTools.setVisibility(this, R.id.payment_method_container, View.VISIBLE);
					setPaymentMethod( PaymentMethod.CASH );
				}
			}
		} else {
			setPaymentMethod( PaymentMethod.CASH );
		}

		SeekBar passengerSeekBar = (SeekBar)findViewById(R.id.passenger_count_seekbar);
		passengerSeekBar.setOnSeekBarChangeListener(mOnPassengerCountSeekBarChangeListener);
		passengerSeekBar.setMax( mMaxPossibleSeats );
		passengerSeekBar.setProgress(0);

		SeekBar luggageSeekBar = (SeekBar)findViewById(R.id.luggage_count_seekbar);
		luggageSeekBar.setOnSeekBarChangeListener(mOnLuggageCountSeekBarChangeListener);
		luggageSeekBar.setMax( mMaxPossibleLuggage );
		luggageSeekBar.setProgress(0);
		WebnetTools.setVisibility(this, R.id.luggage_count_container, (Office.isLuggageSupportEnabled()) ? View.VISIBLE : View.GONE);

		int[] ids = {	R.id.button_vehicle, R.id.button_retry,
						R.id.button_ok, R.id.button_pickup_date,
						R.id.button_pickup_time, R.id.button_now, R.id.button_later,
						R.id.notes, R.id.button_card, R.id.button_cash
					};
		for( int id : ids ) {
			View button = findViewById( id );
			button.setOnClickListener( mOnClickListener );
		}

		if (mDropoffLocation != null ) {
			recalculateFee();
		} else {
			mBookButtonEnabled = true;
			doHandleBusy(false);
		}

		updateDisplay();

		setCustomFonts();
	}

	protected OnSeekBarChangeListener mOnPassengerCountSeekBarChangeListener = new OnSeekBarChangeListener()
	{
		@Override
		public void onStopTrackingTouch( SeekBar seekBar ) {}

		@Override
		public void onStartTrackingTouch( SeekBar seekBar ) {}

		@Override
		public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser ) {
			mRequiredSeats = (progress+1);
			WebnetTools.setText(mMe, R.id.passenger_count, String.valueOf(mRequiredSeats));

			findNewUsableVehicleIfNeeded();
		}
	};
	protected OnSeekBarChangeListener mOnLuggageCountSeekBarChangeListener = new OnSeekBarChangeListener()
	{
		@Override
		public void onStopTrackingTouch( SeekBar seekBar ) {}

		@Override
		public void onStartTrackingTouch( SeekBar seekBar ) {}

		@Override
		public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser ) {
			mRequiredLuggage = progress;
			WebnetTools.setText(mMe, R.id.luggage_count, String.valueOf(mRequiredLuggage));

			findNewUsableVehicleIfNeeded();
		}
	};


	protected void findNewUsableVehicleIfNeeded() {

		if( mVehicle != null ) {
			if( (mVehicle.getPassengerCapacity() < mRequiredSeats) || (mVehicle.getLuggageCapacity() < mRequiredLuggage) ) {
				Boolean foundIt = false;

				for( VehicleData vehicle : mVehicles ) {
					if( vehicle.getPk().equals(mVehicle.getPk()) == false ) {
						if( (vehicle.getPassengerCapacity() >= mRequiredSeats) && (vehicle.getLuggageCapacity() >= mRequiredLuggage) ) {
							mVehicle = vehicle;

							updateDisplay();
							recalculateFee();

							foundIt = true;
							break;
						}
					}
				}

				if( foundIt == false ) {
					showDialog( GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), getString(R.string.new_booking_dialog_error_failed_to_find_vehicle) );
				}
			}
		}
	}

	protected void setPaymentMethod(int method) {
		mPaymentMethod = method;

		switch( method ) {
			case PaymentMethod.CASH: {
				WebnetTools.setVisibility(this, R.id.card_container, View.GONE);
				findViewById(R.id.button_cash).setBackgroundResource(R.drawable.button_toggle_selected_drawable);
				findViewById(R.id.button_card).setBackgroundResource(R.drawable.button_toggle_normal_drawable);
				findViewById(R.id.button_card).setClickable(true);
				findViewById(R.id.button_cash).setClickable(false);
			}
			break;

			case PaymentMethod.CARD: {
				WebnetTools.setVisibility(this, R.id.card_container, View.VISIBLE);
				findViewById(R.id.button_card).setBackgroundResource(R.drawable.button_toggle_selected_drawable);
				findViewById(R.id.button_cash).setBackgroundResource(R.drawable.button_toggle_normal_drawable);
				findViewById(R.id.button_card).setClickable(false);
				findViewById(R.id.button_cash).setClickable(true);
			}
			break;
		}
	}

	protected View.OnClickListener mOnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {

			switch( v.getId() ) {
				case R.id.button_cash: {
					setPaymentMethod(PaymentMethod.CASH);
					recalculateFee();
				}
				break;
				case R.id.button_card: {
					setPaymentMethod(PaymentMethod.CARD);
					recalculateFee();
				}
				break;

				case R.id.button_vehicle: {
					Intent intent = new Intent();
					intent.putExtra(BundleKey.PASSENGER_COUNT, mRequiredSeats);
					intent.putExtra(BundleKey.LUGGAGE_COUNT, mRequiredLuggage);
					intent.putExtra(BundleKey.VEHICLE, mVehicle);
					intent.setComponent( new ComponentName( mContext.getPackageName(), VehicleSelectorActivity.class.getName() ) );
					try {
						startActivityForResult(intent, RequestCode.VEHICLE_SELECTOR);
						overridePendingTransition( R.anim.activity_slide_in_up, R.anim.fade_out);
					} catch ( Exception e ) {
						e.printStackTrace();
					}
				}
				break;

				case R.id.notes: {
					Intent intent = new Intent();
					intent.putExtra(BundleKey.TITLE, getString(R.string.new_booking_dialog_notes_editor_title));
					intent.putExtra(BundleKey.BODY, mNotes);
					intent.setComponent( new ComponentName( mContext.getPackageName(), EditorActivity.class.getName() ) );
					try {
						startActivityForResult(intent, RequestCode.EDITOR);
						overridePendingTransition( R.anim.activity_slide_in_up, R.anim.fade_out);
					} catch ( Exception e ) {
						e.printStackTrace();
					}
				}
				break;

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

						Intent intent = new Intent();
						intent.putExtra( BundleKey.PICKUP_LOCATION, mPickupLocation );
						intent.putExtra( BundleKey.DROPOFF_LOCATION, mDropoffLocation );
						intent.putExtra( BundleKey.VEHICLE_PK, mVehicle.getPk() );
						intent.putExtra( BundleKey.CARD_TOKEN, (mPaymentMethod == PaymentMethod.CARD) ? mCardToken : null );
						intent.putExtra( BundleKey.PICKUP_TIME, (getPickupTimeMillis() == null) ? 0L : getPickupTimeMillis());
						intent.putExtra( BundleKey.NOTES, mNotes);
						intent.putExtra( BundleKey.PAYMENT_METHOD, mPaymentMethod);
						intent.putExtra( BundleKey.PASSENGER_COUNT, mRequiredSeats);
						intent.putExtra( BundleKey.LUGGAGE_COUNT, mRequiredLuggage);

						setResult( Activity.RESULT_OK, intent );
						finish();
					}
				}
				break;

				case R.id.button_retry: {
					recalculateFee();
				}
				break;
			}
		}
	};



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch( requestCode ) {

			case RequestCode.VEHICLE_SELECTOR: {
				if( resultCode == Activity.RESULT_OK ) {
					mRequiredSeats = intent.getIntExtra(BundleKey.PASSENGER_COUNT, mRequiredSeats);
					mRequiredLuggage = intent.getIntExtra(BundleKey.LUGGAGE_COUNT, mRequiredLuggage);
					mVehicle = intent.getParcelableExtra(BundleKey.VEHICLE);
					updateDisplay();
					recalculateFee();
				}
			}
			break;

			case RequestCode.EDITOR: {
				if( resultCode == Activity.RESULT_OK ) {
					String notes = intent.getExtras().getString(BundleKey.BODY);
					mNotes = ( "".equals(notes) ) ? null : notes;
					updateDisplay();
				}
			}
			break;

			default: {
				super.onActivityResult(requestCode, resultCode, intent );
			}
			break;
		}
	}




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

		TextView notes = (TextView)findViewById(R.id.notes);
		notes.setText( (mNotes == null) ? getString(R.string.new_booking_dialog_notes) : mNotes);

		WebnetTools.setText(mMe, R.id.pickup_address, mPickupLocation.getAddress());
		if( mDropoffLocation != null ) {
			WebnetTools.setText(mMe, R.id.dropoff_address, mDropoffLocation.getAddress());
		} else {
			WebnetTools.setVisibility(mMe, R.id.dropoff_container, View.GONE);
			WebnetTools.setVisibility(mMe, R.id.fee_container, View.GONE);
		}

		String pickupTime = getString(R.string.new_booking_dialog_pickup_time_now);
		Boolean dateTimePickersVisibile = false;
		if( mTimePickerInitialized ) {
			pickupTime = String.format(Locale.US, "%02d:%02d", mPickupHour, mPickupMinute);		// FIXME am/pm
			dateTimePickersVisibile = true;
		}
		WebnetTools.setText(mMe, R.id.button_pickup_time, pickupTime);
		WebnetTools.setVisibility( mMe, R.id.pickup_pickers_container,  dateTimePickersVisibile ? View.VISIBLE : View.GONE );
		WebnetTools.setVisibility( mMe, R.id.picker_buttons_container, !dateTimePickersVisibile ? View.VISIBLE : View.GONE );

		String pickupDate = "";
		if( mDatePickerInitialized ) {
			SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.US);
			sdf.setTimeZone(TimeZone.getDefault());
			pickupDate = sdf.format( new Date(mPickupYear-1900, mPickupMonth, mPickupDay, mPickupHour, mPickupMinute) );
		}
		WebnetTools.setText(mMe, R.id.button_pickup_date, pickupDate );

		WebnetTools.setText(mMe, R.id.fee, mBookingFeePriceFormatted);

		if( mVehicle != null ) {
			WebnetTools.setText(this, R.id.button_vehicle, mVehicle.getName());
		}


		if( mDropoffLocation != null ) {
			if( mInitialFeeCalcDone ) {
				WebnetTools.setVisibility(mMe, R.id.button_ok, (mBookButtonEnabled) ? View.VISIBLE : View.INVISIBLE);
				WebnetTools.setVisibility(mMe, R.id.button_retry, (mBookButtonEnabled) ? View.INVISIBLE : View.VISIBLE);
			} else {
				WebnetTools.setVisibility(mMe, R.id.button_ok, View.INVISIBLE);
				WebnetTools.setVisibility(mMe, R.id.button_retry, View.INVISIBLE);
			}
		} else {
			WebnetTools.setVisibility(mMe, R.id.button_ok, View.VISIBLE);
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

			WebnetTools.setText( mMe, R.id.fee, "---" );
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

			WebnetTools.setText( mMe, R.id.fee, "---");
			if ( validatePickupTimeAndShowMessage() ) {
				recalculateFee();
			}
		}
	};



	protected void recalculateFee() {
		if (mDropoffLocation != null) {
			WebnetTools.executeAsyncTask( new GetFeeAsyncTask() );
		}
	}

	protected String mBookingFeePriceFormatted 	= "";
	protected String mBookingFeeDistanceFormatted 	= "";

	protected class GetFeeAsyncTask extends AsyncTask<Void, Void, ApiResponse> {

		@Override
		protected void onPreExecute() {
			lockUI();
			WebnetTools.setText( mMe, R.id.fee, R.string.tdfragment_please_wait );
		}

		@Override
		protected ApiResponse doInBackground( Void ... params ) {

			ApiHelper api = ApiHelper.getInstance(TDApplication.getAppContext());
			ApiResponse response = new ApiResponse();

			try {
				Long  millis = (getPickupTimeMillis() != null) ? getPickupTimeMillis() : System.currentTimeMillis();

				response = api.locationFare( mPickupLocation, mDropoffLocation, millis, mVehicle.getPk(),
						(Office.isBraintreeEnabled()) ? PaymentMethod.CARD : PaymentMethod.CASH );
				if( response.getErrorCode() == ErrorCode.OK ) {
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

			if( response.getErrorCode() == ErrorCode.OK ) {
				mBookButtonEnabled = true;
			} else {
				mBookButtonEnabled = false;
				mBookingFeePriceFormatted = "???";
				showDialog(GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), response.getErrorMessage());
			}

			updateDisplay();
		}
	}

	@Override
	public void lockUI() {
		super.lockUI();
		doHandleBusy(true);
	}

	@Override
	public void unlockUI() {
		doHandleBusy(false);
		super.unlockUI();
	}
	protected void doHandleBusy(Boolean show) {
		ImageView v = (ImageView)findViewById( R.id.busy);
		AnimationDrawable busyAnim = (AnimationDrawable)(v).getBackground();
		if (show) {
			busyAnim.start();
			v.setVisibility(View.VISIBLE);
		} else {
			v.setVisibility(View.GONE);
			busyAnim.stop();
		}
	}



	/**[ spinner listeners ]*************************************************************************************************/

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



} // end of class
