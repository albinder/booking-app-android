package com.tdispatch.passenger.tools;

import android.content.Context;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.iface.ConfigReaderInterface;

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

public final class ConfigReader implements ConfigReaderInterface
{
	protected Context mContext = TDApplication.getAppContext();

	@Override
	public String getString(String key) {
		String result = null;

		try {
			int id = ((R.string.class).getField(key)).getInt(null);
			result = mContext.getResources().getString( id );
		} catch ( Exception e ) {
			throw new RuntimeException("String value for key '" + key + "' not found.");
		}

		return result;
	}

	@Override
	public Boolean getBoolean(String key) {
		Boolean result = null;

		try {
			int id = ((R.bool.class).getField(key)).getInt(null);
			result = mContext.getResources().getBoolean( id );
		} catch ( Exception e ) {
			throw new RuntimeException("Boolean value for key '" + key + "' not found.");
		}

		return result;
	}

	@Override
	public Integer getInteger(String key) {
		Integer result = null;

		try {
			int id = ((R.integer.class).getField(key)).getInt(null);
			result = mContext.getResources().getInteger( id );
		} catch ( Exception e ) {
			throw new RuntimeException("Integer value for key '" + key + "' not found.");
		}

		return result;
	}

	@Override
	public Double getDouble(String key) {
		Double result = null;

		try {
			int id = ((R.dimen.class).getField(key)).getInt(null);
			result = Double.valueOf(mContext.getResources().getDimension( id ));
		} catch ( Exception e ) {
			throw new RuntimeException("Double value for key '" + key + "' not found.");
		}

		return result;
	}



}