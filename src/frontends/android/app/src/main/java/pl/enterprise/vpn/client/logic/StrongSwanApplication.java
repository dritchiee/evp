/*
 * Copyright (C) 2014 Tobias Brunner
 * Hochschule fuer Technik Rapperswil
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.  See <http://www.fsf.org/copyleft/gpl.txt>.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 */

package pl.enterprise.vpn.client.logic;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import pl.enterprise.vpn.client.data.Prefs;
import pl.enterprise.vpn.client.security.LocalCertificateKeyStoreProvider;

import java.security.Security;

public class StrongSwanApplication extends Application
{
	private static Context mContext;

	static {
		Security.addProvider(new LocalCertificateKeyStoreProvider());
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		StrongSwanApplication.mContext = getApplicationContext();
		Prefs.init(mContext);
		startService(new Intent(mContext, ManagedConfigurationService.class));
	}

	/**
	 * Returns the current application context
	 * @return context
	 */
	public static Context getContext()
	{
		return StrongSwanApplication.mContext;
	}
}
