package com.development.ian.nfc_ian;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class set_gac_options extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_gac_options);

        setCheckBoxes();
        Button b=(Button)findViewById(R.id.buttonGacOK);
        b.setOnClickListener(this);
        b=(Button)findViewById(R.id.buttonGacCancel);
        b.setOnClickListener(this);
    }

    private void setCheckBoxes() {
        int gp1=0x50;
        try
        {
               gp1=Integer.parseInt(getIntent().getStringExtra("gacp1"),16);
        }catch(Exception e){};


        if((gp1 < 0) || (gp1 > 255)) gp1=0x50;
        if((gp1 & 0x2F)!=0) gp1=0x50;

        if((gp1&0x80) !=0)
        {
            ((RadioButton)findViewById(R.id.arqc)).setChecked(true);
        }
        else
        {
            if((gp1&0x40)!=0)
            {
                ((RadioButton)findViewById(R.id.tc)).setChecked(true);
            }
            else
            {
                ((RadioButton)findViewById(R.id.aac)).setChecked(true);
            }
        }

        if((gp1&0x10)!=0)
            ((CheckBox)findViewById(R.id.cdaSignatureRequestedRadioButton)).setChecked(true);
        else
            ((CheckBox)findViewById(R.id.cdaSignatureRequestedRadioButton)).setChecked(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.buttonGacOK:
                int gp1=0;
                int rbid=((RadioGroup)findViewById(R.id.certif2genradiogroup)).getCheckedRadioButtonId();
                if(rbid==R.id.tc)
                    gp1=0x40;
                else if(rbid==R.id.aac)
                    gp1=0x00;
                else if(rbid==R.id.arqc)
                    gp1=0x80;

                if(((CheckBox)findViewById(R.id.cdaSignatureRequestedRadioButton)).isChecked())
                    gp1=gp1+0x10;

                Intent i=new Intent();
                i.putExtra("gacp1",String.format("%02X",+gp1));
                setResult(RESULT_OK,i);
                finish();
                break;
            case R.id.buttonGacCancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }
}
