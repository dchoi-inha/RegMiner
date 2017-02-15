/*
 * Copyright 2014 Lynden, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file includes Java implementations of some of the v3_epolys.js 
 * extensions to LatLng for Javascript by Mike Williams and Larry Ross.
 * See included file src/main/resource/html/v3_epoly.js for their original 
 * licence.
 */
package com.lynden.gmapsfx.javascript.object;

import com.lynden.gmapsfx.javascript.JavascriptObject;
import netscape.javascript.JSObject;

/**
 *
 * @author Rob Terpilowski
 */
public class WeightedLocation extends JavascriptObject {

	private double latitude;
	private double longitude;
	private double weight;

    public WeightedLocation(double latitude, double longitude, double weight) {
    	super(GMapObjectType.WEIGHTED_LOCATION, buildJavascriptString(new LatLong(latitude, longitude), weight));
    	this.latitude = latitude;
    	this.longitude = longitude;
        this.weight = weight;
    }

    public WeightedLocation(JSObject jsObject) {
        super(GMapObjectType.WEIGHTED_LOCATION, jsObject);
    }

    public double getWeight() {
    	return invokeJavascriptReturnValue("weight", Number.class ).doubleValue();
    }
    
    
    public static String buildJavascriptString(LatLong location, double weight){
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("location: ").append(location.getVariableName()).append("");
        builder.append(",");
        builder.append("weight: ").append(weight).append("");
        builder.append("}");
//        System.out.println("COMPONENT " + builder.toString());
        return builder.toString();
    }


    @Override
    public String toString() {
        return "lat: " + String.format("%.8G", latitude) + " lng: " + String.format("%.8G", longitude) + " w: " + String.format("%.3G", weight);
    }

}
