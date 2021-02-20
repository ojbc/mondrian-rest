/*
 * Unless explicitly acquired and licensed from Licensor under another license, the contents of
 * this file are subject to the Reciprocal Public License ("RPL") Version 1.5, or subsequent
 * versions as allowed by the RPL, and You may not copy or use this file in either source code
 * or executable form, except in compliance with the terms and conditions of the RPL
 *
 * All software distributed under the RPL is provided strictly on an "AS IS" basis, WITHOUT
 * WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND LICENSOR HEREBY DISCLAIMS ALL SUCH
 * WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RPL for specific language
 * governing rights and limitations under the RPL.
 *
 * http://opensource.org/licenses/RPL-1.5
 *
 * Copyright 2012-2017 Open Justice Broker Consortium
 */
package org.ojbc.mondrian.rest;

import java.util.Collections;
import java.util.Map;

import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;

/**
 * Simple DAO bean representing an MDX query to be submitted to a named connection.
 *
 */
@Value
@NonFinal
public class QueryRequest {
	
	@Value
	public static final class TidyConfig {
		
		private boolean enabled;
		private boolean simplifyNames;
		private Map<String, String> levelNameTranslationMap;
		
		public Map<String, String> getLevelNameTranslationMap() {
			return levelNameTranslationMap == null ? null : Collections.unmodifiableMap(levelNameTranslationMap);
		}
		
	}
	
	private String connectionName;
	private String query;
	private TidyConfig tidy;
	
	@NonFinal @Setter String mondrianRole;
	
	public int getCacheKey() {
		return hashCode();
	}

}
