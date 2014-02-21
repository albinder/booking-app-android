package com.tdispatch.passenger.define;

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

public final class PaymentMethod
{
	public static final int UNKNOWN 			= 0;
	public static final int CASH 				= 1;
	public static final String CASH_STRING 		= "cash";
	public static final int ACCOUNT 			= 2;
	public static final String ACCOUNT_STRING 	= "account";
	public static final int CARD				= 3;
	public static final String CARD_STRING		= "credit-card";
}
