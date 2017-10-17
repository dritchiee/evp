/*
 * Copyright (C) 2012 Tobias Brunner
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

package pl.enterprise.vpn.client.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import pl.enterprise.vpn.client.data.VpnProfile;

public class VpnProfileSelectActivity extends AppCompatActivity implements VpnProfileListFragment.OnVpnProfileSelectedListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(pl.enterprise.vpn.client.R.layout.vpn_profile_select);

		/* we should probably return a result also if the user clicks the back
		 * button before selecting a profile */
		setResult(RESULT_CANCELED);
	}

	@Override
	public void onVpnProfileSelected(VpnProfile profile)
	{
		Intent shortcut = new Intent(MainActivity.START_PROFILE);
		shortcut.putExtra(MainActivity.EXTRA_VPN_PROFILE_ID, profile.getId());

		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcut);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, profile.getName());
//		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher));
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, pl.enterprise.vpn.client.R.mipmap.test_launcher));
		setResult(RESULT_OK, intent);
		finish();
	}
}
