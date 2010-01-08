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
includeTargets << new File ( "scripts/StopSolr.groovy" )

Ant.property(environment: 'env')
grailsHome = Ant.antProject.properties.'env.GRAILS_HOME'

target ( default: "Delete Solr Index") {
	  depends("stopsolr")
		Thread.sleep(1000)
		
		def pluginBasedir = "${solrPluginDir}"
		def solrHome = "${grails.util.BuildSettingsHolder.getSettings().projectWorkDir}/solr-home"
		def indexDir = "${solrHome}/solr/data"
		Ant.delete( dir: indexDir)
		println "Deleted ${indexDir}"

}


