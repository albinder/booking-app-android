package com.tdispatch.passenger.api;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Looper;
import android.text.format.Time;

import com.braintreegateway.encryption.Braintree;
import com.google.android.gms.maps.model.LatLng;
import com.tdispatch.passenger.R;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.model.AccountData;
import com.tdispatch.passenger.model.BookingData;
import com.tdispatch.passenger.model.CardData;
import com.tdispatch.passenger.model.LocationData;
import com.webnetmobile.tools.WebnetLog;

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
final public class ApiHelper extends ApiHelperCore
{
	private TDApplication mContext;

	public ApiHelper(TDApplication app) {
		mContext = app;
	}


	private static ApiHelper _instance = null;
	public static ApiHelper getInstance( TDApplication app ) {
		if( _instance == null ) {
			_instance = new ApiHelper(app);
			_instance.setApplication(app);
		}

		if( Looper.getMainLooper().equals(Looper.myLooper()) ) {
			WebnetLog.e("ERROR: instantiated from UI thread!");
		}

		return (_instance);
	}

	/**[ helpers ]*******************************************************************************************/

	public ApiResponse getOAuthTokens( String tmpAuthCode ) {
		ApiRequest req = new ApiRequest( Const.Api.OAuthTokensUrl );
		req.addPostParam("code", tmpAuthCode);
		req.addPostParam("client_id", Const.getOAuthClientId());
		req.addPostParam("client_secret", Const.getOAuthSecret());
		req.addPostParam("redirect_url", "" );
		req.addPostParam("grant_type", "authorization_code");

		return doPostRequest(req );
	}

	public ApiResponse refreshOAuthAccessToken( String refreshToken ) {
		ApiRequest req = new ApiRequest( Const.Api.OAuthTokensUrl );
		req.addPostParam("refresh_token", refreshToken );
		req.addPostParam("client_id", Const.getOAuthClientId());
		req.addPostParam("client_secret", Const.getOAuthSecret());
		req.addPostParam("grant_type", "refresh_token");

		return doPostRequest( req );
	}



	// Accounts
	public ApiResponse accountCreate( AccountData account ) {
		ApiRequest req = new ApiRequest( Const.Api.AccountNew );
		req.addGetParam("key", Const.Api.FleetApiKey);

		req.addRequestParam("first_name", account.getFirstName());
		req.addRequestParam("last_name", account.getLastName());
		req.addRequestParam("email", account.getEmail());
		req.addRequestParam("phone", account.getPhone());
		req.addRequestParam("password", account.getPassword());

		req.addRequestParam("client_id", Const.Api.ClientId);

		return doPostRequest(req);
	}

	public ApiResponse getAccountProfile() {
		ApiRequest req = new ApiRequest( Const.Api.AccountProfile, TDApplication.getSessionManager().getAccessToken() );
		return doGetRequest( req );
	}

	public ApiResponse getAccountFleetData() {
		ApiRequest req = new ApiRequest( Const.Api.AccountFleetData, TDApplication.getSessionManager().getAccessToken() );
		return doGetRequest( req );
	}


	// Location search
	public ApiResponse locationSearch( String search, int limit, Boolean narrowToPickupOnly ) {
		ApiRequest req = new ApiRequest( Const.Api.LocationSearch, TDApplication.getSessionManager().getAccessToken() );

		req.addGetParam("q", search );
		req.addGetParam("limit", limit);
		if( narrowToPickupOnly ) {
			req.addGetParam("type", "pickup");
		}
//		req.addGetParam("source", "googlemaps");

		return doGetRequest(req);
	}

