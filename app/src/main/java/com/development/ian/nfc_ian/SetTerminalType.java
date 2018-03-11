package com.development.ian.nfc_ian;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Random;


public class SetTerminalType extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_set_terminal_type);

        updateUI(getIntent().getStringExtra("terminalType")); // sets checkboxes according to terminaltype string given

        Button b=(Button)findViewById(R.id.setTerminalTypeOK);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data=new Intent();
                data.putExtra("terminalType",getTerminalType());
                Log.d(getClass().getName(),"terminalType set to "+getTerminalType());
                setResult(RESULT_OK,data);
                finish();
            }
        });

        b=(Button)findViewById(R.id.setTerminalTypeCancel);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                setResult(RESULT_CANCELED);
                finish();
            }
        });


    }

    /** Computes terminal type value from radiobuttons
     *
     * @return
     */
    private String getTerminalType()
    {
       String ret="23";

       if(((RadioButton)findViewById(R.id.bankOperated)).isChecked())
       {
           if(((RadioButton)findViewById(R.id.attended)).isChecked())
           { // bankOperated+attended
                if(((RadioButton)findViewById(R.id.online)).isChecked())
                {
                    ret="11";
                }
                else if(((RadioButton)findViewById(R.id.semionline)).isChecked())
                {
                    ret="12";
                }
                else
                {
                    ret="13";
                }
           }
           else
           { // bankOperated+unattended
               if(((RadioButton)findViewById(R.id.online)).isChecked())
               {
                   ret="14";
               }
               else if(((RadioButton)findViewById(R.id.semionline)).isChecked())
               {
                   ret="15";
               }
               else
               {
                   ret="16";
               }
           }
       }
       else if(((RadioButton)findViewById(R.id.merchantOperated)).isChecked())
       {
           if(((RadioButton)findViewById(R.id.attended)).isChecked())
           { // merchantOperated+attended
               if(((RadioButton)findViewById(R.id.online)).isChecked())
               {
                   ret="21";
               }
               else if(((RadioButton)findViewById(R.id.semionline)).isChecked())
               {
                   ret="22";
               }
               else
               {
                   ret="23";
               }
           }
           else
           { // merchantOperated+unattended
               if(((RadioButton)findViewById(R.id.online)).isChecked())
               {
                   ret="24";
               }
               else if(((RadioButton)findViewById(R.id.semionline)).isChecked())
               {
                   ret="25";
               }
               else
               {
                   ret="26";
               }
           }
       }
       else
       { // cardholder operated
           if(((RadioButton)findViewById(R.id.attended)).isChecked())
           { // cardholderOperated+attended
                Log.i(getClass().getName(),"User selected an impossible combination in UI - defaulting");
           }
           else
           { // cardholderOperated+unattended
               if(((RadioButton)findViewById(R.id.online)).isChecked())
               {
                   ret="34";
               }
               else if(((RadioButton)findViewById(R.id.semionline)).isChecked())
               {
                   ret="35";
               }
               else
               {
                   ret="36";
               }
           }
       }
       return ret;
    }
    /**
     * Set checkboxes correctly according to terminalType
     * @param terminalType
     */
    private void updateUI(String terminalType) {
        byte[] byteArray = Hex.decode(terminalType);
        int i;
        if (byteArray == null || byteArray.length != 1)
            i = 0x23; // default value
        else
            i = byteArray[0];

        // Operational control
        switch (i) {
            case 0x11:
            case 0x12:
            case 0x13:
            case 0x14:
            case 0x15:
            case 0x16:
                ((RadioButton) findViewById(R.id.bankOperated)).setChecked(true);
                break;
            case 0x34:
            case 0x35:
            case 0x36:
                ((RadioButton) findViewById(R.id.cardholderOperated)).setChecked(true);
                break;
            default:
                ((RadioButton) findViewById(R.id.merchantOperated)).setChecked(true);
                break;
        }

        // Attended or not
        switch(i)
        {
            case 0x14:
            case 0x15:
            case 0x16:
            case 0x24:
            case 0x25:
            case 0x26:
            case 0x34:
            case 0x35:
            case 0x36:
                ((RadioButton)findViewById(R.id.unattended)).setChecked(true);
                break;
            default:
                ((RadioButton)findViewById(R.id.attended)).setChecked(true);
                break;
        }

        // Online/offline
        switch(i) {
            case 0x11:
            case 0x21:
            case 0x14:
            case 0x24:
            case 0x34:
                ((RadioButton)findViewById(R.id.online)).setChecked(true);
                break;
            case 0x12:
            case 0x22:
            case 0x15:
            case 0x25:
            case 0x35:
                ((RadioButton)findViewById(R.id.semionline)).setChecked(true);
                break;
            default:
                ((RadioButton)findViewById(R.id.offline)).setChecked(true);
        }
    }
}
