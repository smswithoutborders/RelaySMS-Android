package com.example.sw0b_001;



import static com.example.sw0b_001.R.id.new_gateway_toolbar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.sw0b_001.Models.AppCompactActivityRtlEnabled;
import com.example.sw0b_001.Models.GatewayClients.GatewayClient;
import com.example.sw0b_001.Models.GatewayClients.GatewayClientsHandler;
import com.example.sw0b_001.SettingsActivities.GatewayClientsSettingsActivity;
import com.google.android.material.textfield.TextInputEditText;

public class AddNewGatewayActivity extends AppCompactActivityRtlEnabled {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_gateway);

        Toolbar addNewGateway = findViewById(new_gateway_toolbar);
        setSupportActionBar(addNewGateway);

//         Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
    }


    public void onAddNewGatewayClient(View view ) throws InterruptedException {
        TextInputEditText gatewayClientNumberText = findViewById(R.id.new_gateway_client_text_input);
        String newGatewayClientNumber = gatewayClientNumberText.getText().toString();

        if(newGatewayClientNumber.isEmpty()) {
            gatewayClientNumberText.setError(getString(R.string.gateway_client_settings_add_custom_empty_error));
            return;
        }

        GatewayClient gatewayClient = new GatewayClient();
        gatewayClient.setType("custom");
        gatewayClient.setMSISDN(newGatewayClientNumber);

        GatewayClientsHandler.add(getApplicationContext(), gatewayClient);
        finish();
    }
    public void onContactsClick(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (1) :
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor contactCursor = getApplicationContext().getContentResolver().query(contactData, null, null, null, null);
                    if(contactCursor != null) {
                        if (contactCursor.moveToFirst()) {
                            int contactIndexInformation = contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            String number = contactCursor.getString(contactIndexInformation);

                            EditText numberEditText = findViewById(R.id.new_gateway_client_text_input);
                            numberEditText.setText(number);
                        }
                    }
                }
                break;
        }
    }
}



