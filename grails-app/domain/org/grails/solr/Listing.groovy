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

class Listing {

  static enableSolrSearch = true

  String title
  String categories

  int sicCode
  String sicDescription

  String address1
  String address2
  String city
  String state
  String postalCode
  String phone
  String country = "US"
  double latitude
  double longitude

  float averageRating = 0
  Date averageLastUpdated

  Date dateCreated
  Date lastUpdated

  // Enhanced Listing attributes

  URL url
  String email
  String leadershipContact
  String longDescription
  String serviceDetails
 

  def replaceCategories(String[] cats) {
    categories = ""
    def i = 0
    //if(cats instanceof List) {
    cats?.each {
      categories = categories + ((i++ == 0) ? "*${it}*" : ",*${it}*")
    }
    //} else {
    ///  categories = "*${cats}*"
    //}
  }

  def addCategory(String cat) {
    if(cat && !categories?.contains("*${cat}*")) {
      def cats = getCategoriesAsList()
      cats << cat
      this.replaceCategories((String[]) cats.toArray())
    }
  }

  def getCategoriesAsList = {
    def list = []
    def cats = categories?.split(",")
    cats.each {
      list.add(it - "*" - "*")
    }

    return list

  }

  def indexSolrLatitude(doc) {
    doc.addField(this.solrFieldName("latitude"), latitude )
    doc.addField("latitude_rad_d", Math.toRadians( latitude ))
  }
  def indexSolrLongitude(doc) {
    doc.addField(this.solrFieldName("longitude"), longitude )
    doc.addField("longitude_rad_d", Math.toRadians( longitude ))
  }

  static constraints = {
  title(blank: false, maxSize:50)
  url(nullable:true, url: true)
  categories(nullable: true)

  address1(blank: false, maxSize:50)
  address2(nullable: true, maxSize:50)
  city(blank: false, maxSize:50)
  state(blank: false, maxSize:25)
  postalCode(blank: false, maxSize:9)
  phone(nullable:true, maxSize:25)

  longDescription(nullable: true, maxSize:2500)
  serviceDetails(nullable: true, maxSize:2500)
  averageLastUpdated(nullable: true)

  sicCode(nullable:true)
  sicDescription(nullable:true)
  leadershipContact(nullable:true)
  email(nullable:true, email:true)
  }
}
