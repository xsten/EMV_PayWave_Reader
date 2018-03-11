package com.development.ian.nfc_ian;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;

import java.text.SimpleDateFormat;

public class set_date extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_date);
        CalendarView cv=(CalendarView)findViewById(R.id.calendar);
        cv.setDate(System.currentTimeMillis());

        Button b=(Button)findViewById(R.id.calendarOK);
        b.setOnClickListener(this);
        b=(Button)findViewById(R.id.calendarCanceled);
        b.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        Intent data=new Intent();
        switch(view.getId())
        {
            case R.id.calendarOK:
                SimpleDateFormat sdf=new SimpleDateFormat("yyMMdd");
                CalendarView cv=(CalendarView)findViewById(R.id.calendar);
                data.putExtra("date",sdf.format(cv.getDate()));
                Log.d(getClass().getName(),"intent extra="+data.getStringExtra("date"));
                setResult(RESULT_OK,data);
                finish();
                break;
            case R.id.calendarCanceled:
                setResult(RESULT_CANCELED);
                finish();
                break;
            default:
                break;
        }


    }
}
