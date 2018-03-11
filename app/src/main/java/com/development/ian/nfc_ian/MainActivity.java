package com.development.ian.nfc_ian;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

//MainActivity manages the UI and foreground dispatch
public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView pan, issuer, expires, message, decision,applicationCryptogram,atc;
    private MainActivity instance;

    // Input tags
    private String ttq="33000000";
    private String amount="000000000010";
    private String amountOther="000000000000";
    private String terminalCountryCode="0250";
    private String tvr="0000000000";
    private String curcy="0978";
    private String txDate="180207";
    private String txType="00";
    private String unpredicatableNumber="CAFEBABE";
    private String terminalType="23";

    // Output tags
    private String cardNumber;
    private String expdate;
    private String arqc;
    private String iad;
    private String aip;
    private String card_atc;


    public MainActivity()
    {

        instance=this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b=(Button)findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(instance,SetTerminalTags.class);
                intent.putExtra("ttq",ttq);
                intent.putExtra("amount",amount);
                intent.putExtra("amountOther",amountOther);
                intent.putExtra("terminalCountryCode",terminalCountryCode);
                intent.putExtra("tvr",tvr);
                intent.putExtra("curcy",curcy);
                intent.putExtra("txDate",txDate);
                intent.putExtra("txType",txType);
                intent.putExtra("unpredicatableNumber",unpredicatableNumber);
                intent.putExtra("terminalType",terminalType);

                // startActivity(intent);
                startActivityForResult(intent,0);
            }
        });

        b=(Button)findViewById(R.id.button3);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                // intent.putExtra(Intent.EXTRA_EMAIL,"xavier.stenuit@six-group.com");
                intent.putExtra(Intent.EXTRA_SUBJECT,"EMV Transaction "+new Date());
                String msg="";
                msg+="cardNumber:"+cardNumber+"\n";
                msg+="expdate:"+expdate+"\n";
                msg+="arqc:"+arqc+"\n";
                msg+="iad:"+iad+"\n";
                msg+="amount:"+amount+"\n";
                msg+="amountOther:"+amountOther+"\n";
                msg+="terminalCountryCode:"+terminalCountryCode+"\n";
                msg+="tvr:"+tvr+"\n";
                msg+="curcy:"+curcy+"\n";
                msg+="txDate:"+txDate+"\n";
                msg+="txType:"+txType+"\n";
                msg+="unpredicatableNumber:"+unpredicatableNumber+"\n";
                msg+="aip:"+aip+"\n";
                msg+="card_atc:"+card_atc+"\n";
                intent.putExtra(Intent.EXTRA_TEXT,msg);
                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Setup foreground dispatch
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        //If NFC is turned off let the user know
        if(!nfcAdapter.isEnabled()){
            Snackbar nfcNotEnabledMessage = Snackbar.make(findViewById(R.id.content_view), R.string.message_nfc_off, Snackbar.LENGTH_INDEFINITE);
            nfcNotEnabledMessage.show();
        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        pan = (TextView) findViewById(R.id.pan);
        issuer = (TextView) findViewById(R.id.issuer);
        expires = (TextView) findViewById(R.id.expiry);
        message = (TextView) findViewById(R.id.message);
        decision = (TextView)findViewById(R.id.Decision);
        applicationCryptogram=(TextView)findViewById(R.id.Certificate);
        atc=(TextView)findViewById(R.id.Atc);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().getName(),"resultCode="+resultCode);
        if(requestCode==0 && resultCode==RESULT_OK)
        {
            Log.i(getClass().getName(),"User has changed default terminal values, forwarding to EMVReader");

            ttq=data.getStringExtra("ttq");
            amount=data.getStringExtra("amount");
            amountOther=data.getStringExtra("amountOther");
            terminalCountryCode=data.getStringExtra("terminalCountryCode");
            tvr=data.getStringExtra("tvr");
            curcy=data.getStringExtra("curcy");
            txDate=data.getStringExtra("txDate");
            txType=data.getStringExtra("txType");
            unpredicatableNumber=data.getStringExtra("unpredicatableNumber");
            terminalType=data.getStringExtra("terminalType");

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //disable options menu
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        super.onResume();
    }

    @Override
    public void onPause(){
        nfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent){
        //Carry out IO operations and reading of responses in asynchronous task
        AsyncCardRead asyncCardRead = new AsyncCardRead(this);
        asyncCardRead.setTtq(ttq);
        asyncCardRead.setAmount(amount);
        asyncCardRead.setAmountOther(amountOther);
        asyncCardRead.setCurcy(curcy);
        asyncCardRead.setTerminalCountryCode(terminalCountryCode);
        asyncCardRead.setTvr(tvr);
        asyncCardRead.setTxDate(txDate);
        asyncCardRead.setTxType(txType);
        asyncCardRead.setUnpredicatableNumber(unpredicatableNumber);
        asyncCardRead.setTerminalType(terminalType);
        asyncCardRead.execute(intent);
    }

    private void displayTrack2(EMVReader emvReader){
        //Update the UI with card info
        expires.setText(getResources().getString(R.string.expiry,
                emvReader.expiryMonth, emvReader.expiryYear));
        pan.setText(getResources().getString(R.string.pan, emvReader.pan));
        issuer.setText(getResources().getString(R.string.issuer, emvReader.issuer));
        message.setText(getResources().getString(R.string.message_post));
        decision.setText("Card Decision: "+emvReader.decision);
        applicationCryptogram.setText("Cryptogram:"+emvReader.applicationCryptogram);
        atc.setText("ATC: "+emvReader.atc);


        cardNumber=emvReader.pan;
        expdate=String.format("%2d-%2d",emvReader.expiryYear,emvReader.expiryMonth);
        arqc=emvReader.applicationCryptogram;
        iad=emvReader.iad;
        aip=emvReader.aip;
        card_atc=emvReader.atc;

        Snackbar updatedMessage = Snackbar.make(findViewById(R.id.content_view), R.string.message_ui_updated, Snackbar.LENGTH_LONG);
        updatedMessage.show();
    }

    private void displayError() {
        Snackbar errorMessage = Snackbar.make(findViewById(R.id.content_view), R.string.message_read_error, Snackbar.LENGTH_LONG);
        errorMessage.show();
    }

    protected void updateEMVReader(EMVReader reader){
        //Verify all information needed is present
        if(reader == null || reader.issuer == null || reader.expiryYear == null ||
                reader.expiryMonth == null || reader.pan == null ){
            displayError();
        }else {


            displayTrack2(reader);
        }
    }

    private static String toHex(byte[] in)
    {
        StringBuilder sb = new StringBuilder(in.length * 2);
        for(byte b: in)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }


}
