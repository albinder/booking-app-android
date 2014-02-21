package com.tdispatch.passenger.model;

import org.json.JSONObject;

import com.tdispatch.passenger.core.TDApplication;
import com.webnetmobile.tools.JsonTools;

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

public class OfficeData
{
	protected String mName;
	protected String mEmail;
	protected String mPhone;

	// -------------------------------------------------------------------------------------------------------------

	public OfficeData() {
		// dummy
	}

	public OfficeData(String name, String phone, String email) {
		mName = name;
		mPhone = phone;
		mEmail = email;
	}

	// -------------------------------------------------------------------------------------------------------------

	public OfficeData setName( String name ) {
		mName = name;
		put();
		return this;
	}

	public String getName() {
		return mName;
	}

	public OfficeData setEmail( String email ) {
		mEmail = email;
		put();
		return this;
	}

	public String getEmail() {
		return mEmail;
	}

	public OfficeData setPhone( String phone ) {
		mPhone = phone;
		put();
		return this;
	}

	public String getPhone() {
		return mPhone;
	}


	// -------------------------------------------------------------------------------------------------------------

	public Boolean hasEmail() {
		return ( (mEmail != null) && (mEmail.trim().length() > 0) );
	}
	public Boolean hasPhone() {
		return ( (mPhone != null) && (mPhone.trim().length() > 0) );
	}

	// -------------------------------------------------------------------------------------------------------------

	public OfficeData set( JSONObject json ) {

		setName(JsonTools.getString(json, "name"));
		setEmail(JsonTools.getString(json, "email"));
		setPhone(JsonTools.getString(json, "phone"));

		put();

		return this;
	}

	// storage

	public static OfficeData get() {
		return TDApplication.getOfficeManager().get();
	}

	public OfficeData put() {
		TDApplication.getOfficeManager().put(this);
		return this;
	}

}
