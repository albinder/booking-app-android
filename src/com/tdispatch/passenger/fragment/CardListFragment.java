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

import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tdispatch.passenger.CardActivity;
import com.tdispatch.passenger.R;
import com.tdispatch.passenger.api.ApiHelper;
import com.tdispatch.passenger.api.ApiResponse;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.define.BundleKey;
import com.tdispatch.passenger.define.ErrorCode;
import com.tdispatch.passenger.define.RequestCode;
import com.tdispatch.passenger.fragment.dialog.GenericDialogFragment;
import com.tdispatch.passenger.model.CardData;
import com.tdispatch.passenger.tools.Office;
import com.webnetmobile.tools.WebnetTools;

import de.timroes.android.listview.EnhancedListView;
import de.timroes.android.listview.EnhancedListView.OnDismissCallback;
import de.timroes.android.listview.EnhancedListView.Undoable;

public class CardListFragment extends TDFragment
{
	protected ArrayList<CardData> mItems = new ArrayList<CardData>();
	protected ListAdapter mAdapter;

	protected EnhancedListView mMainListview;


	@Override
	protected int getLayoutId() {
		return R.layout.card_list_fragment;
	}

	@Override
	protected void onPostCreateView() {

		mMainListview = (EnhancedListView)mFragmentView.findViewById(R.id.list);

		mMainListview.setUndoStyle(EnhancedListView.UndoStyle.SINGLE_POPUP);
		mMainListview.setUndoHideDelay((int)WebnetTools.MILLIS_PER_SECOND * 5);
		mMainListview.setRequireTouchBeforeDismiss(false);
		mMainListview.setSwipeDirection(EnhancedListView.SwipeDirection.BOTH);
		mMainListview.setDismissCallback( mOnDismissCallback );
		mMainListview.enableSwipeToDismiss();

		int[] ids = { R.id.button_add_1, R.id.button_add_2 };
		for( int id : ids ) {
			View v = mFragmentView.findViewById( id );
			v.setOnClickListener( mOnClickListener );
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		mItems = CardData.getAll();
		mAdapter = new ListAdapter();
		mMainListview.setAdapter( mAdapter );

		showListEmptyMessage( mItems.size() == 0 );
	}

	@Override
	protected Integer getOverlayBackgroundResourceId() {
		return R.color.background;
	}


	protected void showListEmptyMessage( Boolean isListEmpty ) {
		WebnetTools.setVisibility(mFragmentView, R.id.list_container, (isListEmpty) ? View.GONE : View.VISIBLE);
		WebnetTools.setVisibility(mFragmentView, R.id.list_empty_container, (isListEmpty) ? View.VISIBLE : View.GONE);
	}


	/**[ Enhanced List View ]*************************************************************************************************/

	protected EnhancedListView.OnDismissCallback  mOnDismissCallback = new OnDismissCallback()
	{
		@Override
		public Undoable onDismiss( EnhancedListView listView, final int position ) {
			final CardData card = mItems.get( position );

			mAdapter.remove(position);

			return new EnhancedListView.Undoable()
			{
				@Override
				public void undo() {
					mAdapter.add(position, card);
				}

				@Override
				public String getTitle() {
					return getString(R.string.card_undo_label);
				}

				@Override
				public void discard() {
					WebnetTools.executeAsyncTask(new DeleteCardAsyncTask( card.getToken() ));
					card.delete();
				}
			};

		}
	};


	protected class DeleteCardAsyncTask extends AsyncTask<Void, Void, ApiResponse> {

		String mToken = null;

		public DeleteCardAsyncTask(String token) {
			mToken = token;
		}

		@Override
		protected void onPreExecute() {
			mParentActivity.lockUI();
		}

		@Override
		protected ApiResponse doInBackground( Void ... params ) {
			ApiHelper api = ApiHelper.getInstance(TDApplication.getAppContext());
			return api.braintreeWrapperCardDelete( mToken );
		}

		@Override
		protected void onPostExecute(ApiResponse data) {

			mParentActivity.unlockUI();

			if( data.getErrorCode() != ErrorCode.OK ) {
				showDialog(GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title),
						String.format(getString(R.string.card_error_failed_to_delete_card), data.getErrorMessage()) );
			}

		}
	}

	/**[ listener ]**********************************************************************************************************/

	protected View.OnClickListener mOnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {
			switch( v.getId() ) {

				case R.id.button_add_1:
				case R.id.button_add_2: {
					Boolean canAddNewCard = true;

					int limit = Office.getCardLimit();
					if( limit > 0 ) {
						if(CardData.count() >= limit) {
							canAddNewCard = false;
						}
					}

					if( canAddNewCard ) {
						Intent intent = new Intent();
						intent.putExtra(BundleKey.REQUEST_CODE, RequestCode.CARD_CREATE );
						intent.setComponent( new ComponentName( mContext.getPackageName(), CardActivity.class.getName() ) );
						startActivityForResult(intent, RequestCode.CARD_CREATE);
						mParentActivity.overridePendingTransition( R.anim.activity_slide_in_up, R.anim.fade_out);

					} else {
						showDialog( GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title),
										getString(R.string.card_error_cannot_add_new_card));
					}
				}
				break;

			}

		}
	};

	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent intent ) {

		switch( requestCode ) {

			case RequestCode.CARD_CREATE: {
				if( resultCode == Activity.RESULT_OK ) {

					mItems = CardData.getAll();
					mAdapter = new ListAdapter();
					mMainListview.setAdapter( mAdapter );

					showListEmptyMessage( mItems.size() == 0 );
				}
			}
			break;

			default: {
				super.onActivityResult(requestCode, resultCode, intent);
			}
			break;
		}
	}

	/**[ adapter ]***********************************************************************************************************/

	private class ListAdapter extends BaseAdapter
	{
		public void remove( int position ) {
			mItems.remove(position);
			notifyDataSetChanged();
			showListEmptyMessage(mItems.size() == 0);
		}

		public void add( int position, CardData item ) {
			mItems.add(position, item);
			notifyDataSetChanged();
			showListEmptyMessage(mItems.size() == 0);
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public CardData getItem( int position ) {
			return mItems.get(position);
		}

		@Override
		public long getItemId( int position ) {
			return position;
		}

		@Override
		public View getView( int position, View convertView, ViewGroup parent ) {
			CardData item = mItems.get(position);

			View view = convertView;
			if( view == null ) {
				int layoutId = R.layout.card_list_row;

				LayoutInflater li = (LayoutInflater)TDApplication.getAppContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = li.inflate(layoutId, null);

				WebnetTools.setCustomFonts(TDApplication.getAppContext(), (ViewGroup)view);
			}

			WebnetTools.setText(view, R.id.label, item.getDisplayLabel());
			WebnetTools.setText(view, R.id.number, item.getNumber());

			return (view);
		}
	}



// end of class
}
