package com.tdispatch.passenger.data.manager;

import java.util.ArrayList;

import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.model.BookingData;

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

final public class BookingManager
{
	protected TDApplication mContext;
	protected BookingManager mBookingManager;

	protected ArrayList<BookingData> mBookings = new ArrayList<BookingData>();

	protected static BookingManager _instance = null;
	public static BookingManager getInstance(TDApplication context) {
		if( _instance == null ) {
			_instance = new BookingManager(context);
		}

		return _instance;
	}


	/**[ constructor ]*******************************************************************************************************************/

	public BookingManager( TDApplication context ) {
		mContext = context;
	}


	/**[ booking state listeners ]*******************************************************************************************************/

	public static final int STATE_UNKNOWN				= 0;
	public static final int STATE_LOADING_BOOKINGS	= 1;
	public static final int STATE_BOOKINGS_LOADED		= 2;
	public static final int STATE_BOOKINGS_UPDATED	= 3;


	protected int mCurrentState = STATE_UNKNOWN;

	protected ArrayList<OnBookingManagerStateChangeListener> mOnBookingManagerStateChangeListeners = new ArrayList<OnBookingManagerStateChangeListener>();

	protected void notifyStateChangeListeners( int state ) {
		// notify all listeners
		for( int i=0; i<mOnBookingManagerStateChangeListeners.size(); i++ ) {
			OnBookingManagerStateChangeListener tmp = mOnBookingManagerStateChangeListeners.get( i );
			tmp.onStateChange( state );
		}
	}

	// registers new listener to be notified on each login/logout state change
	// checks for duplicated entries first
	public void setOnStateChangeListener( OnBookingManagerStateChangeListener listener ) {
		setOnStateChangeListener( listener, OnBookingManagerStateChangeListener.FLAG_DEFAULT );
	}
	public void setOnStateChangeListener( OnBookingManagerStateChangeListener listener, int flags ) {
		Boolean listenerAlreadyRegistered = false;

		for( int i=0; i<mOnBookingManagerStateChangeListeners.size(); i++ ) {
			if( mOnBookingManagerStateChangeListeners.get( i ) == listener ) {
				listenerAlreadyRegistered = true;
				break;
			}
		}

		if( listenerAlreadyRegistered == false ) {
			mOnBookingManagerStateChangeListeners.add( listener );

			if( (flags & OnBookingManagerStateChangeListener.FLAG_NOTIFY_WITH_CURRENT_STATE) != 0 ) {
				// notify attached listener on current state
				listener.onStateChange( mCurrentState );
			}
		}
	}

	// removes previously registered listener
	public void unsetOnSignInStateChangeListener( OnBookingManagerStateChangeListener listener ) {
		for( int i=0; i<mOnBookingManagerStateChangeListeners.size(); i++ ) {
			if( mOnBookingManagerStateChangeListeners.get( i ) == listener ) {
				mOnBookingManagerStateChangeListeners.remove( i );
				break;
			}
		}
	}



	/**[ OnBookingManagerStateChangeListener ]*******************************************************************************************************/

	public interface OnBookingManagerStateChangeListener
	{
		// these are BITS (to be OR'ed)!
		public final static int FLAG_DEFAULT						= 0;
		public final static int FLAG_NOTIFY_WITH_CURRENT_STATE	= 1;		// listener will be instantly notified on **current** state. If not specified, listener will be notifed when next change occur

		public void onStateChange( int newStatus );

	}



} // end of class
