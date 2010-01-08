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

import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.orm.hibernate3.SessionFactoryUtils
import org.springframework.orm.hibernate3.SessionHolder

includeTool << gant.tools.Execute

Ant.property(environment: 'env')
grailsHome = Ant.antProject.properties.'env.GRAILS_HOME'
//includeTargets << new File ( "${grailsHome}/scripts/Package.groovy" )
includeTargets << new File ( "${grailsHome}/scripts/Bootstrap.groovy" )

target ( default: "Index a domain class") {
	
	depends( configureProxy, packageApp, classpath )

  // configure context class loader to have access to domain classes
	classLoader = new URLClassLoader([classesDir.toURI().toURL()] as URL[], rootLoader)
  Thread.currentThread().setContextClassLoader(classLoader)

  // configure and load application
  bootstrap()	
  configureHibernateSession()

	if(!args) {
		println "You must pass the name of a Domain class to index"
	}
	
	def domainNames = args.split(" ")

	domainNames.each { domain ->
		
		def theClass = grailsApp.getDomainClass(domain).getClazz()
		def offset = 0
		def total = theClass.count()
		def indexCount = 0
		while(offset < total) {
			theClass.list([max:20, offset: offset]).each {
				it.indexSolr()
				if(++indexCount % 20 == 0)
					println "Indexed ${indexCount} or ${total} "
			}
			offset = offset + 20
		}		
		
	}

}


def configureHibernateSession() {
	// without this you'll get a lazy initialization exception when using a many-to-many relationship
	def sessionFactory = appCtx.getBean("sessionFactory")
	def session = SessionFactoryUtils.getSession(sessionFactory, true)
	TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session))
}