	// fare calculation
	public ApiResponse locationFare( LocationData from, LocationData to, Long pickupMillis, String vehiclePk ) {
		ApiRequest req = new ApiRequest( Const.Api.LocationFare, TDApplication.getSessionManager().getAccessToken() );

		try {
			JSONObject pickup = new JSONObject();
				pickup.put("lat", from.getLatitude());
				pickup.put("lng", from.getLongitude());
			req.addRequestParam("pickup_location", pickup);

			JSONObject dropoff = new JSONObject();
				dropoff.put("lat", to.getLatitude());
				dropoff.put("lng", to.getLongitude());
			req.addRequestParam("dropoff_location", dropoff);

			if( pickupMillis != null ) {
				Time t = new Time();
				t.set( pickupMillis );
				String timeStr = t.format3339(false).replace(".000+", "+");		// FIXME API BUG
				req.addRequestParam( "pickup_time", timeStr );
			}

			if( vehiclePk != null ) {
				req.addRequestParam("car_type", vehiclePk);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return doPostRequest(req);
	}


	/**[ bookings ]*****************************************************************************************************/

	public ApiResponse bookingsGetAll(String status) {
		ApiRequest req = new ApiRequest( Const.Api.BookingsGetAll, TDApplication.getSessionManager().getAccessToken() );

		req.addGetParam("order_by", "-pickup_time");
		req.addGetParam("status", status);
		req.addGetParam("limit", 20);
		req.addGetParam("offset", 0);

		return doGetRequest(req);
	}

	public ApiResponse bookingsNewBooking( JSONObject newBookingJson ) {

		ApiRequest req = new ApiRequest( Const.Api.BookingsNew, TDApplication.getSessionManager().getAccessToken() );
		req.setRequestParameters(newBookingJson);

		return doPostRequest(req);
	}

	public ApiResponse bookingsUpdate( BookingData booking ) {

		String url = String.format(Const.Api.BookingsUpdateFmt, booking.getPk());
		ApiRequest req = new ApiRequest( url, TDApplication.getSessionManager().getAccessToken() );

		try {
			JSONObject j = new JSONObject();

			j.put("status", booking.getTypeName() );

			j.put("is_paid", booking.isPaid() );
			if( booking.isPaid() ) {
				j.put("paid_value", booking.getTotalCostValue());
				j.put("payment_method", booking.getPaymentMethodString());
			}

			j.put("payment_ref", booking.getPaymentReference());

			WebnetLog.d("braintree booking update: url: " + url + " json: " +j);

			req.setRequestParameters( j );

		} catch ( Exception e ) {
			e.printStackTrace();
		}

		return doPostRequest(req);
	}

	public ApiResponse bookingsCancelBooking( String bookingPk, String reason ) {
		String url = String.format(Const.Api.BookingsCancelFmt, bookingPk);
		ApiRequest req = new ApiRequest( url, TDApplication.getSessionManager().getAccessToken() );

		if( (reason != null) && (reason.length()>0) ) {
			req.addRequestParam("description", reason);
		}

		return doPostRequest(req);
	}

	public ApiResponse bookingsTrackBooking( JSONArray bookingPks ) {
		ApiRequest req = new ApiRequest( Const.Api.BookingsTrack, TDApplication.getSessionManager().getAccessToken() );
		req.addRequestParam("booking_pks", bookingPks);
		return doPostRequest(req);
	}

	/**[ vehicles ]*****************************************************************************************************/

	public ApiResponse getVehicleTypes() {
		ApiRequest req = new ApiRequest( Const.Api.VehicleTypes, TDApplication.getSessionManager().getAccessToken() );
		return doGetRequest(req);
	}

	/**[ drivers ]******************************************************************************************************/

	public ApiResponse getNearbyDrivers(LatLng position ) {
		ApiRequest req = new ApiRequest( Const.Api.DriversNearby, TDApplication.getSessionManager().getAccessToken() );

		req.addRequestParam("limit",  15);		// #of cabs
		req.addRequestParam("radius", 10);		// km

		try {
			JSONObject json = new JSONObject();
			json.put("lat", position.latitude);
			json.put("lng", position.longitude);

			req.addRequestParam("location", json);
		} catch( Exception e ) {
			e.printStackTrace();
		}

		return doPostRequest(req);
	}






} // end of class
