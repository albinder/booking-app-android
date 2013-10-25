package com.tdispatch.passenger.fragment.dialog;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.common.Office;
import com.tdispatch.passenger.core.TDDialogFragment;
import com.tdispatch.passenger.model.BookingData;
import com.webnetmobile.tools.WebnetTools;

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
public class BookingCancelConfirmationDialogFragment extends TDDialogFragment
{
	public interface BookingCancelConfirmationDialogClickListener
	{
		public void doBookingCancel(BookingData booking, String reason);
	}

	protected BookingData mBooking;

	public static BookingCancelConfirmationDialogFragment newInstance(BookingData booking) {

		BookingCancelConfirmationDialogFragment frag = new BookingCancelConfirmationDialogFragment();

		if( booking == null ) {
			throw new NullPointerException("Booking cannot be null");
		}

		Bundle args = new Bundle();
		args.putParcelable(Const.Bundle.BOOKING, booking);
		frag.setArguments(args);

		return frag;
	}

	protected BookingCancelConfirmationDialogClickListener mHostFragment;

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);

		try {
			mHostFragment = (BookingCancelConfirmationDialogClickListener)getTargetFragment();
		} catch( ClassCastException e ) {
			throw new ClassCastException("Host Fragment needs to implement BookingCancelConfirmationDialogClickListener");
		}

		Bundle args = getArguments();
		mBooking = args.getParcelable(Const.Bundle.BOOKING);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.booking_cancel_confirmation_dialog_fragment;
	}

	@Override
	protected void onPostCreateView() {

		String dialogMsg= getString(R.string.booking_cancel_confirmation_message);

		int cancelFeeThreshold = Office.getCancellationFeeTimeThresold();
		if ( cancelFeeThreshold > 0 ) {
			if( mBooking.getPickupDate().getTime() < (System.currentTimeMillis() + (cancelFeeThreshold * WebnetTools.MILLIS_PER_MINUTE)) ) {
				dialogMsg = String.format(getString(R.string.booking_cancel_cancellation_fee_warning_fmt), cancelFeeThreshold);
			}
		}

		WebnetTools.setText(mFragmentView, R.id.message, dialogMsg);

		int[] ids = { R.id.button_ok, R.id.button_cancel };
		for( int id : ids ) {
			View button = mFragmentView.findViewById( id );
			button.setOnClickListener( listener );
		}
	}

	protected View.OnClickListener listener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {

			switch( v.getId() ) {

				case R.id.button_ok: {
					EditText et = (EditText)mFragmentView.findViewById(R.id.reason);
					mHostFragment.doBookingCancel(mBooking, et.getText().toString());
					dismiss();
				}
				break;

				case R.id.button_cancel: {
					dismiss();
				}
				break;
			}
		}
	};

}	// end of class
