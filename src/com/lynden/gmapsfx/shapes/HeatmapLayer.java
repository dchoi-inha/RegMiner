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
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MVCArray;
import com.lynden.gmapsfx.javascript.object.MapShape;
import netscape.javascript.JSObject;

/**
 *
 * @author Geoff Capper
 */
public class HeatmapLayer extends JavascriptObject {
	
    public void setMap(GoogleMap map) {
        invokeJavascript("setMap", map);
    }
    
    public HeatmapLayer() {
        super(GMapObjectType.HEAT_MAP);
    }
    
    public HeatmapLayer(HeatmapLayerOptions opts) {
        super(GMapObjectType.HEAT_MAP, opts);
    }
    
    public void setData(MVCArray heatMapData) {
        invokeJavascript("setData", heatMapData);
    }
    
    public void setOptions(HeatmapLayerOptions opts) {
    	invokeJavascript("setOptions", opts);
    }

}
