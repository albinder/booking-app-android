package com.tdispatch.passenger.api;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Looper;
import android.text.format.Time;

import com.braintreegateway.encryption.Braintree;
import com.google.android.gms.maps.model.LatLng;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.define.PaymentMethod;
import com.tdispatch.passenger.model.AccountData;
import com.tdispatch.passenger.model.BookingData;
import com.tdispatch.passenger.model.CardData;
import com.tdispatch.passenger.model.LocationData;
import com.tdispatch.passenger.tools.Office;

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
			throw new RuntimeException("ERROR: instantiated from UI thread!");
		}

		return (_instance);
	}

	/**[ helpers ]*******************************************************************************************/

	public ApiResponse getOAuthTokens( String tmpAuthCode ) {
		ApiRequest req = new ApiRequest( Office.getApiUrl() + "/passenger/oauth2/token" );
		req.addPostParam("code", tmpAuthCode);
		req.addPostParam("client_id", Office.getOAuthClientId());
		req.addPostParam("client_secret", Office.getOAuthSecret());
		req.addPostParam("redirect_url", "" );
		req.addPostParam("grant_type", "authorization_code");

		return doPostRequest(req );
	}

	public ApiResponse refreshOAuthAccessToken( String refreshToken ) {
		ApiRequest req = new ApiRequest( Office.getApiUrl() + "/passenger/oauth2/token" );
		req.addPostParam("refresh_token", refreshToken );
		req.addPostParam("client_id", Office.getOAuthClientId());
		req.addPostParam("client_secret", Office.getOAuthSecret());
		req.addPostParam("grant_type", "refresh_token");

		return doPostRequest( req );
	}


	// Accounts
	public ApiResponse accountCreate( AccountData account ) {
		ApiRequest req = new ApiRequest( Office.getApiUrl() + "/passenger/v1/accounts" );
		req.addGetParam("key", Office.getFleetApiKey());

		req.addRequestParam("first_name", account.getFirstName());
		req.addRequestParam("last_name", account.getLastName());
		req.addRequestParam("email", account.getEmail());
		req.addRequestParam("phone", account.getPhone());
		req.addRequestParam("password", account.getPassword());

		req.addRequestParam("client_id", Office.getOAuthClientId());

		return doPostRequest(req);
	}

	public ApiResponse getAccountProfile() {
		ApiRequest req = new ApiRequest( Office.getApiUrl() + "/passenger/v1/accounts/preferences", TDApplication.getSessionManager().getAccessToken() );
		return doGetRequest( req );
	}

	public ApiResponse getAccountFleetData() {
		ApiRequest req = new ApiRequest( Office.getApiUrl() + "/passenger/v1/accounts/fleetdata", TDApplication.getSessionManager().getAccessToken() );
		return doGetRequest( req );
	}


	public ApiResponse locationSearch( String search, int limit, Boolean narrowToPickupOnly ) {
		ApiRequest req = new ApiRequest( Office.getApiUrl() + "/passenger/v1/locations/search", TDApplication.getSessionManager().getAccessToken() );

		req.addGetParam("q", search );
		req.addGetParam("limit", limit);
		if( narrowToPickupOnly ) {
			req.addGetParam("type", "pickup");
		}

		return doGetRequest(req);
	}

	public ApiResponse locationFare( LocationData from, LocationData to, Long pickupMillis, String vehiclePk, int paymentMethod ) {
		ApiRequest req = new ApiRequest( Office.getApiUrl() + "/passenger/v1/locations/fare", TDApplication.getSessionManager().getAccessToken() );

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

			String method = PaymentMethod.CASH_STRING;
			switch( paymentMethod ) {
				case PaymentMethod.ACCOUNT:
					method = PaymentMethod.ACCOUNT_STRING;
					break;

				case PaymentMethod.CARD:
					method = PaymentMethod.CARD_STRING;
					break;

				case PaymentMethod.CASH:
				default:
					method = PaymentMethod.CASH_STRING;
					break;
			}
			req.addRequestParam("payment_method", method);

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
		ApiRequest req = new ApiRequest( Office.getApiUrl() + "/passenger/v1/bookings", TDApplication.getSessionManager().getAccessToken() );

		req.addGetParam("order_by", "-pickup_time");
		req.addGetParam("status", status);
		req.addGetParam("limit", 20);
		req.addGetParam("offset", 0);

		return doGetRequest(req);
	}

	public ApiResponse bookingsNewBooking( JSONObject newBookingJson ) {

		ApiRequest req = new ApiRequest( Office.getApiUrl() + "/passenger/v1/bookings", TDApplication.getSessionManager().getAccessToken() );
		req.setRequestParameters(newBookingJson);

		return doPostRequest(req);
	}

	public ApiResponse bookingsCancelBooking( String bookingPk, String reason ) {
		String url = String.format(Office.getApiUrl() + "/passenger/v1/bookings/%s/cancel", bookingPk);
		ApiRequest req = new ApiRequest( url, TDApplication.getSessionManager().getAccessToken() );

		if( (reason != null) && (reason.length()>0) ) {
			req.addRequestParam("description", reason);
		}

		return doPostRequest(req);
	}

	public ApiResponse bookingsTrackBooking( JSONArray bookingPks ) {
		ApiRequest req = new ApiRequest( Office.getApiUrl() + "/passenger/v1/bookings/track", TDApplication.getSessionManager().getAccessToken() );
		req.addRequestParam("booking_pks", bookingPks);
		return doPostRequest(req);
	}

	/**[ vehicles ]*****************************************************************************************************/

	public ApiResponse getVehicleTypes() {
		ApiRequest req = new ApiRequest( Office.getApiUrl() + "/passenger/v1/vehicletypes", TDApplication.getSessionManager().getAccessToken() );
		return doGetRequest(req);
	}

	/**[ drivers ]******************************************************************************************************/

	public ApiResponse getNearbyDrivers(LatLng position ) {
		ApiRequest req = new ApiRequest( Office.getApiUrl() + "/passenger/v1/drivers/nearby", TDApplication.getSessionManager().getAccessToken() );

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





	/**[ braintree ]****************************************************************************************************/

	protected static final int BRAINTREE_WRAPPER_MIN_VERSION = 1;

	public ApiResponse braintreeWrapperCardCreate( String userPk, String holdrName, String cardNumber, String cardCvv,
													String cardExpirationMonth, String cardExpirationYear ) {

		Braintree bt = new Braintree( Office.getBraintreeEncryptionKey() );

		ApiRequest req = new ApiRequest( Office.getBraintreeWrapperUrl(), TDApplication.getSessionManager().getAccessToken() );

		req.addGetParam("cmd", "card-create");
		req.addGetParam("version", BRAINTREE_WRAPPER_MIN_VERSION);

		req.addPostParam("customer_pk", userPk );
		req.addPostParam("card_holder_name", holdrName);
		req.addPostParam("card_number", bt.encrypt(cardNumber));
		req.addPostParam("card_expiration_month", bt.encrypt(String.valueOf(cardExpirationMonth)));
		req.addPostParam("card_expiration_year", bt.encrypt(String.valueOf(cardExpirationYear)));

		if( cardCvv != null ) {
			req.addPostParam("card_cvv", bt.encrypt(cardCvv));
		}

		return doPostRequest(req);
	}

	public ApiResponse braintreeWrapperCardList( String userPk ) {
		ApiRequest req = new ApiRequest( Office.getBraintreeWrapperUrl(), TDApplication.getSessionManager().getAccessToken() );
		req.addGetParam("cmd", "card-list");
		req.addGetParam("version", BRAINTREE_WRAPPER_MIN_VERSION);

		req.addPostParam("customer_pk", userPk);

		return doPostRequest(req);
	}

	public ApiResponse braintreeWrapperCardDelete( String cardToken ) {
		ApiRequest req = new ApiRequest( Office.getBraintreeWrapperUrl(), TDApplication.getSessionManager().getAccessToken() );

		req.addGetParam("cmd", "card-delete");
		req.addGetParam("version", BRAINTREE_WRAPPER_MIN_VERSION);

		req.addPostParam("card_token", cardToken );

		return doPostRequest(req);
	}

	public ApiResponse braintreeWrapperTransactionCreate( BookingData booking, CardData card) {
		ApiRequest req = new ApiRequest( Office.getBraintreeWrapperUrl(), TDApplication.getSessionManager().getAccessToken() );

		req.addGetParam("cmd", "transaction-create");
		req.addGetParam("version", BRAINTREE_WRAPPER_MIN_VERSION);

		req.addPostParam("booking_pk", booking.getPk());
		req.addPostParam("booking_key", booking.getBookingKey());
		req.addPostParam("card_token", card.getToken());
		req.addPostParam("amount", booking.getTotalCostValue() );

		return doPostRequest(req);
	}




} // end of class
