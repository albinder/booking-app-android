package com.tdispatch.passenger.model;

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

public class PickupAndDropoff
{
	protected LocationData mPickup;
	protected LocationData mDropoff;

	public PickupAndDropoff() {
		// dummy
	}

	public PickupAndDropoff(LocationData pickup, LocationData dropoff) {
		mPickup = pickup;
		mDropoff = dropoff;
	}

	public PickupAndDropoff setPickup( LocationData data ) {
		mPickup = data;
		return this;

	}

	public LocationData getPickup() {
		return mPickup;
	}

	public PickupAndDropoff setDropoff( LocationData data ) {
		mDropoff = data;
		return this;
	}

	public LocationData getDropoff() {
		return mDropoff;
	}

}
