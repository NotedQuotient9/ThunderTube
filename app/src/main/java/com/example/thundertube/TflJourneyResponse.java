/*
 * class: TflJourneyResponse.java
 * description: a class for handling the json output from the tfl api journey plannner request
 * author: M Beanland
 * date created: 03/02/2021
 * date modified: 14/02/2021
 *
 * */

package com.example.thundertube;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// various classes to cover each aspect of the tfl api response
class TflJourneyResponse {

    @SerializedName("journeys")
    public List<Journey> journeys;

    @SerializedName("lines")
    public List<Line> lines;

}

class Journey {
    @SerializedName("arrivalDateTime")
    public String arrivalDateTime;

    @SerializedName("startDateTime")
    public String startDateTime;

    @SerializedName("legs")
    public List<Leg> legs;
}

class Leg {

    @SerializedName("instruction")
    public Instruction instruction;

}

class Instruction {

    @SerializedName("summary")
    public String summary;

}

class Line {
    @SerializedName("name")
    public String name;

    @SerializedName("modeName")
    public String modeName;

    @SerializedName("lineStatuses")
    public List<Status> statuses;
}

