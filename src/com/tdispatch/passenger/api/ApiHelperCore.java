package com.tdispatch.passenger.api;

import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.define.ErrorCode;
import com.webnetmobile.tools.WebnetLog;

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

public abstract class ApiHelperCore
{
	protected static TDApplication mApplication = null;

	protected int lastErrorCode = ErrorCode.OK;
	protected String lastErrorMessage = "";

	public ApiHelperCore() {
		// dummy
	}

	public ApiHelperCore(TDApplication app) {
		super();
		setApplication(app);
	}

	public void setApplication( TDApplication app ) {
		mApplication = app;
	}

	public int getLastErrorCode() {
		return lastErrorCode;
	}

	public void setLastErrorCode( int lastErrorCode ) {
		this.lastErrorCode = lastErrorCode;
	}

	public String getLastErrorMessage() {
		return lastErrorMessage;
	}

	public void setLastErrorMessage( String lastErrorMessage ) {
		this.lastErrorMessage = lastErrorMessage;
	}

	protected void cleanErrors() {
		setLastErrorCode(ErrorCode.OK);
		setLastErrorMessage("");
	}

	protected final static int TYPE_GET = 0;
	protected final static int TYPE_POST = 1;

	protected ApiResponse doGetRequest( ApiRequest req ) {
		return doRequest(TYPE_GET, req);
	}

	protected ApiResponse doPostRequest( ApiRequest req ) {
		return doRequest(TYPE_POST, req );
	}

	protected ApiResponse doRequest( int type, ApiRequest request ) {

		ApiNetworker apiNetworker = ApiNetworker.getInstance(mApplication);
		ApiResponse response = new ApiResponse();

		request.buildRequest();

		switch( type ){
			case TYPE_GET:
				response = apiNetworker.sendGet(request);
				break;

			case TYPE_POST:
				response = apiNetworker.sendPost(request);
				break;
		}

		WebnetLog.i("response: " + response.getErrorCode());
		return response;
	}

}
