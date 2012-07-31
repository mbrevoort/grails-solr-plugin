package org.grails.solr

import org.apache.solr.client.solrj.SolrQuery

class SolrSearchController {

    def solrService

    def index = {

        List fq = []

        def query = new SolrQuery("${params.q}")

        if (params.fq) {
            query.addFilterQuery(params.fq)
            if (params.fq instanceof String)
                fq << params.fq
            else
                fq = params.fq
        }
        if (params.offset) {
            query.setStart(params.offset as int)
        }
        if (params.max) {
            query.setRows(params.max as int)
        }

        if (params.list('facetfield')) {
            query.facet = true
            params.list('facetfield').each {
                query.addFacetField(it)
            }
            query.setFacetMinCount(1)
            query.setFacetLimit(10)
        }

        [result: solrService.search(query), q: params.q, fq: fq, solrQueryUrl: query.toString()]
    }
}
