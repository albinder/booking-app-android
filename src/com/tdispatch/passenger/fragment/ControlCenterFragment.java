package com.tdispatch.passenger.fragment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tdispatch.passenger.BookingConfirmationActivity;
import com.tdispatch.passenger.R;
import com.tdispatch.passenger.SearchActivity;
import com.tdispatch.passenger.api.ApiHelper;
import com.tdispatch.passenger.api.ApiResponse;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.define.BundleKey;
import com.tdispatch.passenger.define.ErrorCode;
import com.tdispatch.passenger.define.PaymentMethod;
import com.tdispatch.passenger.define.RequestCode;
import com.tdispatch.passenger.fragment.dialog.GenericDialogFragment;
import com.tdispatch.passenger.fragment.dialog.NoLocationDialogFragment;
import com.tdispatch.passenger.hook.BookingHooks;
import com.tdispatch.passenger.iface.host.BookingListHostInterface;
import com.tdispatch.passenger.iface.host.CommonHostInterface;
import com.tdispatch.passenger.iface.host.SlideMenuHostInterface;
import com.tdispatch.passenger.model.AccountData;
import com.tdispatch.passenger.model.ApiSearchLocationData;
import com.tdispatch.passenger.model.BookingData;
import com.tdispatch.passenger.model.BookingTrackingData;
import com.tdispatch.passenger.model.CardData;
import com.tdispatch.passenger.model.LocationData;
import com.tdispatch.passenger.model.PickupAndDropoff;
import com.tdispatch.passenger.model.VehicleData;
import com.tdispatch.passenger.tools.Office;
import com.webnetmobile.tools.GoogleMapRouteHelper;
import com.webnetmobile.tools.JsonTools;
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

public class ControlCenterFragment extends TDFragment
{
	protected static final String PREFS_KEY_LAST_LOCATION_LAT = "prefs_last_location_lat";
	protected static final String PREFS_KEY_LAST_LOCATION_LNG = "prefs_last_location_lng";


	protected Handler mHandler = new Handler();
	protected BookingHooks mBookingHooks = new BookingHooks();

	protected LocationData mAddressMapPointsTo = null;

	protected PickupAndDropoff mPickupAndDropoff = PickupAndDropoff.getInstance();

	protected LocationManager mLocationManager = null;

