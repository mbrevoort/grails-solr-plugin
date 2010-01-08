
// The following properties have been added by the Upgrade process...
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"

log4j = {
    trace 'org.grails.solr'
}

solr {
    url = "http://localhost:8983/solr"
}