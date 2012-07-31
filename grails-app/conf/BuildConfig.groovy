grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.dependency.resolution = {
  inherits("global") {
  }
  log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
  repositories {
      grailsPlugins()
      grailsHome()
      grailsCentral()

      mavenCentral()
  }
  dependencies {
    runtime('org.apache.solr:solr-solrj:3.6.0') {
      excludes 'slf4j-api', 'jcl-over-slf4j'
    }
  }
}
