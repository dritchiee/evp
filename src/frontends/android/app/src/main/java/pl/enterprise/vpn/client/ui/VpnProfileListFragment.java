/*
 * Copyright (C) 2012 Tobias Brunner
 * Copyright (C) 2012 Giuliano Grassi
 * Copyright (C) 2012 Ralf Sager
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import pl.enterprise.vpn.client.R;
import pl.enterprise.vpn.client.data.Prefs;
import pl.enterprise.vpn.client.data.VpnProfile;
import pl.enterprise.vpn.client.data.VpnProfileDataSource;
import pl.enterprise.vpn.client.ui.adapter.VpnProfileAdapter;
import pl.enterprise.vpn.client.utils.Constants;

import static pl.enterprise.vpn.client.logic.ManagedConfigurationContract.Controller.ALLOW_MODIFY_VPN_PROFILE;

public class VpnProfileListFragment extends Fragment
{
	private static final int ADD_REQUEST = 1;
	private static final int EDIT_REQUEST = 2;

	private List<VpnProfile>             mVpnProfiles;
	private VpnProfileDataSource         mDataSource;
	private VpnProfileAdapter            mListAdapter;
	private ListView                     mListView;
	private OnVpnProfileSelectedListener mListener;
	private boolean                      mReadOnly;

	private BroadcastReceiver mProfilesChanged = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			long id, ids[];
			if ((id = intent.getLongExtra(Constants.VPN_PROFILES_SINGLE, 0)) > 0)
			{
				VpnProfile profile = mDataSource.getVpnProfile(id);
				if (profile != null)
				{	/* in case this was an edit, we remove it first */
					mVpnProfiles.remove(profile);
					mVpnProfiles.add(profile);
					mListAdapter.notifyDataSetChanged();
				}
			}
			else if ((ids = intent.getLongArrayExtra(Constants.VPN_PROFILES_MULTIPLE)) != null)
			{
				for (long i : ids)
				{
					Iterator<VpnProfile> profiles = mVpnProfiles.iterator();
					while (profiles.hasNext())
					{
						VpnProfile profile = profiles.next();
						if (profile.getId() == i)
						{
							profiles.remove();
							break;
						}
					}
				}
				mListAdapter.notifyDataSetChanged();
			}
		}
	};
	private ActionMenuShowListener actionMenuListener;

	/**
	 * The activity containing this fragment should implement this interface
	 */
	public interface OnVpnProfileSelectedListener {
		void onVpnProfileSelected(VpnProfile profile);
	}

	public interface ActionMenuShowListener {
		void onActionMenuCreate();
		void onActionMenuDestroy();
	}


	@Override
	public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState)
	{
		super.onInflate(context, attrs, savedInstanceState);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Fragment);
		mReadOnly = a.getBoolean(R.styleable.Fragment_read_only, false);
		a.recycle();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.profile_list_fragment, null);

		mListView = (ListView)view.findViewById(R.id.profile_list);
		mListView.setAdapter(mListAdapter);
		mListView.setEmptyView(view.findViewById(R.id.profile_list_empty));
		mListView.setOnItemClickListener(mVpnProfileClicked);

		if (!mReadOnly)
		{
			mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			mListView.setMultiChoiceModeListener(mVpnProfileSelected);
		}
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null)
		{
			mReadOnly = args.getBoolean("read_only", mReadOnly);
		}

		mReadOnly = !Prefs.get(ALLOW_MODIFY_VPN_PROFILE, true);

		if (!mReadOnly)
		{
			setHasOptionsMenu(true);
		}

		mDataSource = new VpnProfileDataSource(this.getActivity());
		mDataSource.open();

		/* cached list of profiles used as backend for the ListView */
		mVpnProfiles = mDataSource.getAllVpnProfiles();

		mListAdapter = new VpnProfileAdapter(getActivity(), R.layout.profile_list_item, mVpnProfiles);

		IntentFilter profileChangesFilter = new IntentFilter(Constants.VPN_PROFILES_CHANGED);
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mProfilesChanged, profileChangesFilter);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		mDataSource.close();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mProfilesChanged);
	}

	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);

		if (context instanceof OnVpnProfileSelectedListener)
		{
			mListener = (OnVpnProfileSelectedListener)context;
		}

		if (context instanceof ActionMenuShowListener) {
			actionMenuListener = (ActionMenuShowListener) context;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.profile_list, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.add_profile:
				Intent connectionIntent = new Intent(getActivity(),
													 VpnProfileDetailActivity.class);
				startActivityForResult(connectionIntent, ADD_REQUEST);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private final OnItemClickListener mVpnProfileClicked = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> a, View v, int position, long id)
		{
			if (mListener != null)
			{
				mListener.onVpnProfileSelected((VpnProfile)a.getItemAtPosition(position));
			}
		}
	};

	private final MultiChoiceModeListener mVpnProfileSelected = new MultiChoiceModeListener() {
		private HashSet<Integer> mSelected;
		private MenuItem mEditProfile;

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu)
		{
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode)
		{
			actionMenuListener.onActionMenuDestroy();
			for (Integer integer : mSelected) {
				mListView.getChildAt(integer).setBackgroundColor(getResources().getColor(R.color.white));
			}
			mSelected.clear();
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu)
		{
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.profile_list_context, menu);
			mEditProfile = menu.findItem(R.id.edit_profile);
			mSelected = new HashSet<>();
			mode.setTitle(R.string.select_profiles);
			actionMenuListener.onActionMenuCreate();
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item)
		{
			switch (item.getItemId())
			{
				case R.id.edit_profile:
				{
					int position = mSelected.iterator().next();
					VpnProfile profile = (VpnProfile)mListView.getItemAtPosition(position);
					Intent connectionIntent = new Intent(getActivity(), VpnProfileDetailActivity.class);
					connectionIntent.putExtra(VpnProfileDataSource.KEY_ID, profile.getId());
					startActivityForResult(connectionIntent, EDIT_REQUEST);
					break;
				}
				case R.id.delete_profile:
				{
					ArrayList<VpnProfile> profiles = new ArrayList<>();
					for (int position : mSelected)
					{
						profiles.add((VpnProfile)mListView.getItemAtPosition(position));
					}
					long ids[] = new long[profiles.size()];
					for (int i = 0; i < profiles.size(); i++)
					{
						VpnProfile profile = profiles.get(i);
						ids[i] = profile.getId();
						mDataSource.deleteVpnProfile(profile);
					}
					Intent intent = new Intent(Constants.VPN_PROFILES_CHANGED);
					intent.putExtra(Constants.VPN_PROFILES_MULTIPLE, ids);
					LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
					Toast.makeText(VpnProfileListFragment.this.getActivity(),
								   R.string.profiles_deleted, Toast.LENGTH_SHORT).show();
					break;
				}
				default:
					return false;
			}
			mode.finish();
			return true;
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
											  long id, boolean checked)
		{
			if (checked)
			{
				mSelected.add(position);
				mListView.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.text_gray));
			}
			else
			{
				mSelected.remove(position);
				mListView.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.white));
			}
			final int checkedCount = mSelected.size();
			mEditProfile.setEnabled(checkedCount == 1);
			switch (checkedCount)
			{
				case 0:
					mode.setSubtitle(R.string.no_profile_selected);
					break;
				case 1:
					mode.setSubtitle(R.string.one_profile_selected);
					break;
				default:
					mode.setSubtitle(String.format(getString(R.string.x_profiles_selected), checkedCount));
					break;
			}
		}
	};
}
