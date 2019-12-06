#
# Unless explicitly acquired and licensed from Licensor under another license, the contents of
# this file are subject to the Reciprocal Public License ("RPL") Version 1.5, or subsequent
# versions as allowed by the RPL, and You may not copy or use this file in either source code
# or executable form, except in compliance with the terms and conditions of the RPL
#
# All software distributed under the RPL is provided strictly on an "AS IS" basis, WITHOUT
# WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND LICENSOR HEREBY DISCLAIMS ALL SUCH
# WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
# PARTICULAR PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RPL for specific language
# governing rights and limitations under the RPL.
#
# http://opensource.org/licenses/RPL-1.5
#
# Copyright 2012-2017 Open Justice Broker Consortium
#

FROM tomcat:8.5

RUN cd /tmp && \
  curl -O http://central.maven.org/maven2/org/ojbc/mondrian-rest/1.3.3/mondrian-rest-1.3.3.war && \
  mv mondrian-rest-1.3.3.war /usr/local/tomcat/webapps/mondrian-rest.war

# COPY files/mondrian-rest-1.3.3.war /usr/local/tomcat/webapps/mondrian-rest.war

RUN sed -i s/8080/80/g /usr/local/tomcat/conf/server.xml

RUN mkdir -p /usr/local/tomcat/shared/config

COPY files/catalina.properties /usr/local/tomcat/conf/
COPY files/application.properties /usr/local/tomcat/shared/config/

WORKDIR /usr/local/tomcat
