package com.plivo.helper.tools;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.api.client.RestAPIFactory;
import com.plivo.helper.api.response.endpoint.Endpoint;
import com.plivo.helper.api.response.endpoint.EndpointFactory;
import com.plivo.helper.api.response.response.GenericResponse;
import com.plivo.helper.exception.PlivoException;

public class EndPointCleaner {

	private RestAPIFactory		factory;

	private int					timeToRegister	= 1;

	public static final String	CLASS			= EndPointCleaner.class.getName();
	public static final Logger	logger			= Logger.getLogger(CLASS);

	public static void main(String[] args) throws PlivoException {
		if (args.length != 3 || args[0].isEmpty() || args[1].isEmpty() || args[2].isEmpty()) {
			printUsageAndExit();
		}

		EndPointCleaner cleaner = new EndPointCleaner(args[0], args[1], args[2]);
		// cleaner.testMakeEndpoint();
		cleaner.removeAllEndpoints();
	}

	public void setTimeToRegister(int t) {
		timeToRegister = t;
	}

	public void removeAllEndpoints() throws PlivoException {
		logger.entering(CLASS, "removeAllEndpoints");
		int totalRemoved = 0;
		while (true) {
			Set<String> endPointsToRemove = this.getUnregisteredEndpoints();
			if (endPointsToRemove.size() == 0) {
				logger.info("No endpoints to remove. Done.");
				return;
			} else {
				logger.log(Level.INFO, "Found {0} endpoints to remove. \n", endPointsToRemove.size());
			}
			logger.info("Sleeping to give time for endpoints to register if they were meant to.");
			logger.log(Level.INFO, "Sleeping for {0} seconds", timeToRegister);
			for (int i = timeToRegister; i > 0; --i) {
				System.out.print(i);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.removeRegisteredFromSet(endPointsToRemove);
			int removed = this.removeEndpoints(endPointsToRemove);
			totalRemoved += removed;
			logger.info("Removed " + removed + " endpoints, total removed: " + totalRemoved);
			if (removed == 0) {
				break;
			}
		}
		logger.exiting(CLASS, "removeAllEndpoints");
	}

	public EndPointCleaner(String authTkn, String authId, String version) {
		factory = new RestAPIFactory(authId, authTkn, version);
	}

	public void removeRegisteredFromSet(Set<String> endpoints) throws PlivoException {
		Set<String> toKeep = new TreeSet<String>();
		for (String endpoint : endpoints) {
			RestAPI api = factory.getAPI();
			LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
			parameters.put("endpoint_id", endpoint);
				Endpoint details = api.getEndpoint(parameters);
				if (details.error != null && !details.error.isEmpty()) {
					throw new PlivoException(details.error.toString());
				}
				if (details.sipRegistered != null && details.sipRegistered.toLowerCase().equals("false")) {
					toKeep.add(endpoint);
				}
		}
		endpoints.retainAll(toKeep);
	}

	public Set<String> getUnregisteredEndpoints() throws PlivoException {
		Set<String> oldEndpoints = new TreeSet<String>();
		RestAPI api = factory.getAPI();
		LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
		EndpointFactory endpoints = api.getEndpoints(parameters);
		if (endpoints.error != null && !endpoints.error.isEmpty()) {
			throw new PlivoException(endpoints.error);
		}
		if (endpoints.endpointList != null) {
			for (Endpoint endpoint : endpoints.endpointList) {
				if (endpoint != null) {
					if (endpoint.sipRegistered != null && endpoint.sipRegistered.toLowerCase().equals("false")) {
						oldEndpoints.add(endpoint.endpointId);
					}
				}
			}
		}
		return oldEndpoints;
	}

	public int removeEndpoints(Set<String> endpoints) throws PlivoException {
		int totalDeleted = 0;
		for (String endpoint : endpoints) {
			RestAPI api = factory.getAPI();
			LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
			parameters.put("endpoint_id", endpoint);
			GenericResponse response = api.deleteEndpoint(parameters);
			if (response.error != null && !response.error.isEmpty()) {
				throw new PlivoException(response.error);
			}
			++totalDeleted;
		}
		return totalDeleted;
	}

	private static void printUsageAndExit() {
		System.out.println("Usage: authTkn authId version");
		System.exit(0);
	}

	public void testMakeEndpoint() throws PlivoException {

		LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
		parameters.put("username", "merp");
		parameters.put("password", "asdfasdf");
		parameters.put("alias", "test");
		for (int i = 0; i < 250; ++i) {
			RestAPI api = factory.getAPI();
			api.createEndpoint(parameters);
			System.out.println("Made endpoint #" + i);
		}

	}

}
