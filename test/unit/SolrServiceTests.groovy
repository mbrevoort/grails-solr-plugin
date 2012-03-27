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

import grails.test.*
import org.grails.solr.Solr1
import org.grails.solr.SolrService

class SolrServiceTests extends GrailsUnitTestCase {
	
	
	
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

	
/*	void testSpatialSearchUrl() {
		def solrService = new SolrService()
		
		def theUrl = solrService.getSpatialSearchURL("title_s:church", 39.5186002d, -104.7613633d, 50)
		def expected = "http://localhost:8983/solr/select/?q=(title_s:church)%20AND%20_val_:hsin(0.6897296892692897,-1.828430718462952,latitude_radians_d,longitude_radians_d,3963.205)&fq={!frange%20l=0%20u=50}hsin(0.6897296892692897,-1.828430718462952,latitude_radians_d,longitude_radians_d,3963.205)&start=0&rows=10&fl=score&sort=score%20asc"
		assertEquals(expected, theUrl)
		theUrl = solrService.getSpatialSearchURL("title_s:church", 39.5186002d, -104.7613633d, 50, 10, 15, "desc", 4000, "lat", "lng")
		expected = "http://localhost:8983/solr/select/?q=(title_s:church)%20AND%20_val_:hsin(0.6897296892692897,-1.828430718462952,lat,lng,4000)&fq={!frange%20l=0%20u=50}hsin(0.6897296892692897,-1.828430718462952,lat,lng,4000)&start=10&rows=15&fl=score&sort=score%20desc"
		assertEquals(expected, theUrl)
	}*/
	
	void testSpatialQueryBuilder() {
		def solrService = new SolrService()
		def query = solrService.getSpatialQuery("title_s:church", 39.5186002d, -104.7613633d, 50, 10, 15, "desc", "", 4000, "latitude_rad_d", "longitude_rad_d") 
		def expected = "q=%28title_s%3Achurch%29+AND+_val_%3A%22sum%28hsin%284000%2Cfalse%2C0.6897296892692897%2C-1.828430718462952%2Clatitude_rad_d%2Clongitude_rad_d%29%29%22&fq=%7B%21frange+l%3D0+u%3D50%7Dhsin%284000%2Cfalse%2C0.6897296892692897%2C-1.828430718462952%2Clatitude_rad_d%2Clongitude_rad_d%29&start=10&rows=15&sort=score+desc"
		// ok this is a pretty lame test but it's better than nothing
		assertEquals(expected, query.toString())

		query = solrService.getSpatialQuery("", 39.5186002d, -104.7613633d, 50, 10, 15, "desc", "", 4000, "latitude_rad_d", "longitude_rad_d") 
		expected = "q=_val_%3A%22sum%28hsin%284000%2Cfalse%2C0.6897296892692897%2C-1.828430718462952%2Clatitude_rad_d%2Clongitude_rad_d%29%29%22&fq=%7B%21frange+l%3D0+u%3D50%7Dhsin%284000%2Cfalse%2C0.6897296892692897%2C-1.828430718462952%2Clatitude_rad_d%2Clongitude_rad_d%29&start=10&rows=15&sort=score+desc"
		assertEquals(expected, query.toString())

		query = solrService.getSpatialQuery(" ", 39.5186002d, -104.7613633d, 50, 10, 15, "desc", "", 4000, "latitude_rad_d", "longitude_rad_d") 
		expected = "q=_val_%3A%22sum%28hsin%284000%2Cfalse%2C0.6897296892692897%2C-1.828430718462952%2Clatitude_rad_d%2Clongitude_rad_d%29%29%22&fq=%7B%21frange+l%3D0+u%3D50%7Dhsin%284000%2Cfalse%2C0.6897296892692897%2C-1.828430718462952%2Clatitude_rad_d%2Clongitude_rad_d%29&start=10&rows=15&sort=score+desc"
		assertEquals(expected, query.toString())

	}
}
