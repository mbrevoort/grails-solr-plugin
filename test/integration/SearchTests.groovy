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
import org.grails.solr.Solr2
import org.grails.solr.Listing
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.QueryResponse

class SearchTests extends GroovyTestCase {
	
	def solrService

	void testBasicQuery() { 
		def s = new Solr1(astring: "testBasicQuery", aint: 2, afloat: 1.2f, adate: new Date()).save()
		assert(s != null && s.id && s.id > 0)
		
		def result = solrService.search("id:${s.solrId()}")

		assertNotNull(result)		
		assertNotNull(result.resultList)
		assertNotNull(result.queryResponse)						
		assert(result.resultList instanceof java.util.ArrayList)
		assert(result.queryResponse instanceof org.apache.solr.client.solrj.response.QueryResponse)
		assertEquals(result.resultList.size(), 1)
		assertEquals(result.resultList[0].astring, s.astring)
		assertEquals(result.queryResponse.getResults().size(), 1)
		assertEquals(result.queryResponse.getResults()[0].getFieldValue( s.solrFieldName("astring")), s.astring)		
	}
	
	void testFacetedQuery() {
		new Solr1(astring: "one", aint: 9999, afloat: 1.2f, adate: new Date()).save()
		new Solr1(astring: "one", aint: 9999, afloat: 1.2f, adate: new Date()).save()
	  new Solr1(astring: "two", aint: 9999, afloat: 1.2f, adate: new Date()).save()	  
	  
	  def query = new SolrQuery("${Solr1.solrFieldName('aint')}:9999")
	  query.facet = true
	  query.addFacetField(Solr1.solrFieldName("astring"))
	  def result = Solr1.searchSolr(query)
	  
	  def facetField = result.queryResponse.getFacetField(Solr1.solrFieldName("astring"))
	  assertNotNull facetField

	  facetField.getValues().each { 
      if(it.getName()  == "one") {
        assertEquals(2, it.getCount() )
      } else if (it.getName()  == "two") {
        assertEquals(1, it.getCount() )
      } 
	  }	  
	}
	
	void testQuerySpatial() {
		def listing1 = new Listing(title: "Church of Castle Rock", address1: "1 My St", city: "Castle Rock", state: "CO", postalCode: "80104", country: "US", latitude: 39.37436, longitude: -104.859929).save()
        def listing2 = new Listing(title: "Church of Parker", address1: "2 My St", city: "Parker", state: "CO", postalCode: "80104", country: "US", latitude: 39.517363, longitude: -104.785984).save()
		
		assertNotNull(listing1)
		assertNotNull(listing2)
		listing1.indexSolr()
		listing2.indexSolr()
		//-104.9847034, 39.7391536
		def result = solrService.querySpatial("id:${listing1.solrId()} OR id:${listing2.solrId()}", 39.7391536d, -104.9847034d, 100)
		
		assertNotNull(result)		
		assertNotNull(result.resultList)
		assertNotNull(result.queryResponse)						
		assert(result.resultList instanceof java.util.ArrayList)
		assert(result.queryResponse instanceof org.apache.solr.client.solrj.response.QueryResponse)
		assertEquals(result.resultList.size(), 2)
		assertNotNull(result.resultList[0].dist)
		assert(result.resultList[0].dist > 0)
		assert(result.resultList[0].id == listing2.id)
	}
	
	void testStreamingUpdateServer() {
		def streamingServer = solrService.getStreamingUpdateServer(20,3)
		def solrServer = solrService.getServer()
		assertNotNull(solrServer)
		
		solrServer.deleteByQuery("astring:testingstreamingupdateserver")
		100.times {
			def s = new Solr2(astring: "testingstreamingupdateserver", aint: 2, afloat: 1.2f, adate: new Date()).save()
			assert(s != null && s.id && s.id > 0)		
			s.indexSolr(streamingServer)	
		}
		streamingServer.commit()
		def result = Solr2.searchSolr("${Solr2.solrFieldName("astring")}:testingstreamingupdateserver")
		assertEquals(100, result.queryResponse.getResults().numFound);

	}
}