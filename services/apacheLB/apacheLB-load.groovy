/*******************************************************************************
* Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
import org.hyperic.sigar.OperatingSystem
import org.cloudifysource.usm.USMUtils
import org.cloudifysource.dsl.context.ServiceContextFactory

config = new ConfigSlurper().parse(new File("apacheLB-service.properties").toURL())

def requests = args[0]
def concurrency= args[1]

def loadBalancerURL

context = ServiceContextFactory.getServiceContext()

def ctxPath

if ( args.length <3 ) { 
	ctxPath=("default" == context.applicationName)?"/":"/${context.applicationName}"
}	
else {	
	ctxPath = args[2]
	if ( !ctxPath.startsWith("/") ) {
		ctxPath="/${ctxPath}"
	}
}

loadBalancerURL	= "http://127.0.0.1:${config.currentPort}${ctxPath}"

def os = OperatingSystem.getInstance()
def currVendor=os.getVendor()
def abScript
switch (currVendor) {
	case ["Ubuntu", "Debian", "Mint","Red Hat", "CentOS", "Fedora", "Amazon",""]:			
		abScript="${context.serviceDirectory}/load.sh"
		break	
	case ~/.*(?i)(Microsoft|Windows).*/:
		abScript="${context.serviceDirectory}/install/load.sh"
		break	
	default: throw new Exception("Support for ${currVendor} is not implemented")
}

builder = new AntBuilder()
builder.sequential {
	echo(message:"apacheLB-load.groovy: Running ${abScript} -v INFO -n ${requests} -c ${concurrency} ${loadBalancerURL}. OS is ${currVendor} ...")
	exec(executable:"${abScript}",failonerror: "true") {
		arg(value:"${requests}")
		arg(value:"${concurrency}")
		arg(value:"${loadBalancerURL}")		
	}
}