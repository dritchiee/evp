package org.strongswan.android.ui;

import android.app.ActivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import org.strongswan.android.R;
import org.strongswan.android.data.Prefs;

import static org.strongswan.android.logic.ManagedConfigurationContract.Controller.ALLOW_CLEAR_APP_DATA;

/**
 * - Created by akubinski on 09.10.17.
 */

public class ManageSpaceActivity extends AppCompatActivity {

    private Button clearButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_space);
        clearButton = (Button) findViewById(R.id.clear_app_data_button);

        clearButton.setOnClickListener(view -> clearAppData());
    }

    @Override
    protected void onResume() {
        super.onResume();
        clearButton.setEnabled(Prefs.get(ALLOW_CLEAR_APP_DATA, true));
    }

    private void clearAppData() {
        ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
    }
}
