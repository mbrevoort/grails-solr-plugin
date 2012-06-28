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

includeTool << gant.tools.Execute

Ant.property(environment: 'env')
grailsHome = Ant.antProject.properties.'env.GRAILS_HOME'

def pluginBasedir = "${solrPluginDir}"
def solrHome = "${grails.util.BuildSettingsHolder.getSettings().projectWorkDir}/solr-home"
def solrStopPort = "8079"
def solrPort = "8983"
def solrHost = "localhost"

target ( startsolr: "Start Solr Jetty Instance") {
    depends("stopsolr")
    depends("init")

	// check for log directory existence - create or Solr won't start    
    def solrLogDir = "${solrHome}/logs"
	if(! new File(solrLogDir)?.exists()) {
	  Ant.mkdir(dir: "${solrLogDir}")
	}
		
		// overlay the schema.xml config file in the apps grails-app/conf/solr directory (and other conf files)
		Ant.copy(todir:"${solrHome}/solr/conf", failonerror: false) {
			fileset( dir: "${basedir}/grails-app/conf/solr")
		}


		// pause just for a bit more time to be sure Solr Stopped
		Thread.sleep(1000)

		// start it up
		Ant.java ( jar:"${solrHome}/start.jar", dir: "${solrHome}", fork:true, spawn:true) {
			jvmarg(value:"-DSTOP.PORT=${solrStopPort}")
			jvmarg(value:"-DSTOP.KEY=secret")
			arg(line:"etc/jetty-logging.xml etc/jetty.xml")
		}
		
			
		println "Starting Solr - Solr HOME is ${solrHome}"
		println "-----------"
		println "Solr logs can be found here: ${solrHome}/logs"
		println "Console access: http://localhost:${solrPort}/solr/"
		println "-----------"
}

setDefaultTarget ( "startsolr" )

target(checkport: "Test port for solr") {
  condition(property: "solr.not.running") {
    not {
      socket(server: solrHost, port: solrStopPort)      
    }
  }
}

target(stopsolr: "Stop Solr") {
  depends("checkport", "init")

	if ( !Boolean.valueOf(Ant.project.properties.'solr.not.running') ) {
    println "Stopping Solr..."
  	java ( jar:"${solrHome}/start.jar", dir: "${solrHome}", fork:true) {
      jvmarg(value:"-DSTOP.PORT=${solrStopPort}")
  		jvmarg(value:"-DSTOP.KEY=secret")
  		arg(value: "--stop")
  	}
	}
 
}

target(init: "Create the solr-home directory") {
  // copy over the resources for solr home
	Ant.mkdir(dir: "${solrHome}")
	Ant.copy(todir:"${solrHome}") {
		fileset( dir: "${pluginBasedir}/src/solr-local", )
	}
}
