/*
* Copyright 2010 the original author or authors.
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
* ----------------------------------------------------------------------------
* Original Author: Mike Brevoort, http://mike.brevoort.com
* Project sponsored by:
*     Avalon Consulting LLC - http://avalonconsult.com
*     Patheos.com - http://patheos.com
* ----------------------------------------------------------------------------
*/

package org.grails.solr
/**
 * Haversine algorithmu
 * Derived from http://www.movable-type.co.uk/scripts/latlong.html)
 */
class Haversine {
    static final double Rk = 6371
	static final double Rm = 3963.205
	
	static double computeMi(p1_lat, p1_lng, p2_lat, p2_lng) {
		compute(p1_lat, p1_lng, p2_lat, p2_lng, Rm)
	}

	static double computeKm(p1_lat, p1_lng, p2_lat, p2_lng) {
		compute(p1_lat, p1_lng, p2_lat, p2_lng, Rk)
	}
	
    static double compute(p1_lat, p1_lng, p2_lat, p2_lng, rad) {
        def dLat = Math.toRadians(p2_lat - p1_lat);
        def dLon = Math.toRadians(p2_lng - p1_lng);
        def a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos( Math.toRadians(p1_lat) ) *
                Math.cos( Math.toRadians(p2_lat) ) * Math.sin(dLon/2) * Math.sin(dLon/2);
        def c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        def d = rad * c;
        return d
    }

}