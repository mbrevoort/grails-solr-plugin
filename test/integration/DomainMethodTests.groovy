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
import org.grails.solr.Solr1Disabled
import org.grails.solr.Solr1Override
import org.grails.solr.Solr1Annotated
import org.grails.solr.Listing
import org.grails.solr.SolrUtil
import org.grails.solr.TESTENUM
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer
import org.apache.solr.client.solrj.response.QueryResponse

class DomainMethodTests extends GrailsUnitTestCase {
	
	
	
	def solrService
	
  protected void setUp() {
      super.setUp()
			mockLogging(SolrUtil, true)
  }

  protected void tearDown() {
      super.tearDown()
  }	
	
	
	void testBasicIndex() {
		def now = new Date()
		def s = new Solr1(astring: "mystring", aint: 2, afloat: 1.2f, adate: now).save()
		assert(s != null && s.id && s.id > 0)
		
		s.indexSolr()
		def server = solrService.getServer()
		QueryResponse rsp = server.query( new SolrQuery("id:${s.solrId()}") );
		def docs = rsp.getResults();
		docs.each {
			assertEquals(it.getFieldValue("id"), s.solrId())
			assertEquals(it.getFieldValue( s.solrFieldName("astring") ), "mystring")
			assertEquals(it.getFieldValue( s.solrFieldName("aint") ), 2)
			assertEquals(it.getFieldValue( s.solrFieldName("afloat") ), 1.2f)
			assertEquals(it.getFieldValue( s.solrFieldName("adate") ).getTime(), now.getTime())	// for some reason the equal assertion fails... sometimes
		}
		
	}
	
	void testSolrFieldName() {
		def s = new Solr1(astring: "mystring", aint: 2, afloat: 1.2f, adate: new Date()).save()
		assert s.solrFieldName("aint") == "aint_i"
	}
	
	
	/**
	* This tests two things. First if the hasmany functionality is working and second that the
	* override for a hasmany is working.
	*/
	void testHasManyOverride() {
		def s2 = new Solr2(astring: "mystring", aint: 2, afloat: 1.2f, adate: new Date(), categories: "mike,joe,chad").save()
		def s1a = new Solr1(astring: "mystring", aint: 2, afloat: 1.2f, adate: new Date()).save()
		def s1b = new Solr1(astring: "mystring", aint: 2, afloat: 1.2f, adate: new Date()).save()
		def s1c = new Solr1(astring: "mystring", aint: 2, afloat: 1.2f, adate: new Date()).save()
		s2.addToSolrs(s1a)
		s2.addToSolrs(s1b)
		s2.addToSolrsWithOverride(s1c)
		s2.save()
		s2.indexSolr()
		
		def server = solrService.getServer()
		QueryResponse rsp = server.query( new SolrQuery("id:${s2.solrId()}") );
		def docs = rsp.getResults();
		assertEquals(docs.size(), 1)
		assertEquals(docs[0].getFieldValue("arr_solrsWithOverride"), SolrUtil.getSolrId(s1c))		
	}
	
	void testDelete() {
		def now = new Date()
		def s = new Solr1(astring: "mystring", aint: 2, afloat: 1.2f, adate: now).save()
		s.indexSolr()
		s.deleteSolr()
		def server = solrService.getServer()
		QueryResponse rsp = server.query( new SolrQuery("id:${s.solrId()}") );
		assertEquals(rsp.getResults().numFound, 0);
		
	}

	/* test delete with hibernate events */
	void testDeleteByEvent() {
		def now = new Date()
		def s = new Solr1(astring: "testdelete", aint: 2, afloat: 1.2f, adate: now).save()
		def result = s.searchSolr("id:${s.solrId()}")
		//println "${s.solrId()}"
		assertEquals(1, result.queryResponse.getResults().numFound);
		s.delete(flush:true)
		result = s.searchSolr("id:${s.solrId()}")
		assertEquals(0, result.queryResponse.getResults().numFound);		
	}
	
