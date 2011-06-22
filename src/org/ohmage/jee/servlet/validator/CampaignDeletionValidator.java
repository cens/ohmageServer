/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.jee.servlet.validator;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;


/**
 * Checks that all required parameters exist and that they aren't oversized.
 * 
 * @author John Jenkins
 */
public class CampaignDeletionValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(CampaignDeletionValidator.class);
	
	/**
	 * Basic constructor.
	 */
	public CampaignDeletionValidator() {
		// Do nothing.
	}
	
	/**
	 * Basic HTTP validation that all the required parameters exist and aren't
	 * rediculously sized.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		// Get the authentication / session token from the header.
		List<String> tokens = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN);
		if(tokens.size() == 0) {
			if(httpRequest.getParameter(InputKeys.AUTH_TOKEN) == null) {
				throw new MissingAuthTokenException("The required authentication / session token is missing.");
			}
		}
		else if(tokens.size() > 1) {
			throw new MissingAuthTokenException("More than one authentication / session token was found in the request.");
		}
		
		if(httpRequest.getParameter(InputKeys.CAMPAIGN_URN) == null) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("Missing required campaign URN parameter.");
			}
			
			return false;
		}
		else if(httpRequest.getParameter(InputKeys.CLIENT) == null) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("Missing required client parameter.");
			}
			
			return false;
		}
		
		return true;
	}

}
