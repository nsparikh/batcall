package com.neenaparikh.locationsender;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.appengine.datanucleus.query.JPACursorHelper;

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
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listDeviceInfo")
	public CollectionResponse<DeviceInfo> listDeviceInfo(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		EntityManager mgr = null;
		Cursor cursor = null;
		List<DeviceInfo> execute = null;

		try {
			mgr = getEntityManager();
			Query query = mgr
					.createQuery("select from DeviceInfo as DeviceInfo");
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
			}

			if (limit != null) {
				query.setFirstResult(0);
				query.setMaxResults(limit);
			}

			execute = (List<DeviceInfo>) query.getResultList();
			cursor = JPACursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (DeviceInfo obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<DeviceInfo> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

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
	 * Finds the device with the given phone number.
	 * @param phone The given phone number
	 * @return The DeviceInfo object associated with the phone number, or null if there are none.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "findDeviceByPhone")
	public DeviceInfo findDeviceByPhone(@Named("phone") String phone) {
		EntityManager mgr = getEntityManager();
		List<DeviceInfo> resultList = null;

		try {
			Query query = mgr.createQuery("select from DeviceInfo as DeviceInfo where phoneNumber = '" + phone + "'");

			resultList = (List<DeviceInfo>) query.getResultList();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (DeviceInfo obj : resultList)
				;
		} finally {
			mgr.close();
		}

		if (resultList == null || resultList.size() == 0) return null;
		return resultList.get(0);
	}
	
	/**
	 * Finds the devices associated with the given email address.
	 * @param email The given email address
	 * @return A list of DeviceInfo objects with the given email address, or null if there are none.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "findDevicesByEmail")
	public CollectionResponse<DeviceInfo> findDevicesByEmail(@Named("email") String email) {
		EntityManager mgr = getEntityManager();
		List<DeviceInfo> resultList = null;

		try {
			Query query = mgr.createQuery("select from DeviceInfo as DeviceInfo where userEmail = '" + email + "'");

			resultList = (List<DeviceInfo>) query.getResultList();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (DeviceInfo obj : resultList)
				;
		} finally {
			mgr.close();
		}

		if (resultList == null || resultList.size() == 0) return null;
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
			deviceinfo.setUserEmail(user.getEmail());
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