	@Override
	protected int getLayoutId() {
		return R.layout.control_center_fragment;
	}

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);

		mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
	}

	protected Boolean mIitialMapLocationSet = false;

	@Override
	public void onResume() {
		super.onResume();

		if( mIitialMapLocationSet == false ) {
			Location tmp = getMyLocation();
			LatLng loc = null;
			if( tmp != null ) {
				loc = new LatLng( tmp.getLatitude(), tmp.getLongitude() );
				mIitialMapLocationSet = true;
			} else {
				double defaultLat = Office.getDefaultLocationLat();
				double defaultLng = Office.getDefaultLocationLng();

				String latTmp = mPrefs.getString(PREFS_KEY_LAST_LOCATION_LAT, null);
				String lngTmp = mPrefs.getString(PREFS_KEY_LAST_LOCATION_LNG, null);

				double lat = (latTmp == null) ? defaultLat : Double.valueOf(latTmp);
				double lng = (lngTmp == null) ? defaultLng : Double.valueOf(lngTmp);

				loc = new LatLng(lat, lng);
			}

			moveMapToLocation(loc, true, true);
		}

		startCabTracking();
		startBookingTracking();
	}

	@Override
	public void onPause() {
		stopBookingTracking();
		stopCabTracking();

		Location loc = getMyLocation();
		if( loc != null ) {
			SharedPreferences.Editor editor = mPrefs.edit();
			editor.putString(PREFS_KEY_LAST_LOCATION_LAT, "" + loc.getLatitude() );
			editor.putString(PREFS_KEY_LAST_LOCATION_LNG, "" + loc.getLongitude() );
			editor.commit();
		}

		super.onPause();
	}


	protected BookingListHostInterface mBookingListHostActivity;
	protected CommonHostInterface mCommonHostActivity;
	protected SlideMenuHostInterface mSlideMenuHostInterface;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mBookingListHostActivity = (BookingListHostInterface)activity;
		mSlideMenuHostInterface = (SlideMenuHostInterface)activity;
		mCommonHostActivity = (CommonHostInterface)activity;
	}


	@Override
	protected void onPostCreateView() {

		initBusyIndicators();
		showAimPoint( true );

		// try to fix another GMaps v2 related issue
		// http://code.google.com/p/gmaps-api-issues/issues/detail?id=4639
		ViewGroup mapHost = (ViewGroup)mFragmentView.findViewById(R.id.map_container);
		mapHost.requestTransparentRegion(mapHost);

		updateAddresses();
		setUIControlsVisibility(true);

		MapFragment mapFragment = (MapFragment)mFragmentManager.findFragmentById(R.id.map_fragment);
		if( mapFragment != null ) {
			CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo( 15f );

			GoogleMap map = mapFragment.getMap();

			Location currentPosition = map.getMyLocation();
			if( currentPosition != null ) {
				LatLng latLng = new LatLng(currentPosition.getLatitude(), currentPosition.getLongitude());
				cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f);
			}
			map.moveCamera(cameraUpdate);

			map.setOnCameraChangeListener(mMapCameraListener);
			map.setOnMapClickListener( mOnMapClickListener );
			map.setOnMyLocationChangeListener( mOnMyLocationChangeListener );
		} else {
			throw new IllegalStateException("Map is not ready");
		}


		WebnetTools.setVisibility( mFragmentView, R.id.price_box_container, View.INVISIBLE );


		Boolean justPickup = Office.isDropoffSupportDisabled();
		WebnetTools.setVisibility(mFragmentView, R.id.map_aim_point_a_and_b, (justPickup) ? View.GONE : View.VISIBLE);
		WebnetTools.setVisibility(mFragmentView, R.id.map_aim_point_busy_container_a_and_b, (justPickup) ? View.GONE : View.VISIBLE);
		WebnetTools.setVisibility(mFragmentView, R.id.map_aim_point_only_a, (justPickup) ? View.VISIBLE : View.GONE);
		WebnetTools.setVisibility(mFragmentView, R.id.map_aim_point_busy_container_only_a, (justPickup) ? View.VISIBLE : View.GONE);

		WebnetTools.setVisibility(mFragmentView, R.id.dropoff_location, (justPickup) ? View.GONE : View.VISIBLE );

		TextView tv = (TextView)mFragmentView.findViewById(R.id.pickup_location);
		tv.setMaxLines( (justPickup) ? 2 : 1);
		tv.setLines( (justPickup) ? 2 : 1);


		int ids[] = {	R.id.pickup_location, R.id.dropoff_location,

				R.id.button_mylocation, R.id.button_book,
				R.id.button_start_new_booking,

				R.id.left_menu_drag_handle, R.id.right_menu_drag_handle,

				R.id.button_set_as_pickup, R.id.button_set_as_dropoff,
				R.id.button_set_as_pickup_only_a
		};
		for( int id : ids ) {
			View v = mFragmentView.findViewById( id );
			if( v != null ) {
				v.setOnClickListener( mOnClickListener );
			}
		}

		ids = new int[] { R.id.button_set_as_pickup, R.id.button_set_as_pickup_only_a, R.id.button_set_as_dropoff, R.id.button_mylocation };
		for( int id : ids ) {
			View v = mFragmentView.findViewById( id );
			if( v != null ) {
				v.setOnLongClickListener( mOnLongClickListener );
			}
		}


		// unveil map
		View mapCurtain = mFragmentView.findViewById(R.id.map_curtain);
		mapCurtain.startAnimation( AnimationUtils.loadAnimation(TDApplication.getAppContext(), R.anim.map_curtain_fade_out));
	}


	/**[ map related listeners ]**************************************************************************************/

	protected OnCameraChangeListener mMapCameraListener = new OnCameraChangeListener()
	{
		@Override
		public void onCameraChange( CameraPosition position ) {
			mIitialMapLocationSet = true;

			showAimPoint();
			doReverseGeoCoding( position.target );
		}
	};

	protected GoogleMap.OnMapClickListener mOnMapClickListener = new OnMapClickListener()
	{
		@Override
		public void onMapClick( LatLng point ) {
			mIitialMapLocationSet = true;
			showAimPoint(true);
		}
	};

	protected GoogleMap.OnMyLocationChangeListener mOnMyLocationChangeListener = new GoogleMap.OnMyLocationChangeListener()
	{
		@Override
		public void onMyLocationChange( Location location ) {

			if( mIitialMapLocationSet == false ) {
				LatLng loc = new LatLng( location.getLatitude(), location.getLongitude() );
				moveMapToLocation(loc, false, false);
				mIitialMapLocationSet = true;
			}
		}
	};


	/**[ reverse geocoder ]*******************************************************************************************/

	protected AtomicBoolean mReverseGeoCodingRunning = new AtomicBoolean();
	protected ConcurrentLinkedQueue<LatLng> mReverseQueue = new ConcurrentLinkedQueue<LatLng>();

	protected Boolean isReverseGeoCoderRunning() {
		return mReverseGeoCodingRunning.get();
	}

	protected void doReverseGeoCoding( Location pos ) {
		if( pos != null ) {
			doReverseGeoCoding( new LatLng(pos.getLatitude(), pos.getLongitude()) );
		}
	}

	protected void doReverseGeoCoding( LatLng pos ) {
		mReverseQueue.add(pos);

		if( mReverseGeoCodingRunning.compareAndSet(false, true) ) {
			mAddressMapPointsTo = null;
			WebnetTools.executeAsyncTask( new ReverseGeoAsyncTask() );
		}
	}

	public class ReverseGeoAsyncTask extends AsyncTask<Void, Void, LocationData> {

		@Override
		protected void onPreExecute() {
			showBusy(BUSY_GETTING_MAP_ADDRESS);
		}

		@Override
		protected LocationData doInBackground(Void ... params) {

			LocationData result = null;

			do {
				LatLng lastLocation = null;

				while( mReverseQueue.isEmpty() == false ) {
					lastLocation = mReverseQueue.poll();
				}

				result = getReverseUsingGoogleApis( lastLocation );

			} while (mReverseQueue.isEmpty() == false);

			return result;
		}

		@Override
		protected void onPostExecute(LocationData addr) {

			setAddressMapPointsTo( addr );
			mReverseGeoCodingRunning.set(false);

			hideBusy(BUSY_GETTING_MAP_ADDRESS);
		}

		protected LocationData getReverseUsingGoogleApis( LatLng loc ) {

			LocationData result = null;

			String address1 = "", address2 = "", city = "", state = "", country = "", county = "", postCode = "";
			try {
				String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + loc.latitude +  "," + loc.longitude + "&sensor=true";

				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost( url );
				HttpResponse response;

				response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				InputStream is = null;

				is = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
				StringBuilder sb = new StringBuilder();
				sb.append(reader.readLine() + "\n");
				String line = "0";
				while( (line = reader.readLine()) != null ) {
					sb.append(line + "\n");
				}
				is.close();
				reader.close();

				JSONObject jsonObject = new JSONObject( sb.toString() );

				String Status = jsonObject.getString("status");
				if(Status.equalsIgnoreCase("OK")) {
					JSONArray Results = jsonObject.getJSONArray("results");
					JSONObject zero = Results.getJSONObject(0);
					JSONArray addressComponents = zero.getJSONArray("address_components");

					for(int i=0; i<addressComponents.length(); i++) {
						JSONObject zero2 = addressComponents.getJSONObject(i);

						String longName = zero2.getString("long_name");
						JSONArray typesArray = zero2.getJSONArray("types");

						for( int typeIdx=0; typeIdx<typesArray.length(); typeIdx++ ) {
							String singleType = typesArray.getString(typeIdx);

							if( TextUtils.isEmpty(longName) == false ) {

								if(singleType.equalsIgnoreCase("street_number")) {
									address1 = longName + " ";
								}
								else if(singleType.equalsIgnoreCase("route")) {
									address1 += longName;
								}
								else if(singleType.equalsIgnoreCase("sublocality")) {
									address2 = longName;
								}
								else if(singleType.equalsIgnoreCase("locality")) {
									city = longName;
								}
								else if(singleType.equalsIgnoreCase("postal_town")) {
									city = longName;
								}
								else if(singleType.equalsIgnoreCase("administrative_area_level_2")) {
									county = longName;
								}
								else if(singleType.equalsIgnoreCase("administrative_area_level_1")) {
									state = longName;
								}
								else if(singleType.equalsIgnoreCase("country")) {
									country = longName;
								}
								else if(singleType.equalsIgnoreCase("postal_code")) {
									postCode = longName;
								}
							}
						}
					}

					ApiSearchLocationData tmp = new ApiSearchLocationData();

					tmp.setAddress( address1 );
					tmp.setTown( city );
					tmp.setPostCode(postCode);
					tmp.setCountry(country);
					tmp.setCounty(county);
					tmp.setLocation( loc );

					result = new LocationData( tmp );
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;
		}
	}


	/**[ busy indicators ]********************************************************************************************/

	protected static final int BUSY_GETTING_ROUTE_AND_PRICE 	= 1;
	protected static final int BUSY_GETTING_MAP_ADDRESS		= 2;

	public void showBusy(int what) {
		doShowHideBusy(what, +1);
	}
	public void hideBusy(int what) {
		doShowHideBusy(what, -1);
	}

	protected int mBookBusyCount = 0;
	protected int mPickupDropoffBusyCount = 0;
	protected void initBusyIndicators() {
		doShowHideBusy(0, 0);
	}
	protected void doShowHideBusy(int what, int step) {

		switch( what ) {
			case BUSY_GETTING_MAP_ADDRESS: {
				mPickupDropoffBusyCount += step;
			}
			break;

			case BUSY_GETTING_ROUTE_AND_PRICE: {
				mPickupDropoffBusyCount += step;
				mBookBusyCount += step;
			}
			break;
		}


		View v = mFragmentView.findViewById( R.id.busy_container );
		if( v != null ) {
			AnimationDrawable bookBusyAnim = (AnimationDrawable)((ImageView)mFragmentView.findViewById(R.id.busy)).getBackground();

			if( (mBookBusyCount > 0) ) {
				bookBusyAnim.start();
				v.setVisibility(View.VISIBLE);
			} else {
				v.setVisibility(View.GONE);
				bookBusyAnim.stop();
			}
		}


		int mapAimPointContainerId = R.id.map_aim_point_busy_container_a_and_b;
		int mapBusyImageViewId = R.id.map_aim_point_busy_a_and_b;
		if( Office.isDropoffSupportDisabled() ) {
			mapAimPointContainerId = R.id.map_aim_point_busy_container_only_a;
			mapBusyImageViewId = R.id.map_aim_point_busy_only_a;
		}

		v = mFragmentView.findViewById( mapAimPointContainerId );
		if( v != null ) {
			AnimationDrawable mapAimBusyAnim = (AnimationDrawable)((ImageView)mFragmentView.findViewById(mapBusyImageViewId)).getBackground();
			if( (mPickupDropoffBusyCount > 0) ) {
				mapAimBusyAnim.start();
				v.setVisibility(View.VISIBLE);
			} else {
				v.setVisibility(View.GONE);
				mapAimBusyAnim.stop();
			}
		}

	}

	/**[ Get directions ]*********************************************************************************************/

	protected AtomicBoolean mRouteAndFeeRunning = new AtomicBoolean();
	protected ConcurrentLinkedQueue<PickupAndDropoff> mRouteAndFeeQueue = new ConcurrentLinkedQueue<PickupAndDropoff>();

	protected List<LatLng> mRoutePointList = null;
	protected AtomicBoolean mBookingFeeCalculated = new AtomicBoolean();
	protected String mBookingFeeCalculatedLastErrorMessage = null;
	protected String mBookingFeePriceFormatted 	= "";
	protected String mBookingFeeDistanceFormatted 	= "";

	protected void getRouteAndBookingPrice( LocationData pickup, LocationData dropoff ) {
		if( (pickup != null) && (dropoff != null) ) {
			if( mBookingHooks.isJourneyRouteValid(pickup, dropoff) ) {
				mRouteAndFeeQueue.add( new PickupAndDropoff(pickup, dropoff));
				if( mRouteAndFeeRunning.compareAndSet(false, true) ) {
					WebnetTools.executeAsyncTask( new GetRouteAndFeeAsyncTask() );
				}
			} else {
				showDialog(GenericDialogFragment.DIALOG_TYPE_ERROR, R.string.dialog_error_title, mBookingHooks.getJourneyRouteValidationError());
				mBookingFeeCalculated.set(false);
				refreshMapOverlays();
			}
		} else {
			mBookingFeeCalculated.set(false);
			refreshMapOverlays();
		}
	}

	protected class GetRouteAndFeeAsyncTask extends AsyncTask<Void, Void, Boolean> {

		protected ApiResponse mFeeResult =  new ApiResponse();
		protected List<LatLng> mRoute = null;

		@Override
		protected void onPreExecute() {
			showBusy(BUSY_GETTING_ROUTE_AND_PRICE);
			WebnetTools.setVisibility(mFragmentView, R.id.price_container, View.INVISIBLE);
			WebnetTools.setVisibility(mFragmentView, R.id.price_box_container, View.VISIBLE);

			// clear old route, set new markers
			mRoutePointList = null;
			refreshMapOverlays();
		}

		@Override
		protected Boolean doInBackground( Void ... params ) {

			Boolean result = false;
			PickupAndDropoff lastLocation = null;

			mBookingFeeCalculated.set(false);

			ApiHelper api = ApiHelper.getInstance(TDApplication.getAppContext());

			do {
				while( mRouteAndFeeQueue.isEmpty() == false ) {
					lastLocation = mRouteAndFeeQueue.poll();
				}

				try {
					VehicleData defaultVehicle = VehicleData.getDefault();

					mFeeResult = api.locationFare( lastLocation.getPickup(), lastLocation.getDropoff(),
							System.currentTimeMillis(),
							(defaultVehicle!=null) ? defaultVehicle.getPk() : null,
									(Office.isBraintreeEnabled() ? PaymentMethod.CARD : PaymentMethod.CASH)
							);
					if( mFeeResult.getErrorCode() == ErrorCode.OK ) {
						JSONObject feeJson = JsonTools.getJSONObject( mFeeResult.getJSONObject(), "fare");
						mBookingFeePriceFormatted = JsonTools.getString(feeJson, "formatted_total_cost");

						JSONObject distJson = JsonTools.getJSONObject(feeJson, "distance");
						if( WebnetTools.useMetricUnits() ) {
							mBookingFeeDistanceFormatted = String.format( getString(R.string.journey_distance_metrics_fmt), JsonTools.getString(distJson, "km") );
						} else {
							mBookingFeeDistanceFormatted = String.format( getString(R.string.journey_distance_imperial_fmt), JsonTools.getString(distJson, "miles") );
						}

						mBookingFeeCalculated.set(true);
					}

				} catch ( Exception e ) {
					e.printStackTrace();
				}

			} while (mRouteAndFeeQueue.isEmpty() == false);

			if( mBookingFeeCalculated.get() ) {
				GoogleMapRouteHelper gm = new GoogleMapRouteHelper( lastLocation.getPickup().getLatLng(), lastLocation.getDropoff().getLatLng() );
				mRoute = gm.getDirections();
				result = true;
			}

			return result;
		}
		@Override
		protected void onPostExecute( Boolean result ) {

			hideBusy(BUSY_GETTING_ROUTE_AND_PRICE);
			WebnetTools.setVisibility(mFragmentView, R.id.price_container, View.VISIBLE);

			mRoutePointList = mRoute;
			refreshMapOverlays();

			if( (result == false) && (mFeeResult.getErrorMessage() != null) ) {
				mBookingFeeCalculatedLastErrorMessage = mFeeResult.getErrorMessage();
				showDialog(GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), mBookingFeeCalculatedLastErrorMessage);
			} else {
				mBookingFeeCalculatedLastErrorMessage = null;
			}

			mRouteAndFeeRunning.set(false);
		}
	}


	/*****************************************************************************************************************/

	protected Location getMyLocation() {

		Location result = null;

		MapFragment mapFragment = (MapFragment)mFragmentManager.findFragmentById(R.id.map_fragment);
		if( mapFragment != null ) {
			GoogleMap map = mapFragment.getMap();
			if( map != null ) {
				result = map.getMyLocation();
			}
		}

		return result;
	}

	protected void showCurrentLocation() {
		showCurrentLocation(false);
	}
	protected void showCurrentLocation( Boolean disableAnimation ) {
		moveMapToLocation(null, false, false);
	}
	protected void moveMapToLocation( ApiSearchLocationData addr ) {
		if( addr != null ) {
			moveMapToLocation(new LatLng(addr.getLatitude(), addr.getLongitude()), true, false );
		}
	}
	public void moveMapToLocation( LocationData location ) {
		moveMapToLocation(location, true);
	}
	public void moveMapToLocation( LocationData location, Boolean disableAnimation ) {
		if( location != null ) {
			moveMapToLocation( new LatLng( location.getLatitude(), location.getLongitude()), disableAnimation, false );
		}
	}
	protected void moveMapToLocation( LatLng location, Boolean disableAnimation, Boolean resetCamera ) {

		MapFragment mapFragment = (MapFragment)mFragmentManager.findFragmentById(R.id.map_fragment);
		if( mapFragment != null ) {
			GoogleMap map = mapFragment.getMap();
			if( map != null ) {

				if( location == null ) {
					Location tmp = map.getMyLocation();
					if( tmp != null ) {
						location = new LatLng( tmp.getLatitude(), tmp.getLongitude() );
					}
				}

				if( location == null ) {
					Location lastKnownLoc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if( lastKnownLoc == null ) {
						lastKnownLoc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					}

					if (lastKnownLoc != null ) {
						location = new LatLng( lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude() );
					}
				}

				if( location != null ) {
					CameraUpdate cameraUpdate;
					if( resetCamera ) {
						CameraPosition cameraPosition = new CameraPosition.Builder()
						.target( location )
						.zoom(15f)
						.bearing(0f)
						.tilt(0)
						.build();
						cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
					} else {
						cameraUpdate = CameraUpdateFactory.newLatLng(location);
					}

					if( disableAnimation ) {
						map.moveCamera(cameraUpdate);
					} else {
						map.animateCamera(cameraUpdate);
					}
				} else {
					NoLocationDialogFragment frag = NoLocationDialogFragment.newInstance( GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.unable_to_get_current_location_body), getString( R.string.unable_to_get_current_location_button_show_settings) );
					frag.setTargetFragment(mMe, 0);
					frag.show(((FragmentActivity)mParentActivity).getSupportFragmentManager(), "nolocationdialog");
				}
			}
		}
	}

	/*****************************************************************************************************************/

	protected View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener()
	{
		@Override
		public boolean onLongClick( View v ) {

			Boolean result = false;

			switch( v.getId() ) {

				case R.id.button_mylocation: {
					moveMapToLocation(null, false, true);
				}
				break;

				case R.id.button_set_as_pickup:
				case R.id.button_set_as_pickup_only_a: {
					if( getPickupAddress() != null ) {
						setPickupAddress(null);
						updateAddresses();
					}

					result = true;
				}
				break;

				case R.id.button_set_as_dropoff: {
					if( getDropoffAddress() != null ) {
						setDropoffAddress(null);
						updateAddresses();
					}

					result = true;
				}
				break;
			}

			return result;
		}
	};

	protected View.OnClickListener mOnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {

			int id = v.getId();

			switch( id ) {

				case R.id.button_set_as_pickup:
				case R.id.button_set_as_pickup_only_a: {
					LocationData tmp = getAddressMapPointsTo();
					if( tmp != null ) {
						hideAimPoint();
						setPickupAddress( tmp );
						updateAddresses();
					} else {
						showDialog(GenericDialogFragment.DIALOG_TYPE_ERROR, R.string.dialog_error_title, R.string.map_aim_location_unknown_body);
						//						TDApplication.playSound(Sound.BUZZ);
					}
				}
				break;

				case R.id.button_set_as_dropoff: {
					LocationData tmp = getAddressMapPointsTo();
					if( tmp != null ) {
						hideAimPoint();
						setDropoffAddress( tmp );
						updateAddresses();
					} else {
						showDialog(GenericDialogFragment.DIALOG_TYPE_ERROR, R.string.dialog_error_title, R.string.map_aim_location_unknown_body);
						//						TDApplication.playSound(Sound.BUZZ);
					}
				}
				break;

				case R.id.left_menu_drag_handle: {
					mSlideMenuHostInterface.showLeftMenu();
				}
				break;

				case R.id.right_menu_drag_handle: {
					mSlideMenuHostInterface.showRightMenu();
				}
				break;

				case R.id.button_mylocation: {
					showCurrentLocation(false);
				}
				break;

				case R.id.button_book: {
					String errorMsg = null;

					if( getPickupAddress() == null ) {
						errorMsg = getString(R.string.new_booking_no_pickup_location_body);
					}

					if( (errorMsg==null) && (Office.isDropoffLocationMandatory()) ) {
						if( getDropoffAddress() == null ) {
							errorMsg = getString(R.string.new_booking_both_locations_required_to_place_booking);
						}
					}

					if( (errorMsg==null) && (Office.isBraintreeEnabled()) && (CardData.count() == 0)) {
						if( Office.isCashPaymentDisabled() ) {
							errorMsg = getString(R.string.new_booking_no_payment_card_defined);
						}
					}

					if( (errorMsg==null) ) {
						if( mBookingHooks.isJourneyRouteValid(getPickupAddress(), getDropoffAddress()) == false ) {
							errorMsg = getString(mBookingHooks.getJourneyRouteValidationError());
						}
					}

					if( (errorMsg==null) && ( Office.isDropoffLocationMandatory() ) ) {
						if( mBookingFeeCalculated.get() == false ) {
							errorMsg = (mBookingFeeCalculatedLastErrorMessage != null) ? mBookingFeeCalculatedLastErrorMessage : getString(R.string.new_booking_generic_booking_error);
						}
					}

					if( errorMsg == null ) {
						Intent intent = new Intent();
						intent.putExtra(BundleKey.PICKUP_LOCATION, getPickupAddress());
						intent.putExtra(BundleKey.DROPOFF_LOCATION, getDropoffAddress());
						intent.setComponent( new ComponentName( mContext.getPackageName(), BookingConfirmationActivity.class.getName() ) );
						startActivityForResult(intent, RequestCode.BOOKING_CONFIRMATION);
						mParentActivity.overridePendingTransition( R.anim.activity_slide_in_up, R.anim.fade_out);
					} else {
						showDialog(GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), errorMsg);
					}
				}
				break;

				case R.id.button_start_new_booking: {

					if( getAddressMapPointsTo() != null ) {
						setPickupAddress( getAddressMapPointsTo() );
						updateAddresses();
					}

					setUIControlsVisibility(true);
				}
				break;

				case R.id.pickup_location: {
					doAddressSearch( SearchActivity.TYPE_PICKUP, getPickupAddress() );
				}
				break;
				case R.id.dropoff_location: {
					doAddressSearch( SearchActivity.TYPE_DROPOFF, getDropoffAddress() );
				}

			}
		}
	};


	protected class NewBookingAsyncTask extends AsyncTask<JSONObject, Void, ApiResponse> {

		protected JSONObject mBookingJson;

		protected String mCardToken;
		protected BookingData mCreatedBooking = null;
		protected int mPaymentMethod;

		public NewBookingAsyncTask(JSONObject bookingJson, String cardToken, int paymentMethod) {
			mBookingJson = bookingJson;
			mCardToken = cardToken;
			mPaymentMethod = paymentMethod;
		}

		@Override
		protected void onPreExecute() {
			showBusy(BUSY_GETTING_ROUTE_AND_PRICE);
		}

		@Override
		protected ApiResponse doInBackground( JSONObject ... params ) {

			ApiResponse response = new ApiResponse();

			ApiHelper api = ApiHelper.getInstance( TDApplication.getAppContext() );
			try {
				// prepaid only
				switch( mPaymentMethod ) {
					case PaymentMethod.CARD: {
						mBookingJson.put("status", BookingData.TYPE_PENDING_STRING);
						mBookingJson.put("prepaid", true);
						mBookingJson.put("payment_method", PaymentMethod.CARD_STRING);

						ApiResponse tmpBookingResponse = api.bookingsNewBooking(mBookingJson);

						if( tmpBookingResponse.getErrorCode() == ErrorCode.OK ) {
							mCreatedBooking = new BookingData( JsonTools.getJSONObject( tmpBookingResponse.getJSONObject(), "booking") );

							CardData card = CardData.getByToken( mCardToken );
							if ( card != null ) {
								response = api.braintreeWrapperTransactionCreate( mCreatedBooking, card);
								if( response.getErrorCode() == ErrorCode.OK ) {
									JSONObject transactionJson = JsonTools.getJSONObject(response.getJSONObject(), "transaction");
									String transactionId = JsonTools.getString(transactionJson, "id");

									mCreatedBooking.setPaymentMethod( PaymentMethod.CARD );
									mCreatedBooking.setType( BookingData.TYPE_INCOMING );
									mCreatedBooking.setPaidValue( mCreatedBooking.getTotalCostValue() );
									mCreatedBooking.setIsPaid(true);
									mCreatedBooking.setPaymentReference( transactionId );
								}
							} else {
								response.setErrorMessage("73911");
								response.setErrorCode(ErrorCode.UNKNOWN_ERROR);
							}
						} else {
							response = tmpBookingResponse;
						}
					}
					break;

					case PaymentMethod.CASH: {
						mBookingJson.put("status", BookingData.TYPE_INCOMING_STRING);
						response = api.bookingsNewBooking(mBookingJson);
						if( response.getErrorCode() == ErrorCode.OK ) {
							mCreatedBooking = new BookingData( JsonTools.getJSONObject( response.getJSONObject(), "booking") );
						}
					}
					break;
				}
			} catch( Exception e) {
				e.printStackTrace();
			}

			return response;
		}

		@Override
		protected void onPostExecute(ApiResponse result) {

			if( result.getErrorCode() == ErrorCode.OK ) {
				// update booking list
				mBookingListHostActivity.addBooking( mCreatedBooking );

				// placed booking info
				String msg = "???";
				if( mBookingJson.has("pickup_time") ) {
					msg = String.format( getString(R.string.new_booking_body_later_fmt), mCreatedBooking.getPickupLocation().getAddress());
				} else {
					msg = String.format( getString(R.string.new_booking_body_fmt), mCreatedBooking.getPickupLocation().getAddress());
				}
				showDialog( GenericDialogFragment.DIALOG_TYPE_OK, getString(R.string.new_booking_title), msg );

				// reset UI (addresses)
				mRoutePointList = null;

				setPickupAddress(null);
				setDropoffAddress(null);

				updateAddresses();
				setUIControlsVisibility(false);

				refreshMapOverlays();
			} else {
				showDialog( GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title),
						String.format(getString(R.string.new_booking_failed_body_fmt), result.getErrorMessage())
						);
			}

			hideBusy(BUSY_GETTING_ROUTE_AND_PRICE);
			mCommonHostActivity.unlockUI();
		}
	}

	/*****************************************************************************************************************/

	protected Boolean mAimPointVisible = false;

	protected void hideAimPoint() {
		if( mAimPointVisible ) {
			mAimPointVisible = false;

			View v = mFragmentView.findViewById( R.id.map_aim_point_container );
			v.clearAnimation();
			v.startAnimation( AnimationUtils.loadAnimation(mContext, R.anim.fade_out));

			WebnetTools.setVisibility(mFragmentView, R.id.map_aim_point_minimal_container, View.VISIBLE);

			int[] ids = { R.id.button_set_as_pickup, R.id.button_set_as_dropoff };
			for( int id : ids ) {
				v = mFragmentView.findViewById( id );
				v.setClickable(false);
				v.setLongClickable(false);
			}
		}
	}

	protected void showAimPoint() {
		if(  isUIControlsVisibile() ) {
			showAimPoint(false);
		}
	}
	protected void showAimPoint(Boolean animate) {
		if( mAimPointVisible == false ) {
			mAimPointVisible = true;

			View v = mFragmentView.findViewById( R.id.map_aim_point_container );
			v.clearAnimation();

			v.startAnimation( AnimationUtils.loadAnimation(mContext,
					( animate ) ? R.anim.fade_in : R.anim.fade_in_instant
					));

			WebnetTools.setVisibility(mFragmentView, R.id.map_aim_point_minimal_container, View.INVISIBLE);

			int[] ids = { R.id.button_set_as_pickup, R.id.button_set_as_pickup_only_a, R.id.button_set_as_dropoff };
			for( int id : ids ) {
				v = mFragmentView.findViewById( id );
				v.setClickable(true);
				v.setLongClickable(true);
			}
		}
	}

	/*****************************************************************************************************************/

	protected void setAddressMapPointsTo( LocationData addr ) {
		mAddressMapPointsTo = addr;
	}
	protected LocationData getAddressMapPointsTo() {
		return mAddressMapPointsTo;
	}

	public void setPickupAddress( LocationData addr ) {
		mRouteAndFeeQueue.clear();
		mRoutePointList = null;
		if( addr != null ) {
			mIitialMapLocationSet = true;
		}
		mPickupAndDropoff.setPickup(addr);
	}
	public LocationData getPickupAddress() {
		return mPickupAndDropoff.getPickup();
	}
	public void setDropoffAddress( LocationData addr ) {
		mRouteAndFeeQueue.clear();
		mRoutePointList = null;
		if( addr != null ) {
			mIitialMapLocationSet = true;
		}
		mPickupAndDropoff.setDropoff(addr);
	}
	public LocationData getDropoffAddress() {
		return mPickupAndDropoff.getDropoff();
	}

	public void updateAddresses() {
		LocationData pickup = getPickupAddress();
		LocationData dropoff = getDropoffAddress();

		if( pickup != null ) {
			WebnetTools.setText(mFragmentView, R.id.pickup_location, pickup.getAddress() );
		} else {
			WebnetTools.setText(mFragmentView, R.id.pickup_location, R.string.pickup_line_default);
		}

		if( dropoff != null ) {
			WebnetTools.setText(mFragmentView, R.id.dropoff_location, dropoff.getAddress());
		} else {
			WebnetTools.setText(mFragmentView, R.id.dropoff_location, R.string.dropoff_line_default );
		}

		// calc route if we can
		getRouteAndBookingPrice(pickup, dropoff);

		if( (pickup != null) || (dropoff != null) ) {
			if( isUIControlsVisibile() == false ) {
				setUIControlsVisibility(true);
			}
		}
	}

	/**[ booking tracking ]*********************************************************************************/

	protected AtomicBoolean mBookingTrackingEnabled = new AtomicBoolean();
	protected void startBookingTracking() {
		if( mBookingTrackingEnabled.compareAndSet(false, true) ) {
			mHandler.post(mUpdateBookingTrackingRunnable);
		}
	}
	protected void stopBookingTracking() {
		mHandler.removeCallbacks(mUpdateBookingTrackingRunnable);
		mBookingTrackingEnabled.set(false);
	}

	protected Runnable mUpdateBookingTrackingRunnable = new Runnable()
	{
		@Override
		public void run() {
			if( mBookingTrackingEnabled.get() ) {
				if( mUpdateBookingTrackingAsyncTaskRunning.compareAndSet(false, true) ) {
					WebnetTools.executeAsyncTask( new UpdateBookingTrackingAsyncTask() );
					mHandler.postDelayed(this,  WebnetTools.MILLIS_PER_SECOND * 30);
				}
			}
		}
	};

	protected AtomicBoolean mUpdateBookingTrackingAsyncTaskRunning = new AtomicBoolean(false);
	protected class UpdateBookingTrackingAsyncTask extends AsyncTask<Void, Void, ApiResponse> {

		protected ArrayList<BookingTrackingData> mTrackedBookings = new ArrayList<BookingTrackingData>();

		@Override
		protected ApiResponse doInBackground( Void ... params ) {

			ApiResponse response = new ApiResponse();

			JSONArray pks = BookingData.getAllTrackable();
			if( pks.length() > 0 ) {

				try {
					ApiHelper api = ApiHelper.getInstance( TDApplication.getAppContext() );
					ApiResponse r = api.bookingsTrackBooking(pks);
					if( r.getErrorCode() == ErrorCode.OK ) {
						JSONArray b = r.getJSONObject().getJSONArray("bookings");

						for( int i=0; i<b.length(); i++ ) {
							JSONObject tmp = (JSONObject)b.get(i);
							String bookingPk = JsonTools.getString(tmp, "pk");

							if( JsonTools.getInt(tmp, "return_code", 404) == 200 ) {
								BookingData booking = BookingData.getByPk( bookingPk );
								if( booking != null ) {
									booking.setType( JsonTools.getString(tmp, "status") );
									booking.update();

									JSONObject driver = JsonTools.getJSONObject(tmp, "driver");
									if( driver != null ) {
										mTrackedBookings.add( new BookingTrackingData( driver ));
									}
								}
							}
						}
					}

				} catch ( Exception e ) {
					e.printStackTrace();
				}
			} else {
				response.setErrorCode(ErrorCode.OK);
			}

			return response;
		}

		@Override
		protected void onPostExecute(ApiResponse response) {
			if( response.getErrorCode() == ErrorCode.OK ) {
				mBookingTracking = mTrackedBookings;
				refreshMapOverlays();
			}

			mUpdateBookingTrackingAsyncTaskRunning.set(false);
		}
	}

	/**[ nearby taxis ]*************************************************************************************/

	protected AtomicBoolean mCabTrackingEnabled = new AtomicBoolean();
	protected void startCabTracking() {

		if( Office.isNearbyCabsTrackingEnabled() ) {
			if( mCabTrackingEnabled.compareAndSet(false, true) ) {
				mHandler.post(mUpdateNearbyCabsRunnable);
			}
		}
	}
	protected void stopCabTracking() {
		mHandler.removeCallbacks(mUpdateNearbyCabsRunnable);
		mCabTrackingEnabled.set(false);
	}

	protected ArrayList<LatLng> mNearbyTaxis = null;
	protected ArrayList<BookingTrackingData> mBookingTracking = null;

	protected Runnable mUpdateNearbyCabsRunnable = new Runnable()
	{
		@Override
		public void run() {
			if( mCabTrackingEnabled.get() ) {
				Location pos = getMyLocation();
				if( pos != null ) {
					UpdateNearbyCabsAsyncTask task = new UpdateNearbyCabsAsyncTask( pos );
					WebnetTools.executeAsyncTask( task );
				}

				mHandler.postDelayed(this, WebnetTools.MILLIS_PER_SECOND * 30);
			}
		}
	};

	protected class UpdateNearbyCabsAsyncTask extends AsyncTask<Void, Void, ApiResponse> {
		protected ArrayList<LatLng> mTaxis = null;
		protected LatLng mPos;

		public UpdateNearbyCabsAsyncTask( Location position ) {
			mPos = new LatLng( position.getLatitude(), position.getLongitude() );
		}
		public UpdateNearbyCabsAsyncTask( LatLng position ) {
			mPos = position;
		}

		@Override
		protected ApiResponse doInBackground( Void ... params ) {

			ApiResponse response = new ApiResponse();

			try {
				ApiHelper api = ApiHelper.getInstance(TDApplication.getAppContext());
				response = api.getNearbyDrivers( mPos );

				if( response.getErrorCode() == ErrorCode.OK ) {

					mTaxis = new ArrayList<LatLng>();

					JSONArray tmp = response.getJSONObject().getJSONArray("drivers");

					for( int i=0; i<tmp.length(); i++ ) {
						JSONObject item = (JSONObject)tmp.get(i);
						mTaxis.add( new LatLng(item.getDouble("lat"), item.getDouble("lng")) );
					}
				}

			} catch ( Exception e ) {
				e.printStackTrace();
				response.setErrorCode(ErrorCode.EXCEPTION_ERROR);
				response.setException(e);
			}

			return response;
		}

		@Override
		protected void onPostExecute(ApiResponse response) {
			if( response.getErrorCode() == ErrorCode.OK ) {
				mNearbyTaxis = mTaxis;
				refreshMapOverlays();
			}
		}
	}


	/**[ Map overlays ]*************************************************************************************/

	protected void refreshMapOverlays() {

		MapFragment mapFragment = (MapFragment)mFragmentManager.findFragmentById(R.id.map_fragment);
		if( mapFragment != null ) {
			GoogleMap map = mapFragment.getMap();
			if( map != null ) {
				map.clear();

				if( mBookingTracking != null ) {
					for( int i=0; i<mBookingTracking.size(); i++ ) {
						BookingTrackingData bd = mBookingTracking.get(i);
						map.addMarker(new MarkerOptions()
										.position( bd.getLatLng() )
										.title( bd.getDriverName() )
										.icon( BitmapDescriptorFactory.fromResource(R.drawable.map_marker_booked_cab))
								);
					}
				}

				if( mNearbyTaxis != null ) {
					for( int i=0; i<mNearbyTaxis.size(); i++ ) {
						map.addMarker(new MarkerOptions()
										.position( mNearbyTaxis.get(i) )
										.icon( BitmapDescriptorFactory.fromResource(R.drawable.map_marker_nearby_cab))
								);
					}
				}

				// pickup-dropoff route
				if(    (getPickupAddress() != null) && (getDropoffAddress() != null)
						&& (mRoutePointList != null) && (mRoutePointList.size() >= 2) )
				{
					LatLng cabPickupLocation = mRoutePointList.get(0);
					LatLng cabDropoffLocation = mRoutePointList.get( mRoutePointList.size()-1 );

					MarkerOptions mPickup = new MarkerOptions()
												.position(cabPickupLocation)
												.anchor(0.5f,0.5f)
												.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_cab_pickup));
					map.addMarker( mPickup );

					MarkerOptions mDestination = new MarkerOptions()
												.position(cabDropoffLocation)
												.anchor(0.5f,0.5f)
												.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_cab_dropoff));
					map.addMarker( mDestination );

					PolylineOptions rectLine = new PolylineOptions().width(5).color( getResources().getColor(R.color.map_route_fg));
					for (int i = 0; i < mRoutePointList.size(); i++) {
						rectLine.add(mRoutePointList.get(i));
					}
					map.addPolyline(rectLine);

					createDashedLine(map, getPickupAddress().getLatLng(), mRoutePointList.get(0), getResources().getColor(R.color.pickup_location) );
					createDashedLine(map, getDropoffAddress().getLatLng(), mRoutePointList.get( mRoutePointList.size()-1 ), getResources().getColor(R.color.dropoff_location) );
				}


				if( getPickupAddress() != null ) {
					MarkerOptions mPickup = new MarkerOptions()
					.position( getPickupAddress().getLatLng() )
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_pickup_big));
					map.addMarker( mPickup );
				}

				if( getDropoffAddress() != null ) {
					MarkerOptions mDestination = new MarkerOptions()
					.position( getDropoffAddress().getLatLng() )
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_dropoff));
					map.addMarker( mDestination );
				}


