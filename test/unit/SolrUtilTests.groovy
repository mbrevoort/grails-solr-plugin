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
import org.grails.solr.SolrUtil

class SolrUtilTests extends GrailsUnitTestCase {	

   protected void setUp() {
       super.setUp()
   }

   protected void tearDown() {
       super.tearDown()
   }

  void testSolrId() {	
		def s = new Solr1(astring: "mystring", aint: 2, afloat: 1.2f, adate: new Date())
		s.id = 25
		def solrId = SolrUtil.getSolrId(s);
		assertEquals(solrId, "org.grails.solr.Solr1-25")

		def parsed = SolrUtil.parseSolrId(solrId)
		assertEquals("org.grails.solr.Solr1", parsed.class)
		assertEquals(25, parsed.id)
		assertEquals(SolrUtil.parseSolrId(solrId).id, 25)
  }

	void testReinstantiateClass() {		
		def s = new Solr1(astring: "mystring", aint: 2, afloat: 1.2f, adate: new Date(), id: 25)
		def solrId = SolrUtil.getSolrId(s);		
		def parsed = SolrUtil.parseSolrId(solrId)
		
		def theClass =  Thread.currentThread().contextClassLoader.loadClass("org.grails.solr.Solr1").newInstance()
		theClass.id = 25
		assertEquals(theClass.class.name, s.class.name)		
	}
	

}
