/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2013-06-26 16:27:34 UTC)
 * on 2013-07-30 at 05:13:58 UTC 
 * Modify at your own risk.
 */

package com.neenaparikh.locationsender.deviceinfoendpoint.model;

/**
 * Model definition for DeviceInfo.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the . For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class DeviceInfo extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String deviceInformation;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String deviceRegistrationID;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String phoneNumber;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long timestamp;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String userAuthDomain;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String userEmail;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String userFederatedIdentity;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String userId;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String userName;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getDeviceInformation() {
    return deviceInformation;
  }

  /**
   * @param deviceInformation deviceInformation or {@code null} for none
   */
  public DeviceInfo setDeviceInformation(java.lang.String deviceInformation) {
    this.deviceInformation = deviceInformation;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getDeviceRegistrationID() {
    return deviceRegistrationID;
  }

  /**
   * @param deviceRegistrationID deviceRegistrationID or {@code null} for none
   */
  public DeviceInfo setDeviceRegistrationID(java.lang.String deviceRegistrationID) {
    this.deviceRegistrationID = deviceRegistrationID;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getPhoneNumber() {
    return phoneNumber;
  }

  /**
   * @param phoneNumber phoneNumber or {@code null} for none
   */
  public DeviceInfo setPhoneNumber(java.lang.String phoneNumber) {
    this.phoneNumber = phoneNumber;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getTimestamp() {
    return timestamp;
  }

  /**
   * @param timestamp timestamp or {@code null} for none
   */
  public DeviceInfo setTimestamp(java.lang.Long timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getUserAuthDomain() {
    return userAuthDomain;
  }

  /**
   * @param userAuthDomain userAuthDomain or {@code null} for none
   */
  public DeviceInfo setUserAuthDomain(java.lang.String userAuthDomain) {
    this.userAuthDomain = userAuthDomain;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getUserEmail() {
    return userEmail;
  }

  /**
   * @param userEmail userEmail or {@code null} for none
   */
  public DeviceInfo setUserEmail(java.lang.String userEmail) {
    this.userEmail = userEmail;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getUserFederatedIdentity() {
    return userFederatedIdentity;
  }

  /**
   * @param userFederatedIdentity userFederatedIdentity or {@code null} for none
   */
  public DeviceInfo setUserFederatedIdentity(java.lang.String userFederatedIdentity) {
    this.userFederatedIdentity = userFederatedIdentity;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getUserId() {
    return userId;
  }

  /**
   * @param userId userId or {@code null} for none
   */
  public DeviceInfo setUserId(java.lang.String userId) {
    this.userId = userId;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getUserName() {
    return userName;
  }

  /**
   * @param userName userName or {@code null} for none
   */
  public DeviceInfo setUserName(java.lang.String userName) {
    this.userName = userName;
    return this;
  }

  @Override
  public DeviceInfo set(String fieldName, Object value) {
    return (DeviceInfo) super.set(fieldName, value);
  }

  @Override
  public DeviceInfo clone() {
    return (DeviceInfo) super.clone();
  }

}
