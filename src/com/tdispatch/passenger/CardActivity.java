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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.tdispatch.passenger.api.ApiHelper;
import com.tdispatch.passenger.api.ApiResponse;
import com.tdispatch.passenger.core.TDActivity;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.define.ErrorCode;
import com.tdispatch.passenger.fragment.dialog.GenericDialogFragment;
import com.tdispatch.passenger.model.CardData;
import com.webnetmobile.tools.JsonTools;
import com.webnetmobile.tools.WebnetTools;
import com.tdispatch.passenger.tools.Office;


public class CardActivity extends TDActivity
{
	@Override
	public void onCreate( Bundle savedInstanceState ) {

		super.onCreate(savedInstanceState);

		setContentView( R.layout.card_activity );

		int ids[] = { R.id.button_ok };
		for( int id : ids ) {
			findViewById(id).setOnClickListener( mOnClickListener );
		}

		WebnetTools.setVisibility(mMe, R.id.demo_warning_container, Office.isDemoWarningDisabled() ? View.GONE : View.VISIBLE );

		setCustomFonts();
	}

	protected View.OnClickListener mOnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {

			switch( v.getId() ) {
				case R.id.button_ok: {
					if( validateDataAndShowMessage() ) {

						String holderName = ((EditText)findViewById(R.id.card_holder_name)).getText().toString().trim();
						String cardNumber = ((EditText)findViewById(R.id.card_number)).getText().toString().trim();
						String expMonth = ((EditText)findViewById(R.id.card_expiration_month)).getText().toString().trim();
						String expYear = ((EditText)findViewById(R.id.card_expiration_year)).getText().toString().trim();
						String cvv = ((EditText)findViewById(R.id.card_cvv)).getText().toString().trim();

						CardParams card = new CardParams();
						card.userPk = TDApplication.getSessionManager().getAccountData().getPk();
						card.holderName = holderName;
						card.cardNumber = cardNumber;
						card.cardExpirationMonth = expMonth;
						card.cardExpirationYear = expYear;
						card.cardCvv = ( cvv.length() > 0 ) ? cvv : null;

						WebnetTools.executeAsyncTask( new AddCardAsyncTask(), card);
					}
				}
				break;
			}
		}
	};

	protected Boolean validateDataAndShowMessage() {
		Boolean result = true;

		int msgId = R.string.new_card_error_generic;

		if( result ) {
			String holderName = ((EditText)findViewById(R.id.card_holder_name)).getText().toString().trim();

			if( holderName.length() < 4 ) {
				msgId = R.string.new_card_error_invalid_holder_name;
				result = false;
			}
		}

		String cardNumber = ((EditText)findViewById(R.id.card_number)).getText().toString().trim();
		if( (result) && (isDigitsOnly(cardNumber) == false) ) {
			msgId = R.string.new_card_error_invalid_card_number;
			result = false;
		}

		if( result ) {
			String expMonth = ((EditText)findViewById(R.id.card_expiration_month)).getText().toString().trim();
			String expYear = ((EditText)findViewById(R.id.card_expiration_year)).getText().toString().trim();

			if( (isDigitsOnly(expMonth) == false) || (isDigitsOnly(expYear) == false) ) {
				msgId = R.string.new_card_error_invalid_expiration_date;
				result = false;
			}
		}

		if( result ) {
			String cvv = ((EditText)findViewById(R.id.card_cvv)).getText().toString().trim();

			if( cvv.length() > 0 ) {
				if( isDigitsOnly(cvv) == false ) {
					msgId = R.string.new_card_error_invalid_cvv_number;
					result = false;
				}
			}
		}

		if( !result ) {
			showDialog( GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), getString(msgId) );
		}

		return result;
	}

	protected Boolean isDigitsOnly(String str) {
		return ( (str != null) && (str.length()>0) && (TextUtils.isDigitsOnly(str)) );
	}

	protected void doOk() {
		setResult( Activity.RESULT_OK );
		finish();
	}

	protected void doCancel() {
		setResult( Activity.RESULT_CANCELED );
		finish();
	}


	/**[ card create task ]*********************************************************************************************************/

	protected class AddCardAsyncTask extends AsyncTask<CardParams, Integer, ApiResponse> {

		@Override
		protected void onPreExecute() {
			lockUI();
		}

		@Override
		protected ApiResponse doInBackground( CardParams ... params ) {
			ApiResponse result = new ApiResponse();

			CardParams p = params[0];

			ApiHelper api = ApiHelper.getInstance( TDApplication.getAppContext() );
			ApiResponse response = api.braintreeWrapperCardCreate(p.userPk, p.holderName, p.cardNumber,
															p.cardCvv, p.cardExpirationMonth, p.cardExpirationYear);
			if( response.getErrorCode() == ErrorCode.OK ) {
				CardData card = new CardData( JsonTools.getJSONObject( response.getJSONObject(), "card" ) );
				card.insert();
			}

			result = response;

			return result;
		}

		@Override
		protected void onPostExecute( ApiResponse result ) {
			unlockUI();

			if( result.getErrorCode() == ErrorCode.OK ) {
				doOk();
			} else {
				String msg = String.format( getString(R.string.new_card_error_create_card_failed_fmt), result.getErrorMessage());
				showDialog( GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), msg );
			}
		}
	}

	protected class CardParams {
		String userPk;
		String holderName;
		String cardNumber;
		String cardCvv;
		String cardExpirationMonth;
		String cardExpirationYear;
	}


} // end of class
