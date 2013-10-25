package com.tdispatch.passenger.model;

import java.util.ArrayList;

import org.json.JSONObject;

import android.database.Cursor;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.db.CardDbAdapter;
import com.webnetmobile.tools.JsonTools;

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
public class CardData
{
	protected Long 	mLocalId;
	protected String	mToken;
	protected String	mNumber;
	protected Boolean	mDefault = false;
	protected Boolean	mIsExpired = false;
	protected String	mCardType;
	protected String	mExpirationDate;
	protected String	mHolderName;

	// -------------------------------------------------------------------------------------------------------------

	public CardData() {
		// dummy
	}

	public CardData( Cursor c ) {
		setLocalId( c.getLong( c.getColumnIndex( CardDbAdapter.KEY_LOCAL_ID )) );
		setToken( c.getString( c.getColumnIndex( CardDbAdapter.KEY_TOKEN )) );
		setNumber( c.getString( c.getColumnIndex( CardDbAdapter.KEY_NUMBER )) );
		setDefault( c.getInt( c.getColumnIndex( CardDbAdapter.KEY_DEFAULT )) );
		setIsExpired( c.getInt( c.getColumnIndex( CardDbAdapter.KEY_EXPIRED )) );
		setCardType( c.getString( c.getColumnIndex( CardDbAdapter.KEY_CARD_TYPE )) );
		setExpirationDate( c.getString( c.getColumnIndex( CardDbAdapter.KEY_EXPIRATION_DATE )) );
		setHolderName( c.getString( c.getColumnIndex( CardDbAdapter.KEY_HOLDER_NAME )) );
	}

	public CardData( JSONObject json ) {
		set( json );
	}

	// -------------------------------------------------------------------------------------------------------------


	public CardData setLocalId(long localId) {
		mLocalId = localId;
		return this;
	}
	public long getLocalId() {
		return mLocalId;
	}

	public CardData setToken( String token ) {
		mToken = token;
		return this;
	}
	public String getToken() {
		return mToken;
	}

	public CardData setNumber( String numer ) {
		mNumber = numer;
		return this;
	}
	public String getNumber() {
		return mNumber;
	}

	public CardData setDefault( Integer value ) {
		return setDefault( (value==1) );
	}
	public CardData setDefault( Boolean value ) {
		mDefault = value;
		return this;
	}
	public Boolean isDefault() {
		return mDefault;
	}

	public CardData setIsExpired(int val) {
		return setIsExpired( (val != 0) );
	}
	public CardData setIsExpired(Boolean val) {
		mIsExpired = val;
		return this;
	}
	public Boolean isExpired() {
		return mIsExpired;
	}
	public CardData setCardType(String val) {
		mCardType = val;
		return this;
	}
	public String getCardTypeString() {
		return mCardType;
	}
	public CardData setExpirationDate(String val) {
		mExpirationDate = val;
		return this;
	}
	public String getExpirationDate() {
		return mExpirationDate;
	}
	public CardData setHolderName(String val) {
		mHolderName = val;
		return this;
	}
	public String getHolderName() {
		return mHolderName;
	}

	// -------------------------------------------------------------------------------------------------------------

	public String getDisplayLabel() {
		String label = TDApplication.getAppContext().getResources().getString( R.string.card_list_row_no_label);

		if ( ( mHolderName != null) && (!("".equals(mHolderName))) ) {
			label = getHolderName();
		}

		return label;
	}

	// -------------------------------------------------------------------------------------------------------------

	public CardData set( JSONObject json ) {

		setDefault( JsonTools.getBoolean(json, "default", false));
		setIsExpired( JsonTools.getBoolean(json, "expired", false));
		setNumber( JsonTools.getString(json, "maskedNumber"));
		setToken( JsonTools.getString(json, "token"));
		setCardType( JsonTools.getString(json,"cardType"));
		setExpirationDate( JsonTools.getString(json, "expirationDate"));
		setHolderName(JsonTools.getString(json,"cardholderName"));

		return this;
	}


	// -------------------------------------------------------------------------------------------------------------

	public static int count() {
		CardDbAdapter db = new CardDbAdapter(TDApplication.getAppContext());
		return db.count();
	}

	public static ArrayList<CardData> getAll() {
		CardDbAdapter db = new CardDbAdapter(TDApplication.getAppContext());
		return db.getAll();
	}

	public static CardData getByToken(String token) {
		CardDbAdapter db = new CardDbAdapter(TDApplication.getAppContext());
		return db.getByToken(token);
	}

	public long insert() {
		CardDbAdapter db = new CardDbAdapter(TDApplication.getAppContext());
		return db.insert(this);
	}
	public int delete() {
		CardDbAdapter db = new CardDbAdapter(TDApplication.getAppContext());
		return db.delete(getLocalId());
	}
	public static int delete(long localId) {
		CardDbAdapter db = new CardDbAdapter(TDApplication.getAppContext());
		return db.delete(localId);
	}

	public static void deleteAll() {
		CardDbAdapter db = new CardDbAdapter(TDApplication.getAppContext());
		db.deleteAll();
	}


}
