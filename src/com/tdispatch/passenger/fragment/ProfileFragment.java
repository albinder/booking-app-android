package com.tdispatch.passenger.fragment;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.model.AccountData;
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

public class ProfileFragment extends TDFragment
{
	@Override
	protected int getLayoutId() {
		return R.layout.profile_fragment;
	}

	@Override
	protected void onPostCreateView() {

		AccountData ad = TDApplication.getSessionManager().getAccountData();

		if( ad != null ) {
			String name = ad.getFullName();

			String phone = ad.getPhone();
			if( phone.length() == 0 ) {
				phone = "---";
			}

			String email = ad.getEmail();
			if( email.length() == 0 ) {
				email = "---";
			}

			WebnetTools.setText( mFragmentView, R.id.profile_name, name );
			WebnetTools.setText( mFragmentView, R.id.profile_phone, phone );
			WebnetTools.setText( mFragmentView, R.id.profile_email, email );

		}
	}
}
