package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.request.PasswordChangeAwRequest;

/**
 * Creates the request for changing a user's password.
 * 
 * @author John Jenkins
 */
public class PasswordChangeAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(PasswordChangeAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public PasswordChangeAwRequestCreator() {
		// Do nothing.
	}

	/**
	 * Creates a new password change request where the credentials being used
	 * to authenticate this user must be the username and hashed password.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest request) {
		_logger.info("Creating a new request to handle a password change.");
		
		PasswordChangeAwRequest newRequest = new PasswordChangeAwRequest(request.getParameter(InputKeys.NEW_PASSWORD));
		
		String username = request.getParameter(InputKeys.USERNAME);
		String password = request.getParameter(InputKeys.PASSWORD);
		User user = new User();
		user.setUserName(username);
		user.setPassword(password);
		newRequest.setUser(user);
		
		NDC.push("client=" + request.getParameter(InputKeys.CLIENT));
		
		return newRequest;
	}
}