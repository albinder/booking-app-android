package com.tdispatch.passenger;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.tdispatch.passenger.core.TDActivity;
import com.tdispatch.passenger.define.BundleKey;
import com.tdispatch.passenger.define.RequestCode;
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

final public class EditorActivity extends TDActivity
{
	protected Boolean mVoiceSearchAvailable = false;
	protected String mBody = null;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor_activity);

		EditText et = (EditText)findViewById(R.id.editor);

		Bundle extras = getIntent().getExtras();
		if( extras != null ) {
			mBody = extras.getString( BundleKey.BODY );
			if( mBody != null ) {
				et.setText( mBody );
				et.setSelection(mBody.length());
			}

			String title = extras.getString(BundleKey.TITLE);
			if(title == null) {
				title = getString(R.string.editor_default_title);
			}
			WebnetTools.setText(this, R.id.title, title);
		}

		// Check to see if a voice recognition activity is present on device
		PackageManager pm = mContext.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities( new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		mVoiceSearchAvailable = (activities.size() != 0);

		WebnetTools.setVisibility(this, R.id.button_voice_search, mVoiceSearchAvailable ? View.VISIBLE : View.GONE);

		int ids[] = { R.id.button_ok, R.id.button_voice_search};
		for( int id : ids ) {
			View v = findViewById(id);
			v.setOnClickListener(mOnClickListener);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if( (requestCode == RequestCode.VOICE_RECOGNITION) && (resultCode == Activity.RESULT_OK) ) {
			ArrayList<String> matches = intent.getStringArrayListExtra( RecognizerIntent.EXTRA_RESULTS );
			if( matches.size() > 0 ) {
				EditText et = (EditText)findViewById(R.id.editor);

				String text = et.getText().toString() + " " + matches.get(0) + " ";
				et.setText( text );
				et.setSelection( text.length() );
			}
		}
	}

	private final OnClickListener mOnClickListener = new OnClickListener()
	{
		@Override
		public void onClick( View v ) {

			switch( v.getId() ){
				case R.id.button_voice_search: {
					Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM );
					try {
						startActivityForResult(intent, RequestCode.VOICE_RECOGNITION);
					} catch ( Exception e ) {
						e.printStackTrace();
					}
				}
				break;

				case R.id.button_ok: {
					String body = ((TextView)findViewById(R.id.editor)).getText().toString().trim();

					Intent intent = new Intent();
					intent.putExtra(BundleKey.BODY, (body.length() > 0) ? body : null);
					setResult(Activity.RESULT_OK, intent);
					finish();
				}
				break;
			}
		}
	};

	@Override
	public void onBackPressed() {
		if( isUserContentPresent() == false ) {
			setResult( Activity.RESULT_CANCELED );
			finish();
		} else {
			showExitAlert();
		}
	}

	protected Boolean isUserContentPresent() {
		Boolean result = true;

		String body = ((TextView)findViewById( R.id.editor)).getText().toString();
		if( (body.length() == 0) || ( (mBody!=null)&&(body.equals(mBody)) ) ) {
			result = false;
		}

		return result;
	}

	private void showExitAlert()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder( mContext );

		builder.setIcon( android.R.drawable.ic_dialog_alert );
		builder.setCancelable(false);
		builder.setTitle( R.string.editor_exit_confirm_dialog_title );
		builder.setMessage( R.string.editor_exit_confirm_dialog_message );
		builder.setPositiveButton(
				R.string.editor_exit_confirm_dialog_button_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						setResult( Activity.RESULT_CANCELED );
						finish();
					}
				});
		builder.setNegativeButton(
				R.string.editor_exit_confirm_dialog_button_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

}
