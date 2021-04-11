/*
* class: Commute.java
* description: a simple class to store user information
* author: M Beanland
* date created: 12/02/2021
* date modified: 14/02/2021
*
* */


package com.example.thundertube;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.List;

public class Commute implements Serializable {

    public String startPostcode;

    public String endPostcode;

    public String arrivalTime;

    public List<DayOfWeek> arrivalDaysList;

}
