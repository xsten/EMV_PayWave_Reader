package com.development.ian.nfc_ian;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

                setResult(RESULT_OK,data);
                finish();
            }
        });
    }
}
