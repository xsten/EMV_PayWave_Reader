package com.development.ian.nfc_ian;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

public class set_transaction_type extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_transaction_type);

        Button b=(Button)findViewById(R.id.setTransactionTypeOK);
        b.setOnClickListener(this);
        b=(Button)findViewById(R.id.setTransactionTypeCancel);
        b.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.setTransactionTypeOK:
                Intent intent=new Intent();
                intent.putExtra("transactionType",getTransactionTypeCode());
                setResult(RESULT_OK,intent);
                finish();
                break;
            case R.id.setTransactionTypeCancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
            default:
                break;

        }
    }

    private String getTransactionTypeCode() {
        String ret="00";
        RadioGroup rg=(RadioGroup)findViewById(R.id.transactionTypeRadio);
        switch(rg.getCheckedRadioButtonId())
        {
            case R.id.good_and_services:
                ret="00";
                break;
            case R.id.cash:
                ret="01";
                break;
            case R.id.debit_adjusment:
                ret="02";
                break;
            case R.id.refund:
                ret="20";
                break;
            case R.id.available_funds_inquiry:
                ret="30";
                break;
            case R.id.balance_inquiry:
                ret="31";
                break;
            case R.id.payment_from_account:
                ret="50";
                break;
            case R.id.payment_to_account:
                ret="53";
                break;
            default:
                break;
        }
        return ret;
    }
}
