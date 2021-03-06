/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.query.impl;

import java.util.List;

import javax.sql.DataSource;

import org.ohmage.domain.Document;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IUserCampaignDocumentQueries;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * This class contains all of the functionality for reading and writing 
 * information specific to user-campaign-document relationships.
 * 
 * @author John Jenkins
 */
public final class UserCampaignDocumentQueries extends Query implements IUserCampaignDocumentQueries {
	// Check if the user is a supervisor in any campaign with which the 
	// document is associated.
	private static final String SQL_EXISTS_USER_IS_SUPERVISOR_IN_ANY_CAMPAIGN_ASSOCIATED_WITH_DOCUMENT = 
		"SELECT EXISTS(" +
			"SELECT c.urn " +
			"FROM user u, campaign c, user_role ur, user_role_campaign urc, " +
				"document d, document_campaign_role dcr " +
			// Switch on the username
			"WHERE u.username = ? " +
			// and the document's ID.
			"AND d.uuid = ? " +
			// Ensure that they are a supervisor in the campaign.
			"AND u.id = urc.user_id " +
			"AND c.id = urc.campaign_id " +
			"AND ur.id = urc.user_role_id " +
			"AND ur.role = '" + Campaign.Role.SUPERVISOR + "' " +
			// Ensure that the campaign is associated with the document.
			"AND d.id = dcr.document_id " +
			"AND c.id = dcr.campaign_id" +
		")";
	
	// Gets all of the document IDs visible to a user in a campaign.
	private static final String SQL_GET_DOCUMENTS_SPECIFIC_TO_CAMPAIGN_FOR_REQUESTING_USER = 
		"SELECT distinct(d.uuid) " +
		"FROM user u, campaign c, user_role ur, user_role_campaign urc, " +
			"document d, document_role dr, document_privacy_state dps, document_campaign_role dcar " +
		"WHERE u.username = ? " +
		"AND c.urn = ? " +
		"AND dcar.document_id = d.id " +
		"AND dcar.document_role_id = dr.id " +
		"AND dcar.campaign_id = c.id " +
		"AND dcar.campaign_id = urc.campaign_id " +
		"AND urc.user_id = u.id " +
		"AND urc.user_role_id = ur.id " +
		"AND d.privacy_state_id = dps.id " +
		"AND (" +
			"(dps.privacy_state = '" + Document.PrivacyState.SHARED + "' " +
			"AND ur.role != '" + Campaign.Role.PARTICIPANT + "')" +
			" OR " +
			"(ur.role = '" + Campaign.Role.SUPERVISOR + "')" +
			" OR " +
			"(dr.role = '" + Document.Role.OWNER + "')" +
		")";
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private UserCampaignDocumentQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Retrieves the list of document IDs for all of the documents associated
	 * with a campaign.
	 * 
	 * @param username The username of the requesting user.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @return A list of document IDs.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public List<String> getVisibleDocumentsToUserInCampaign(String username, String campaignId) 
	 	throws DataAccessException {
		
		try {
			return getJdbcTemplate().query(
					SQL_GET_DOCUMENTS_SPECIFIC_TO_CAMPAIGN_FOR_REQUESTING_USER, 
					new Object[] { username, campaignId },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_DOCUMENTS_SPECIFIC_TO_CAMPAIGN_FOR_REQUESTING_USER + " with parameters: " +
					username + ", " + campaignId, e);
		}
	}
	
	/**
	 * Checks if the user is a supervisor in any class to which the document is
	 * associated.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param documentId The unique document identifier for the document.
	 * 
	 * @return Returns true if the user is a supervisor in any of the classes
	 * 		   to which the document is associated.
	 */
	public Boolean getUserIsSupervisorInAnyCampaignAssociatedWithDocument(String username, String documentId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_IS_SUPERVISOR_IN_ANY_CAMPAIGN_ASSOCIATED_WITH_DOCUMENT, 
					new Object[] { username, documentId }, 
					Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_USER_IS_SUPERVISOR_IN_ANY_CAMPAIGN_ASSOCIATED_WITH_DOCUMENT +
					"' with parameters: " + username + ", " + documentId, e);
		}
	}
}
