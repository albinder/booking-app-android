package com.tdispatch.passenger.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tdispatch.passenger.R;
import com.tdispatch.passenger.api.ApiHelper;
import com.tdispatch.passenger.api.ApiResponse;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.define.ErrorCode;
import com.tdispatch.passenger.fragment.dialog.BookingCancelConfirmationDialogFragment;
import com.tdispatch.passenger.fragment.dialog.BookingCancelConfirmationDialogFragment.BookingCancelConfirmationDialogClickListener;
import com.tdispatch.passenger.iface.host.MapHostInterface;
import com.tdispatch.passenger.model.BookingData;
import com.tdispatch.passenger.model.ListDataContainer;
import com.tdispatch.passenger.model.LocationData;
import com.tdispatch.passenger.tools.Office;
import com.webnetmobile.tools.WebnetLog;
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

public class BookingListFragment extends TDFragment implements BookingCancelConfirmationDialogClickListener
{
	protected Handler mHandler = new Handler();
	protected MapHostInterface mMapHostActivity;

	protected Boolean mJustDropoff = Office.isDropoffSupportDisabled();
//	protected Boolean mPickupTimeSmartModeEnabled = Office.isBoolinkListPickupDateRelativeModeEnabled();

	protected ArrayList<ListDataContainer> mBookings = new ArrayList<ListDataContainer>();
	protected ListAdapter mAdapter;

	protected PullToRefreshListView			mPullListview;
	protected ListView						mMainListview;


	@Override
	protected int getLayoutId() {
		return R.layout.booking_list_fragment;
	}

