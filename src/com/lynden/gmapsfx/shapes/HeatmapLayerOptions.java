/*
 * Copyright 2014 Geoff Capper.
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
 */

package com.lynden.gmapsfx.shapes;

import com.lynden.gmapsfx.javascript.JavascriptObject;
import com.lynden.gmapsfx.javascript.object.GMapObjectType;
import com.lynden.gmapsfx.javascript.object.MVCArray;

/**
 *
 * @author Geoff Capper
 */
public class HeatmapLayerOptions extends JavascriptObject {
    private MVCArray data;
    private double opacity = 0.2;
    private double radius;
    
    public HeatmapLayerOptions() {
    	super(GMapObjectType.OBJECT);
    }

	public HeatmapLayerOptions gradient(MVCArray rgbarray) {
        setProperty("gradient", rgbarray);
        return this;
    }
    
    public HeatmapLayerOptions opacity(double opacity) {
        setProperty("opacity", opacity);
        this.opacity = opacity;
        return this;
    }
    
    public HeatmapLayerOptions radius(double radius) {
        setProperty("radius", radius);
        this.radius = radius;
        return this;
    }
    
    public HeatmapLayerOptions data(MVCArray heatMapData) {
        setProperty("data", heatMapData);
        this.data = heatMapData;
        return this;
    }


}
