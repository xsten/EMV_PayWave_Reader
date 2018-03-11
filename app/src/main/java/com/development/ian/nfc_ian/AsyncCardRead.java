package com.development.ian.nfc_ian;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

//AsyncCardRead starts an asynchronous task to process responses and carry out card IO
public class AsyncCardRead extends AsyncTask<Intent, Void, EMVReader> {

    private final MainActivity MAIN_ACTIVITY;
    private String ttq="68000000";
    private String amount="000000000010";
    private String amountOther="000000000000";
    private String terminalCountryCode="0250";
    private String tvr="0000000000";
    private String curcy="0978";
    private String txDate="180207";
    private String txType="00";
    private String unpredicatableNumber;
    private String terminalType;

    public String getTtq() {
        return ttq;
    }

    public void setTtq(String ttq) {
        this.ttq = ttq;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAmountOther() {
        return amountOther;
    }

    public void setAmountOther(String amountOther) {
        this.amountOther = amountOther;
    }

    public String getTerminalCountryCode() {
        return terminalCountryCode;
    }

    public void setTerminalCountryCode(String terminalCountryCode) {
        this.terminalCountryCode = terminalCountryCode;
    }

    public String getTvr() {
        return tvr;
    }

    public void setTvr(String tvr) {
        this.tvr = tvr;
    }

    public String getCurcy() {
        return curcy;
    }

    public void setCurcy(String curcy) {
        this.curcy = curcy;
    }

    public String getTxDate() {
        return txDate;
    }

    public void setTxDate(String txDate) {
        this.txDate = txDate;
    }

    public String getTxType() {
        return txType;
    }

    public void setTxType(String txType) {
        this.txType = txType;
    }

    public String getUnpredicatableNumber() {
        return unpredicatableNumber;
    }

    public void setUnpredicatableNumber(String unpredicatableNumber) {
        this.unpredicatableNumber = unpredicatableNumber;
    }
    public void setTerminalType(String terminalType) {
        this.terminalType=terminalType;
    }


    protected AsyncCardRead(MainActivity mainActivity){
        MAIN_ACTIVITY = mainActivity;
    }


    @Override
    protected EMVReader doInBackground(Intent... intents) {
        Intent intent = intents[0];
        PaywaveHandler paywaveHandler = new PaywaveHandler(intent);
        EMVReader emvReader=null;

        try {
            emvReader = new EMVReader(paywaveHandler, null, paywaveHandler.transceive(EMVReader.SELECT_PPSE));
            emvReader.read(ttq,amount,amountOther,terminalCountryCode,tvr,curcy,txDate,txType,unpredicatableNumber,terminalType);
            return emvReader;
        } catch (IOException e) {
            Log.e(getClass().getName(),"Exception caught :",e);
            return emvReader;
        }
    }

    @Override
    protected void onPostExecute(EMVReader emvReader){
        MAIN_ACTIVITY.updateEMVReader(emvReader);
    }
}
