/*
 * class: MainActivity.java
 * description: an android studio activity class for handling the main page
 * author: M Beanland
 * date created: 02/02/2021
 * date modified: 14/02/2021
 *
 * */


package com.example.thundertube;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    // api information, base urls for calls and api keys
    public static String BaseUrl = "http://api.openweathermap.org/";
    public static String AppId = "28ac68f074857a66f42a23e8ab272dac";
    public static String tflURL = "https://api.tfl.gov.uk/";

    // variables used for journey information
    public static String startPostcode;
    public static String endPostcode;
    public static String arriveTime;
    public static String arriveDay;
    public String arriveDayOfWeek;

    // views that display data
    private TextView weatherData;
    private TextView tubeData;
    private TextView journeyData;
    private ImageView weatherIcon;
    private ScrollView statusPage;
    private ConstraintLayout disruptionsPage;
    private ConstraintLayout alternatePage;
    private Button disruptionButton;
    private Button alternateButton;



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get custom toolbar with settings button
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.customtoolbar);


        // find all views used in page
        weatherData = findViewById(R.id.weatherText);
        journeyData = findViewById(R.id.journeyText);
        weatherIcon = findViewById(R.id.weatherIcon);
        statusPage = findViewById(R.id.statusPage);
        disruptionsPage = findViewById(R.id.disruptionsPage);
        alternatePage = findViewById(R.id.alternatePage);
        disruptionButton = findViewById(R.id.disruptionButton);
        alternateButton = findViewById(R.id.alternateButton);
        tubeData = findViewById(R.id.tubeText);
        ImageButton showMenu = findViewById(R.id.btnShow);

        // load saved commute info from phone storage
        Commute loadedCommute = loadedStorageCommute();

        // get journey to display from saved commute
        initializeCommute(loadedCommute);

        // get weather data
        getWeatherData();

        // get tube disruption data
        getTubeData();


        // if loaded commute exists then get journey data
        if (loadedCommute != null) {
            getJourneyData();
        } else {
            // if no loaded commute then launch straight into settings
            settingsMenu();
        }

        // WC2N 5DN


        // set onclick listener for popup settings menu
        showMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, v);
                popup.setOnMenuItemClickListener(MainActivity.this);
                popup.inflate(R.menu.popup_menu);
                popup.show();
            }
        });


        // set onclick listeners to handle alternate route and tube disruption pages
        findViewById(R.id.alternateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusPage.setVisibility(View.GONE);
                disruptionButton.setVisibility(View.GONE);
                alternateButton.setVisibility(View.GONE);
                alternatePage.setVisibility(View.VISIBLE);

                findViewById(R.id.statusButton2).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        statusPage.setVisibility(View.VISIBLE);
                        disruptionButton.setVisibility(View.VISIBLE);
                        alternateButton.setVisibility(View.VISIBLE);
                        alternatePage.setVisibility(View.GONE);
                    }
                });
            }
        });

        findViewById(R.id.disruptionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusPage.setVisibility(View.GONE);
                disruptionButton.setVisibility(View.GONE);
                alternateButton.setVisibility(View.GONE);
                disruptionsPage.setVisibility(View.VISIBLE);

                findViewById(R.id.statusButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        statusPage.setVisibility(View.VISIBLE);
                        disruptionButton.setVisibility(View.VISIBLE);
                        alternateButton.setVisibility(View.VISIBLE);
                        disruptionsPage.setVisibility(View.GONE);
                    }
                });
            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void initializeCommute(Commute loadedCommute) {
        // if commute load was successful/if they have a commute saved
        if (loadedCommute != null){
            //get address info and arrival time
            startPostcode = loadedCommute.startPostcode;
            endPostcode = loadedCommute.endPostcode;
            arriveTime = loadedCommute.arrivalTime;

            //get current date
            LocalDate ld = LocalDate.now();

            // if they have an arrival day selected in their route
            if (loadedCommute.arrivalDaysList.size() != 0){
                // set arrival date to the first day in the list
                // this is done by finding the nearest date matching that day to the current date
                LocalDate baseArrivalDate = ld.with(TemporalAdjusters.next(loadedCommute.arrivalDaysList.get(0)));

                // loop through all other saved selected days
                for (int i = 1; i < loadedCommute.arrivalDaysList.size(); i++) {
                    // convert that day into a date
                    LocalDate arrivalDate = ld.with(TemporalAdjusters.next(loadedCommute.arrivalDaysList.get(i)));

                    // check if the next day in list is earlier than the current earliest date found
                    // then it becomes the new earliest date
                    if (arrivalDate.isBefore(baseArrivalDate)){
                        baseArrivalDate = arrivalDate;

                    }
                }

                // the nearest day is converted into a string so it can be used in the api call
                // and the literal day of arrival is converted into a string for display
                arriveDay = baseArrivalDate.toString();
                arriveDayOfWeek = baseArrivalDate.getDayOfWeek().name();

                // if the journey is on a monday and the day is monday the LocalDate.next() function
                // ignores the current day and sets the date to next monday which is bad
                // so here we check whether or not any of the days in the list are the current day
                for (int i = 0; i < loadedCommute.arrivalDaysList.size(); i++) {

                    if (loadedCommute.arrivalDaysList.get(i) == ld.getDayOfWeek()) {
                        // if any of them are we get the current time
                        LocalTime lt = LocalTime.now();
                        String hour = Integer.toString(lt.getHour());
                        String minute = Integer.toString(lt.getMinute());
                        // we convert the times into formats where they can be easily compraed
                        int currentTime = Integer.parseInt(hour + minute);
                        int targetTime = Integer.parseInt(arriveTime.replace(":",""));

                        // the two halves of the time(hour/minute) are concatenated then compared
                        // if the time of the journey is earlier than the current time then set the
                        // arrival date to today
                        if (currentTime < targetTime) {
                            LocalDate arrivalDate = ld.with(TemporalAdjusters.next(loadedCommute.arrivalDaysList.get(i)));
                            arriveDay = arrivalDate.toString();
                            arriveDayOfWeek = arrivalDate.getDayOfWeek().name();

                        }
                    }
                }

                // take the dashes out of the arrive day string so it is in the required format
                // for the travel api
                arriveDay = arriveDay.replace("-", "");

                // if there is no days of the week saved then travel day becomes as soon as possible
            } else {
                arriveDayOfWeek = "ASAP";
            }


        }
    }

    // get saved commute settings from local storage
    public Commute loadedStorageCommute() {

        // create new commute
        Commute commute;

        // create new file in commute directory in storage
        File directory = getFilesDir();
        File file = new File(directory, "commute");

        // try to retrieve saved commute from storage
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            commute = (Commute) ois.readObject();
            ois.close();
            // if successful return saved commute
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

    public void contentRefresh(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // launch settings activity
    public void settingsMenu() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    // launch about activity
    public void aboutPage() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    // handle pop up settings menu
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                settingsMenu();
                return true;
            case R.id.about:
                aboutPage();
                return true;
            default:
                return false;
        }
    }


    // display a given journey on screen using a given starting location for the first instruction
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    int displayJourney(Journey journey, int locationTop, int locationLeft, int parentLayoutId, boolean alternate){
        int priorId = 0;
        ConstraintLayout parentLayout = (ConstraintLayout)findViewById(parentLayoutId);
        //create extra text view if is alternate journey

        for (int i = 0; i < journey.legs.size(); i++) {
            // create constraints for adding new views
            ConstraintSet set = new ConstraintSet();
            // create linear layout for housing icon and text
            LinearLayout childView = new LinearLayout(getApplicationContext());
            childView.setId(View.generateViewId());
            //create image view for icon
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setId(View.generateViewId());
            //create text view for text
            TextView infoView = new TextView(getApplicationContext());
            infoView.setId(View.generateViewId());
            infoView.setTextSize(20);


            // get text with travel instruction
            String text = journey.legs.get(i).instruction.summary;
            // use text to determine image for icon
            if (text.contains("line") || text.contains("Railway")){
                imageView.setImageDrawable(getDrawable(R.drawable.ic_train_orange));
            } else if (text.contains("bus")){
                imageView.setImageDrawable(getDrawable(R.drawable.ic_directions_bus_orange));
            } else if (text.contains("Walk")){
                imageView.setImageDrawable(getDrawable(R.drawable.ic_directions_walk_orange));
            } else {
                imageView.setImageDrawable(getDrawable(R.drawable.ic_directions_orange));
            }
            // set text for textview
            infoView.setText(text);
            // add icon and text to linear layout
            childView.addView(imageView);
            childView.addView(infoView);
            //making linear layout display horizontal
            childView.setOrientation(LinearLayout.HORIZONTAL);
            //add linear layout to parent view
            parentLayout.addView(childView, 0);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) childView.getLayoutParams();
            params.topMargin = 50;
            infoView.setMaxLines(100);
//                        childView.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
            infoView.setLayoutParams(new LinearLayout.LayoutParams(900, LinearLayout.LayoutParams.WRAP_CONTENT));
            set.clone(parentLayout);
            // add constraints for new linear layout
            //if first instruction
            if (i == 0) {


                    //top to bottom of journey text
                    set.connect(childView.getId(), ConstraintSet.TOP, locationTop, ConstraintSet.BOTTOM);
                    //left to left of weather text
                    set.connect(childView.getId(), ConstraintSet.LEFT, locationLeft, ConstraintSet.LEFT);
                    params.topMargin = 0;

//                            set.connect(childView.getId(), ConstraintSet.RIGHT, parentLayout.getId(), ConstraintSet.RIGHT);
            } else { // if not first instruction
                //top to bottom of previous instruction
                set.connect(childView.getId(), ConstraintSet.TOP, priorId, ConstraintSet.BOTTOM);
                //left to left of previous instruction
                set.connect(childView.getId(), ConstraintSet.LEFT, priorId, ConstraintSet.LEFT);
            }
            // if last instruction
            if (i == journey.legs.size() - 1) {
                // set spacer to bottom of last instruction
                set.connect(R.id.scrollBottomSpace, ConstraintSet.TOP, childView.getId(), ConstraintSet.BOTTOM);
                set.connect(R.id.alternateScrollBottomSpace, ConstraintSet.TOP, childView.getId(), ConstraintSet.BOTTOM);
            }
            //set layout params
            childView.setLayoutParams(params);
            set.applyTo(parentLayout);
            // this this layout's id to prior id
            priorId = childView.getId();
        }

        return priorId;

    }

    void getJourneyData() {
        // call tfl service
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(tflURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        TflService service = retrofit.create(TflService.class);
        Call<TflJourneyResponse> call = service.getCurrentJourneyData(startPostcode, endPostcode, arriveDay, arriveTime.replace(":",""), "Arriving");

        call.enqueue(new Callback<TflJourneyResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(Call<TflJourneyResponse> call, Response<TflJourneyResponse> response) {
                if (response.code() == 200) {
                    // if respsonse is successful
                    TflJourneyResponse tflJourneyResponse = response.body();
                    assert tflJourneyResponse != null;
                    // display api call to console for debugging
                    Log.d("response", response.raw().toString());

                    // get start time of journey from response
                    String timeString = tflJourneyResponse.journeys.get(0).startDateTime;
                    // convert time string to a more readable format for display
                    timeString = timeString.substring(timeString.indexOf("T") + 1);
                    timeString = timeString.substring(0,timeString.lastIndexOf(":"));

                    // create string to display at start of journey
                    String stringBuilder = "Route for " + arriveTime + " arrival on " +
                            arriveDayOfWeek.toLowerCase() + ":" + "\nSet off at " + timeString;

                    // display info string on screen
                    journeyData.setText(stringBuilder);


                    // display main journey, prior id is the id of the last journey displayed
                    int priorId = displayJourney(tflJourneyResponse.journeys.get(0), R.id.journeyText, R.id.journeyText, R.id.parentLayout, false);


                    // get view to display all the alternate journeys in
                    ConstraintLayout parentLayout = (ConstraintLayout)findViewById(R.id.alternateConstraintView);
                    //loop through all remaining journeys and display them in alternate journeys section
                    for (int i = 1; i < tflJourneyResponse.journeys.size(); i++){

                        // create new text view for journey label
                        TextView journeyLabelView = new TextView(getApplicationContext());
                        journeyLabelView.setId(View.generateViewId());
                        journeyLabelView.setTextSize(18);
                        journeyLabelView.setTypeface(null, Typeface.BOLD);
                        String labelText = "Alternate Route " + i + ":";
                        journeyLabelView.setText(labelText);
                        parentLayout.addView(journeyLabelView);

                        ConstraintSet set = new ConstraintSet();

                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) journeyLabelView.getLayoutParams();
                        params.topMargin = 50;

                        set.clone(parentLayout);

                        // if first journey in list then display alternate journey at top of page
                        // if not then display underneath the prior journey added
                        if (i == 1) {
                            //top to bottom of journey text
                            set.connect(journeyLabelView.getId(), ConstraintSet.TOP, R.id.alternateText, ConstraintSet.BOTTOM);
                            //left to left of weather text
                            set.connect(journeyLabelView.getId(), ConstraintSet.LEFT, R.id.alternateText, ConstraintSet.LEFT);
                            set.applyTo(parentLayout);
                            priorId = displayJourney(tflJourneyResponse.journeys.get(i), journeyLabelView.getId(), journeyLabelView.getId(), R.id.alternateConstraintView, true);
                        } else {
                            //top to bottom of journey text
                            set.connect(journeyLabelView.getId(), ConstraintSet.TOP, priorId, ConstraintSet.BOTTOM);
                            //left to left of weather text
                            set.connect(journeyLabelView.getId(), ConstraintSet.LEFT, priorId, ConstraintSet.LEFT);
                            set.applyTo(parentLayout);
                            priorId = displayJourney(tflJourneyResponse.journeys.get(i), journeyLabelView.getId(), journeyLabelView.getId(), R.id.alternateConstraintView, true);
                        }

                    }

                }
            }

            @Override
            public void onFailure(Call<TflJourneyResponse> call, Throwable t) {
                // if error then display message in place of journey
                journeyData.setText(t.getMessage());
            }
        });

    }


    // checks which tube line is being referred to in string and returns it's associated colour
    public int getLineColour(String text) {
        // check which tube line has issue and change warning icon to that line's colour
        if (text.contains("Hammersmith and City Line")) {
            return R.color.tubePink;
        } else if (text.contains("Bakerloo")){
            return R.color.tubeBrown;
        } else if (text.contains("Circle")){
            return R.color.tubeYellow;
        } else if (text.contains("District Line")){
            return R.color.tubeGreen;
        } else if (text.contains("DLR")){
            return R.color.tubeCyan;
        }
        else if (text.contains("Central")){
           return R.color.tubeRed;
        } else if (text.contains("Jubilee")){
            return R.color.tubeGrey;
        } else if (text.contains("Metropolitan")){
            return R.color.tubeMagenta;
        } else if (text.contains("Northern")){
            return Color.BLACK;
        } else if (text.contains("Overground")){
            return R.color.tubeOrange;
        } else if (text.contains("Piccadilly")){
            return R.color.tubeBlue;
        } else if (text.contains("Tramlink")){
            return R.color.tubeLightGreen;
        } else if (text.contains("Victoria")){
            return R.color.tubeLightBlue;
        } else if (text.contains("Waterloo and City")){
            return R.color.tubeLightCyan;
        } else {
            // if no line is found just return black
            return Color.BLACK;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void displayTubeDisruptions(List<TflDisruptionResponse> tflDisruptionResponse) {

        // create prior id which will be used for adding new disruptions
        int priorId = 0;

        // get parent layout that all disruption data will be added to
        ConstraintLayout parentLayout = (ConstraintLayout)findViewById(R.id.disruptionsConstraintLayout);


        // for each line disruption found in the response
        for (int i = 0; i < tflDisruptionResponse.size(); i++) {

            // create constraints for adding new views
            ConstraintSet set = new ConstraintSet();

            // create linear layout for housing icon and text
            LinearLayout childView = new LinearLayout(getApplicationContext());
            childView.setId(View.generateViewId());
            //create image view for icon
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setId(View.generateViewId());
            //create text view for text
            TextView infoView = new TextView(getApplicationContext());
            infoView.setId(View.generateViewId());
            infoView.setTextSize(20);


            // get text with disruption information
            String text = tflDisruptionResponse.get(i).description;


            // set image to correct line colour
            imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), getLineColour(text)));

            // set image to warning symbol
            imageView.setImageDrawable(getDrawable(R.drawable.ic_warning_black));
            // set text for textview
            infoView.setText(text);

            // add icon and text to linear layout
            childView.addView(imageView);
            childView.addView(infoView);
            //making linear layout display horizontal
            childView.setOrientation(LinearLayout.HORIZONTAL);
            //add linear layout to parent view
            parentLayout.addView(childView, 0);

            // add margin to top of linear layout to space out text
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) childView.getLayoutParams();
            params.topMargin = 50;

            // set width of text view (required for text to wrap properly)
            infoView.setLayoutParams(new LinearLayout.LayoutParams(900, LinearLayout.LayoutParams.WRAP_CONTENT));

            // clone parent Layout constraints for adding new constraints
            set.clone(parentLayout);
            // add constraints for new linear layout
            //if first disruption
            if (i == 0) {
                //top to bottom of tube text
                set.connect(childView.getId(), ConstraintSet.TOP, R.id.tubeText, ConstraintSet.BOTTOM);
                //left to left of tube text
                set.connect(childView.getId(), ConstraintSet.LEFT, R.id.tubeText, ConstraintSet.LEFT);
                // top margin space not needed for first item
                params.topMargin = 0;
            } else { // if not first instruction
                //top to bottom of previous disruption
                set.connect(childView.getId(), ConstraintSet.TOP, priorId, ConstraintSet.BOTTOM);
                //left to left of previous disruption
                set.connect(childView.getId(), ConstraintSet.LEFT, priorId, ConstraintSet.LEFT);
            }
            // if last instruction
            if (i == tflDisruptionResponse.size() - 1) {
                // set spacer to bottom of last disruption
                // needed because of how the scroll view works with the back button
                set.connect(R.id.lineLogo, ConstraintSet.TOP, childView.getId(), ConstraintSet.BOTTOM);
                set.connect(R.id.lineLogo, ConstraintSet.LEFT, childView.getId(), ConstraintSet.LEFT);

                set.connect(R.id.disruptionScrollBottomSpace, ConstraintSet.TOP, R.id.lineLogo, ConstraintSet.BOTTOM);


            }
            //set layout params
            childView.setLayoutParams(params);

            set.applyTo(parentLayout);

            // this this layout's id to prior id
            priorId = childView.getId();

        }

    }

    // get current tube disruption data
    void getTubeData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(tflURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Log.d("url", retrofit.baseUrl().toString());
        TflService service = retrofit.create(TflService.class);
        Log.d("service", service.toString());

        Call<List<TflDisruptionResponse>> call = service.getCurrentTflData();
        call.enqueue(new Callback<List<TflDisruptionResponse>>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(Call<List<TflDisruptionResponse>> call, Response<List<TflDisruptionResponse>> response) {
                if (response.code() == 200) {
                    //if call was successful
                    // get list of all disruptions
                    List<TflDisruptionResponse> tflDisruptionResponse = response.body();
                    assert tflDisruptionResponse != null;

                    // log response url for debugging
                    Log.d("response", response.raw().toString());

                    // display list of disruptions if some are returned
                    if (tflDisruptionResponse.size() != 0){
                        displayTubeDisruptions(tflDisruptionResponse);
                    }


                }
            }

            @Override
            public void onFailure(Call<List<TflDisruptionResponse>> call, Throwable t) {
                // if call failed then display error message
                tubeData.setText(t.getMessage());
            }
        });
    }

    void getWeatherData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.getCurrentWeatherData("london", AppId, "metric");
        call.enqueue(new Callback<WeatherResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (response.code() == 200) {
                    // if response was successful
                    // get data from repsonse and check not null
                    WeatherResponse weatherResponse = response.body();
                    assert weatherResponse != null;

                    // get current weather description
                    String weatherDescription = weatherResponse.weather.get(0).description;

                    // create weather string, combining description and temperature
                    // originally the weather location would be decided by postcode
                    // but the api would give the same output for all london postcodes, so it was
                    // pointless, and i just set it to london
                    String stringBuilder = weatherDescription.substring(0, 1).toUpperCase() + weatherDescription.substring(1)  + " in London\n" +
                            "Temperature is " + weatherResponse.main.temp + "°C";


                    // code for checking which icon to use for the weather
                    // the only weather icons included with android studio are cloudy and sunny
                    // so those are the only two I check for at the moment
                    if (weatherResponse.weather.get(0).description.contains("cloud")){
                        weatherIcon.setImageDrawable(getDrawable(R.drawable.ic_wb_cloudy_blue));
                    }

                    // display weather data
                    weatherData.setText(stringBuilder);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                // if error then display error message
                weatherData.setText(t.getMessage());
            }
        });
    }


    // I have no idea what this is for?!?!?
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}