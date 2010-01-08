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
import org.apache.log4j.Logger;

class SolrUtil {
  
  private static final Logger log = Logger.getLogger(SolrUtil)
  
  def static final TYPE_FIELD = "doctype_s"
  def static final TITLE_FIELD = "title_t"
  def static final IGNORED_PROPS = ["attached", "errors", "constraints", "metaClass", "log", "class", "version", "id", "hasMany", "domainClass"]
  
  /*
    Solr dynamic field datatype suffix mapping
  */
  def static final typeMapping = ["int":"_i", 
                  "float":"_f", 
                  "long":"_l",
                  "boolean":"_b",
                  "double": "_d",
                  "class java.lang.String":"_s",  // _t for text, _s for string
                  "class java.util.Date": "_tdt"] 
              
  def static final typeSuffixes = ["_i", "_s", "_l", "_t", "_b", "_f", "_d", "_dt", "_ti", "_tl", "_tf", "_td", "_tdt", "_pi"]            
  static stripFieldName(name){
    //def strippedName = name
    typeSuffixes.each {
      if(name.endsWith(it))
        name = name - it
    }   
    return name     
  }
  
  
  /**
  * Return a map with values class and id parse from the SolrDomainId
  * ie Book-21 would return [class: "Book", id: 21]
  */
  static parseSolrId(solrDomainId) {
    if(solrDomainId && solrDomainId.contains("-")) {
      def dash = solrDomainId.lastIndexOf("-")
      return [class: solrDomainId.substring(0, dash), id: solrDomainId.substring(dash+1) as long]
    } else {
      return null
    }
  } 
  
  /**
  * Get the Id that will be stored in the SOLR index: ClassName-Id
  */
  static getSolrId(aClass) {
    "${aClass.class.name}-${aClass.id}"
  } 
  
  /**
   * Parse a Solrj result document and attempt to return an object
   */
  static resultAsObject(doc) {
    def classType = doc.getFieldValue(SolrUtil.TYPE_FIELD)
    log.trace "classType for doc is ${classType}"
    
    if(!classType)
      return
      
    def obj =  Thread.currentThread().contextClassLoader.loadClass(classType).newInstance()
    log.trace "Object is ${obj} for classType ${classType}"
    
    if(!obj)
      return
      
    doc.getFieldNames().each { 
      log.trace "Processing field ${it}"
      if(!IGNORED_PROPS.contains(it)) {
        log.trace "Field ${it} not ignored"
        
        //TODO handle hasmany props and the like
        try {
          def theField = stripFieldName(it)
          def overrideMethodName = "fromSolr${theField[0].toUpperCase()}${theField.substring(1)}" 
          def overrideMethod = obj.metaClass.getMetaMethod(overrideMethodName)
          if(overrideMethod != null) 
            obj."${stripFieldName(it)}" = overrideMethod.invoke(delegateDomainOjbect, doc.getFieldValue(it))
          else 
            obj."${theField}" = doc.getFieldValue(it)
            
          log.trace "obj.${theField} = ${doc.getFieldValue(it)}"
    
        } catch (Exception ex) { 
          log.debug ("Couldn't parse field ${it} into object ${ex}")
          //println "oh no! blew up on ${it}"
        }
      }
    }

    obj.id = parseSolrId(doc.getFieldValue("id"))?.id
    return obj
  } 
  
}