package com.tdispatch.passenger.fragment;

import java.util.ArrayList;

import com.tdispatch.passenger.iface.host.AddressSearchModuleInterface;
import com.tdispatch.passenger.model.LocationData;
import com.tdispatch.passenger.model.PredefinedLocation;

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

public class SearchStationsFragment extends SearchStationsFragmentBase implements AddressSearchModuleInterface
{
	protected static final ArrayList<PredefinedLocation> mItems = new ArrayList<PredefinedLocation>() {
		private static final long serialVersionUID = 1L;
		{
			add( PredefinedLocation.Trainstation( new LocationData("Bury St Edmonds Station", "IP32 6AQ", 52.253708, 0.712454) ) );
			add( PredefinedLocation.Trainstation( new LocationData("Chelmsford Station", "CM1 1HT", 51.736465, 0.468708) ) );
			add( PredefinedLocation.Trainstation( new LocationData("Colchester Station", "CO4 5EY", 51.901230, 0.893736) ) );
			add( PredefinedLocation.Trainstation( new LocationData("Ely Station", "CB7 4DJ", 52.391209, 0.265048) ) );
			add( PredefinedLocation.Trainstation( new LocationData("Norwich Station", "NR1 1EH", 52.627151, 1.306835) ) );
			add( PredefinedLocation.Trainstation( new LocationData("Ingatestone Station", "CM4 0BW", 51.666916, 0.383777) ) );
		}
	};

	@Override
	public ArrayList<PredefinedLocation> getItems() {
		return mItems;
	}

}
