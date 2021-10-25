package com.applozic.mobicomkit.api.account.user;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.notification.MuteUserResponse;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.contact.database.ContactDatabase;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.feed.RegisteredUsersApiResponse;
import com.applozic.mobicomkit.feed.SyncBlockUserApiResponse;
import com.applozic.mobicomkit.listners.AlCallback;
import com.applozic.mobicomkit.sync.SyncUserBlockFeed;
import com.applozic.mobicomkit.sync.SyncUserBlockListFeed;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.task.AlTask;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * For working with users/contacts.
 *
 * <p>For methods with asynchronous support, refer to {@link com.applozic.mobicomkit.api.conversation.ApplozicConversation.Contacts}.</p>
 *
 * <p>All methods of this class run blocking. You will need to run them asynchronously.</p>
 */
public class UserService {
    private static final String TAG = "UserService";

    Context context; //Cleanup: private
    UserClientService userClientService; //Cleanup: private
    BaseContactService baseContactService; //Cleanup: private
    private final MobiComUserPreference userPreference;

    @SuppressLint("StaticFieldLeak") //only application context is passed to this field
    private static UserService userService;

    private UserService(@NonNull Context context) {
        this.context = ApplozicService.getContext(context);
        userClientService = new UserClientService(context);
        userPreference = MobiComUserPreference.getInstance(context);
        baseContactService = new AppContactService(context);
    }

    public static @NonNull UserService getInstance(@NonNull Context context) {
        if (userService == null) {
            userService = new UserService(ApplozicService.getContext(context));
        }
        return userService;
    }

    //Cleanup: default
    //Cleanup: this method should be renamed sync user/contact details
    /**
     * Wraps around {@link UserClientService#postUserDetailsByUserIds(Set)}.
     *
     * <p>Note: This method has database and network operation. Run it asynchronously.</p>
     */
    public void processUserDetailsByUserIds(@Nullable Set<String> userIds) {
        userClientService.postUserDetailsByUserIds(userIds);
    }

