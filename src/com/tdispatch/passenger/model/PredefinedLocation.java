package com.tdispatch.passenger.model;

import android.util.SparseIntArray;

import com.tdispatch.passenger.R;

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

public class PredefinedLocation {

	protected static final SparseIntArray mIcons = new SparseIntArray(0) {
		{
			put(ICON_NONE			, ICON_NONE);
			put(ICON_EMPTY			, R.drawable.location_type_empty);

			put(ICON_TRAIN			, R.drawable.location_type_train);
			put(ICON_PLANE			, R.drawable.location_type_plane);
			put(ICON_SHIP			, R.drawable.location_type_ship);
			put(ICON_BUS			, R.drawable.location_type_bus);
			put(ICON_ATM 			, R.drawable.location_type_atm);
			put(ICON_BAR 			, R.drawable.location_type_bar);
			put(ICON_CAFE			, R.drawable.location_type_cafe);
			put(ICON_GAS_STATION	, R.drawable.location_type_gas_station);
			put(ICON_HOSPITAL 		, R.drawable.location_type_hospital);
			put(ICON_HOTEL 			, R.drawable.location_type_hotel);
			put(ICON_CINEMA 		, R.drawable.location_type_cinema);
			put(ICON_PARKING 		, R.drawable.location_type_parking);
			put(ICON_PIZZA 			, R.drawable.location_type_pizza);
			put(ICON_POST_OFFICE	, R.drawable.location_type_post_office);
			put(ICON_RESTAURANT 	, R.drawable.location_type_restaurant);
			put(ICON_STORE 			, R.drawable.location_type_store);
			put(ICON_TAXI 			, R.drawable.location_type_taxi);
		}
	};

	public static final int TYPE_SEPARATOR		= 0;
	public static final int TYPE_LOCATION		= 1;
	public static final int TYPE_MAX_COUNT  	= 2;

	public static final int ICON_NONE 			= 0;
	public static final int ICON_EMPTY		= 10;
	public static final int ICON_TRAIN	= 11;
	public static final int ICON_PLANE		= 12;
	public static final int ICON_SHIP		= 13;
	public static final int ICON_BUS			= 14;
	public static final int ICON_ATM 			= 15;
	public static final int ICON_BAR 			= 16;
	public static final int ICON_CAFE			= 17;
	public static final int ICON_GAS_STATION	= 18;
	public static final int ICON_HOSPITAL 		= 19;
	public static final int ICON_HOTEL 			= 20;
	public static final int ICON_CINEMA 		= 21;
	public static final int ICON_PARKING 		= 22;
	public static final int ICON_PIZZA 			= 23;
	public static final int ICON_POST_OFFICE 	= 24;
	public static final int ICON_RESTAURANT 	= 25;
	public static final int ICON_STORE 			= 26;
	public static final int ICON_TAXI 			= 27;

	protected int mType;
	protected int mIconType;
	protected LocationData mLocation;
	protected String mLabel;

	public PredefinedLocation( int icon, LocationData location ) {
		setType(TYPE_LOCATION);
		setIconType(icon);
		setLocation(location);
	}
	public PredefinedLocation( int type, int icon, LocationData location ) {
		setType(type);
		setIconType(icon);
		setLocation(location);
	}
	public PredefinedLocation( String label ) {
		setType(TYPE_SEPARATOR);
		setIconType(ICON_NONE);
		setLabel(label);
	}


	// helpers
	public static PredefinedLocation Location(LocationData loc) {
		return new PredefinedLocation(TYPE_LOCATION, ICON_NONE, loc);
	}
	public static PredefinedLocation Separator(String label) {
		return new PredefinedLocation(label);
	}
	public static PredefinedLocation Airport(LocationData loc) {
		return new PredefinedLocation(TYPE_LOCATION, ICON_PLANE, loc);
	}
	public static PredefinedLocation Trainstation(LocationData loc) {
		return new PredefinedLocation(TYPE_LOCATION, ICON_TRAIN, loc);
	}


	protected void setType(int type) {
		mType = type;
	}

	public int getType() {
		return mType;
	}

	protected void setIconType(int iconId) {
		mIconType = iconId;
	}
	public int getIconDrawableId() {
		return mIcons.get(mIconType);
	}

	protected void setLocation(LocationData location) {
		setLabel(location.getAddress());
		mLocation = location;
	}
	public LocationData getLocation() {
		return mLocation;
	}

	protected void setLabel(String label) {
		mLabel = label;
	}
	public String getLabel() {
		return mLabel;
	}
}