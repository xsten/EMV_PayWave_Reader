package com.development.ian.nfc_ian;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class set_amount_currency extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_amount_currency);
        setInputFilter();

        RadioGroup rg=(RadioGroup)findViewById(R.id.currencyRadioGroup);
        rg.setOnCheckedChangeListener(this);

        String amountStr=getIntent().getStringExtra("amount");
        amountStr=amountStr==null?"":amountStr;
        String amountOther=getIntent().getStringExtra("amountOther");
        amountOther=amountOther==null?"":amountOther;

        long a=Long.parseLong(amountStr);
        long ao=Long.parseLong(amountOther);

        amountStr=String.format("%d.%02d",a/100,a%100);
        amountOther=String.format("%d.%02d",ao/100,ao%100);

        Log.d(getClass().getName(),"Amount:"+amountStr);
        Log.d(getClass().getName(),"AmountOther:"+amountOther);
        Pattern mPattern=Pattern.compile("\\d{0,10}||(\\d{0,10}\\.\\d{0,2})");
        if(!mPattern.matcher(amountStr).matches()) {
            Log.d(getClass().getName(),"match failed for amountStr");
            amountStr = "";
        }
        if(!mPattern.matcher(amountOther).matches())
            amountOther="";

        ((TextView)findViewById(R.id.AmountTextField)).setText(amountStr);
        ((EditText)findViewById(R.id.AmountOtherTextField)).setText(amountOther);

        setRadioChecks();

        ((Button)findViewById(R.id.amount_currency_OK)).setOnClickListener(this);
        ((Button)findViewById(R.id.amount_currency_Cancel)).setOnClickListener(this);
    }

    private void setRadioChecks() {
        if(getIntent().getStringExtra("currency")==null)return;
        switch(getIntent().getStringExtra("currency"))
        {
            case "0752":
                ((RadioButton)findViewById(R.id.SEK)).setChecked(true);
                break;
            case "0578":
                ((RadioButton)findViewById(R.id.NOK)).setChecked(true);
                break;
            case "0826":
                ((RadioButton)findViewById(R.id.GBP)).setChecked(true);
                break;
            case "0756":
                ((RadioButton)findViewById(R.id.CHF)).setChecked(true);
                break;
            case "0840":
                ((RadioButton)findViewById(R.id.USD)).setChecked(true);
                break;
            case "0978":
                ((RadioButton)findViewById(R.id.EUR)).setChecked(true);
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        setInputFilter(); // if currency changed, the comma may need to be checked differently
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.amount_currency_OK:

                Intent intent=new Intent();
                EditText et=(EditText)findViewById(R.id.AmountTextField);
                long l=Long.parseLong(et.getText().toString().replaceAll("\\.",""));
                if(!et.getText().toString().contains("."))
                    l*=100;
                intent.putExtra("amount",String.format("%012d",l));
                et=(EditText)findViewById(R.id.AmountOtherTextField);
                l=Long.parseLong(et.getText().toString().replaceAll("\\.",""));
                if(!et.getText().toString().contains("."))
                    l*=100;
                intent.putExtra("amountOther",String.format("%012d",l));

                RadioGroup rg=(RadioGroup)findViewById(R.id.currencyRadioGroup);
                String curcy="0978";
                switch(rg.getCheckedRadioButtonId())
                {
                    case R.id.EUR:
                        curcy="0978";
                        break;
                    case R.id.USD:
                        curcy="0840";
                        break;
                    case R.id.CHF:
                        curcy="0756";
                        break;
                    case R.id.GBP:
                        curcy="0826";
                        break;
                    case R.id.NOK:
                        curcy="0578";
                        break;
                    case R.id.SEK:
                        curcy="0752";
                        break;
                }
                intent.putExtra("curcy",curcy);

                setResult(RESULT_OK,intent);
                finish();
                break;
            case R.id.amount_currency_Cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }

    class DecimalDigitsInputFilter implements InputFilter
    {
        Pattern mPattern;

        public DecimalDigitsInputFilter(int digitsBeforeZero,int digitsAfterZero) {
            if(digitsAfterZero==0)
                mPattern=Pattern.compile("\\d{0,"+(digitsBeforeZero-1)+"}");
            else {
                // mPattern=Pattern.compile("[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
                mPattern = Pattern.compile("\\d{0," + (digitsBeforeZero - 1) + "}|\\d{0," + (digitsBeforeZero - 1) + "}\\.\\d{0," + (digitsAfterZero - 1) + "}");
            }
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            Matcher matcher=mPattern.matcher(dest);
            if(!matcher.matches())
                return "";


            return null;
        }
    }

    private void setInputFilter()
    {
        InputFilter[] inputFilters=new InputFilter[1];

        EditText et1=(EditText)findViewById(R.id.AmountTextField);
        EditText et2=(EditText)findViewById(R.id.AmountOtherTextField);

        RadioGroup rg=(RadioGroup)findViewById(R.id.currencyRadioGroup);
        switch(rg.getCheckedRadioButtonId())
        {
            case R.id.EUR:
            case R.id.USD:
            case R.id.CHF:
            case R.id.GBP:
            case R.id.NOK:
                inputFilters[0]=new DecimalDigitsInputFilter(10,2);
                break;
            case R.id.SEK:
                inputFilters[0]=new DecimalDigitsInputFilter(12,0);
                break;

        }

        et1.setFilters(inputFilters);
        et2.setFilters(inputFilters);
    }
}