	void testBasicSearch() {
		def now = new Date()
		def s = new Solr1(astring: "mytestsearchstring", aint: 2, afloat: 1.2f, adate: now).save()
		s.indexSolr()
		def result = s.searchSolr("${s.solrFieldName('astring')}:mytestsearchstring")
		def foundit = false
		result.resultList.each {
			if(it.id == s.id) {
				assertEquals(it.astring, s.astring)
				assertEquals(it.aint, s.aint)
				assertEquals(it.afloat, s.afloat)
				assertEquals(it.adate, s.adate)

				foundit = true
			}
		}
		
		assertTrue (foundit)
	}
	
	void testBasicOverride() {
		def s = new Solr1Override(astring: "mystring", aint: 2, afloat: 1.2f, adate: new Date()).save()
		assert(s != null && s.id && s.id > 0)
		s.indexSolr()
		def result = s.searchSolr("${s.solrFieldName('astring')}:MYSTRING")
		def foundit = false
		result.resultList.each {
			println "${it.id} == ${s.id}"
			if(it.id == s.id) {
				assertEquals(it.astring, s.astring.toUpperCase())
				foundit = true
			}
		}	
		
		assertTrue (foundit)
		QueryResponse rsp = result.queryResponse
		def docs = rsp.getResults();
		assertEquals(docs.size(), 1)
		assertEquals(docs[0].getFieldValue( s.solrFieldName("astring") ), s.astring.toUpperCase())		
			
	}
	
	
	void testSearchWithHasManyOverride() {
		def s2 = new Solr2(astring: "testSearchWithHasManyOverride", aint: 2, afloat: 1.2f, adate: new Date(), categories: "mike,joe,chad").save()
		def s1a = new Solr1(astring: "mystring", aint: 2, afloat: 1.2f, adate: new Date()).save()
		def s1b = new Solr1(astring: "mystring", aint: 2, afloat: 1.2f, adate: new Date()).save()
		def s1c = new Solr1(astring: "mystring", aint: 2, afloat: 1.2f, adate: new Date()).save()
		s2.addToSolrs(s1a)
		s2.addToSolrs(s1b)
		s2.addToSolrsWithOverride(s1c)
		s2.save()
		s2.indexSolr()
		def result = s2.searchSolr("id:${s2.solrId()}")
		def foundit = false
		result.resultList.each {
			if(it.id == s2.id) {
				assertEquals(it.astring, s2.astring)
				assertEquals(it.aint, s2.aint)
				assertEquals(it.afloat, s2.afloat)
				assertEquals(it.adate.getTime(), s2.adate.getTime())	
				
				s2.attach()
				assertEquals(s2.categories, "mike,joe,chad")

				foundit = true
			}
		}
		
		assertTrue (foundit)
	}
	
	void testTitleIndex() {
		def s = new Solr1(astring: "mystring", aint: 2, afloat: 1.2f, adate: new Date()).save()
		assert(s != null && s.id && s.id > 0)
		
		s.indexSolr()
		def server = solrService.getServer()
		QueryResponse rsp = server.query( new SolrQuery("id:${s.solrId()}") );
		def docs = rsp.getResults();
		docs.each {
			assertEquals(it.getFieldValue(SolrUtil.TITLE_FIELD), "myobjecttitle")
		}
		
	}
	
	void testStrippedSolrFieldName() {
		def now = new Date()
		def s = new Solr1(astring: "strippedSolrFieldName", aint: 2, afloat: 1.2f, adate: now)
		assertEquals( "astring", SolrUtil.stripFieldName(s.solrFieldName("astring")))
		assertEquals( "aint", SolrUtil.stripFieldName(s.solrFieldName("aint")))
		assertEquals( "afloat", SolrUtil.stripFieldName(s.solrFieldName("afloat")))
		assertEquals( "adate", SolrUtil.stripFieldName(s.solrFieldName("adate")))
	}

