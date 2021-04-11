/*
 * class: AboutActivity.java
 * description: an android studio activity class for handling the about/info page
 * author: M Beanland
 * date created: 13/02/2021
 * date modified: 14/02/2021
 *
 * */

package com.example.thundertube;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // get custom toolbar,
        // the settings page version since it already has a link back to the homepage
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.settingstoolbar);

        // get text view on toolbar and change it to show this page's title instead
        TextView toolbarTitle = findViewById(R.id.settingsToolbarTitle);
        toolbarTitle.setText("About ThunderTube");

    }

    // method to handle changing page back to main menu
    public void mainMenu(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
