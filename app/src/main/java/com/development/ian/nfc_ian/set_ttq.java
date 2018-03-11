package com.development.ian.nfc_ian;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class set_ttq extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ttq);


        Button b = (Button) findViewById(R.id.setTTQOK);
        b.setOnClickListener(this);
        b = (Button) findViewById(R.id.setTTQCancel);
        b.setOnClickListener(this);

        setAllCheckboxes(getIntent().getStringExtra("ttq"));
    }

    private void setAllCheckboxes(String ttq) {
        clearAllCheckboxes();
        Log.d(getClass().getName(), "Settings tags for TTQ " + ttq);
        byte[] ttqbytes = Hex.decode(ttq);

        if (ttqbytes == null || ttqbytes.length != 4) return;

        if ((ttqbytes[0] & 0x80) != 0)            ((CheckBox) findViewById(R.id.msd_not_supported)).setChecked(true);
        if ((ttqbytes[0] & 0x20) != 0)            ((CheckBox) findViewById(R.id.qvsdc_supported)).setChecked(true);
        if ((ttqbytes[0] & 0x10) != 0)            ((CheckBox) findViewById(R.id.emv_contact_supported)).setChecked(true);
        if ((ttqbytes[0] & 0x08) != 0)            ((CheckBox) findViewById(R.id.online_capable_reader)).setChecked(true);
        if ((ttqbytes[0] & 0x04) != 0)            ((CheckBox) findViewById(R.id.online_pin_supported)).setChecked(true);
        if ((ttqbytes[0] & 0x02) != 0)            ((CheckBox) findViewById(R.id.signature_supported)).setChecked(true);
        if ((ttqbytes[0] & 0x01) != 0)            ((CheckBox) findViewById(R.id.oda_not_supported)).setChecked(true);
        if ((ttqbytes[1] & 0x80) != 0)            ((CheckBox) findViewById(R.id.online_cryptogram_required)).setChecked(true);
        if ((ttqbytes[1] & 0x40) != 0)            ((CheckBox) findViewById(R.id.cvm_not_required)).setChecked(true);
        if ((ttqbytes[1] & 0x20) != 0)            ((CheckBox) findViewById(R.id.contact_pin_not_supported)).setChecked(true);
        if ((ttqbytes[2] & 0x80) != 0)            ((CheckBox) findViewById(R.id.issuer_update_processing_not_supported)).setChecked(true);
        if ((ttqbytes[2] & 0x40) != 0)            ((CheckBox) findViewById(R.id.mobile_functionality_supported)).setChecked(true);

    }

    private void clearAllCheckboxes() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.ttqLinearLayout);
        for (int i = 0; i < ll.getChildCount(); i++) {
            View v = (View) ll.getChildAt(i);
            if (v instanceof CheckBox) {
                ((CheckBox) v).setChecked(false);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setTTQOK:
                String s = buildTTQString();
                Intent i = new Intent();
                i.putExtra("ttq", s);
                setResult(RESULT_OK, i);
                finish();
                break;
            case R.id.setTTQCancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
            default:

        }
    }

    private String buildTTQString() {
        boolean b;
        byte[] ttqbytes=new byte[4];

        b=((CheckBox)findViewById(R.id.msd_not_supported)).isChecked();if(b) ttqbytes[0]|=0x80;
        b=((CheckBox)findViewById(R.id.qvsdc_supported)).isChecked();if(b) ttqbytes[0]|=0x20;
        b=((CheckBox)findViewById(R.id.emv_contact_supported)).isChecked();if(b) ttqbytes[0]|=0x10;
        b=((CheckBox)findViewById(R.id.online_capable_reader)).isChecked();if(b) ttqbytes[0]|=0x08;
        b=((CheckBox)findViewById(R.id.online_pin_supported)).isChecked();if(b) ttqbytes[0]|=0x04;
        b=((CheckBox)findViewById(R.id.signature_supported)).isChecked();if(b) ttqbytes[0]|=0x02;
        b=((CheckBox)findViewById(R.id.oda_not_supported)).isChecked();if(b) ttqbytes[0]|=0x01;
        b=((CheckBox)findViewById(R.id.online_cryptogram_required)).isChecked();if(b) ttqbytes[1]|=0x80;
        b=((CheckBox)findViewById(R.id.cvm_not_required)).isChecked();if(b) ttqbytes[1]|=0x40;
        b=((CheckBox)findViewById(R.id.contact_pin_not_supported)).isChecked();if(b) ttqbytes[1]|=0x20;
        b=((CheckBox)findViewById(R.id.issuer_update_processing_not_supported)).isChecked();if(b) ttqbytes[2]|=0x80;
        b=((CheckBox)findViewById(R.id.mobile_functionality_supported)).isChecked();if(b) ttqbytes[2]|=0x40;


        return Hex.encode(ttqbytes,0,4);
    }
}