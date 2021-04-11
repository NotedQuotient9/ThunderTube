/*
 * class: WeatherService.java
 * description: a service for making calls to the openWeather api
 * author: M Beanland
 * date created: 02/02/2021
 * date modified: 14/02/2021
 *
 * */

package com.example.thundertube;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("data/2.5/weather?")
    Call<WeatherResponse> getCurrentWeatherData(@Query("q") String city,
                                                @Query("APPID") String app_id, @Query("units") String units);
}
