package com.jdrago.rubik;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class RubikActivity extends Activity
{
    private RubikView view_;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_rubik);

        view_ = new RubikView(getApplication());
        setContentView(view_);

        getActionBar().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.rubik, menu);
        return true;
    }
}
