package com.development.ian.nfc_ian;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class set_tvr extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_tvr);

        Button b=(Button)findViewById(R.id.setTvrOK);
        b.setOnClickListener(this);
        b=(Button)findViewById(R.id.setTvrCancel);
        b.setOnClickListener(this);

        setAllCheckboxes(getIntent().getStringExtra("tvr"));
    }

    private void setAllCheckboxes(String tvr) {
        clearAllCheckboxes();
        Log.d(getClass().getName(),"Settings tags for TVR "+tvr);
        byte[] tvrbytes=Hex.decode(tvr);
        if(tvrbytes==null||tvrbytes.length!=5) return;

        if((tvrbytes[0]&0x80)!=0) ((CheckBox)findViewById(R.id.offline_da_notperformed)).setChecked(true);
        if((tvrbytes[0]&0x40)!=0) ((CheckBox)findViewById(R.id.sda_failed)).setChecked(true);
        if((tvrbytes[0]&0x20)!=0) ((CheckBox)findViewById(R.id.icc_data_missing)).setChecked(true);
        if((tvrbytes[0]&0x10)!=0) ((CheckBox)findViewById(R.id.card_blacklisted)).setChecked(true);
        if((tvrbytes[0]&0x08)!=0) ((CheckBox)findViewById(R.id.dda_failed)).setChecked(true);
        if((tvrbytes[0]&0x04)!=0) ((CheckBox)findViewById(R.id.cda_failed)).setChecked(true);

        if((tvrbytes[1]&0x80)!=0) ((CheckBox)findViewById(R.id.icc_terminal_appversion_mismatch)).setChecked(true);
        if((tvrbytes[1]&0x40)!=0) ((CheckBox)findViewById(R.id.expired_application)).setChecked(true);
        if((tvrbytes[1]&0x20)!=0) ((CheckBox)findViewById(R.id.application_not_yet_effective)).setChecked(true);
        if((tvrbytes[1]&0x10)!=0) ((CheckBox)findViewById(R.id.requested_service_not_allowed)).setChecked(true);
        if((tvrbytes[1]&0x08)!=0) ((CheckBox)findViewById(R.id.new_card)).setChecked(true);

        if((tvrbytes[2]&0x80)!=0) ((CheckBox)findViewById(R.id.cvm_failed)).setChecked(true);
        if((tvrbytes[2]&0x40)!=0) ((CheckBox)findViewById(R.id.unknown_cvm)).setChecked(true);
        if((tvrbytes[2]&0x20)!=0) ((CheckBox)findViewById(R.id.pin_try_limit_exceeded)).setChecked(true);
        if((tvrbytes[2]&0x10)!=0) ((CheckBox)findViewById(R.id.pinpad_not_present_not_working)).setChecked(true);
        if((tvrbytes[2]&0x08)!=0) ((CheckBox)findViewById(R.id.pin_not_entered)).setChecked(true);
        if((tvrbytes[2]&0x04)!=0) ((CheckBox)findViewById(R.id.online_pin_entered)).setChecked(true);

        if((tvrbytes[3]&0x80)!=0) ((CheckBox)findViewById(R.id.transaction_floor_limit_exceeded)).setChecked(true);
        if((tvrbytes[3]&0x40)!=0) ((CheckBox)findViewById(R.id.lower_consecutive_offline_count_reached)).setChecked(true);
        if((tvrbytes[3]&0x20)!=0) ((CheckBox)findViewById(R.id.upper_consecutive_offline_count_reached)).setChecked(true);
        if((tvrbytes[3]&0x10)!=0) ((CheckBox)findViewById(R.id.transaction_selected_randomly)).setChecked(true);
        if((tvrbytes[3]&0x08)!=0) ((CheckBox)findViewById(R.id.merchant_forced_online)).setChecked(true);

        if((tvrbytes[4]&0x80)!=0) ((CheckBox)findViewById(R.id.default_tdol_used)).setChecked(true);
        if((tvrbytes[4]&0x40)!=0) ((CheckBox)findViewById(R.id.issuer_authentication_failed)).setChecked(true);
        if((tvrbytes[4]&0x20)!=0) ((CheckBox)findViewById(R.id.script_failed_before_gac)).setChecked(true);
        if((tvrbytes[4]&0x10)!=0) ((CheckBox)findViewById(R.id.script_failed_after_gac)).setChecked(true);


    }

    private void clearAllCheckboxes() {
        LinearLayout ll=(LinearLayout)findViewById(R.id.tvrLinearLayout);
        for(int i=0;i<ll.getChildCount();i++) {
            View v = (View) ll.getChildAt(i);
            if(v instanceof CheckBox)
            {
                ((CheckBox)v).setChecked(false);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.setTvrOK:
                String s=buildTvrString();
                Intent i=new Intent();
                i.putExtra("tvr",s);
                setResult(RESULT_OK,i);
                finish();
                break;
            case R.id.setTvrCancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
            default:
                break;
        }
    }

    private String buildTvrString() {
        boolean b;
        byte[] tvrbytes=new byte[5];

        b=((CheckBox)findViewById(R.id.offline_da_notperformed)).isChecked(); if(b) tvrbytes[0]|=0x80;
        b=((CheckBox)findViewById(R.id.sda_failed)).isChecked(); if(b) tvrbytes[0]|=0x40;
        b=((CheckBox)findViewById(R.id.icc_data_missing)).isChecked(); if(b) tvrbytes[0]|=0x20;
        b=((CheckBox)findViewById(R.id.card_blacklisted)).isChecked(); if(b) tvrbytes[0]|=0x10;
        b=((CheckBox)findViewById(R.id.dda_failed)).isChecked(); if(b) tvrbytes[0]|=0x08;
        b=((CheckBox)findViewById(R.id.cda_failed)).isChecked(); if(b) tvrbytes[0]|=0x04;
        b=((CheckBox)findViewById(R.id.icc_terminal_appversion_mismatch)).isChecked(); if(b) tvrbytes[1]|=0x80;
        b=((CheckBox)findViewById(R.id.expired_application)).isChecked(); if(b) tvrbytes[1]|=0x40;
        b=((CheckBox)findViewById(R.id.application_not_yet_effective)).isChecked(); if(b) tvrbytes[1]|=0x20;
        b=((CheckBox)findViewById(R.id.requested_service_not_allowed)).isChecked(); if(b) tvrbytes[1]|=0x10;
        b=((CheckBox)findViewById(R.id.new_card)).isChecked(); if(b) tvrbytes[1]|=0x08;
        b=((CheckBox)findViewById(R.id.cvm_failed)).isChecked(); if(b) tvrbytes[2]|=0x80;
        b=((CheckBox)findViewById(R.id.unknown_cvm)).isChecked(); if(b) tvrbytes[2]|=0x40;
        b=((CheckBox)findViewById(R.id.pin_try_limit_exceeded)).isChecked(); if(b) tvrbytes[2]|=0x20;
        b=((CheckBox)findViewById(R.id.pinpad_not_present_not_working)).isChecked(); if(b) tvrbytes[2]|=0x10;
        b=((CheckBox)findViewById(R.id.pin_not_entered)).isChecked(); if(b) tvrbytes[2]|=0x08;
        b=((CheckBox)findViewById(R.id.online_pin_entered)).isChecked(); if(b) tvrbytes[2]|=0x04;
        b=((CheckBox)findViewById(R.id.transaction_floor_limit_exceeded)).isChecked(); if(b) tvrbytes[3]|=0x80;
        b=((CheckBox)findViewById(R.id.lower_consecutive_offline_count_reached)).isChecked(); if(b) tvrbytes[3]|=0x40;
        b=((CheckBox)findViewById(R.id.upper_consecutive_offline_count_reached)).isChecked(); if(b) tvrbytes[3]|=0x20;
        b=((CheckBox)findViewById(R.id.transaction_selected_randomly)).isChecked(); if(b) tvrbytes[3]|=0x10;
        b=((CheckBox)findViewById(R.id.merchant_forced_online)).isChecked(); if(b) tvrbytes[3]|=0x08;
        b=((CheckBox)findViewById(R.id.default_tdol_used)).isChecked(); if(b) tvrbytes[4]|=0x80;
        b=((CheckBox)findViewById(R.id.issuer_authentication_failed)).isChecked(); if(b) tvrbytes[4]|=0x40;
        b=((CheckBox)findViewById(R.id.script_failed_before_gac)).isChecked(); if(b) tvrbytes[4]|=0x20;
        b=((CheckBox)findViewById(R.id.script_failed_after_gac)).isChecked(); if(b) tvrbytes[4]|=0x10;


        return Hex.encode(tvrbytes,0,5);
    }
}
