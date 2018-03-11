package com.development.ian.nfc_ian;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class set_country extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_country);

        setCheckBoxes();
        Button b=(Button)findViewById(R.id.set_country_OK);
        b.setOnClickListener(this);
        b=(Button)findViewById(R.id.set_country_Cancel);
        b.setOnClickListener(this);
    }

    /**
     *
     * @return country code selected (e.g. 0250 for France)
     */
    private String getSelectedCountry()
    {
        String ret="0250";

        switch(((RadioGroup)findViewById(R.id.CountryRadioGroup)).getCheckedRadioButtonId())
        {
            case R.id.France:
                ret="0250";
                break;
            case R.id.Belgium:
                ret="0056";
                break;
            case R.id.Germany:
                ret="0276";
                break;
            case R.id.UK:
                ret="0826";
                break;
            case R.id.Netherlands:
                ret="0528";
                break;
            case R.id.USA:
                ret="0840";
                break;
            case R.id.Switserland:
                ret="0756";
                break;
            default:
                break;
        }
        return ret;
    }

    private void setCheckBoxes()
    {

        switch(getIntent().getStringExtra("country"))
        {
            case "0250":
                ((RadioButton)findViewById(R.id.France)).setChecked(true);
                break;
            case "0056":
                ((RadioButton)findViewById(R.id.Belgium)).setChecked(true);
                break;
            case "0276":
                ((RadioButton)findViewById(R.id.Germany)).setChecked(true);
                break;
            case "0826":
                ((RadioButton)findViewById(R.id.UK)).setChecked(true);
                break;
            case "0528":
                ((RadioButton)findViewById(R.id.Netherlands)).setChecked(true);
                break;
            case "0840":
                ((RadioButton)findViewById(R.id.USA)).setChecked(true);
                break;
            case "0756":
                ((RadioButton)findViewById(R.id.Switserland)).setChecked(true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.set_country_OK:
                Intent intent=new Intent();
                intent.putExtra("country",getSelectedCountry());
                setResult(RESULT_OK,intent);
                finish();
                break;
            case R.id.set_country_Cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }
}
