package com.neenaparikh.locationsender;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

@Api(
	name = "deviceinfoendpoint", 
	namespace = @ApiNamespace(
		ownerDomain = "neenaparikh.com", 
		ownerName = "neenaparikh.com", 
		packagePath = "locationsender"
	),
	clientIds = {"655975699066-c4qfm3pbqol9vgu47qafsln27o9e7k8l.apps.googleusercontent.com",
				 "655975699066.apps.googleusercontent.com"},
	audiences = {"655975699066-c4qfm3pbqol9vgu47qafsln27o9e7k8l.apps.googleusercontent.com"}
)
public class DeviceInfoEndpoint {

	

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getDeviceInfo")
	public DeviceInfo getDeviceInfo(@Named("id") String id) {
		EntityManager mgr = getEntityManager();
		DeviceInfo deviceinfo = null;
		try {
			deviceinfo = mgr.find(DeviceInfo.class, id);
		} finally {
			mgr.close();
		}
		return deviceinfo;
	}
	
	/**
	 * Finds the devices with the given phone numbers.
	 * @param phones The given phone numbers in a single string, separated by commas
	 * @return DeviceInfo objects associated with the phone number, or null if there are none.
	 * 	These DeviceInfo objects do not contain all information from the datastore, but rather
	 * 	just their keys and associated phone numbers because that is all the information we need.
	 */
	@ApiMethod(name = "findDevicesByPhoneList")
	public CollectionResponse<DeviceInfo> findDevicesByPhoneList(@Named("phoneListString") String phoneListString) {
		if (phoneListString == null || phoneListString.length() == 0) return null;
		
		String[] phones = phoneListString.split(",");
		
		List<DeviceInfo> resultList = new ArrayList<DeviceInfo>();
		EntityManager  mgr = getEntityManager();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		try {
			// Iterate through each phone number and query the datastore individually
			for (String phone : phones) {
				
				// Create filter on phone number and prepare query 
				Filter phoneFilter = new FilterPredicate("phoneNumber", FilterOperator.EQUAL, phone);
				PreparedQuery preparedQuery = datastore.prepare(new Query("DeviceInfo").setFilter(phoneFilter).setKeysOnly());
				Entity result = preparedQuery.asSingleEntity();
				if (result != null) {
					// If we've found a match, create a filler DeviceInfo object to hold the ID and phone number
					DeviceInfo matchedPhoneDevice = new DeviceInfo();
					matchedPhoneDevice.setPhoneNumber(phone);
					matchedPhoneDevice.setDeviceRegistrationID(result.getKey().getName());
					resultList.add(matchedPhoneDevice);
				}
			}
		} finally {
			mgr.close();
		}
		

		if (resultList.size() == 0) return null;
		return CollectionResponse.<DeviceInfo> builder().setItems(resultList).build();
	}
	
	/**
	 * Finds the devices with the given email addresses.
	 * @param emailListString The given email addresses in a single string, separated by commas
	 * @return The DeviceInfo objects associated with the phone number, or null if there are none.
	 * 	These DeviceInfo objects do not contain all information from the datastore, but rather
	 * 	just their keys and associated email addresses because that is all the information we need.
	 */
	@ApiMethod(name = "findDevicesByEmailList")
	public CollectionResponse<DeviceInfo> findDevicesByEmailList(@Named("emailListString") String emailListString) {
		if (emailListString == null || emailListString.length() == 0) return null;
		
		String[] emails = emailListString.split(",");
		
		List<DeviceInfo> resultList = new ArrayList<DeviceInfo>();
		EntityManager  mgr = getEntityManager();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		try {
			// Iterate through each email address and query the datastore individually
			for (String email : emails) {
				// Create filter on email address and prepare query 
				Filter phoneFilter = new FilterPredicate("userEmail", FilterOperator.EQUAL, email);
				PreparedQuery preparedQuery = datastore.prepare(new Query("DeviceInfo").setFilter(phoneFilter).setKeysOnly());
				List<Entity> result = preparedQuery.asList(FetchOptions.Builder.withLimit(5));
				if (result != null && result.size() > 0) {
					for (Entity e : result) {
						// If we've found a match, create a filler DeviceInfo object to hold the ID and email
						DeviceInfo matchedEmailDevice = new DeviceInfo();
						matchedEmailDevice.setUserEmail(email);
						matchedEmailDevice.setDeviceRegistrationID(e.getKey().getName());
						resultList.add(matchedEmailDevice);
					}
				}
			}
		} finally {
			mgr.close();
		}

		if (resultList.size() == 0) return null;
		return CollectionResponse.<DeviceInfo> builder().setItems(resultList).build();
	}
	
	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param deviceinfo the entity to be inserted.
	 * @return The inserted entity.
	 * @throws OAuthRequestException 
	 */
	@ApiMethod(name = "insertDeviceInfo")
	public DeviceInfo insertDeviceInfo(DeviceInfo deviceinfo, User user) throws OAuthRequestException {
		if (user == null) {
			throw new OAuthRequestException("User is not authenticated");
		}
		EntityManager mgr = getEntityManager();
		try {
			if (containsDeviceInfo(deviceinfo)) {
				throw new EntityExistsException("Object already exists");
			}
			
			deviceinfo.setUserName(user.getNickname());
			deviceinfo.setUserEmail(user.getEmail().toLowerCase());
			deviceinfo.setUserAuthDomain(user.getAuthDomain());
			deviceinfo.setUserFederatedIdentity(user.getFederatedIdentity());
			deviceinfo.setUserId(user.getUserId());
			
			mgr.persist(deviceinfo);
		} finally {
			mgr.close();
		}
		return deviceinfo;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param deviceinfo the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateDeviceInfo")
	public DeviceInfo updateDeviceInfo(DeviceInfo deviceinfo) {
		EntityManager mgr = getEntityManager();
		try {
			if (!containsDeviceInfo(deviceinfo)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.persist(deviceinfo);
		} finally {
			mgr.close();
		}
		return deviceinfo;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeDeviceInfo")
	public void removeDeviceInfo(@Named("id") String id) {
		EntityManager mgr = getEntityManager();
		try {
			DeviceInfo deviceinfo = mgr.find(DeviceInfo.class, id);
			mgr.remove(deviceinfo);
		} finally {
			mgr.close();
		}
	}

	private boolean containsDeviceInfo(DeviceInfo deviceinfo) {
		EntityManager mgr = getEntityManager();
		boolean contains = true;
		try {
			DeviceInfo item = mgr.find(DeviceInfo.class,
					deviceinfo.getDeviceRegistrationID());
			if (item == null) {
				contains = false;
			}
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static EntityManager getEntityManager() {
		return EMF.get().createEntityManager();
	}

}
