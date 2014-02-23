package com.tdispatch.passenger.tools;

import com.tdispatch.passenger.define.PaymentMethod;

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

public final class Office
{
	protected static ConfigReader mReader = new ConfigReader();

	public static String getApiUrl() {
		return mReader.getString("caboffice_api_url");
	}


	public static String getFleetApiKey() {
		return mReader.getString("caboffice_fleet_api_key");
	}
	public static String getOAuthClientId() {
		return mReader.getString("caboffice_client_id");
	}
	public static String getOAuthSecret() {
		return mReader.getString("caboffice_client_secret");
	}

	public static Boolean isLuggageSupportEnabled() {
		return mReader.getBoolean("caboffice_luggage_support_enabled");
	}


	public static double getDefaultLocationLat() {
		return Double.valueOf( mReader.getString("caboffice_default_location_latitude").replace(",", ".") );
	}
	public static double getDefaultLocationLng() {
		return Double.valueOf( mReader.getString("caboffice_default_location_longitude").replace(",", ".") );
	}

	public static int getNewBookingMaxDaysAhead() {
		return mReader.getInteger("caboffice_settings_new_bookings_max_days_ahead");
	}


	public static Boolean isDemoWarningDisabled() {
		return mReader.getBoolean("caboffice_settings_hide_demo_warning");
	}


	public static Boolean getEnableLocationSearchModules() {
		return mReader.getBoolean("caboffice_settings_enable_location_search_modules");
	}


	public static Boolean isDropoffLocationMandatory() {
		Boolean result = false;

		if(    mReader.getBoolean( "caboffice_settings_dropoff_location_is_mandatory" )
			|| mReader.getBoolean( "caboffice_braintree_enabled" ) ) {

			result = true;
		}

		return result;
	}

	public static Boolean isNearbyCabsTrackingEnabled() {
		return mReader.getBoolean("caboffice_settings_track_nearby_cabs");
	}

	public static Integer getMinimumAllowedPickupTimeOffset() {
		return mReader.getInteger( "caboffice_minimum_allowed_pickup_time_offset_in_minutes" );
	}


	public static Boolean isDropoffSupportDisabled() {
		return mReader.getBoolean("caboffice_settings_disable_dropoff_location");
	}


	public static Boolean isPaymentTokenSupportEnabled() {
		return isBraintreeEnabled();
	}


	public static Boolean isTOSRequiredToSignup() {
		return mReader.getBoolean("caboffice_tos_must_accept_on_signup");
	}
	public static String getTOSUrl() {
		return mReader.getString( "caboffice_tos_url" );
	}


	public static Integer getTimeFormat() {
		return mReader.getInteger("caboffice_time_format");
	}
	public static Integer getDateFormat() {
		return mReader.getInteger("caboffice_date_format");
	}
	public static Integer getDateTimeOrder() {
		return mReader.getInteger("caboffice_date_time_order");
	}


	public static int getCancellationFeeTimeThresold() {
		return mReader.getInteger("caboffice_cancellation_fee_time_threshold");
	}

	public static Boolean isInAppPaymentEnabled() {
		Boolean result = true;

		if(    (isBraintreeEnabled() == false)
			|| (isPaymentTokenSupportEnabled() == false)
			) {
			result = false;
		}

		return result;
	}

	public static Boolean shouldBookingBeChargedInAdvance() {
		return true;
	}

	public static Boolean isBraintreeEnabled() {
		return mReader.getBoolean( "caboffice_braintree_enabled" );
	}


	public static String getBraintreeWrapperUrl() {
		return mReader.getString("caboffice_braintree_wraper_url");
	}
	public static String getBraintreeEncryptionKey() {
		return mReader.getString("caboffice_braintree_clientside_encryption_key");
	}



	public static Integer getCardLimit() {
		return mReader.getInteger("caboffice_card_limit");
	}


	public static Integer getBookingLocationValidationHookMode() {
		return mReader.getInteger("caboffice_new_booking_location_validation_hook");
	}


	public static boolean isCashPaymentDisabled() {
		return mReader.getBoolean("caboffice_disable_cash_payment_method");
	}
	public static int getDefaultPaymentMethod() {
		int result = PaymentMethod.UNKNOWN;

		if( isBraintreeEnabled() ) {
			result = PaymentMethod.CARD;
		} else {
			result = PaymentMethod.CASH;
		}

		return result;
	}

}