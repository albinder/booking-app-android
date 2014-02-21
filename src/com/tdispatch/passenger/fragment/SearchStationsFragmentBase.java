package com.tdispatch.passenger.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.SearchActivity;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.define.BundleKey;
import com.tdispatch.passenger.iface.host.AddressSearchHostInterface;
import com.tdispatch.passenger.iface.host.AddressSearchModuleInterface;
import com.tdispatch.passenger.model.PredefinedLocation;
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

abstract public class SearchStationsFragmentBase extends TDFragment implements AddressSearchModuleInterface
{
	protected static ArrayList<PredefinedLocation> mItems;

	protected ListAdapter mAdapter;
	protected int mType = SearchActivity.TYPE_UNKNOWN;

	protected AddressSearchHostInterface mAddressSearchHost;

	@Override
	protected int getLayoutId() {
		return R.layout.search_stations_fragment;
	}

	protected AddressSearchHostInterface mHostActivity;
	@Override
	public void onAttach( Activity activity ) {
		super.onAttach( activity );
		mHostActivity = (AddressSearchHostInterface)activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if( args != null ) {
			mType = args.getInt(BundleKey.TYPE);
		} else {
			throw new IllegalArgumentException("Arguments not passed");
		}
	}

	@Override
	protected void onPostCreateView() {
		mItems = getItems();

		ListView lv = (ListView)mFragmentView.findViewById( R.id.list );
		mAdapter = new ListAdapter( mParentActivity, 0, mItems );
		lv.setAdapter( mAdapter );
	}

	@Override
	protected Boolean isBusyOverlayPresent() {
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();

		if( mItems.size() == 0 ) {
			WebnetTools.setVisibility(mFragmentView, R.id.list_container, View.GONE );
			WebnetTools.setVisibility(mFragmentView, R.id.list_empty_container, View.VISIBLE);
		} else {
			WebnetTools.setVisibility(mFragmentView, R.id.list_container, View.VISIBLE );
			WebnetTools.setVisibility(mFragmentView, R.id.list_empty_container, View.GONE);
		}

	}

	/************************************************************************************************************************/

	abstract public ArrayList<PredefinedLocation> getItems();

	/**[ adapter ]***********************************************************************************************************/

	protected class ListAdapter extends ArrayAdapter<PredefinedLocation>
	{
		protected Context mContext;

		public ListAdapter(Activity activity, int textViewResourceId, ArrayList<PredefinedLocation> objects) {
			super(activity, textViewResourceId, objects);
			mContext = activity;
		}

		@Override
		public int getItemViewType(int position) {
			return getItems().get(position).getType();
	    }

		@Override
		public int getViewTypeCount() {
			return PredefinedLocation.TYPE_MAX_COUNT;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			int itemType = getItems().get(position).getType();

			View view = convertView;
			if( view == null) {
				int layoutId = R.layout.search_stations_row;

				switch( itemType ) {
					case PredefinedLocation.TYPE_SEPARATOR:
						layoutId = R.layout.search_stations_row_separator;
						break;

					case PredefinedLocation.TYPE_LOCATION:
						layoutId = R.layout.search_stations_row;
						break;
				}

				LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = li.inflate(layoutId, null);

				WebnetTools.setCustomFonts( TDApplication.getAppContext(), (ViewGroup)view );

				switch( itemType ) {
					case PredefinedLocation.TYPE_LOCATION:
						view.setOnClickListener( mOnClickListener );
						break;

					default:
						// dummy
						break;
				}
			}

			ImageView iv = (ImageView)view.findViewById(R.id.item_icon);
			if( iv != null ) {
				int iconId = getItems().get(position).getIconDrawableId();
				if( iconId == PredefinedLocation.ICON_NONE ) {
					iv.setVisibility(View.GONE);
				} else {
					iv.setImageResource(iconId);
					iv.setVisibility(View.VISIBLE);
				}
			}

			WebnetTools.setText( view, R.id.item_address, getItems().get(position).getLabel() );
			view.setTag( R.id.tag_key_position, position );

			return( view );
		}

		protected View.OnClickListener mOnClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int position = (Integer) v.getTag(R.id.tag_key_position);
				if( getItems().get(position).getType() == PredefinedLocation.TYPE_LOCATION ) {
					mHostActivity.doSearchOk( mType, getItems().get(position).getLocation() );
				}
			}
		};
	}


	// AddressSearchModuleInterface
	@Override
	public void doEnterPage() {
		// dummy
	}

	@Override
	public void doLeavePage() {
		// dummy
	}


	// end of class
}
