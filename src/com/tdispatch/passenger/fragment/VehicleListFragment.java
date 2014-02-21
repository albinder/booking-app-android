package com.tdispatch.passenger.fragment;

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
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.iface.host.VehicleListFragmentInterface;
import com.tdispatch.passenger.iface.host.VehicleSelectorHostInterface;
import com.tdispatch.passenger.model.VehicleData;
import com.webnetmobile.tools.WebnetTools;

public class VehicleListFragment extends TDFragment implements VehicleListFragmentInterface
{
	protected Cursor mCursor;
	protected ListCursorAdapter mCursorAdapter;
	protected VehicleSelectorHostInterface mHost;

	@Override
	protected int getLayoutId() {
		return R.layout.vehicle_list_fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mHost = (VehicleSelectorHostInterface)activity;
	}

	@Override
	protected void onPostCreateView() {
		mCursor = VehicleData.getAllAsCursor();
		mCursorAdapter = new ListCursorAdapter(mContext, mCursor, 0);

		ListView lv = (ListView)mFragmentView.findViewById( R.id.list );
		lv.setAdapter( mCursorAdapter );
		lv.setOnItemClickListener( mOnItemClickListener );
	}

	/**[ adapter ]***********************************************************************************************************/

	protected class ListCursorAdapter extends CursorAdapter {
		public ListCursorAdapter( Context context, Cursor cursor, int flags ) {
			super( context, cursor, flags );
		}


		private int getItemViewType(Cursor cursor) {
			VehicleData v = new VehicleData(cursor);
			return ( (v.getPassengerCapacity() >= mRequiredSeats) && (v.getLuggageCapacity() >= mRequiredLuggage) ) ? 0 : 1;
		}

		@Override
		public int getItemViewType(int position) {
		    Cursor cursor = (Cursor) getItem(position);
		    return getItemViewType(cursor);
		}

		@Override
		public int getViewTypeCount() {
		    return 2;
		}

		@Override
		public void bindView( View view, Context context, Cursor cursor ) {
			VehicleData v = new VehicleData(cursor);

			WebnetTools.setText(view, R.id.name, v.getName());
			WebnetTools.setText(view, R.id.seats, String.valueOf(v.getPassengerCapacity()));
			WebnetTools.setText(view, R.id.luggage, String.valueOf(v.getLuggageCapacity()));
		}

		@Override
		public View newView( Context context, Cursor cursor, ViewGroup parent ) {

			final LayoutInflater inflater = LayoutInflater.from(context);

			VehicleData v = new VehicleData(cursor);
			int layoutId = ( (v.getPassengerCapacity() >= mRequiredSeats) && (v.getLuggageCapacity() >= mRequiredLuggage) )
											? R.layout.vehicle_list_row_enabled : R.layout.vehicle_list_row_disabled;

			View view = inflater.inflate(layoutId, null);
			WebnetTools.setCustomFonts( mApp, (ViewGroup)view );

			return view;
		}
	}


	protected OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			VehicleData v = VehicleData.getByLocalId(id);

			if( (v.getPassengerCapacity() >= mRequiredSeats) && (v.getLuggageCapacity() >= mRequiredLuggage) ) {
				mHost.doOk( v );
			} else {
				mHost.doCancel();
			}
		}
	};

	/**[ VehicleListFragmentInterface ]*******************************************************************************/

	protected int mRequiredSeats = 1;
	protected int mRequiredLuggage = 0;
	protected VehicleData mCurrentVehicle;

	@Override
	public void setVehicleFilter(VehicleData currentVehicle, int requiredSeats, int requiredLuggage) {
		mCurrentVehicle = currentVehicle;
		mRequiredSeats = requiredSeats;
		mRequiredLuggage = requiredLuggage;

		mCursorAdapter.notifyDataSetChanged();

		//mCursorAdapter.swapCursor( VehicleData.getAllAsCursor(currentVehicle, requiredSeats, requiredLuggage) );
	}

} // eoc