	@Override
	protected void onPostCreateView() {

		mPullListview = (PullToRefreshListView) mFragmentView.findViewById(R.id.list);
		mPullListview.setOnRefreshListener(mListOnRefreshListener); 		// Set a listener to be invoked when the list should be refreshed.
		mMainListview = mPullListview.getRefreshableView();					// get the "real" mMainListview object to cope with


		mAdapter = new ListAdapter( mParentActivity, 0, mBookings );
		mMainListview.setAdapter( mAdapter );

		int[] ids = { R.id.button_retry };
		for( int id : ids ) {
			View v = mFragmentView.findViewById( id );
			v.setOnClickListener( mOnClickListener );
		}

		WebnetTools.setVisibility(mFragmentView, R.id.booking_list_container, View.GONE );
		WebnetTools.setVisibility(mFragmentView, R.id.booking_list_empty_container, View.GONE);
		WebnetTools.setVisibility(mFragmentView, R.id.booking_error_container, View.GONE);

		downloadBookings();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mMapHostActivity = (MapHostInterface)activity;
		} catch( ClassCastException e ) {
			throw new ClassCastException("Host Activity needs to implement MapHostInterface");
		}
	}

	@Override
	protected Integer getOverlayBackgroundResourceId() {
		return R.color.background;
	}



	protected Boolean canCancelBooking( BookingData item ) {
		Boolean canCancelBooking = false;

		switch( item.getType() ) {
			case BookingData.TYPE_INCOMING:
			case BookingData.TYPE_FROM_PARTNER:
			case BookingData.TYPE_DISPATCHED:
			case BookingData.TYPE_CONFIRMED:
				canCancelBooking = true;
				break;

			case BookingData.TYPE_ACTIVE:
			case BookingData.TYPE_QUOTING:
			case BookingData.TYPE_DRAFT:
			case BookingData.TYPE_COMPLETED:
			case BookingData.TYPE_REJECTED:
			case BookingData.TYPE_CANCELLED:
			default:
				canCancelBooking = false;
				break;
		}

		if( canCancelBooking ) {
			long diff = (System.currentTimeMillis() - item.getPickupDate().getTime());
			if( (diff>0) && (diff > WebnetTools.MILLIS_PER_HOUR) ) {
				canCancelBooking = false;
			}
		}

		return canCancelBooking;
	}


	/**[ listener ]**********************************************************************************************************/

	protected View.OnClickListener mOnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {
			switch( v.getId() ) {
				case R.id.button_retry: {
					downloadBookings();
				}
				break;
			}

		}
	};


	protected OnRefreshListener mListOnRefreshListener	= new PullToRefreshListView.OnRefreshListener() {

		@Override
		public void onRefresh() {
			mPullListview.onRefreshComplete();
			downloadBookings();

		}
	};

	/**[ adapter ]***********************************************************************************************************/

	protected class ListAdapter extends ArrayAdapter<ListDataContainer>
	{
		protected TDApplication mApp;
		protected Context mContext;

		public ListAdapter(Activity activity, int textViewResourceId, ArrayList<ListDataContainer> objects) {
			super(activity, textViewResourceId, objects);

			mContext = activity;
		}

		@Override
		public int getItemViewType(int position) {
			return 0;
	    }

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ListDataContainer ldc = mBookings.get(position);
			BookingData item = (BookingData)ldc.getData();

			View view = convertView;
			if( view == null) {
				LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = li.inflate(R.layout.booking_list_row, null);

				if (mJustDropoff) {
					WebnetTools.setVisibility(view, R.id.button_dropoff, View.INVISIBLE);
					WebnetTools.setVisibility(view, R.id.button_pickup_and_dropoff, View.INVISIBLE);
					WebnetTools.setVisibility(view, R.id.dropoff_location, View.GONE);
				}

				WebnetTools.setCustomFonts( TDApplication.getAppContext(), (ViewGroup)view );
			}

			WebnetTools.setVisibility(view, R.id.label, View.GONE);

			WebnetTools.setText( view, R.id.pickup_location, item.getPickupLocation().getAddress() );

			if( mJustDropoff == false ) {
				LocationData dropoffLocationData = item.getDropoffLocation();
				if( dropoffLocationData != null ) {
					WebnetTools.setText( view, R.id.dropoff_location, dropoffLocationData.getAddress() );
					WebnetTools.setVisibility(view, R.id.button_dropoff, View.VISIBLE);
					WebnetTools.setVisibility(view, R.id.button_pickup_and_dropoff, View.VISIBLE);
				} else {
					WebnetTools.setText( view, R.id.dropoff_location, "---");
					WebnetTools.setVisibility(view, R.id.button_dropoff, View.INVISIBLE);
					WebnetTools.setVisibility(view, R.id.button_pickup_and_dropoff, View.INVISIBLE);
				}
			}

			// http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
			String dateFormat = null;
			String formattedTimestamp = "";

			String dateStr = null;

			int officeDateFormat = Office.getDateFormat();
			int officeTimeFormat = Office.getTimeFormat();

			switch( officeDateFormat ) {
				default:
				case 0:		// 0 - smart format (... ago)
					dateStr = WebnetTools.dateDiffToString( item.getPickupDate() );
					officeTimeFormat = 0;
					break;
				case 10:	// 10 - system date/time (short) - 12/31/1999
					dateStr = android.text.format.DateFormat.getDateFormat(TDApplication.getAppContext()).format(new Date(item.getPickupDate().getTime()));
					break;
				case 11:	// 11 - system date/time (medium) - Jan 3, 2000
					dateStr = android.text.format.DateFormat.getMediumDateFormat(TDApplication.getAppContext()).format(new Date(item.getPickupDate().getTime()));
					break;
				case 12:	// 12 - system date/time (long) - Monday, January 3
					dateStr = android.text.format.DateFormat.getLongDateFormat(TDApplication.getAppContext()).format(new Date(item.getPickupDate().getTime()));
					break;
				case 50:	// 50 - Dec 23
					dateFormat = "MMM d";
					break;
				case 51:	// 51 - Dec 23 2014
					dateFormat = "MMM d, yyyy";
					break;
				case 52:	// 52 - Wed, Dec 23
					dateFormat = "EEE, MMM d, yyyy";
					break;
				case 53:	// 53 - Wed, Dec 23 2014
					dateFormat = "EEE, MMM d yyyy";
					break;
				case 54:	// 54 - Wed, 12/23
					dateFormat = "EEE, M/d";
					break;
				case 55:	// 55 - Wed, 12/23/14
					dateFormat = "EEE, M/d/yy";
					break;
				case 56:	// 56 - 23.12.2014
					dateFormat = "d.M.yyyy";
					break;
				case 57:	// 57 - Wed, 23.12.2014
					dateFormat = "EEE, d.M.yyyy";
					break;
				case 58:	// 58 - 2014.12.23 (month, day with leading zeros)
					dateFormat = "yyyy.MM.dd";
					break;
				case 59:	// 59 - Wed, 2014.12.23 (month, day with leading zeros)
					dateFormat = "EEE, yyyy.MM.dd";
					break;
				case 60:	// 60 - 23 Dec 2014
					dateFormat = "d MMM yyyy";
					break;
				case 16:	// 61 - Wed, 23 Dec 2014
					dateFormat = "EEE, d MMM yyyy";
					break;
			}
			if( dateFormat != null ) {
				SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
				sdf.setTimeZone(TimeZone.getDefault());
				dateStr = sdf.format(item.getPickupDate());
			}

			// time
			String timeStr = null;
			String timeFormat = null;

			switch( officeTimeFormat ) {
				case 0:		// none
					timeStr = timeFormat = null;
					break;

				default:
				case 1:		// 1 - system default
					timeFormat = null;
					timeStr = android.text.format.DateFormat.getTimeFormat(TDApplication.getAppContext()).format(new Date(item.getPickupDate().getTime()));
					break;

				case 2:		// 2 - 24h without leading zeros: i.e. 7:49
					timeFormat = "H:mm";
					break;
				case 3:		// 3 - 24h with leading zeros: i.e. 07:49
					timeFormat = "HH:mm";
					break;

				case 4:		// 4 - 12h without leading zeros: i.e. 7:49 AM
					timeFormat = "K:mm a";
					break;
				case 5:		// 5 - 12h with leading zeros: i.e. 7:49 AM
					timeFormat = "KK:mm a";
					break;
			}
			if( timeFormat != null ) {
				SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.getDefault());
				sdf.setTimeZone(TimeZone.getDefault());
				timeStr = sdf.format(item.getPickupDate());
			}


			switch( Office.getDateTimeOrder() ) {
				case 1:
					if( timeStr != null ) {
						formattedTimestamp = timeStr;
					}
					if( dateStr != null ) {
						formattedTimestamp += ", " + dateStr;
					}
					break;

				default:
					if( dateStr != null ) {
						formattedTimestamp = dateStr;
					}
					if( timeStr != null ) {
						formattedTimestamp += ", " + timeStr;
					}
					break;
			}

			WebnetTools.setText( view, R.id.pickup_date, formattedTimestamp );


			int[] ids = { 	R.id.row_info_container,
							R.id.button_pickup, R.id.button_dropoff, R.id.button_pickup_and_dropoff,
							R.id.button_cancel_booking };
			for( int id : ids ) {
				View v = view.findViewById(id);
				if( v != null ) {
					v.setOnClickListener( mOnClickListener );
					v.setOnLongClickListener( mOnLongClickListener );
					v.setTag( R.id.tag_key_position, position );
				}
			}


			WebnetTools.setVisibility(view, R.id.button_cancel_booking, (canCancelBooking(item)==true) ? View.VISIBLE : View.GONE);



			WebnetTools.setVisibility(view, R.id.row_action_menu_container, ldc.isActionBarFolded() ? View.GONE : View.VISIBLE);

			int bgResourceId = ((position % 2) == 0) ? R.color.booking_list_bg_even : R.color.booking_list_bg_odd;;
			if( ldc.isActionBarFolded() == false ) {
				bgResourceId = R.color.list_row_bg_highlight;
			}
			view.setBackgroundResource( bgResourceId );

			return( view );
		}

		protected View.OnLongClickListener mOnLongClickListener = new View. OnLongClickListener()
		{
			@Override
			public boolean onLongClick( View v ) {
				Boolean result = false;

				int position = (Integer) v.getTag(R.id.tag_key_position);
				ListDataContainer ldc = mBookings.get(position);
				BookingData booking = (BookingData)ldc.getData();

				switch( v.getId() ) {
					case R.id.button_pickup: {
						mMapHostActivity.moveMapToLocation( booking.getPickupLocation() );
						result = true;
					}
					break;

					case R.id.button_dropoff: {
						if( booking.getDropoffLocation() != null ) {
							mMapHostActivity.moveMapToLocation( booking.getDropoffLocation() );
							result = true;
						}
					}
					break;
				}

				return result;
			}
		};

		protected View.OnClickListener mOnClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				int position = (Integer) v.getTag(R.id.tag_key_position);
				ListDataContainer ldc = mBookings.get(position);
				BookingData booking = (BookingData)ldc.getData();

				switch( v.getId() ) {
					case R.id.row_info_container: {
						toggleRowActionBar(position);
					}
					break;

					case R.id.button_pickup: {
						mMapHostActivity.setPickupLocation( booking.getPickupLocation() );
					}
					break;

					case R.id.button_dropoff: {
						if( booking.getDropoffLocation() != null ) {
							mMapHostActivity.setDropoffLocation( booking.getDropoffLocation() );
						}
					}
					break;

					case R.id.button_pickup_and_dropoff: {
						mMapHostActivity.setLocation( booking.getPickupLocation(), booking.getDropoffLocation() );
					}
					break;

					case R.id.button_cancel_booking: {
						if( canCancelBooking(booking)) {
							BookingCancelConfirmationDialogFragment frag = BookingCancelConfirmationDialogFragment.newInstance(booking);
							frag.setTargetFragment(BookingListFragment.this, 0);
							frag.show(((FragmentActivity)mParentActivity).getSupportFragmentManager(), "bookingcancelconfirmation");
						}
					}
					break;
				}
			}
		};


		public int toggleRowActionBar( int position ) {

			int foldedRows = 0;

			for( int i=0; i<mBookings.size(); i++ ) {
				if( i != position ) {
					ListDataContainer ldc = mBookings.get(i);

					if( ldc.isActionBarFolded() == false) {
						ldc.setActionBarFolded( true );
						foldedRows++;
					}
				}
			}


			if( position != -1 ) {
				ListDataContainer ldc = mBookings.get( position );
				ldc.toogleActionBarFold();
			}

			mAdapter.notifyDataSetChanged();

			return foldedRows;
		}

	} // end of class


	/**[ fetching bookings ]*************************************************************************************************/

	public void addBooking( BookingData booking ) {
		mBookings.add( 0, new ListDataContainer(booking) );
		mAdapter.notifyDataSetChanged();

		showListEmptyMessage( (mBookings.size() == 0) );
	}

	public void downloadBookings() {
		WebnetTools.executeAsyncTask( new GetBookingsAsyncTask() );
	}

	public class GetBookingsAsyncTask extends AsyncTask<Void, Void, ApiResponse> {

		@Override
		protected void onPreExecute() {

			showDownloadErrorMessage( false );

			lockUI(true);

			mBookings = new ArrayList<ListDataContainer>();
		}

		@Override
		protected ApiResponse doInBackground( Void ... args ) {

			ApiResponse response = new ApiResponse();

			try {
				ApiHelper api = ApiHelper.getInstance( mApp );
				response = api.bookingsGetAll( "incoming,completed,confirmed,active,dispatched" );

				if( response.getErrorCode() == ErrorCode.OK ) {

					BookingData.removeAll();

					JSONArray bookingArray = response.getJSONObject().getJSONArray("bookings");
					int bookingCount = bookingArray.length();
					if( bookingCount > 0 ) {
						for( int i=0; i<bookingCount; i++ ) {
							BookingData booking = new BookingData( bookingArray.getJSONObject(i) );
							mBookings.add( new ListDataContainer(booking) );
							booking.insert();
						}
					}

				} else {
					WebnetLog.e("Failed to get bookings: " + response.getErrorCode() );
				}

			} catch ( Exception e ) {
				e.printStackTrace();
			}

			return response;
		}

		@Override
		protected void onPostExecute(ApiResponse response) {

			Boolean downloadFailed = true;

			lockUI(false);

			if( response != null ) {
				if( response.getErrorCode() == ErrorCode.OK) {
					showListEmptyMessage( (mBookings.size() == 0) );

					mAdapter = new ListAdapter( mParentActivity, 0, mBookings );
					mMainListview.setAdapter( mAdapter );

					downloadFailed = false;
				}
			}

			if( downloadFailed ) {
				showDownloadErrorMessage(true);
			}
		}

	}


	/**[ helpers ]***********************************************************************************************************/

	protected void showDownloadErrorMessage( Boolean showMessage ) {
		WebnetTools.setVisibility(mFragmentView, R.id.booking_list_container, (showMessage) ? View.GONE : View.VISIBLE);
		WebnetTools.setVisibility(mFragmentView, R.id.booking_error_container, (showMessage) ? View.VISIBLE : View.GONE );
	}

	protected void showListEmptyMessage( Boolean isListEmpty ) {
		WebnetTools.setVisibility(mFragmentView, R.id.booking_list_container, (isListEmpty) ? View.GONE : View.VISIBLE);
		WebnetTools.setVisibility(mFragmentView, R.id.booking_list_empty_container, (isListEmpty) ? View.VISIBLE : View.GONE);
	}

	/**[ BookingCancelConfirmationDialogClickListener ]**********************************************************************/

	@Override
	public void doBookingCancel(BookingData booking, String reason) {
		WebnetTools.executeAsyncTask( new CancelBookingAsyncTask( booking, reason ) );
	}

	public class CancelBookingAsyncTask extends AsyncTask<Void, Void, ApiResponse> {

		protected BookingData mBooking;
		protected String mReason;

		public CancelBookingAsyncTask( BookingData booking, String reason ) {
			mBooking = booking;
			mReason = reason;
		}

		@Override
		protected void onPreExecute() {
			lockUI(true);
		}

		@Override
		protected ApiResponse doInBackground( Void ... args ) {

			ApiResponse response = new ApiResponse();

			try {
				ApiHelper api = ApiHelper.getInstance( mApp );
				response = api.bookingsCancelBooking( mBooking.getPk(), mReason );

			} catch ( Exception e ) {
				e.printStackTrace();
			}

			return response;
		}

		@Override
		protected void onPostExecute(ApiResponse response) {

			if( response != null ) {
				if( response.getErrorCode() == ErrorCode.OK ) {
					doCancel( mBooking );
				} else {
					WebnetLog.e("Failed to cancel booking: " + response.getErrorCode() );
				}
			}

			lockUI(false);
		}
	}


	protected void doCancel( BookingData booking ) {
		for( int i=0; i<mBookings.size(); i++ ) {
			ListDataContainer ldc = mBookings.get(i);

			if( ldc.getType() == ListDataContainer.TYPE_BOOKING ) {
				if( ((BookingData)ldc.getData()).getPk().equals( booking.getPk() ) ) {
					mBookings.remove(i);

					mAdapter = new ListAdapter( mParentActivity, 0, mBookings );
					mMainListview.setAdapter( mAdapter );

					showListEmptyMessage( (mBookings.size() == 0) );

					break;
				}
			}
		}
	}


// end of class
}
