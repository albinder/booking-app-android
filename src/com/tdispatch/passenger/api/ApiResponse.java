package com.tdispatch.passenger.api;

import org.json.JSONObject;

import com.tdispatch.passenger.define.ErrorCode;

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

final public class ApiResponse
{
	private int mErrorCode = ErrorCode.UNKNOWN_ERROR;
	private String mErrorMessage = "";
	private Exception mException = null;

	private int mApiErrorCode = 0; // only set if errorCode == API_ERROR
	private String mApiErrorMessage = "";

	private JSONObject mJsonObject = null;

	public void setErrorCode( int errorCode ) {
		mErrorCode = errorCode;
	}

	public int getErrorCode() {
		return (mErrorCode);
	}

	public void setErrorMessage( String errorMessage ) {
		mErrorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return mErrorMessage;
	}

	public void setException( Exception e ) {
		mException = e;
	}

	public Exception getException() {
		return mException;
	}

	public void setJSONObject( JSONObject data ) {
		mJsonObject = data;
	}

	public JSONObject getJSONObject() {
		return (mJsonObject);
	}

	public void setApiErrorCode( int code ) {
		mApiErrorCode = code;
	}

	public int getApiErrorCode() {
		return mApiErrorCode;
	}

	public void setApiErrorMessage( String msg ) {
		mApiErrorMessage = msg;
	}

	public String getApiErrorMessage() {
		return mApiErrorMessage;
	}

}