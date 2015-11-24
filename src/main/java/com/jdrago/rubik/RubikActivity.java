package com.jdrago.rubik;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class RubikActivity extends Activity
{
    private RubikView view_;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_rubik);

        //getActionBar().hide();

        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        Log.e("RUBIK", "actionBarHeight: "+actionBarHeight);

        view_ = new RubikView(getApplication(), actionBarHeight);
        setContentView(view_);

        Log.e("RUBIK", "restore() start");

        int cubies[] = null;
        if(savedInstanceState != null)
        {
            cubies = savedInstanceState.getIntArray("cube");
        }
        if(cubies == null)
        {
            view_.cube_.unshuffle();
        }
        else
        {
            int i = 0;
            for (int face = 0; face < 6; ++face)
            {
                for (int cubie = 0; cubie < 9; ++cubie)
                {
                    view_.cube_.cubies_[face][cubie] = cubies[i++];
                }
            }
        }
        Log.e("RUBIK", "restore() done");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.rubik, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        int cubies[] = new int[54];
        int i = 0;
        for (int face = 0; face < 6; ++face)
        {
            for (int cubie = 0; cubie < 9; ++cubie)
            {
                cubies[i++] = view_.cube_.cubies_[face][cubie];
            }
        }
        savedInstanceState.putIntArray("cube", cubies);

        Log.e("RUBIK", "save() done");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if(id == R.id.shuffle)
        {
            view_.cube_.shuffle();
        }
        else if(id == R.id.unshuffle)
        {
            view_.cube_.unshuffle();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        view_.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        view_.onResume();
    }
}
