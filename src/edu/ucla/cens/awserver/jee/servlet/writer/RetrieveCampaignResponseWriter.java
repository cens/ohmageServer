package edu.ucla.cens.awserver.jee.servlet.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.CampaignQueryResult;
import edu.ucla.cens.awserver.domain.ErrorResponse;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author selsky
 */
public class RetrieveCampaignResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(RetrieveCampaignResponseWriter.class);
	
	public RetrieveCampaignResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}
	
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		Writer writer = null;
		
		try {
			// Prepare for sending the response to the client
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
			String responseText = null;
			
			// Sets the HTTP headers to disable caching
			expireResponse(response);
			
			response.setContentType("application/json");
			
			// Build the appropriate response 
			if(! awRequest.isFailedRequest()) {
				
				@SuppressWarnings("unchecked")
				List<CampaignQueryResult> results = (List<CampaignQueryResult>) awRequest.getResultList();
				int numberOfResults = results.size();
				
				JSONObject rootObject = new JSONObject().put("result", "success");
				JSONObject metadata = new JSONObject();
				metadata.put("number_of_results", numberOfResults);
				rootObject.put("metadata", metadata);
				
				JSONArray itemArray = new JSONArray();
				metadata.put("items", itemArray);
				JSONArray dataArray = new JSONArray();
				rootObject.put("data", dataArray);
				
				// doing only short output_format for now
				
				for(int i = 0; i < numberOfResults; i++) {
					CampaignQueryResult result = results.get(i);
					JSONObject campaignObject = new JSONObject();
					campaignObject.put("name", result.getName());
					campaignObject.put("running_state", result.getRunningState());
					campaignObject.put("privacy_state", result.getPrivacyState());
					campaignObject.put("creation_timestamp", result.getCreationTimestamp());
					campaignObject.put("user_roles", new JSONArray(result.getUserRoles()));
					
					dataArray.put(new JSONObject().put(result.getUrn(), campaignObject));
					itemArray.put(result.getUrn());
				}
				
				responseText = rootObject.toString();
				
			} else {
				
				if(null != awRequest.getFailedRequestErrorMessage()) {
					responseText = awRequest.getFailedRequestErrorMessage();
				} else {
					responseText = generalJsonErrorMessage();
				}
			}
			
			_logger.info("about to write output");
			writer.write(responseText);
		}
		
		catch(Exception e) { // catch Exception in order to avoid redundant catch block functionality
			
			_logger.error("an unrecoverable exception occurred while running an retrieve config query", e);
			
			try {
				
				writer.write(this.generalJsonErrorMessage());
				
			} catch (Exception ee) {
				
				_logger.error("caught Exception when attempting to write to HTTP output stream", ee);
			}
			
		} finally {
			
			if(null != writer) {
				
				try {
					
					writer.flush();
					writer.close();
					writer = null;
					
				} catch (IOException ioe) {
					
					_logger.error("caught IOException when attempting to free resources", ioe);
				}
			}
		}
	}
}