package com.quiz.rockquiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BottomNavigationBarManager extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        navigationView = (BottomNavigationView) findViewById(R.id.navigationBar);
        navigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.play: {
                startActivity(new Intent(BottomNavigationBarManager.this, MainActivity.class));
                break;
            }
            case R.id.profile: {
                startActivity(new Intent(BottomNavigationBarManager.this, ProfileActivity.class));
            }
            default: {
                return false;
            }
        }
        return true;
    }


    private void updateNavigationBarState() {
        int actionId = getBottomNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }



    void selectBottomNavigationBarItem(int itemId) {
        MenuItem item = navigationView.getMenu().findItem(itemId);
        item.setChecked(true);
    }

    // Abstract methods:

    // This is to return which layout(activity) needs to display when clicked on tabs:
    abstract int getLayoutId();

    // Which menu item selected and change the state of that menu item:
    abstract int getBottomNavigationMenuItemId();
}
