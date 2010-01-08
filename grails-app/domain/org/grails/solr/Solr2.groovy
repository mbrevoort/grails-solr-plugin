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
class Solr2 {
  static enableSolrSearch = true

  String astring
  int aint
  long along
  Date adate
  float afloat
  Solr2 composition
  TESTENUM testenum
  
  String categories

  static hasMany = [ solrs: Solr1, solrsWithOverride: Solr1 ]
  static constraints = {
    composition(nullable: true)
    categories(nullable: true)
    testenum(nullable:true)
  }

  
  def indexSolrCategories(doc) {
    if(categories) {
      categories.split(",").each {
//        doc.addField(this.solrFieldName("categories"), it)
        doc.addField("arr_categories", it)
      }
    } 
  }
  
  
  def indexSolrSolrsWithOverride(doc) {
    solrsWithOverride.each {
//      doc.addField(this.solrFieldName("solrsWithOverride"), it.solrId())
      doc.addField("arr_solrsWithOverride", it.solrId())
    }
  }
}