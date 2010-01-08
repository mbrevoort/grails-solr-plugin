//
// This script is executed by Grails after plugin was installed to project.
// This script is a Gant script so you can use all special variables provided
// by Gant (such as 'baseDir' which points on project base dir). You can
// use 'ant' to access a global instance of AntBuilder
//
// For example you can create directory under project tree:
//
//    ant.mkdir(dir:"${basedir}/grails-app/jobs")
//
def solrConfDir = "${basedir}/grails-app/conf/solr"
if(! new File(solrConfDir)?.exists()) {
  Ant.mkdir(dir: "${basedir}/grails-app/conf/solr")
  Ant.copy(todir:"${basedir}/grails-app/conf/solr") {
    fileset(dir: "${pluginBasedir}/src/solr-local/solr/conf" )
  }  
}
