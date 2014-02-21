package com.tdispatch.passenger.api;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.entity.mime.FormBodyPart;
import ch.boye.httpclientandroidlib.entity.mime.HttpMultipartMode;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntity;
import ch.boye.httpclientandroidlib.entity.mime.content.StringBody;

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

final public class ApiRequest
{
	protected String mUrl = "";
	protected JSONObject mGetParams = new JSONObject();
	protected JSONObject mPostParams = new JSONObject();
	protected JSONObject mRequestParams = null;
	protected String mAccessToken = null;
	protected Boolean mRequestBuilt = false;

	public ApiRequest() {
		// dummy
	}
	public ApiRequest( String url ) {
		setUrl( url );
	}
	public ApiRequest( String url, String accessToken ) {
		setUrl( url );
		setAccessToken(accessToken);
	}

	public void setUrl( String url ) {
		mUrl = url;
	}
	public String getUrl() {
		return mUrl;
	}

	public void setAccessToken( String accessToken ) {
		mAccessToken = accessToken;
	}

	public void addGetParam( String key, Object value ) {
		try {
			mGetParams.put( key, (value==null) ? JSONObject.NULL : value );
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	public void setRequestParameters( JSONObject json ) {
		mRequestParams = json;
	}
	public JSONObject getRequestParameters() {
		return mRequestParams;
	}
	public void addRequestParam( String key, Object value ) {
		if( mRequestParams == null ) {
			mRequestParams = new JSONObject();
		}

		try {
			mRequestParams.put( key, (value==null) ? JSONObject.NULL : value );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	public void addRequestParam( String key, Object[] val ) {
		if( mRequestParams == null ) {
			mRequestParams = new JSONObject();
		}

		JSONArray array = new JSONArray();
		for( Object o : val ) {
			array.put(o);
		}

		try {
			mRequestParams.put( key, array );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	public Boolean sendPostAsJson() {
		return (mRequestParams != null);
	}



	public void addPostParam( String key, Object value ) {
		try {
			mPostParams.put( key, (value==null) ? JSONObject.NULL : value );
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	public JSONObject getPostParam() {
		return mPostParams;
	}


	public ApiRequest buildRequest() {

		if( mRequestBuilt == false ) {

			try {
				String url = getUrl();

				if( url.substring( url.length()-1).equals("?") == false ) {
					url += "?";
				}

				String separator = "";

				if( mAccessToken != null ) {
					url += "access_token=" + URLEncoder.encode( mAccessToken, "UTF-8" );
					separator = "&";
				}

				Iterator<String> iter = mGetParams.keys();
			    while (iter.hasNext()) {
			        String key = iter.next();
			        try {
						url += separator + URLEncoder.encode( key, "UTF-8" ) + "=" + URLEncoder.encode( mGetParams.get(key).toString(), "UTF-8" );
						separator = "&";
			        } catch (Exception e) {
			        	e.printStackTrace();
			        }
			    }

				setUrl( url );

				mRequestBuilt = true;
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}

		return this;
	}


	public MultipartEntity getMultipartEntitiyWithPostParams() {
		MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE);

		Iterator<String> iter = mPostParams.keys();
	    while (iter.hasNext()) {
	        String key = iter.next();
	        try {
				entity.addPart( new FormBodyPart( key, new StringBody(mPostParams.get(key).toString(), Charset.forName("UTF-8")) ) );
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
	    }

		return entity;
	}


	public StringEntity getStringEntityWithRequestParams() {

		StringEntity entity = null;
		try {
			entity = new StringEntity("{}");

			if( mRequestParams != null ) {
				entity = new StringEntity(mRequestParams.toString(), "UTF-8");
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		return entity;
	}

}
