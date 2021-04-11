/*
 * class: TflJourneyResponse.java
 * description: a class for handling the json output from the tfl api disruption request
 * author: M Beanland
 * date created: 02/02/2021
 * date modified: 14/02/2021
 *
 * */

package com.example.thundertube;

import com.google.gson.annotations.SerializedName;

// various classes to cover each aspect of the tfl api response

public class TflDisruptionResponse {

    @SerializedName("description")
    public String description;

}


class Status{
    @SerializedName("statusSeverity")
    public String severityLevel;

    @SerializedName("statusSeverityDescription")
    public String severityLevelDescription;

}