    /**
     * Retrieves a list of the current online users from the server. They are also saved locally.
     *
     * <p>This methods updates the local database for these retrieved users by calling {@link UserService#processUserDetail(Set)}.</p>
     *
     * <p>Note: This method has database and network operation. Run it asynchronously.</p>
     *
     * @param numberOfUser the number of users to return
     * @return array of userIds of the online users
     */
    public synchronized @Nullable String[] getOnlineUsers(int numberOfUser) {
        try {
            Map<String, String> userMapList = userClientService.getOnlineUserList(numberOfUser);
            if (userMapList != null && userMapList.size() > 0) {
                String[] userIdArray = new String[userMapList.size()];
                Set<String> userIds = new HashSet<String>();
                int i = 0;
                for (Map.Entry<String, String> keyValue : userMapList.entrySet()) {
                    Contact contact = new Contact();
                    contact.setUserId(keyValue.getKey());
                    contact.setConnected(keyValue.getValue().contains("true"));
                    userIdArray[i] = keyValue.getKey();
                    userIds.add(keyValue.getKey());
                    baseContactService.upsert(contact);
                    i++;
                }
                processUserDetails(userIds);
                return userIdArray;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Cleanup: rename (syncRegisteredUsers)
    /**
     * Retrieves a list of registered users from the server. They are also saved locally.
     *
     * <p>This methods updates the local user/contact data for these retrieved users
     * by calling {@link UserService#processUserDetail(Set)}.</p>
     *
     * <p>You can use the asynchronous {@link RegisteredUsersAsyncTask} instead of directly using this method.</p>
     *
     * <p>Note: This method has database and network operation. Run it asynchronously.</p>
     *
     * @param startTime the start time in milliseconds. eg: a start time of X will return all the
     *                  registered users created after X. pass 0 for all users.
     * @return the server api response. use {@link RegisteredUsersApiResponse#getUsers()}
     */
    public synchronized @Nullable RegisteredUsersApiResponse getRegisteredUsersList(@NonNull Long startTime, int pageSize) {
        String response = userClientService.getRegisteredUsers(startTime, pageSize);
        RegisteredUsersApiResponse apiResponse = null;
        if (!TextUtils.isEmpty(response) && !MobiComKitConstants.ERROR.equals(response)) {
            apiResponse = (RegisteredUsersApiResponse) GsonUtils.getObjectFromJson(response, RegisteredUsersApiResponse.class);
            if (apiResponse != null) {
                processUserDetail(apiResponse.getUsers());
                userPreference.setRegisteredUsersLastFetchTime(apiResponse.getLastFetchTime());
            }
            return apiResponse;
        }
        return null;
    }

    /**
     * Will return and sync a list of contacts matching the search term, from the server.
     *
     * <p>Note: The local contacts are also updated locally.</p>
     *
     * <p>Note: This method has database and network operation. Run it asynchronously.</p>
     *
     * @param searchString the search term
     * @return a list of users (as Contact objects)
     * @throws ApplozicException when the backend returns an error response
     */
    public @Nullable List<Contact> getUserListBySearch(@Nullable String searchString) throws ApplozicException {
        try {
            ApiResponse response = userClientService.getUsersBySearchString(searchString);

            if (response == null) {
                return null;
            }

            if (response.isSuccess()) {
                UserDetail[] userDetails = (UserDetail[]) GsonUtils.getObjectFromJson(GsonUtils.getJsonFromObject(response.getResponse(), List.class), UserDetail[].class);
                List<Contact> contactList = new ArrayList<>();

                for (UserDetail userDetail : userDetails) {
                    contactList.add(getContactFromUserDetail(userDetail));
                }
                return contactList;
            } else {
                if (response.getErrorResponse() != null && !response.getErrorResponse().isEmpty()) {
                    throw new ApplozicException(GsonUtils.getJsonFromObject(response.getErrorResponse(), List.class));
                }
            }
        } catch (Exception e) {
            throw new ApplozicException(e.getMessage());
        }
        return null;
    }

    /**
     * Updates details of the current (logged) user.
     *
     * <p>This method updates more than just the display name or image link. See the parameters.</p>
     *
     * <p>Note: This method has database and network operation. Run it asynchronously.</p>
     */
    public @Nullable String updateDisplayNameORImageLink(@Nullable String displayName, @Nullable String profileImageLink, @Nullable String localURL, @Nullable String status, @Nullable String contactNumber, @Nullable Map<String, String> metadata) {
        return updateDisplayNameORImageLink(displayName, profileImageLink, localURL, status, null, null, null, null);
    }

    /**
     * Updates the user details for the given user.
     *
     * <p>User details are updated in the backend as well as locally.
     * Locally the Contact objects are updated in the database.</p>
     *
     * <p>Note: This method has database and network operation. Run it asynchronously.</p>
     *
     * @param user the user data to update. don't forget the userId.
     * @return The user update api response
     */
    public @Nullable ApiResponse updateUserWithResponse(@NonNull User user) {
        return updateUserWithResponse(user.getDisplayName(), user.getImageLink(), user.getLocalImageUri(), user.getStatus(), user.getContactNumber(), user.getEmail(), user.getMetadata(), user.getUserId());
    }

    //Cleanup: rename to something better
    /**
     * Blocks/unblocks the user with the given userId for the current logged in user.
     *
     * <p>In-case of success, the local data is also updated.</p>
     *
     * <p>Note: This method has database and network operation. Run it asynchronously.</p>
     *
     * @param userId the userId of the user to block
     * @param block true to block/false to unblock
     * @return response from the backend
     */
    public @Nullable ApiResponse processUserBlock(@Nullable String userId, boolean block) {
        ApiResponse apiResponse = userClientService.userBlock(userId, block);
        if (apiResponse != null && apiResponse.isSuccess()) {
            baseContactService.updateUserBlocked(userId, block);
            return apiResponse;
        }
        return null;
    }

    /**
     * Mutes notifications for the given user Id.
     *
     * <p>The current user will be taken from the user shared preferences.</p>
     *
     * <p>Note: This method has database and network operation. Run it asynchronously.</p>
     *
     * @param userId the user id of the user to mute
     * @param notificationAfterTime the time (in milliseconds) to mute the user for
     * @return the api response
     */
    public @Nullable ApiResponse muteUserNotifications(@Nullable String userId, @Nullable Long notificationAfterTime) {
        ApiResponse response = userClientService.muteUserNotifications(userId, notificationAfterTime);

        if (response == null) {
            return null;
        }
        if (response.isSuccess()) {
            new ContactDatabase(context).updateNotificationAfterTime(userId, notificationAfterTime);
        }

        return response;
    }

    /**
     * Gets the list of users muted by the current user. Also syncs them locally.
     *
     * <p>The muted user ids can be accessed from the array list items by using {@link MuteUserResponse#getUserId()}.</p>
     *
     * <p>Note: This method has database and network operation. Run it asynchronously.</p>
     *
     * @return an array list of {@link MuteUserResponse}
     */
    public @Nullable List<MuteUserResponse> getMutedUserList() {
        MuteUserResponse[] mutedUserList = userClientService.getMutedUserList();

        if (mutedUserList == null) {
            return null;
        }
        for (MuteUserResponse muteUserResponse : mutedUserList) {
            processMuteUserResponse(muteUserResponse);
        }
        return Arrays.asList(mutedUserList);
    }

    /**
     * This method converts a {@link UserDetail} object to a {@link Contact} object.
     *
     * <p>I will also save/update the Contact object in the local database.</p>
     *
     * <p>Note: This method has database and network operation. Run it asynchronously.</p>
     *
     * @param userDetail the user details object
     * @return the contact object
     */
    public synchronized @NonNull Contact getContactFromUserDetail(@NonNull UserDetail userDetail) {
        return getContactFromUserDetail(userDetail, Contact.ContactType.APPLOZIC);
    }

    //deprecated >>>

    //Cleanup: private
    /**
     * @deprecated This method is not longer used and will be removed soon.
     */
    @Deprecated
    public String updateDisplayNameORImageLink(String displayName, String profileImageLink, String localURL, String status) {
        return updateDisplayNameORImageLink(displayName, profileImageLink, localURL, status, null, null, null, null);
    }

    //Cleanup: private
    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public String updateLoggedInUser(User user) {
        return updateDisplayNameORImageLink(user.getDisplayName(), user.getImageLink(), user.getLocalImageUri(), user.getStatus(), user.getContactNumber(), user.getMetadata());
    }

    //Cleanup: private
    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public String updateUser(User user) {
        return updateDisplayNameORImageLink(user.getDisplayName(), user.getImageLink(), user.getLocalImageUri(), user.getStatus(), user.getContactNumber(), user.getEmail(), user.getMetadata(), user.getUserId());
    }

    //Cleanup: private
    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public ApiResponse processUserReadConversation() {
        return userClientService.getUserReadServerCall();
    }

    //Cleanup: private
    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public String processUpdateUserPassword(String oldPassword, String newPassword) {
        String response = userClientService.updateUserPassword(oldPassword, newPassword);
        if (!TextUtils.isEmpty(response) && MobiComKitConstants.SUCCESS.equals(response)) {
            userPreference.setPassword(newPassword);
        }
        return response;
    }

    //Cleanup: not needed
    /**
     * @deprecated Use the newer {@link com.applozic.mobicomkit.api.conversation.ApplozicConversation.Contacts#updateUserDetails(Context, User)}.
     */
    @Deprecated
    public void updateUser(User user, AlCallback callback) {
        AlTask.execute(new AlUserUpdateTask(context, user, callback));
    }

    //internal methods >>>

    /** Internal. Do not use. */
    //Cleanup: private
    public String updateDisplayNameORImageLink(String displayName, String profileImageLink, String localURL, String status, String contactNumber, String emailId, Map<String, String> metadata, String userId) {

        ApiResponse response = userClientService.updateDisplayNameORImageLink(displayName, profileImageLink, status, contactNumber, emailId, metadata, userId);

        if (response == null) {
            return null;
        }
        if (response.isSuccess()) {
            Contact contact = baseContactService.getContactById(!TextUtils.isEmpty(userId) ? userId : MobiComUserPreference.getInstance(context).getUserId());
            if (!TextUtils.isEmpty(displayName)) {
                contact.setFullName(displayName);
            }
            if (!TextUtils.isEmpty(profileImageLink)) {
                contact.setImageURL(profileImageLink);
            }
            contact.setLocalImageUrl(localURL);
            if (!TextUtils.isEmpty(status)) {
                contact.setStatus(status);
            }
            if (!TextUtils.isEmpty(contactNumber)) {
                contact.setContactNumber(contactNumber);
            }
            if (!TextUtils.isEmpty(emailId)) {
                contact.setEmailId(emailId);
            }
            if (metadata != null && !metadata.isEmpty()) {
                Map<String, String> existingMetadata = contact.getMetadata();
                if (existingMetadata == null) {
                    existingMetadata = new HashMap<>();
                }
                existingMetadata.putAll(metadata);
                contact.setMetadata(existingMetadata);
            }
            baseContactService.upsert(contact);
            Contact contact1 = baseContactService.getContactById(!TextUtils.isEmpty(userId) ? userId : MobiComUserPreference.getInstance(context).getUserId());
            Utils.printLog(context, TAG, contact1.getImageURL() + ", " + contact1.getDisplayName() + "," + contact1.getStatus() + "," + contact1.getStatus() + "," + contact1.getMetadata() + "," + contact1.getEmailId() + "," + contact1.getContactNumber());
        }
        return response.getStatus();
    }

    /** Internal. Do not use. */
    public synchronized void processUserDetails(Set<String> userIds) {
        String response = userClientService.getUserDetails(userIds);
        if (!TextUtils.isEmpty(response)) {
            UserDetail[] userDetails = (UserDetail[]) GsonUtils.getObjectFromJson(response, UserDetail[].class);
            for (UserDetail userDetail : userDetails) {
                processUser(userDetail);
            }
        }
    }

    //Cleanup: default
    /** Internal. Do not use. **/
    public void processUserDetailsResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            List<UserDetail> userDetails = (List<UserDetail>) GsonUtils.getObjectFromJson(response, new TypeToken<List<UserDetail>>() {
            }.getType());
            for (UserDetail userDetail : userDetails) {
                processUser(userDetail);
            }
        }
    }

    /**
     * Internal. Do not use.
     *
     * Updates the display name for the given userId (remote and local).
     *
     * <p>Note: This method has database and network operation. Run it asynchronously.</p>
     *
     * @param userId the user id of the user
     * @param userDisplayName the new display name
     * @return api response from the server
     */
    public ApiResponse updateUserDisplayName(String userId, String userDisplayName) {
        ApiResponse response =  userClientService.updateUserDisplayName(userId,userDisplayName);
        if (response != null && response.isSuccess()) {
            baseContactService.updateMetadataKeyValueForUserId(userId, Contact.AL_DISPLAY_NAME_UPDATED, "true");
            Utils.printLog(context, TAG, " Update display name Response :" + response.getStatus());
        }
        return response;
    }

    //Cleanup: rename (saveUsers/saveContacts)
    /**
     * Internal. Do not use.
     *
     * Saves the passed user details in the local database.
     *
     * <p>User details are save in the form of {@link Contact} objects internally.</p>
     *
     * <p>Note: This method has database and network operation. Run it asynchronously.</p>
     *
     * @param userDetails the set of user details to save
     */
    public synchronized void processUserDetail(Set<UserDetail> userDetails) {
        if (userDetails != null && userDetails.size() > 0) {
            for (UserDetail userDetail : userDetails) {
                processUser(userDetail);
            }
        }
    }

    /** Internal. Do not use. **/
    //Cleanup: private
    public synchronized void processUserDetails(String userId) {
        Set<String> userIds = new HashSet<String>();
        userIds.add(userId);
        processUserDetails(userIds);
    }

    //Cleanup: private
    /** Internal. Do not use. */
    public ApiResponse updateUserWithResponse(String displayName, String profileImageLink, String localURL, String status, String contactNumber, String emailId, Map<String, String> metadata, String userId) {

        ApiResponse response = userClientService.updateDisplayNameORImageLink(displayName, profileImageLink, status, contactNumber, emailId, metadata, userId);

        if (response == null) {
            return null;
        }

        if (response.isSuccess()) {
            Contact contact = baseContactService.getContactById(!TextUtils.isEmpty(userId) ? userId : MobiComUserPreference.getInstance(context).getUserId());
            if (!TextUtils.isEmpty(displayName)) {
                contact.setFullName(displayName);
            }
            if (!TextUtils.isEmpty(profileImageLink)) {
                contact.setImageURL(profileImageLink);
            }
            contact.setLocalImageUrl(localURL);
            if (!TextUtils.isEmpty(status)) {
                contact.setStatus(status);
            }
            if (!TextUtils.isEmpty(contactNumber)) {
                contact.setContactNumber(contactNumber);
            }
            if (!TextUtils.isEmpty(emailId)) {
                contact.setEmailId(emailId);
            }
            if (metadata != null && !metadata.isEmpty()) {
                Map<String, String> existingMetadata = contact.getMetadata();
                if (existingMetadata == null) {
                    existingMetadata = new HashMap<>();
                }
                existingMetadata.putAll(metadata);
                contact.setMetadata(existingMetadata);
            }
            baseContactService.upsert(contact);
        }
        return response;
    }

    /** Internal. Do not use. **/
    //Cleanup: private
    public synchronized void processUser(UserDetail userDetail) {
        processUser(userDetail, Contact.ContactType.APPLOZIC);
    }

    //Cleanup: private
    /**
     * Internal. Use {@link #getContactFromUserDetail(UserDetail)}.
     */
    public synchronized @NonNull Contact getContactFromUserDetail(@NonNull UserDetail userDetail, @NonNull Contact.ContactType contactType) {
        Contact contact = new Contact();
        contact.setUserId(userDetail.getUserId());
        contact.setContactNumber(userDetail.getPhoneNumber());
        contact.setConnected(userDetail.isConnected());
        contact.setStatus(userDetail.getStatusMessage());
        if (!TextUtils.isEmpty(userDetail.getDisplayName())) {
            contact.setFullName(userDetail.getDisplayName());
        }
        contact.setLastSeenAt(userDetail.getLastSeenAtTime());
        contact.setUserTypeId(userDetail.getUserTypeId());
        contact.setUnreadCount(0);
        contact.setLastMessageAtTime(userDetail.getLastMessageAtTime());
        contact.setMetadata(userDetail.getMetadata());
        contact.setRoleType(userDetail.getRoleType());
        contact.setDeletedAtTime(userDetail.getDeletedAtTime());
        contact.setEmailId(userDetail.getEmailId());
        if (!TextUtils.isEmpty(userDetail.getImageLink())) {
            contact.setImageURL(userDetail.getImageLink());
        }
        contact.setContactType(contactType.getValue());
        baseContactService.upsert(contact);
        return contact;
    }

    //Cleanup: private or rename
    /**
     * Internal. Do not use.
     *
     * This method inserts/updates the user(user detail) in the local database.
     *
     * <p>Note: This method has database and network operation. Run it asynchronously.</p>
     *
     * @param userDetail the user detail to save
     * @param contactType the contact type of the user
     */
    public synchronized void processUser(UserDetail userDetail, Contact.ContactType contactType) {
        Contact contact = new Contact();
        contact.setUserId(userDetail.getUserId());
        contact.setContactNumber(userDetail.getPhoneNumber());
        contact.setConnected(userDetail.isConnected());
        contact.setStatus(userDetail.getStatusMessage());
        if (!TextUtils.isEmpty(userDetail.getDisplayName())) {
            contact.setFullName(userDetail.getDisplayName());
        }
        contact.setLastSeenAt(userDetail.getLastSeenAtTime());
        contact.setUserTypeId(userDetail.getUserTypeId());
        contact.setUnreadCount(0);
        contact.setLastMessageAtTime(userDetail.getLastMessageAtTime());
        contact.setMetadata(userDetail.getMetadata());
        contact.setRoleType(userDetail.getRoleType());
        contact.setDeletedAtTime(userDetail.getDeletedAtTime());
        contact.setEmailId(userDetail.getEmailId());
        if (!TextUtils.isEmpty(userDetail.getImageLink())) {
            contact.setImageURL(userDetail.getImageLink());
        }
        contact.setContactType(contactType.getValue());
        baseContactService.upsert(contact);
    }

    /**
     * Internal. Do not use.
     */
    //Cleanup: private
    public synchronized void processMuteUserResponse(MuteUserResponse response) {
        Contact contact = new Contact();
        contact.setUserId(response.getUserId());
        BroadcastService.sendMuteUserBroadcast(context, BroadcastService.INTENT_ACTIONS.MUTE_USER_CHAT.toString(), true, response.getUserId());
        if (!TextUtils.isEmpty(response.getImageLink())) {
            contact.setImageURL(response.getImageLink());
        }
        contact.setUnreadCount(response.getUnreadCount());
        if (response.getNotificationAfterTime() != null && response.getNotificationAfterTime() != 0) {
            contact.setNotificationAfterTime(response.getNotificationAfterTime());
        }
        contact.setConnected(response.isConnected());
        baseContactService.upsert(contact);
    }

    /**
     * Internal. Do not use.
     *
     * This method handle block user syncing. Calling this method is done internally by the SDK.
     */
    public synchronized void processSyncUserBlock() {
        try {
            SyncBlockUserApiResponse apiResponse = userClientService.getSyncUserBlockList(userPreference.getUserBlockSyncTime());
            if (apiResponse != null && SyncBlockUserApiResponse.SUCCESS.equals(apiResponse.getStatus())) {
                SyncUserBlockListFeed syncUserBlockListFeed = apiResponse.getResponse();
                if (syncUserBlockListFeed != null) {
                    List<SyncUserBlockFeed> blockedToUserList = syncUserBlockListFeed.getBlockedToUserList();
                    List<SyncUserBlockFeed> blockedByUserList = syncUserBlockListFeed.getBlockedByUserList();
                    if (blockedToUserList != null && blockedToUserList.size() > 0) {
                        for (SyncUserBlockFeed syncUserBlockedFeed : blockedToUserList) {
                            Contact contact = new Contact();
                            if (syncUserBlockedFeed.getUserBlocked() != null && !TextUtils.isEmpty(syncUserBlockedFeed.getBlockedTo())) {
                                if (baseContactService.isContactExists(syncUserBlockedFeed.getBlockedTo())) {
                                    baseContactService.updateUserBlocked(syncUserBlockedFeed.getBlockedTo(), syncUserBlockedFeed.getUserBlocked());
                                } else {
                                    contact.setBlocked(syncUserBlockedFeed.getUserBlocked());
                                    contact.setUserId(syncUserBlockedFeed.getBlockedTo());
                                    baseContactService.upsert(contact);
                                    baseContactService.updateUserBlocked(syncUserBlockedFeed.getBlockedTo(), syncUserBlockedFeed.getUserBlocked());
                                }
                            }
                        }
                    }
                    if (blockedByUserList != null && blockedByUserList.size() > 0) {
                        for (SyncUserBlockFeed syncUserBlockByFeed : blockedByUserList) {
                            Contact contact = new Contact();
                            if (syncUserBlockByFeed.getUserBlocked() != null && !TextUtils.isEmpty(syncUserBlockByFeed.getBlockedBy())) {
                                if (baseContactService.isContactExists(syncUserBlockByFeed.getBlockedBy())) {
                                    baseContactService.updateUserBlockedBy(syncUserBlockByFeed.getBlockedBy(), syncUserBlockByFeed.getUserBlocked());
                                } else {
                                    contact.setBlockedBy(syncUserBlockByFeed.getUserBlocked());
                                    contact.setUserId(syncUserBlockByFeed.getBlockedBy());
                                    baseContactService.upsert(contact);
                                    baseContactService.updateUserBlockedBy(syncUserBlockByFeed.getBlockedBy(), syncUserBlockByFeed.getUserBlocked());
                                }
                            }
                        }
                    }
                }
                userPreference.setUserBlockSyncTime(apiResponse.getGeneratedAt());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @VisibleForTesting
    public void setBaseContactService(BaseContactService baseContactService) {
        this.baseContactService = baseContactService;
    }

    @VisibleForTesting
    public void setUserClientService(UserClientService userClientService) {
        this.userClientService = userClientService;
    }
}
