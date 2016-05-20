package com.plivo.helper.tools;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;

import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.api.client.RestAPIFactory;
import com.plivo.helper.api.response.endpoint.Endpoint;
import com.plivo.helper.api.response.endpoint.EndpointFactory;
import com.plivo.helper.api.response.response.GenericResponse;
import com.plivo.helper.exception.PlivoException;

public class EndPointCleaner {

	private RestAPIFactory factory;
	
	private static final int TIME_TO_REGISTER = 1;

	public static void main(String[] args) {
		if (args.length != 3 || args[0].isEmpty() || args[1].isEmpty() || args[2].isEmpty()) {
			printUsageAndExit();
		}

		EndPointCleaner cleaner = new EndPointCleaner(args[0], args[1], args[2]);
		// cleaner.testMakeEndpoint();
		int totalRemoved = 0;
		while (true) {
			Set<String> endPointsToRemove = cleaner.getUnregisteredEndpoints();
			if (endPointsToRemove.size() == 0) {
				System.out.println("No endpoints to remove. Done.");
				return;
			} else {
				System.out.printf("Found %d endpoints to remove. \n", endPointsToRemove.size());
			}
			System.out.println("Sleeping to give time for endpoints to register if they were meant to.");
			System.out.print("Seconds remaining: ");
			for (int i = TIME_TO_REGISTER; i > 0; --i) {
				System.out.print(i);
				try {
					Thread.sleep(500);
					System.out.print(" .. ");
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (i == 1) {
					System.out.println(0);
				}
			}
			cleaner.removeRegisteredFromSet(endPointsToRemove);
			int removed = cleaner.removeEndpoints(endPointsToRemove);
			totalRemoved += removed;
			System.out.printf("Removed %d endpoints, total removed: %d\n", removed, totalRemoved);
			if (removed == 0) {
				break;
			}
		}
		System.out.println("Done");

	}

	public EndPointCleaner(String authTkn, String authId, String version) {
		factory = new RestAPIFactory(authId, authTkn, version);
		System.out.println("Cleaner Instantiated");
	}

	public void removeRegisteredFromSet(Set<String> endpoints) {
		int startSize = endpoints.size();
		System.out.println("Removing endpoints from set which have now registered");
		Set<String> toKeep = new TreeSet<String>();
		for (String endpoint : endpoints) {
			RestAPI api = factory.getAPI();
			LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
			parameters.put("endpoint_id", endpoint);
			try {
				Endpoint details = api.getEndpoint(parameters);
				if (details.error != null && !details.error.isEmpty()) {
					throw new PlivoException(details.error.toString());
				}
				if (details.sipRegistered != null && details.sipRegistered.toLowerCase().equals("false")) {
					toKeep.add(endpoint);
				}
			} catch (PlivoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		endpoints.retainAll(toKeep);
		System.out.println("Removed " + (startSize - endpoints.size()) + " endpoints from set");
	}

	public Set<String> getUnregisteredEndpoints() {
		System.out.println("Getting unregistered endpoints");
		Set<String> oldEndpoints = new TreeSet<String>();
		RestAPI api = factory.getAPI();
		try {
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
		} catch (PlivoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return oldEndpoints;
	}

	public int removeEndpoints(Set<String> endpoints) {
		System.out.println("Removing old endpoints in: " + endpoints.toString());
		int totalDeleted = 0;
		for (String endpoint : endpoints) {
			RestAPI api = factory.getAPI();
			LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
			parameters.put("endpoint_id", endpoint);
			System.out.printf("Attempting to delete endpoint: %s\n", endpoint);
			try {
				GenericResponse response = api.deleteEndpoint(parameters);
				if (response.error != null && !response.error.isEmpty()) {
					throw new PlivoException(response.error);
				}
				++totalDeleted;
			} catch (PlivoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return totalDeleted;
	}

	private static void printUsageAndExit() {
		System.out.println("Usage: authTkn authId version");
		System.exit(0);
	}

	public void testMakeEndpoint() {

		LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
		parameters.put("username", "merp");
		parameters.put("password", "asdfasdf");
		parameters.put("alias", "test");
		for (int i = 0; i < 250; ++i) {
			RestAPI api = factory.getAPI();
			try {
				api.createEndpoint(parameters);
				System.out.println("Made endpoint #" + i);
			} catch (PlivoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
