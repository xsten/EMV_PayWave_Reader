package com.development.ian.nfc_ian;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

public class SetTerminalTags extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_terminal_tags);

        Intent data=getIntent();
        TextView tv;
        tv=(TextView)SetTerminalTags.this.findViewById(R.id.TTQ_field);
        tv.setText(data.getStringExtra("ttq"));

        tv=(TextView)SetTerminalTags.this.findViewById(R.id.Amount_field);
        tv.setText(data.getStringExtra("amount"));

        tv=(TextView)SetTerminalTags.this.findViewById(R.id.AmountOther_field);
        tv.setText(data.getStringExtra("amountOther"));

        tv=(TextView)SetTerminalTags.this.findViewById(R.id.TerminalCountryCode_field);
        tv.setText(data.getStringExtra("terminalCountryCode"));

        tv=(TextView)SetTerminalTags.this.findViewById(R.id.TVR_field);
        tv.setText(data.getStringExtra("tvr"));

        tv=(TextView)SetTerminalTags.this.findViewById(R.id.Currency_field);
        tv.setText(data.getStringExtra("curcy"));

        tv=(TextView)SetTerminalTags.this.findViewById(R.id.TransactionDate_field);
        tv.setText(data.getStringExtra("txDate"));

        tv=(TextView)SetTerminalTags.this.findViewById(R.id.TransactionType_field);
        tv.setText(data.getStringExtra("txType"));

        tv=(TextView)SetTerminalTags.this.findViewById(R.id.UnpredictableNumber_field);
        tv.setText(data.getStringExtra("unpredicatableNumber"));

        tv=(TextView)SetTerminalTags.this.findViewById(R.id.TerminalType_field);
        tv.setText(data.getStringExtra("terminalType"));

        Button b=(Button)findViewById(R.id.button2);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView tv;
                Intent data=new Intent();

                tv=(TextView)SetTerminalTags.this.findViewById(R.id.TTQ_field);
                data.putExtra("ttq",tv.getText().toString());

                tv=(TextView)SetTerminalTags.this.findViewById(R.id.Amount_field);
                data.putExtra("amount",tv.getText().toString());

                tv=(TextView)SetTerminalTags.this.findViewById(R.id.AmountOther_field);
                data.putExtra("amountOther",tv.getText().toString());

                tv=(TextView)SetTerminalTags.this.findViewById(R.id.TerminalCountryCode_field);
                data.putExtra("terminalCountryCode",tv.getText().toString());

                tv=(TextView)SetTerminalTags.this.findViewById(R.id.TVR_field);
                data.putExtra("tvr",tv.getText().toString());

                tv=(TextView)SetTerminalTags.this.findViewById(R.id.Currency_field);
                data.putExtra("curcy",tv.getText().toString());

                tv=(TextView)SetTerminalTags.this.findViewById(R.id.TransactionDate_field);
                data.putExtra("txDate",tv.getText().toString());

                tv=(TextView)SetTerminalTags.this.findViewById(R.id.TransactionType_field);
                data.putExtra("txType",tv.getText().toString());

                tv=(TextView)SetTerminalTags.this.findViewById(R.id.UnpredictableNumber_field);
                data.putExtra("unpredicatableNumber",tv.getText().toString());

                tv=(TextView)SetTerminalTags.this.findViewById(R.id.TerminalType_field);
                data.putExtra("terminalType",tv.getText().toString());

                setResult(RESULT_OK,data);
                finish();
            }
        });

        b=(Button)findViewById(R.id.button4);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent data=new Intent();

                setResult(RESULT_CANCELED,data);
                finish();
            }
        });

        b=(Button)findViewById(R.id.TerminalTypeButton);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //  Intent intent=new Intent(instance,SetTerminalTags.class);
                Intent data=new Intent(getApplicationContext(),SetTerminalType.class);
                TextView tv=(TextView)SetTerminalTags.this.findViewById(R.id.TerminalType_field);
                data.putExtra("terminalType",tv.getText().toString());

                startActivityForResult(data,0);
            }
        });

        b=(Button)findViewById(R.id.UNButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final char[] nibble={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
                StringBuffer sb=new StringBuffer();
                Random r=new Random();
                for(int i=0;i<8;i++)
                {
                    sb.append(nibble[r.nextInt(16)]);
                }
                TextView tv=(TextView)findViewById(R.id.UnpredictableNumber_field);
                tv.setText(sb);
            }
        });

        b=(Button)findViewById(R.id.TXDateButton);
        b.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(),set_date.class),0);
            }
        });

        b=(Button)findViewById(R.id.TXTypeButton);
        b.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(),set_transaction_type.class),0);
            }
        });

        b=(Button)findViewById(R.id.TVRButton);
        b.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),set_tvr.class);
                TextView tv=(TextView)findViewById(R.id.TVR_field);
                intent.putExtra("tvr",tv.getText().toString());
                startActivityForResult(intent,0);
            }
        });

        b=(Button)findViewById(R.id.TTQButton);
        b.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),set_ttq.class);
                TextView tv=(TextView)findViewById(R.id.TTQ_field);
                intent.putExtra("ttq",tv.getText().toString());
                startActivityForResult(intent,0);
            }
        });

        b=(Button)findViewById(R.id.AmountButton);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),set_amount_currency.class);
                TextView tv=(TextView)findViewById(R.id.Amount_field);
                intent.putExtra("amount",tv.getText().toString());
                tv=(TextView)findViewById(R.id.AmountOther_field);
                intent.putExtra("amountOther",tv.getText().toString());
                tv=(TextView)findViewById(R.id.Currency_field);
                intent.putExtra("currency",tv.getText().toString());
                startActivityForResult(intent,0);
            }
        });

        b=(Button)findViewById(R.id.AmountOtherButton);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),set_amount_currency.class);
                TextView tv=(TextView)findViewById(R.id.Amount_field);
                intent.putExtra("amount",tv.getText().toString());
                tv=(TextView)findViewById(R.id.AmountOther_field);
                intent.putExtra("amountOther",tv.getText().toString());
                tv=(TextView)findViewById(R.id.Currency_field);
                intent.putExtra("currency",tv.getText().toString());
                startActivityForResult(intent,0);
            }
        });

        b=(Button)findViewById(R.id.CurrencyButton);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),set_amount_currency.class);
                TextView tv=(TextView)findViewById(R.id.Amount_field);
                intent.putExtra("amount",tv.getText().toString());
                tv=(TextView)findViewById(R.id.AmountOther_field);
                intent.putExtra("amountOther",tv.getText().toString());
                tv=(TextView)findViewById(R.id.Currency_field);
                intent.putExtra("currency",tv.getText().toString());
                startActivityForResult(intent,0);
            }
        });

        b=(Button)findViewById(R.id.TCCButton);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),set_country.class);
                TextView tv=(TextView)findViewById(R.id.TerminalCountryCode_field);
                intent.putExtra("country",tv.getText().toString());
                startActivityForResult(intent,0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().getName(),"resultCode="+resultCode);
        if(requestCode==0 && resultCode==RESULT_OK && data!=null)
        {

            if(data.getStringExtra("transactionType")!=null)
                ((TextView)findViewById(R.id.TransactionType_field)).setText(data.getStringExtra("transactionType"));
            if(data.getStringExtra("terminalType")!=null)
                ((TextView)findViewById(R.id.TerminalType_field)).setText(data.getStringExtra("terminalType"));
            if(data.getStringExtra("date")!=null)
                ((TextView)findViewById(R.id.TransactionDate_field)).setText(data.getStringExtra("date"));
            if(data.getStringExtra("tvr")!=null)
                ((TextView)findViewById(R.id.TVR_field)).setText(data.getStringExtra("tvr"));
            if(data.getStringExtra("ttq")!=null)
                ((TextView)findViewById(R.id.TTQ_field)).setText(data.getStringExtra("ttq"));
            if(data.getStringExtra("amount")!=null)
                ((TextView)findViewById(R.id.Amount_field)).setText(data.getStringExtra("amount"));
            if(data.getStringExtra("amountOther")!=null)
                ((TextView)findViewById(R.id.AmountOther_field)).setText(data.getStringExtra("amountOther"));
            if(data.getStringExtra("curcy")!=null)
                ((TextView)findViewById(R.id.Currency_field)).setText(data.getStringExtra("curcy"));
            if(data.getStringExtra("country")!=null)
                ((TextView)findViewById(R.id.TerminalCountryCode_field)).setText(data.getStringExtra("country"));

        }
    }
}
