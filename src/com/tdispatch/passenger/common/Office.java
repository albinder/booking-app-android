package com.tdispatch.passenger.common;

import android.R.integer;
import android.content.Context;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.core.TDApplication;

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
public final class Office
{
	protected static Context mContext = TDApplication.getAppContext();


	public static double getDefaultLocationLat() {
		return Double.valueOf( mContext.getResources().getString(R.string.caboffice_default_location_latitude).replace(",", ".") );
	}
	public static double getDefaultLocationLong() {
		return Double.valueOf( mContext.getResources().getString(R.string.caboffice_default_location_longitude).replace(",", ".") );
	}

	public static int getNewBookingMaxDaysAhead() {
		return mContext.getResources().getInteger(R.integer.caboffice_settings_new_bookings_max_days_ahead);
	}


	public static Boolean isDemoWarningDisabled() {
		return mContext.getResources().getBoolean(R.bool.caboffice_settings_hide_demo_warning);
	}


	public static Boolean getEnableLocationSearchModules() {
		return mContext.getResources().getBoolean(R.bool.caboffice_settings_enable_location_search_modules);
	}


	public static Boolean isDropoffLocationMandatory() {
		Boolean result = false;

		if(    mContext.getResources().getBoolean( R.bool.caboffice_settings_dropoff_location_is_mandatory )
			|| mContext.getResources().getBoolean( R.bool.td_braintree_enabled ) ) {

			result = true;
		}

		return result;
	}

	public static Boolean isNearbyCabsTrackingEnabled() {
		return mContext.getResources().getBoolean(R.bool.caboffice_settings_track_nearby_cabs);
	}
	public static Boolean isBookingTrackingEnabled() {
		return mContext.getResources().getBoolean(R.bool.caboffice_settings_track_bookings );
	}

	public static Boolean useAlternativeDropoffLabel() {
		return mContext.getResources().getBoolean(R.bool.caboffice_settings_use_alternative_dropoff_label);
	}

	public static Integer getMinimumAllowedPickupTimeOffset() {
		return mContext.getResources().getInteger( R.integer.caboffice_minimum_allowed_pickup_time_offset_in_minutes );
	}


	public static Boolean isDropoffSupportDisabled() {
		return mContext.getResources().getBoolean(R.bool.caboffice_settings_disable_dropoff_location);
	}


	public static Boolean isPaymentTokenSupportEnabled() {
		return isBraintreeEnabled();
	}


	public static Boolean isTOSRequiredToSignup() {
		return mContext.getResources().getBoolean(R.bool.caboffice_tos_must_accept_on_signup);
	}
	public static String getTOSUrl() {
		return mContext.getResources().getString( R.string.caboffice_tos_url );
	}


	public static Boolean isBoolinkListPickupDateRelativeModeEnabled() {
		return mContext.getResources().getBoolean(R.bool.caboffice_booking_list_pickup_time_relative_mode);
	}


	public static int getCancellationFeeTimeThresold() {
		return mContext.getResources().getInteger(R.integer.cabooffice_cancellation_fee_time_threshold);
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
		return mContext.getResources().getBoolean( R.bool.td_braintree_enabled );
	}

	public static Integer getCardLimit() {
		return mContext.getResources().getInteger(R.integer.caboffice_card_limit);
	}
}