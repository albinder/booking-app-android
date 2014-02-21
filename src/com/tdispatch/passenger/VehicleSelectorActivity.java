package com.tdispatch.passenger;

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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tdispatch.passenger.core.TDActivity;
import com.tdispatch.passenger.define.BundleKey;
import com.tdispatch.passenger.iface.host.VehicleListFragmentInterface;
import com.tdispatch.passenger.iface.host.VehicleSelectorHostInterface;
import com.tdispatch.passenger.model.VehicleData;


public class VehicleSelectorActivity extends TDActivity implements VehicleSelectorHostInterface
{
	protected int mRequiredSeats = 1;
	protected int mRequiredLuggage = 0;
	protected VehicleData mCurrentVehicle;

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.vehicle_selector_activity );

		Bundle extras = getIntent().getExtras();
		mRequiredSeats = extras.getInt(BundleKey.PASSENGER_COUNT);
		mRequiredLuggage = extras.getInt(BundleKey.LUGGAGE_COUNT);
		mCurrentVehicle = extras.getParcelable(BundleKey.VEHICLE);

		VehicleListFragmentInterface frag = (VehicleListFragmentInterface)getSupportFragmentManager().findFragmentById(R.id.vehicle_list_fragment);
		frag.setVehicleFilter(mCurrentVehicle, mRequiredSeats, mRequiredLuggage);

		setCustomFonts();
	}

	@Override
	public void doOk( VehicleData vehicle ) {
		Intent intent = new Intent();

		if( vehicle.getPk().equals(mCurrentVehicle.getPk()) ) {
			setResult( Activity.RESULT_CANCELED, intent );
		} else {
			intent.putExtra(BundleKey.VEHICLE, vehicle);
			setResult( Activity.RESULT_OK, intent );
		}
		finish();
	}

	@Override
	public void doCancel() {
		setResult( Activity.RESULT_CANCELED );
		finish();
	}

} // end of class
