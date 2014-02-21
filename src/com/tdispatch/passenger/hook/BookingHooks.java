package com.tdispatch.passenger.hook;

import java.util.ArrayList;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.fragment.SearchStationsFragment;
import com.tdispatch.passenger.iface.hook.BookingHooksInterface;
import com.tdispatch.passenger.model.LocationData;
import com.tdispatch.passenger.model.PredefinedLocation;
import com.tdispatch.passenger.tools.Office;
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

public class BookingHooks implements BookingHooksInterface
{
	protected int mJourneyRouteValidationErrorMsgId;

	@Override
	public int getJourneyRouteValidationError() {
		return mJourneyRouteValidationErrorMsgId;
	}

	@Override
	public Boolean isJourneyRouteValid(LocationData pickup, LocationData dropoff) {
		Boolean result = true;

		switch( Office.getBookingLocationValidationHookMode() ) {
			// A is one of predefined
			case 1: {
				result = checkIfIsPredefinedLocation(pickup);
				if( result ) {
					mJourneyRouteValidationErrorMsgId = R.string.new_booking_pickup_not_in_required_area;
				}
			}
			break;

			// Just B
			case 2: {
				result = checkIfIsPredefinedLocation(dropoff);
				if( result ) {
					mJourneyRouteValidationErrorMsgId = R.string.new_booking_dropoff_not_in_required_area;
				}
			}
			break;

			// A AND B
			case 3: {
				if ( (checkIfIsPredefinedLocation(pickup) == false) || (checkIfIsPredefinedLocation(dropoff) == false) ) {
					mJourneyRouteValidationErrorMsgId = R.string.new_booking_pickup_and_dropoff_not_in_required_area;
					result = false;
				}
			}
			break;

			// A OR B
			case 4: {
				if ( (checkIfIsPredefinedLocation(pickup) == false) && (checkIfIsPredefinedLocation(dropoff) == false) ) {
					mJourneyRouteValidationErrorMsgId = R.string.new_booking_pickup_or_dropoff_not_in_required_area;
					result = false;
				}
			}
			break;

			case 0:
			default:
				break;
		}

		return result;
	}


	protected Boolean checkIfIsPredefinedLocation(LocationData loc) {

		ArrayList<ArrayList<PredefinedLocation>> lists = new ArrayList<ArrayList<PredefinedLocation>>() {
			private static final long serialVersionUID = 1L;
			{
				add((new SearchStationsFragment()).getItems());
			}
		};

		Boolean result = false;
		if (loc != null) {

			checkLoop:
			for( ArrayList<PredefinedLocation> singleList : lists ) {
				for( PredefinedLocation s : singleList ) {
					if(WebnetTools.calcDistance(loc.getLatLng(), s.getLocation().getLatLng()) < 0.5) {
						result = true;
						break checkLoop;
					}
				}
			}

		}

		return result;
	}

}
