/*
 * class: SettingsActivity.java
 * description: an android studio activity class for handling the settings page
 * author: M Beanland
 * date created: 12/02/2021
 * date modified: 14/02/2021
 *
 * */


package com.example.thundertube;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.DayOfWeek;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    public Commute loadedCommute;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // get custom toolbar with back button to menu
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.settingstoolbar);

        // load currently saved commute from storage
        loadedCommute = loadedStorageCommute();

        // if saved commute exists
        if (loadedCommute != null){

            // get all input fields in settings menu including day of week checkboxes
            TextView startPostcodeView = findViewById(R.id.startPostcodeText);
            TextView endPostcodeView = findViewById(R.id.endPostcodeText);
            TextView arrivalTimeView1 = findViewById(R.id.arriveTimeText1);
            TextView arrivalTimeView2 = findViewById(R.id.arriveTimeText2);


            CheckBox mondayCheckbox = findViewById(R.id.mondayCheckbox);
            CheckBox tuesdayCheckbox = findViewById(R.id.tuesdayCheckbox);
            CheckBox wednesdayCheckbox = findViewById(R.id.wednesdayCheckbox);
            CheckBox thursdayCheckbox = findViewById(R.id.thursdayCheckbox);
            CheckBox fridayCheckbox = findViewById(R.id.fridayCheckbox);
            CheckBox saturdayCheckbox = findViewById(R.id.saturdayCheckbox);
            CheckBox sundayCheckbox = findViewById(R.id.sundayCheckbox);

            // set input fields to show currently stored information
            startPostcodeView.setText(loadedCommute.startPostcode);
            endPostcodeView.setText(loadedCommute.endPostcode);

            // arrival time must be split up into hour and minutes for the 2 inputs
            String[] splitTime = loadedCommute.arrivalTime.split(":");
            arrivalTimeView1.setText(splitTime[0]);
            arrivalTimeView2.setText(splitTime[1]);


            // a very awkward way of setting the correct info into the checkboxes
            // but I couldn't think of anything better :(
            // loop through all the days and check the box if it's included in the list
            // i guess it's not the worst thing in the world since no more days are likely to
            // ever be added but still i'm sure there's a better way to do this
            for (int i = 0; i < loadedCommute.arrivalDaysList.size(); i++) {
                DayOfWeek day = loadedCommute.arrivalDaysList.get(i);
                if (day == DayOfWeek.MONDAY){
                    mondayCheckbox.setChecked(true);
                } else if (day == DayOfWeek.TUESDAY) {
                    tuesdayCheckbox.setChecked(true);
                } else if (day == DayOfWeek.WEDNESDAY) {
                    wednesdayCheckbox.setChecked(true);
                } else if (day == DayOfWeek.THURSDAY) {
                    thursdayCheckbox.setChecked(true);
                } else if (day == DayOfWeek.FRIDAY) {
                    fridayCheckbox.setChecked(true);
                } else if (day == DayOfWeek.SATURDAY) {
                    saturdayCheckbox.setChecked(true);
                } else if (day == DayOfWeek.SUNDAY) {
                    sundayCheckbox.setChecked(true);
                }
            }

        } else {
            // if no previously loaded commute then show initial setup title instead
            TextView toolbarTitle = findViewById(R.id.settingsToolbarTitle);
            toolbarTitle.setText(R.string.setupText);

            ImageButton toolbarButton = findViewById(R.id.btnMenu);
            toolbarButton.setVisibility(View.GONE);
        }



    }

    // starts main activity
    public void mainMenu(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // loads currently saved commute from local storage
    public Commute loadedStorageCommute() {

        Commute commute;
        File directory = getFilesDir();
        File file = new File(directory, "commute");

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            commute = (Commute) ois.readObject();
            ois.close();
            return commute;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    // save new commute details into local storage
    public void saveCommuteIntoStorage(Commute commute) {

        // if there is not already a commute saved then create a new commute object
        if (loadedStorageCommute() == null) {
            loadedCommute = new Commute();
            loadedCommute = commute;
            // if there is then fetch old one to be overwritten
        } else {
            loadedCommute = commute;
        }

        File directory = getFilesDir(); //or getExternalFilesDir(null); for external storage
        File file = new File(directory, "commute");

        Log.d("directory", directory.toString());

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(loadedCommute);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // begin commute saving process
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void saveCommute(View view){

        // get all appropriate inputs for commute
        TextView startPostcodeView = findViewById(R.id.startPostcodeText);
        TextView endPostcodeView = findViewById(R.id.endPostcodeText);
        //arrival time is taken from 2 inputs (hour and minute) and concatenated
        TextView arrivalTimeView1 = findViewById(R.id.arriveTimeText1);
        TextView arrivalTimeView2 = findViewById(R.id.arriveTimeText2);
        CheckBox mondayCheckbox = findViewById(R.id.mondayCheckbox);
        CheckBox tuesdayCheckbox = findViewById(R.id.tuesdayCheckbox);
        CheckBox wednesdayCheckbox = findViewById(R.id.wednesdayCheckbox);
        CheckBox thursdayCheckbox = findViewById(R.id.thursdayCheckbox);
        CheckBox fridayCheckbox = findViewById(R.id.fridayCheckbox);
        CheckBox saturdayCheckbox = findViewById(R.id.saturdayCheckbox);
        CheckBox sundayCheckbox = findViewById(R.id.sundayCheckbox);

        // create new commute object
        Commute commute = new Commute();

        // create bool to track input validity
        boolean valid = true;
        //each input is checked against regex to make sure it is what is expected, if not then
        //error message is shown

        // regex for postcode validation taken from: https://stackoverflow.com/questions/164979/regex-for-matching-uk-postcodes
        if (!startPostcodeView.getText().toString().matches("([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y][0-9]{1,2})|(([A-Za-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9][A-Za-z]?))))\\s?[0-9][A-Za-z]{2})")) {
            valid = false;
            Toast.makeText(this, "Start Postcode Invalid", Toast.LENGTH_SHORT).show();
        }

        if (!endPostcodeView.getText().toString().matches("([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y][0-9]{1,2})|(([A-Za-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9][A-Za-z]?))))\\s?[0-9][A-Za-z]{2})")) {
            valid = false;
            Toast.makeText(this, "End Postcode Invalid", Toast.LENGTH_SHORT).show();
        }

        if (!arrivalTimeView1.getText().toString().matches("^[0-9]{1,2}[:.,-]?$") || !arrivalTimeView2.getText().toString().matches("^[0-9]{1,2}[:.,-]?$")) {
            valid = false;
            Toast.makeText(this, "Arrival Time Invalid", Toast.LENGTH_SHORT).show();
        }

        // set info from first four inputs
        commute.startPostcode = startPostcodeView.getText().toString();
        commute.endPostcode = endPostcodeView.getText().toString();

        // temporary string to merge the two halves of the time
        String tempTime = arrivalTimeView1.getText().toString();
        // if they have only entered one digit for the hour (e.g. 9 instead of 09), then add an 0 at the start
        if (tempTime.matches("(?<!\\S)\\d(?!\\S)")){
            tempTime = "0" + tempTime;
        }
        tempTime = tempTime + ":" + arrivalTimeView2.getText().toString();
        commute.arrivalTime = tempTime;

        // if all input checks passed then save
        if (valid) {
            // create list to store days of week info
            ArrayList<DayOfWeek> arrivalDays = new ArrayList<>();

            // check if each checkbox is checked, if so then add that day to list
            if (mondayCheckbox.isChecked()){
                arrivalDays.add(DayOfWeek.MONDAY);
            }
            if (tuesdayCheckbox.isChecked()){
                arrivalDays.add(DayOfWeek.TUESDAY);
            }
            if (wednesdayCheckbox.isChecked()){
                arrivalDays.add(DayOfWeek.WEDNESDAY);
            }
            if (thursdayCheckbox.isChecked()){
                arrivalDays.add(DayOfWeek.THURSDAY);
            }
            if (fridayCheckbox.isChecked()){
                arrivalDays.add(DayOfWeek.FRIDAY);
            }
            if (saturdayCheckbox.isChecked()){
                arrivalDays.add(DayOfWeek.SATURDAY);
            }
            if (sundayCheckbox.isChecked()){
                arrivalDays.add(DayOfWeek.SUNDAY);
            }

            // add days list to commute
            commute.arrivalDaysList = arrivalDays;

            // save Commute into storage
            saveCommuteIntoStorage(commute);

            // reload loaded commute to newly saved one
            loadedStorageCommute();

            // show success message
            Toast.makeText(this, "Information Saved", Toast.LENGTH_SHORT).show();

            mainMenu(view);
        }


    }

}