	void testListingDomain() {
		def now = new Date()
		def listing1 = new Listing(title: "Church of Castle Rock", address1: "1 My St", city: "Castle Rock", state: "CO", postalCode: "80104", country: "US", latitude: 39.37436, longitude: -104.859929).save()
        def listing2 = new Listing(title: "Church of Parker", address1: "2 My St", city: "Parker", state: "CO", postalCode: "80104", country: "US", latitude: 39.517363, longitude: -104.785984).save()
		//def s = new Listing(title: "mystring", aint: 2, afloat: 1.2f, adate: now).save()
		assert(listing1 != null && listing1.id && listing1.id > 0)
		
		listing1.indexSolr()
		listing2.indexSolr()
		def server = solrService.getServer()
		QueryResponse rsp = server.query( new SolrQuery("id:${listing1.solrId()}") );
		def docs = rsp.getResults();
		docs.each {
			assertEquals(it.getFieldValue("id"), listing1.solrId())
			assertEquals(it.getFieldValue( listing1.solrFieldName("title") ), "Church of Castle Rock")
		}
		
		def searchResult = Listing.searchSolr("${Listing.solrFieldName('title')}:Church of Castle Rock")
		assertNotNull(searchResult)
		assertNotNull(searchResult.resultList)
		searchResult.resultList.each {
			assertEquals(it.title, "Church of Castle Rock")
		}

		searchResult = Listing.searchSolr("${Listing.solrFieldName('title')}:Church of Parker")
		assertNotNull(searchResult)
		assertNotNull(searchResult.resultList)
		searchResult.resultList.each {
			assertEquals(it.title, "Church of Parker")
		}		
		
	}
	
	void testSolrDisabled() {
		def now = new Date()
		def s = new Solr1Disabled(astring: "strippedSolrFieldName", aint: 2, afloat: 1.2f, adate: now).save()
		
		try {
			s.indexSolr()
			assert false
		} catch(groovy.lang.MissingMethodException e) {
			assert true
		}
		
	}
	
/*	commented out because removed composition property indexing because of index staleness issues
	void testComposition() {
		def now = new Date()
		def s2a = new Solr2(astring: "tobecomposed1", aint: 2555, afloat: 1212.2f, adate: now).save()
		def s2b = new Solr2(astring: "tobecomposed2", aint: 2555, afloat: 1212.2f, adate: now, composition: s2a).save()
		def s2c = new Solr2(astring: "testcomp", aint: 3, afloat: 14.2f, adate: new Date(), categories: "inner,outer", composition: s2b).save()
		s2c.indexSolr()	
		def result = Solr2.searchSolr("id:${s2c.solrId()}")		
		QueryResponse rsp = result.queryResponse
		def docs = rsp.getResults();
		assertEquals(docs.size(), 1)
		assertEquals(docs[0].getFieldValue( s2c.solrFieldName("composition.astring") ), s2b.astring)		
		assertEquals(docs[0].getFieldValue( s2c.solrFieldName("composition.composition.astring") ), s2a.astring)	
		
		assertEquals(result.resultList[0]."composition.astring", s2b.astring)		
		assertEquals(result.resultList[0]."composition.composition.astring", s2a.astring)				
	}
*/
	
	void testDomainProperty() {
		def now = new Date()
		def s2a = new Solr2(astring: "tobecomposed1", aint: 2555, afloat: 1212.2f, adate: now).save()
		def s2b = new Solr2(astring: "tobecomposed2", aint: 2555, afloat: 1212.2f, adate: now, composition: s2a).save()
		def s2c = new Solr2(astring: "testcomp", aint: 3, afloat: 14.2f, adate: new Date(), categories: "inner,outer", composition: s2b).save()
		s2c.indexSolr()	
		def result = Solr2.searchSolr("id:${s2c.solrId()}")		
		QueryResponse rsp = result.queryResponse
		def docs = rsp.getResults();
		assertEquals(docs.size(), 1)
		assertEquals(docs[0].getFieldValue( s2c.solrFieldName("composition") ), SolrUtil.getSolrId(s2b))		
	}
	
	void testAnnotation() {
		def now = new Date()
		def s = new Solr1Annotated(astring: "annotatedSolrFieldName", bstring: "should be text", aint: 2, afloat: 1.2f, adate: now).save()

		s.indexSolr()
		def server = solrService.getServer()
		QueryResponse rsp = server.query( new SolrQuery("id:${s.solrId()}") );
		def docs = rsp.getResults();
		assertEquals(docs.size(), 1)
		docs.each {
			assertEquals(it.getFieldValue("id"), s.solrId())
			assertEquals(it.getFieldValue( "astringanothername_s" ), "annotatedSolrFieldName")
			// should have a suffix of _t if text
			assertEquals(it.getFieldValue( "bstring_t") , s.bstring)
		}
	}
		
}