/*
 * class: TflService.java
 * description: a service for making calls to the tfl api
 * author: M Beanland
 * date created: 02/02/2021
 * date modified: 14/02/2021
 *
 * */


package com.example.thundertube;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TflService {
    @GET("Line/Mode/tube/Disruption?")
    Call<List<TflDisruptionResponse>> getCurrentTflData();

    @GET("Journey/JourneyResults/{startPostcode}/to/{endPostcode}?")
    Call<TflJourneyResponse> getCurrentJourneyData(@Path("startPostcode") String startPostcode, @Path("endPostcode") String endPostcode,
                                                   @Query("date") String date, @Query("time") String time, @Query("timeIs") String timeIs);



}