//    			// station coverage
//				for( PredefinedLocation station : SearchStationsFragment.mItems ) {
//					 map.addCircle(new CircleOptions()
//					 					.center( station.getLocation().getLatLng() )
//					 					.radius(500)
//					 					.strokeColor(0x4400ff00)
//					 					.fillColor(0x2200ff00)
//					 );
//				}

				// booking fee
				if( (getPickupAddress() == null) || (getDropoffAddress() == null)) {
					WebnetTools.setVisibility( mFragmentView, R.id.price_box_container, View.INVISIBLE );
				} else {
					if( mBookingFeeCalculated.get() == true ) {
						WebnetTools.setText( mFragmentView, R.id.price, mBookingFeePriceFormatted );
						WebnetTools.setText( mFragmentView, R.id.distance, mBookingFeeDistanceFormatted );
						WebnetTools.setVisibility( mFragmentView, R.id.price_box_container, View.VISIBLE );
					} else {
						WebnetTools.setVisibility( mFragmentView, R.id.price_box_container, View.INVISIBLE );
					}
				}
			}
		}
	}

	public void createDashedLine(GoogleMap map, LatLng begin, LatLng end, int color) {
		double diffLat = (end.latitude - begin.latitude);
		double diffLng = (end.longitude - begin.longitude);

		double zoom = (map.getCameraPosition().zoom) * 2;

		double divLat = diffLat / zoom;
		double divLng = diffLng / zoom;

		LatLng tmpLat = begin;

		for(int i = 0; i < zoom; i++) {
			LatLng loopLatLng = tmpLat;

			if( i == (zoom - 1) ) {
				loopLatLng = end;
			} else {
				if(i > 0) {
					loopLatLng = new LatLng(tmpLat.latitude + (divLat * 0.25f), tmpLat.longitude + (divLng * 0.25f));
				}
			}

			map.addPolyline(new PolylineOptions()
								.add(loopLatLng)
								.add(new LatLng(tmpLat.latitude + divLat, tmpLat.longitude + divLng))
								.color(color)
								.width(5f));

			tmpLat = new LatLng(tmpLat.latitude + divLat, tmpLat.longitude + divLng);
		}
	}

	/*******************************************/

	protected Boolean mUIControlsVisibile = false;
	protected void setUIControlsVisibility( Boolean visible ) {
		mUIControlsVisibile = visible;
		disableBookingControls( !(visible) );
	}
	protected Boolean isUIControlsVisibile() {
		return mUIControlsVisibile;
	}

	protected void disableBookingControls( Boolean disabled ) {

		WebnetTools.setVisibility(mFragmentView, R.id.booking_addresses_container, (disabled) ? View.INVISIBLE : View.VISIBLE);
		WebnetTools.setVisibility(mFragmentView, R.id.bottom_container, (disabled) ? View.INVISIBLE : View.VISIBLE);

		WebnetTools.setVisibility(mFragmentView, R.id.map_aim_point_container, (disabled) ? View.INVISIBLE : View.VISIBLE);

		WebnetTools.setVisibility(mFragmentView, R.id.bottom_start_new_booking_container, (disabled) ? View.VISIBLE : View.INVISIBLE);

		if( disabled ) {
			WebnetTools.setVisibility( mFragmentView, R.id.price_box_container, View.INVISIBLE );
			hideAimPoint();
		} else {
			showAimPoint(true);
		}
	}

	/**[ address search wrapper ]***************************/

	protected void doAddressSearch( int type, LocationData address ) {
		Intent intent = new Intent();
		intent.putExtra(BundleKey.TYPE, type);
		intent.putExtra(BundleKey.LOCATION, address);
		intent.putExtra(BundleKey.REQUEST_CODE, RequestCode.ADDRESS_SEARCH );
		intent.setComponent( new ComponentName( mContext.getPackageName(), SearchActivity.class.getName() ) );
		startActivityForResult(intent, RequestCode.ADDRESS_SEARCH);
	}

	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent intent ) {

		switch( requestCode ) {

			case RequestCode.BOOKING_CONFIRMATION: {
				if( resultCode == Activity.RESULT_OK ) {
					LocationData pickup = intent.getExtras().getParcelable(BundleKey.PICKUP_LOCATION);
					LocationData dropoff = intent.getExtras().getParcelable(BundleKey.DROPOFF_LOCATION);
					Long pickupMillis = intent.getExtras().getLong(BundleKey.PICKUP_TIME);
					if( pickupMillis == 0L ) {
						pickupMillis = null;
					}
					String vehiclePk = intent.getExtras().getString(BundleKey.VEHICLE_PK);
					String cardToken = intent.getExtras().getString(BundleKey.CARD_TOKEN);

					int maxDaysAhead = Office.getNewBookingMaxDaysAhead();

					Boolean result = false;
					Boolean pickupMillisInvalid = false;
					String pickupMillisBodyId = "";

					if( pickupMillis != null ) {
						Long diff = (pickupMillis - System.currentTimeMillis());

						if( diff > 0 ) {

							if( diff > (WebnetTools.MILLIS_PER_MINUTE * 5) ) {
								if( diff < (WebnetTools.MILLIS_PER_DAY * maxDaysAhead) ) {
									// 	keep it
								} else {
									pickupMillisBodyId  = getString(R.string.new_booking_pickup_date_too_ahead_body_fmt, maxDaysAhead);
									pickupMillisInvalid = true;
								}
							} else {
								pickupMillis = null;
							}

						} else {
							pickupMillisBodyId  = getString(R.string.new_booking_pickup_date_already_passed);
							pickupMillisInvalid = true;
						}
					}


					if( pickupMillisInvalid == false ) {
						if( getPickupAddress() != null ) {

							mCommonHostActivity.lockUI();

							int paymentMethod = intent.getExtras().getInt(BundleKey.PAYMENT_METHOD);

							try {
								AccountData user = TDApplication.getSessionManager().getAccountData();

								JSONObject json = new JSONObject();

								JSONObject passenger = new JSONObject();
								passenger.put("name", user.getFullName());
								passenger.put("phone", (user.getPhone() != null) ? user.getPhone() : "");
								passenger.put("email", (user.getEmail() != null) ? user.getEmail() : "");
								json.put("passenger", passenger);

								// payment method
								String paymentMethodString = null;
								switch( paymentMethod ) {
									case PaymentMethod.CASH: {
										paymentMethodString = PaymentMethod.CASH_STRING;
									}
									case PaymentMethod.CARD: {
										paymentMethodString = PaymentMethod.CARD_STRING;
									}

									case PaymentMethod.UNKNOWN:
									default: {
										// nothing
									}
									break;
								}
								if( paymentMethodString != null ) {
									json.put("payment_method", paymentMethodString);
								}

								// pickup location
								json.put( "pickup_location", pickup.toJSON() );

								if( pickupMillis != null ) {
									Time t = new Time();
									t.set( pickupMillis );

									String timeStr = t.format3339(false).replace(".000+", "+");		// FIXME API BUG
									json.put("pickup_time", timeStr);
								}

								// dropoff
								if( getDropoffAddress() != null ) {
									json.put( "dropoff_location", dropoff.toJSON() );
								}

								if ( vehiclePk != null ) {
									VehicleData v = VehicleData.getByPk(vehiclePk);
									if( v != null ) {
										json.put("vehicle_type", v.getPk());
									} else {
										throw new IllegalStateException("Nonexisting vehicle referenced by provided vehiclePk");
									}
								}

								json.put("passengers", intent.getExtras().getInt(BundleKey.PASSENGER_COUNT));
								json.put("luggage", intent.getExtras().getInt(BundleKey.LUGGAGE_COUNT));

								String notes = intent.getExtras().getString(BundleKey.NOTES);
								if( notes != null ) {
									json.put("extra_instructions", notes);
								}

								WebnetTools.executeAsyncTask( new NewBookingAsyncTask(json, cardToken, paymentMethod ));

								result = true;

							} catch ( Exception e ) {
								e.printStackTrace();
							}

						} else {
							showDialog( GenericDialogFragment.DIALOG_TYPE_ERROR,
									R.string.dialog_error_title, R.string.new_booking_no_pickup_location_body );
						}

					} else {
						showDialog( GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), pickupMillisBodyId );
					}

					if( result == false ) {
						mCommonHostActivity.unlockUI();
					}

				}
			}
			break;

			case RequestCode.ADDRESS_SEARCH: {
				if( resultCode == Activity.RESULT_OK ) {

					int type = intent.getExtras().getInt(BundleKey.TYPE);
					LocationData location = intent.getExtras().getParcelable( BundleKey.LOCATION );

					switch( type ) {
						case SearchActivity.TYPE_PICKUP:
							setPickupAddress( location );
							break;
						case SearchActivity.TYPE_DROPOFF:
							setDropoffAddress( location );
							break;
					}

					updateAddresses();
					moveMapToLocation(location);
				}
			}
			break;

			default: {
				super.onActivityResult(requestCode, resultCode, intent);
			}
			break;
		}
	}

} // end of class
