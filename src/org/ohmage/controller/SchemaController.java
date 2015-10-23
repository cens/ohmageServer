package org.ohmage.controller;

import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ohmage.bin.MultiValueResult;
import org.ohmage.bin.MultiValueResultAggregation;
import org.ohmage.bin.StreamBin;
import org.ohmage.bin.SurveyBin;
import org.ohmage.domain.DataPoint;
import org.ohmage.domain.Schema;
import org.ohmage.domain.auth.AuthorizationToken;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.javax.servlet.filter.AuthFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * The controller for all requests for schemas and their data.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(SchemaController.ROOT_MAPPING)
public class SchemaController extends OhmageController {
	/**
	 * The root API mapping for this Servlet.
	 */
	public static final String ROOT_MAPPING = "/schemas";

	/**
	 * The path and parameter key for schema IDs.
	 */
	public static final String KEY_SCHEMA_ID = "id";
    /**
     * The path and parameter key for schema versions.
     */
    public static final String KEY_SCHEMA_VERSION = "version";
    /**
     * The path and parameter key for a data point.
     */
    public static final String KEY_POINT_ID = "point_id";
	/**
	 * The name of the parameter for querying for specific values.
	 */
	public static final String KEY_QUERY = "query";

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(SchemaController.class.getName());

    /**
     * Returns a list of visible schema IDs.
     *
     * @param query
     *        A value that should appear in either the name or description.
     *
     * @param numToSkip
     *        The number of stream IDs to skip.
     *
     * @param numToReturn
     *        The number of stream IDs to return.
     *
     * @param rootUrl
     *        The root URL of the request. This should be of the form
     *        <tt>http[s]://{domain}[:{port}]{servlet_root_path}</tt>.
     *
     * @return A list of visible schema IDs.
     */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public static @ResponseBody ResponseEntity<MultiValueResult<String>> getSchemaIds(
		@RequestParam(value = KEY_QUERY, required = false) final String query,
        @ModelAttribute(PARAM_PAGING_NUM_TO_SKIP) final long numToSkip,
        @ModelAttribute(PARAM_PAGING_NUM_TO_RETURN) final long numToReturn,
        @ModelAttribute(OhmageController.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl) {

		LOGGER.info("Creating a schema ID read request.");

        LOGGER.info("Retrieving the stream IDs.");
        MultiValueResult<String> streamIds =
            StreamBin
                .getInstance()
                .getStreamIds(query, false, 0, numToSkip + numToReturn);

        LOGGER.info("Retrieving the survey IDs.");
        MultiValueResult<String> surveyIds =
            SurveyBin
                .getInstance()
                .getSurveyIds(query, false, 0, numToSkip + numToReturn);

        LOGGER.debug("Building the result.");
        MultiValueResultAggregation.Aggregator<String> aggregator =
            new MultiValueResultAggregation.Aggregator<String>(streamIds);
        aggregator.add(surveyIds);
        MultiValueResultAggregation<String> ids =
            aggregator.build(numToSkip, numToReturn);

        LOGGER.info("Building the paging headers.");
        HttpHeaders headers =
            OhmageController
                .buildPagingHeaders(
                        numToSkip,
                        numToReturn,
                        Collections.<String, String>emptyMap(),
                        ids,
                        rootUrl + ROOT_MAPPING);

        LOGGER.info("Creating the response object.");
        ResponseEntity<MultiValueResult<String>> result =
            new ResponseEntity<MultiValueResult<String>>(
                ids,
                headers,
                HttpStatus.OK);

        LOGGER.info("Returning the schema IDs.");
        return result;
	}

	/**
	 * Returns a list of versions for the given schema.
	 *
	 * @param schemaId
	 *        The schema's unique identifier.
     *
     * @param numToSkip
     *        The number of stream IDs to skip.
     *
     * @param numToReturn
     *        The number of stream IDs to return.
     *
     * @param rootUrl
     *        The root URL of the request. This should be of the form
     *        <tt>http[s]://{domain}[:{port}]{servlet_root_path}</tt>.
	 *
	 * @return A list of the visible versions.
	 */
	@RequestMapping(
		value = "{" + KEY_SCHEMA_ID + "}",
		method = RequestMethod.GET)
	public static @ResponseBody ResponseEntity<MultiValueResult<Long>> getSchemaVersions(
		@PathVariable(KEY_SCHEMA_ID) final String schemaId,
		@RequestParam(value = KEY_QUERY, required = false) final String query,
        @ModelAttribute(PARAM_PAGING_NUM_TO_SKIP) final long numToSkip,
        @ModelAttribute(PARAM_PAGING_NUM_TO_RETURN) final long numToReturn,
        @ModelAttribute(OhmageController.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl) {

		LOGGER.info("Creating a request to read the versions of a schema: " +
					schemaId);

        LOGGER.info("Validating the number to skip.");
        if(numToSkip < 0) {
            throw
                new InvalidArgumentException(
                    "The number to skip must be greater than or equal to 0.");
        }
        LOGGER.info("Validating the number to return.");
        if(numToReturn <= 0) {
            throw
                new InvalidArgumentException(
                    "The number to return must be greater than 0.");
        }
        LOGGER.info("Validating the upper bound of the number to return.");
        if(numToReturn > MAX_NUM_TO_RETURN) {
            throw
                new InvalidArgumentException(
                    "The number to return must be less than the upper limit " +
                        "of " +
                        MAX_NUM_TO_RETURN +
                        ".");
        }

        LOGGER.info("Retrieving the versions.");
        ResponseEntity<MultiValueResult<Long>> result;
        if(StreamBin.getInstance().exists(schemaId, null, false)) {
            LOGGER.info("The schema is a stream.");
            result =
                StreamController
                    .getStreamVersions(
                        schemaId,
                        query,
                        numToSkip,
                        numToReturn,
                        rootUrl);
        }
        else if(SurveyBin.getInstance().exists(schemaId, null, false)) {
            LOGGER.info("The schema is a survey.");
            result =
                SurveyController
                    .getSurveyVersions(
                        schemaId,
                        query,
                        numToSkip,
                        numToReturn,
                        rootUrl);
        }
        else {
            throw
                new UnknownEntityException(
                    "The schema ID is unknown.");
        }

        LOGGER.info("Returning the versions.");
        return result;
	}

    /**
     * Returns the definition for a given schema.
     *
     * @param schemaId
     *        The schema's unique identifier.
     *
     * @return The schema definition.
     */
    @RequestMapping(
        value = "{" + KEY_SCHEMA_ID + "}/latest",
        method = RequestMethod.GET)
    public static @ResponseBody Schema getSchemaDefinition(
        @PathVariable(KEY_SCHEMA_ID) final String schemaId) {

        LOGGER.info("Creating a request for the latest schema definition.");

        return getSchemaDefinition(schemaId, null);
    }

    /**
     * Returns the definition for a given schema.
     *
     * @param schemaId
     *        The schema's unique identifier.
     *
     * @param schemaVersion
     *        The version of the schema.
     *
     * @return The schema definition.
     */
    @RequestMapping(
        value = "{" + KEY_SCHEMA_ID + "}/{" + KEY_SCHEMA_VERSION + "}",
        method = RequestMethod.GET)
    public static @ResponseBody Schema getSchemaDefinition(
        @PathVariable(KEY_SCHEMA_ID) final String schemaId,
        @PathVariable(KEY_SCHEMA_VERSION) final Long schemaVersion) {

        LOGGER.info("Creating a request for a schema definition.");

        LOGGER.info("Retrieving the definition.");
        Schema result;
        if(StreamBin.getInstance().exists(schemaId, schemaVersion, false)) {
            LOGGER.info("The schema is a stream.");
            result =
                StreamController.getStreamDefinition(schemaId, schemaVersion);
        }
        else if(
            SurveyBin.getInstance().exists(schemaId, schemaVersion, false)) {

            LOGGER.info("The schema is a survey.");
            result =
                SurveyController.getSurveyDefinition(schemaId, schemaVersion);
        }
        else {
            throw
                new UnknownEntityException(
                    "The schema ID"+(schemaVersion == null?"":"-version pair")+" is unknown.");
        }

        LOGGER.info("Returning the schema.");
        return result;
    }

    /**
     * Returns the data corresponding to the schema ID and version.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param schemaId
     *        The schema's unique identifier.
     *
     * @param schemaVersion
     *        The version of the schema.
     *
     * @param startDate
     *        The earliest date for a given point.
     *
     * @param endDate
     *        The latest date for a given point.
     *
     * @param chronological
     *        Whether or not the data should be sorted in chronological order
     *        (as opposed to reverse-chronological order).
     *
     * @param numToSkip
     *        The number of stream IDs to skip.
     *
     * @param numToReturn
     *        The number of stream IDs to return.
     *
     * @param rootUrl
     *        The root URL of the request. This should be of the form
     *        <tt>http[s]://{domain}[:{port}]{servlet_root_path}</tt>.
     *
     * @return The data corresponding to the schema ID and version.
     */
    @RequestMapping(
        value = "{" + KEY_SCHEMA_ID + "}/{" + KEY_SCHEMA_VERSION + "}/data",
        method = RequestMethod.GET)
    public static @ResponseBody ResponseEntity<? extends MultiValueResult<? extends DataPoint<?>>> getData(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @PathVariable(KEY_SCHEMA_ID) final String schemaId,
        @PathVariable(KEY_SCHEMA_VERSION) final Long schemaVersion,
        @RequestParam(value = PARAM_START_DATE, required = false)
            final String startDate,
        @RequestParam(value = PARAM_END_DATE, required = false)
            final String endDate,
        @RequestParam(
            value = PARAM_CHRONOLOGICAL,
            required = false,
            defaultValue = PARAM_DEFAULT_CHRONOLOGICAL)
            final boolean chronological,
        @ModelAttribute(PARAM_PAGING_NUM_TO_SKIP) final long numToSkip,
        @ModelAttribute(PARAM_PAGING_NUM_TO_RETURN) final long numToReturn,
        @ModelAttribute(OhmageController.ATTRIBUTE_REQUEST_URL_ROOT)
            final String rootUrl) {

        LOGGER.info("Creating a request for schema data: " +
                    schemaId + ", " +
                    schemaVersion);

        LOGGER.info("Delegating the request.");
        ResponseEntity<? extends MultiValueResult<? extends DataPoint<?>>>
            result;
        if(StreamBin.getInstance().exists(schemaId, schemaVersion, false)) {
            LOGGER.info("The schema is a stream.");
            result =
                StreamController
                    .getData(
                        authToken,
                        schemaId,
                        schemaVersion,
                        startDate,
                        endDate,
                        chronological,
                        numToSkip,
                        numToReturn,
                        rootUrl);
        }
        else if(
            SurveyBin.getInstance().exists(schemaId, schemaVersion, false)) {
            LOGGER.info("The schema is a survey.");
            result =
                SurveyController
                    .getData(
                        authToken,
                        schemaId,
                        schemaVersion,
                        startDate,
                        endDate,
                        chronological,
                        numToSkip,
                        numToReturn,
                        rootUrl);
        }
        else {
            throw
                new UnknownEntityException(
                    "The schema ID-verion pair is unknown.");
        }

        LOGGER.info("Returning the data.");
        return result;
    }

    /**
     * Returns a specific data point corresponding to the schema ID and
     * version.
     *
     * @param authToken
     *        The authorization information corresponding to the user that is
     *        making this call.
     *
     * @param schemaId
     *        The schema's unique identifier.
     *
     * @param schemaVersion
     *        The version of the schema.
     *
     * @param pointId
     *        The unique identifier for a specific point.
     *
     * @return The data corresponding to the schema ID and version and point
     *         ID.
     */
    @RequestMapping(
        value =
            "{" + KEY_SCHEMA_ID + "}" +
            "/" +
            "{" + KEY_SCHEMA_VERSION + "}" +
            "/" +
            "data" +
            "/" +
            "{" + KEY_POINT_ID + "}",
        method = RequestMethod.GET)
    public static @ResponseBody DataPoint<?> getPoint(
        @ModelAttribute(AuthFilter.ATTRIBUTE_AUTH_TOKEN)
            final AuthorizationToken authToken,
        @PathVariable(KEY_SCHEMA_ID) final String schemaId,
        @PathVariable(KEY_SCHEMA_VERSION) final Long schemaVersion,
        @PathVariable(KEY_POINT_ID) final String pointId) {

        LOGGER.info("Creating a request for a specific schema data point: " +
                    schemaId + ", " +
                    schemaVersion);

        LOGGER.info("Retrieving the data.");
        DataPoint<?> result;
        if(StreamBin.getInstance().exists(schemaId, schemaVersion, false)) {
            LOGGER.info("The schema is a stream.");
            result =
                StreamController
                    .getPoint(authToken, schemaId, schemaVersion, pointId);
        }
        else if(
            SurveyBin.getInstance().exists(schemaId, schemaVersion, false)) {

            LOGGER.info("The schema is a survey.");
            result =
                SurveyController
                    .getPoint(authToken, schemaId, schemaVersion, pointId);
        }
        else {
            throw
                new UnknownEntityException(
                    "The schema ID-verion pair is unknown.");
        }

        LOGGER.info("Returning the data point.");
        return result;
    }
}