package com.applozic.mobicomkit.api.account.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.applozic.mobicomkit.annotations.ApplozicInternal;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.data.AlPrefSettings;

import java.io.File;
import java.util.Set;

/**
 * The <code>MobiComUserPreference</code> class is a wrapper around {@link SharedPreferences} used to store user and session level local data.
 *
 * @ApplozicInternal This class is not a part of Applozic's public API. Direct access to the methods and fields of this class will not be required.
 */
@ApplozicInternal //move the code used here to Applozic class eg: Applozic.getCurrentUser();
public class MobiComUserPreference {

    private static final String USER_ID = "userId";

    public static final @ApplozicInternal String AL_USER_PREF_KEY = "al_user_pref_key";

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public static MobiComUserPreference userpref; //ApplozicInternal: private

    private static String device_registration_id = "device_registration_id";
    private static String device_key_string = "device_key_string";
    private static String last_outbox_sync_time = "last_outbox_sync_time";
    private static String delivery_report_pref_key = "delivery_report_pref_key";
    private static String last_inbox_sync_time = "last_inbox_sync_time";
    private static String last_message_stat_sync_time = "last_message_stat_sync_time";
    private static String sent_sms_sync_pref_key = "sent_sms_sync_pref_key";
    private static String email = "email";
    private static String email_verified = "email_verified";
    private static String user_key_string = "user_key_string";
    private static String stop_service = "stop_service";
    private static String patch_available = "patch_available";
    private static String webhook_enable_key = "webhook_enable_key";
    private static String group_sms_freq_key = "group_sms_freq_key";
    private static String update_push_registration = "update_push_registration";
    private static String verify_contact_number = "verify_contact_number";
    private static String received_sms_sync_pref_key = "received_sms_sync_pref_key";
    private static String phone_number_key = "phone_number_key";
    private static String call_history_display_within_messages_pref_key = "call_history_display_within_messages_pref_key";
    private static String mobitexter_contact_sync_key = "mobitexter_contact_sync_key";
    private static String last_sms_sync_time = "last_sms_sync_time";
    private static String new_message_flag = "new_message_flag";
    private static String base_url = "base_url";
    private static String display_name = "display_name";
    private static String logged_in = "logged_in";
    private static String lastSeenAtSyncTime = "lastSeenAtSyncTime";
    private static String channelSyncTime = "channelSyncTime";
    private static String device_time_offset_from_UTC = "device_time_offset_from_UTC";
    private static String image_compression_enabled = "image_compression_enabled";
    private static String userBlockSyncTime = "user_block_Sync_Time";
    private static String max_compressed_image_size = "max_compressed_image_size";
    private static String image_link = "image_link";
    private static String registered_users_last_fetch_time = "registered_users_last_fetch_time";
    private static String password = "password";
    private static String authenticationType = "authenticationType";
    private static String mqtt_broker_url = "mqtt_broker_url";
    private static String contact_list_server_call = "contact_list_server_call";
    private static String pricing_package = "pricing_package";
    private static String delete_channel = "delete_channel";
    private static String encryption_Key = "encryption_Key";
    private static String enable_encryption = "enable_encryption";
    private static String enable_auto_download_on_wifi = "enable_auto_download_on_wifi";
    private static String enable_auto_download_on_cellular = "enable_auto_download_on_cellular";
    private static String video_call_token = "video_call_token";
    private static String user_type_id = "user_type_id";
    private static String application_info_call_done = "application_info_call_done";
    private static String CONTACTS_GROUP_ID = "CONTACTS_GROUP_ID";
    private static String CONTACT_GROUP_ID_LISTS = "contactGroupIdLists";
    private static String IS_CONTACT_GROUP_NAME_LIST = "isContactGroupNameList";
    private static String last_sync_time_for_metadata_update = "lastSyncTimeForMetadataUpdate";
    private static String START_TIME_FOR_MESSAGE_LIST_SCROLL = "startTimeForMessageListScroll";
    private static String USER_ROLE_TYPE = "userRoleType";
    private static String sync_contacts = "sync_contacts";
    private static String contact_sync_time = "contact_sync_time";
    private static String device_contact_sync_time = "device_contact_sync_time";
    private static String PARENT_GROUP_KEY = "PARENT_GROUP_KEY";
    private static String user_encryption_Key = "user_encryption_Key";
    private static String CATEGORY_NAME_KEY = "CATEGORY_KEY";
    private static String USER_AUTH_TOKEN = "USER_AUTH_TOKEN";
    private static String AUTH_TOKEN_VALID_UPTO_MINS = "AUTH_TOKEN_VALID_UPTO_MINS";
    private static String AUTH_TOKEN_CREATED_AT_TIME = "AUTH_TOKEN_CREATED_AT_TIME";
    private static String USER_DEACTIVATED = "USER_DEACTIVATED";
    private static String CHANNEL_LIST_LAST_GENERATED_TIME = "channelListLastGeneratedAtTime";
    private static String CHANNEL_LIST_LAST_GENERATED_DEFAULT_VALUE = "10000";
    private static String LOGGED_USER_DELETE_FROM_DASHBOARD = "loggedUserDeletedFromDashboard";

    private SharedPreferences sharedPreferences;
    private Context context;
    private String countryCode;


    private MobiComUserPreference(Context context) {
        this.context = ApplozicService.getContext(context);
        ApplozicService.initWithContext(context);
        renameSharedPrefFile(this.context);
        sharedPreferences = this.context.getSharedPreferences(MobiComUserPreference.AL_USER_PREF_KEY, Context.MODE_PRIVATE);
        moveKeysToSecured();
    }

    @ApplozicInternal
    public static MobiComUserPreference getInstance(Context context) {
        if (userpref == null) {
            userpref = new MobiComUserPreference(ApplozicService.getContext(context));
        }
        return userpref;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public synchronized static void renameSharedPrefFile(Context context) {
        File oldFile = new File("/data/data/" + Utils.getPackageName(context) + "/shared_prefs/" + MobiComKitClientService.getApplicationKey(context) + ".xml");
        if (oldFile.exists()) {
            oldFile.renameTo(new File("/data/data/" + Utils.getPackageName(context) + "/shared_prefs/" + MobiComUserPreference.AL_USER_PREF_KEY + ".xml"));
        }
    }

    //These Keys might not be used in the SDK and until then won't me moved.
    //The user might still see them in the prefs, so moving them even if they are not used
    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public synchronized void moveKeysToSecured() {
        if (sharedPreferences != null) {
            if (sharedPreferences.contains(password)) {
                setPassword(sharedPreferences.getString(password, null));
                sharedPreferences.edit().remove(password).commit();
            }
            if (sharedPreferences.contains(user_encryption_Key)) {
                setUserEncryptionKey(sharedPreferences.getString(user_encryption_Key, null));
                sharedPreferences.edit().remove(user_encryption_Key).commit();
            }
            if (sharedPreferences.contains(encryption_Key)) {
                setEncryptionKey(encryption_Key);
                sharedPreferences.edit().remove(encryption_Key).commit();
            }
        }
    }

    @Deprecated
    public boolean isRegistered() {
        return !TextUtils.isEmpty(getDeviceKeyString());
    }

    @ApplozicInternal
    public String getDeviceRegistrationId() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(device_registration_id, null);
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setDeviceRegistrationId(String deviceRegistrationId) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(device_registration_id, deviceRegistrationId).commit();
        }
    }

    @ApplozicInternal
    public String getDeviceKeyString() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(device_key_string, null);
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setDeviceKeyString(String deviceKeyString) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(device_key_string, deviceKeyString).commit();
        }
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public long getLastOutboxSyncTime() {
        return sharedPreferences.getLong(last_outbox_sync_time, 0L);
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setLastOutboxSyncTime(long lastOutboxSyncTime) {
        sharedPreferences.edit().putLong(last_outbox_sync_time, lastOutboxSyncTime).commit();
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public boolean isReportEnable() {
        return sharedPreferences.getBoolean(delivery_report_pref_key, false);
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setReportEnable(boolean reportEnable) {
        sharedPreferences.edit().putBoolean(delivery_report_pref_key, reportEnable).commit();
    }

    @ApplozicInternal
    public String getLastSyncTime() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(last_sms_sync_time, "0");
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setLastSyncTime(String lastSyncTime) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(last_sms_sync_time, lastSyncTime).commit();
        }
    }

    @ApplozicInternal
    public long getLastInboxSyncTime() {
        return sharedPreferences.getLong(last_inbox_sync_time, 0L);
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setLastInboxSyncTime(long lastInboxSyncTime) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putLong(last_inbox_sync_time, lastInboxSyncTime).commit();
        }
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public Long getLastMessageStatSyncTime() {
        return sharedPreferences.getLong(last_message_stat_sync_time, 0);
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setLastMessageStatSyncTime(long lastMessageStatSyncTime) {
        sharedPreferences.edit().putLong(last_message_stat_sync_time, lastMessageStatSyncTime).commit();
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public boolean isSentSmsSyncFlag() {
        return sharedPreferences.getBoolean(sent_sms_sync_pref_key, true);
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setSentSmsSyncFlag(boolean sentSmsSyncFlag) {
        sharedPreferences.edit().putBoolean(sent_sms_sync_pref_key, sentSmsSyncFlag).commit();
    }

    @ApplozicInternal
    public String getEmailIdValue() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(email, null);
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setEmailIdValue(String emailIdValue) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(email, emailIdValue).commit();
        }
    }

    @ApplozicInternal
    public String getUserId() {
        if (sharedPreferences != null) {
            String userId = sharedPreferences.getString(USER_ID, null);
            if (TextUtils.isEmpty(userId)) {
                return getEmailIdValue();
            }
            return userId;
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setUserId(String userId) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(USER_ID, userId).commit();
        }
    }

    @ApplozicInternal
    public boolean isEmailVerified() {
        return sharedPreferences.getBoolean(email_verified, true);
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setEmailVerified(boolean emailVerified) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(email_verified, emailVerified).commit();
        }
    }

    @ApplozicInternal
    public String getSuUserKeyString() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(user_key_string, null);
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setSuUserKeyString(String suUserKeyString) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(user_key_string, suUserKeyString).commit();
        }
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public boolean isStopServiceFlag() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(stop_service, false);
        }
        return false;
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setStopServiceFlag(Boolean stopServiceFlag) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(stop_service, stopServiceFlag).commit();
        }
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public boolean isPatchAvailable() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(patch_available, false);
        }
        return false;
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setPatchAvailable(Boolean patchAvailable) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(patch_available, patchAvailable).commit();
        }
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public boolean isWebHookEnable() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(webhook_enable_key, false);
        }
        return false;
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setWebHookEnable(boolean enable) {
        sharedPreferences.edit().putBoolean(webhook_enable_key, enable).commit();
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public int getGroupSmsDelayInSec() {
        return sharedPreferences.getInt(group_sms_freq_key, 0);
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setDelayGroupSmsDelayTime(int delay) {
        sharedPreferences.edit().
                putInt(group_sms_freq_key, delay).commit();
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public boolean isUpdateRegFlag() {
        return sharedPreferences.getBoolean(update_push_registration, false);
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setUpdateRegFlag(boolean updateRegFlag) {
        sharedPreferences.edit().putBoolean(update_push_registration, updateRegFlag).commit();
    }

    @ApplozicInternal
    public String getCountryCode() {
        return countryCode;
    }

    @ApplozicInternal
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public boolean isVerifyContactNumber() {
        return sharedPreferences.getBoolean(verify_contact_number, false);
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setVerifyContactNumber(boolean verifyContactNumber) {
        sharedPreferences.edit().putBoolean(verify_contact_number, verifyContactNumber).commit();
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public boolean getReceivedSmsSyncFlag() {
        return sharedPreferences.getBoolean(received_sms_sync_pref_key, true);
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setReceivedSmsSyncFlag(boolean receivedSmsSyncFlag) {
        sharedPreferences.edit().putBoolean(received_sms_sync_pref_key, receivedSmsSyncFlag).commit();
    }

    @ApplozicInternal
    public String getContactNumber() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(phone_number_key, null);
        }
        return null;
    }

    @ApplozicInternal
    public void setContactNumber(String contactNumber) {
        // contactNumber = ContactNumberUtils.getPhoneNumber(contactNumber, getCountryCode());
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(phone_number_key, contactNumber).commit();
        }
    }

    @ApplozicInternal
    public boolean isDisplayCallRecordEnable() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(call_history_display_within_messages_pref_key, false);
        }
        return false;
    }

    @ApplozicInternal
    public void setDisplayCallRecordEnable(boolean enable) {
        sharedPreferences.edit().putBoolean(call_history_display_within_messages_pref_key, enable).commit();
    }

    @ApplozicInternal
    public boolean getNewMessageFlag() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(new_message_flag, false);
        }
        return false;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setNewMessageFlag(boolean enable) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(new_message_flag, enable).commit();
        }
    }

    @ApplozicInternal
    public long getDeviceTimeOffset() {
        if (sharedPreferences != null) {
            return sharedPreferences.getLong(device_time_offset_from_UTC, 0L);
        }
        return 0;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public boolean setDeviceTimeOffset(long diiference) {
        if (sharedPreferences != null) {
            return sharedPreferences.edit().putLong(device_time_offset_from_UTC, diiference).commit();
        }
        return false;
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public boolean isMobiTexterContactSyncCompleted() {
        return sharedPreferences.getBoolean(mobitexter_contact_sync_key, false);
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setMobiTexterContactSyncCompleted(boolean status) {
        sharedPreferences.edit().
                putBoolean(mobitexter_contact_sync_key, status).commit();
    }

    @ApplozicInternal
    public String getUrl() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(base_url, null);
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setUrl(String url) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(base_url, url).commit();
        }
    }

    @ApplozicInternal
    public String getMqttBrokerUrl() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(mqtt_broker_url, null);
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setMqttBrokerUrl(String url) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(mqtt_broker_url, url).commit();
        }
    }

    @ApplozicInternal
    public int getPricingPackage() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt(pricing_package, RegistrationResponse.PricingType.STARTER.getValue());
        }
        return 0;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setPricingPackage(int pricingPackage) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putInt(pricing_package, pricingPackage).commit();
        }
    }

    @ApplozicInternal
    public String getDisplayName() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(display_name, null);
        }
        return null;
    }

    @ApplozicInternal
    public void setDisplayName(String displayName) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(display_name, displayName).commit();
        }
    }

    @ApplozicInternal
    public boolean isLoggedIn() {
        if (sharedPreferences != null) {
            return !TextUtils.isEmpty(getUserId());
        }
        return false;
    }

    @ApplozicInternal
    public String getLastSeenAtSyncTime() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(lastSeenAtSyncTime, "0");
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setLastSeenAtSyncTime(String lastSeenAtTime) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(lastSeenAtSyncTime, lastSeenAtTime).commit();
        }
    }

    @ApplozicInternal
    public String getChannelSyncTime() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(channelSyncTime, "0");
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setChannelSyncTime(String syncChannelTime) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(channelSyncTime, syncChannelTime).commit();
        }
    }

    @ApplozicInternal
    public int getCompressedImageSizeInMB() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt(max_compressed_image_size, 10);
        }
        return 0;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setCompressedImageSizeInMB(int maxSize) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putInt(max_compressed_image_size, maxSize).commit();
        }
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setLastSyncTimeForMetadataUpdate(String lastSyncTime) {
        sharedPreferences.edit().putString(last_sync_time_for_metadata_update, lastSyncTime).commit();
    }

    @ApplozicInternal
    public String getLastSyncTimeForMetadataUpdate() {
        return sharedPreferences.getString(last_sync_time_for_metadata_update, null);
    }

    @ApplozicInternal
    public String getUserBlockSyncTime() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(userBlockSyncTime, "0");
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setUserBlockSyncTime(String lastUserBlockSyncTime) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(userBlockSyncTime, lastUserBlockSyncTime).commit();
        }
    }

    @ApplozicInternal
    public long getRegisteredUsersLastFetchTime() {
        if (sharedPreferences != null) {
            return sharedPreferences.getLong(registered_users_last_fetch_time, 0l);
        }
        return 0;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setRegisteredUsersLastFetchTime(long lastFetchTime) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putLong(registered_users_last_fetch_time, lastFetchTime).commit();
        }
    }

    @ApplozicInternal
    public String getImageLink() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(image_link, null);
        }
        return null;
    }

    @ApplozicInternal
    public void setImageLink(String imageUrl) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(image_link, imageUrl).commit();
        }
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public String getPassword() {
        if (sharedPreferences != null) {
            String decryptedPassword = AlPrefSettings.getInstance(context).getPassword();
            if (!TextUtils.isEmpty(decryptedPassword)) {
                return decryptedPassword;
            }
            String savedPassword = sharedPreferences.getString(password, null);
            if (!TextUtils.isEmpty(savedPassword)) {
                setPassword(savedPassword);
                sharedPreferences.edit().remove(password).commit();
            }
            return savedPassword;
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setPassword(String val) {
        AlPrefSettings.getInstance(context).setPassword(val);
    }

    @ApplozicInternal
    public String getAuthenticationType() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(authenticationType, "0");
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setAuthenticationType(String val) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(authenticationType, val).commit();
        }
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setDeleteChannel(boolean channelDelete) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(delete_channel, channelDelete).commit();
        }
    }

    @ApplozicInternal
    public boolean isChannelDeleted() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(delete_channel, false);
        }
        return false;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public MobiComUserPreference setUserAuthToken(String authToken) {
        AlPrefSettings.getInstance(context).setUserAuthToken(authToken);
        return this;
    }

    @ApplozicInternal
    public String getUserAuthToken() {
        String decodedUserAuthToken = AlPrefSettings.getInstance(context).getUserAuthToken();
        if (!TextUtils.isEmpty(decodedUserAuthToken)) {
            return decodedUserAuthToken;
        }
        if (sharedPreferences != null) {
            String savedUserAuthToken = sharedPreferences.getString(USER_AUTH_TOKEN, null);
            if (!TextUtils.isEmpty(savedUserAuthToken)) {
                setUserAuthToken(savedUserAuthToken);
                sharedPreferences.edit().remove(USER_AUTH_TOKEN).commit();
            }
            return savedUserAuthToken;
        }
        return null;
    }

    @Override
    public String toString() {
        return "MobiComUserPreference{" +
                "context=" + context +
                ", countryCode='" + getCountryCode() + '\'' +
                ", deviceKeyString=" + getDeviceKeyString() +
                ", contactNumber=" + getContactNumber() +
                '}';
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public boolean clearAll() {
        if (sharedPreferences != null) {
            return sharedPreferences.edit().clear().commit();
        }
        return false;
    }

    @ApplozicInternal
    public boolean isImageCompressionEnabled() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(image_compression_enabled, true);
        }
        return false;
    }

    @ApplozicInternal
    public void setImageCompressionEnabled(boolean imageCompressionEnabled) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(image_compression_enabled, imageCompressionEnabled).commit();
        }
    }

    @ApplozicInternal
    public boolean getWasContactListServerCallAlreadyDone() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(contact_list_server_call, false);
        }
        return false;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setWasContactListServerCallAlreadyDone(Boolean serverCallAlreadyDone) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(contact_list_server_call, serverCallAlreadyDone).commit();
        }
    }

    @ApplozicInternal
    public String getEncryptionKey() {
        String decodedEncryptionKey = AlPrefSettings.getInstance(context).getEncryptionKey();
        if (!TextUtils.isEmpty(decodedEncryptionKey)) {
            return decodedEncryptionKey;
        }
        if (sharedPreferences != null) {
            String savedEncryptionKey = sharedPreferences.getString(encryption_Key, null);
            if (!TextUtils.isEmpty(savedEncryptionKey)) {
                setEncryptionKey(savedEncryptionKey);
                sharedPreferences.edit().remove(encryption_Key).commit();
            }
            return savedEncryptionKey;
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setEncryptionKey(String encryptionKey) {
        AlPrefSettings.getInstance(context).setEncryptionKey(encryptionKey);
    }

    @ApplozicInternal
    public boolean isEncryptionEnabled() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(enable_encryption, false);
        }
        return false;
    }

    @ApplozicInternal
    public void enableEncryption(boolean enableEncryption) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(enable_encryption, enableEncryption).commit();
        }
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public boolean getAutoDownloadOnWifi() {
        return sharedPreferences.getBoolean(enable_auto_download_on_wifi, false);
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setAutoDownloadOnWifi(boolean enable) {
        sharedPreferences.edit().putBoolean(enable_auto_download_on_wifi, enable).commit();
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public boolean getAutoDownloadOnCellular() {
        return sharedPreferences.getBoolean(enable_auto_download_on_cellular, false);
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setAutoDownloadOnCellular(boolean enable) {
        sharedPreferences.edit().putBoolean(enable_auto_download_on_cellular, enable).commit();
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public String getVideoCallToken() {
        return sharedPreferences.getString(video_call_token, null);
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setVideoCallToken(String token) {
        sharedPreferences.edit().putString(video_call_token, token).commit();

    }

    @ApplozicInternal
    public String getUserTypeId() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(user_type_id, null);
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setUserTypeId(String userTypeId) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(user_type_id, userTypeId).commit();
        }
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public boolean getApplicationInfoCall() {
        return sharedPreferences.getBoolean(application_info_call_done, false);
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setApplicationInfoCallDone(boolean customerResponse) {
        sharedPreferences.edit().putBoolean(application_info_call_done, customerResponse).commit();
    }

    @ApplozicInternal
    public String getContactsGroupId() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(CONTACTS_GROUP_ID, null);
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setContactsGroupId(String contactsGroupId) {
        sharedPreferences.edit().putString(CONTACTS_GROUP_ID, contactsGroupId).commit();
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setContactGroupIdList(Set<String> contactGroupList) {
        sharedPreferences.edit().putStringSet(CONTACT_GROUP_ID_LISTS, contactGroupList).commit();
    }

    @ApplozicInternal
    public Set<String> getContactGroupIdList() {
        if (sharedPreferences != null) {
            return sharedPreferences.getStringSet(CONTACT_GROUP_ID_LISTS, null);
        }
        return null;
    }

    @ApplozicInternal
    public boolean isContactGroupNameList() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(IS_CONTACT_GROUP_NAME_LIST, false);
        }
        return false;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setIsContactGroupNameList(boolean isContactGroupNameList) {
        sharedPreferences.edit().putBoolean(IS_CONTACT_GROUP_NAME_LIST, isContactGroupNameList).commit();
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setStartTimeForPagination(Long startTime) {
        sharedPreferences.edit().putLong(START_TIME_FOR_MESSAGE_LIST_SCROLL, startTime).commit();
    }

    @ApplozicInternal
    public Long getStartTimeForPagination() {
        if (sharedPreferences != null) {
            return sharedPreferences.getLong(START_TIME_FOR_MESSAGE_LIST_SCROLL, 0);
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setUserRoleType(Short roleType) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putInt(USER_ROLE_TYPE, roleType).commit();
        }
    }

    @ApplozicInternal
    public Short getUserRoleType() {
        if (sharedPreferences != null) {
            return Short.valueOf((short) sharedPreferences.getInt(USER_ROLE_TYPE, 0));
        }
        return 0;
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public boolean isSyncRequired() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(sync_contacts, false);
        }
        return false;
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setSyncContacts(boolean syncConatcts) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(sync_contacts, syncConatcts).commit();
        }
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public long getDeviceContactSyncTime() {
        if (sharedPreferences != null) {
            return sharedPreferences.getLong(device_contact_sync_time, 0);
        }
        return 0;
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setDeviceContactSyncTime(long contactSyncTime) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putLong(device_contact_sync_time, contactSyncTime).commit();
        }
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public long getContactSyncTime() {
        if (sharedPreferences != null) {
            return sharedPreferences.getLong(contact_sync_time, 0);
        }
        return 0;
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setContactSyncTime(long contactSyncTime) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putLong(contact_sync_time, contactSyncTime).commit();
        }
    }

    @ApplozicInternal
    public Integer getParentGroupKey() {
        return sharedPreferences.getInt(PARENT_GROUP_KEY, 0);
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setParentGroupKey(Integer parentGroupKey) {
        sharedPreferences.edit().putInt(PARENT_GROUP_KEY, parentGroupKey).commit();
    }

    @ApplozicInternal
    public String getUserEncryptionKey() {
        String decodedUserEncryptionKey = AlPrefSettings.getInstance(context).getUserEncryptionKey();
        if (!TextUtils.isEmpty(decodedUserEncryptionKey)) {
            return decodedUserEncryptionKey;
        }
        if (sharedPreferences != null) {
            String savedUserEncryptionKey = sharedPreferences.getString(user_encryption_Key, null);
            if (!TextUtils.isEmpty(savedUserEncryptionKey)) {
                setUserEncryptionKey(savedUserEncryptionKey);
                sharedPreferences.edit().remove(user_encryption_Key).commit();
            }
            return savedUserEncryptionKey;
        }
        return null;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setUserEncryptionKey(String userEncryptionKey) {
        AlPrefSettings.getInstance(context).setUserEncryptionKey(userEncryptionKey);
    }

    @ApplozicInternal
    public String getCategoryName() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(CATEGORY_NAME_KEY, null);
        }
        return null;
    }

    /**
     * @deprecated This method is no longer used and will be removed soon.
     */
    @Deprecated
    public void setCategoryName(String category) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(CATEGORY_NAME_KEY, category).commit();
        }
    }

    @ApplozicInternal
    public long getTokenCreatedAtTime() {
        if (sharedPreferences != null) {
            return sharedPreferences.getLong(AUTH_TOKEN_CREATED_AT_TIME, 0);
        }
        return 0;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public MobiComUserPreference setTokenCreatedAtTime(Long authTokenCreatedAtTime) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putLong(AUTH_TOKEN_CREATED_AT_TIME, authTokenCreatedAtTime).commit();
        }
        return this;
    }

    @ApplozicInternal
    public int getTokenValidUptoMins() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt(AUTH_TOKEN_VALID_UPTO_MINS, 0);
        }
        return 0;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public MobiComUserPreference setTokenValidUptoMins(Integer validUptoMins) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putInt(AUTH_TOKEN_VALID_UPTO_MINS, validUptoMins).commit();
        }
        return this;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setUserDeactivated(boolean isDeactivated) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(USER_DEACTIVATED, isDeactivated).commit();
        }
    }

    @ApplozicInternal
    public boolean isUserDeactivated() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(USER_DEACTIVATED, false);
        }
        return false;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setChannelListLastGeneratedAtTime(String generatedAt) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(CHANNEL_LIST_LAST_GENERATED_TIME, generatedAt).commit();
        }
    }

    @ApplozicInternal
    public String getChannelListLastGeneratedAtTime() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(CHANNEL_LIST_LAST_GENERATED_TIME, CHANNEL_LIST_LAST_GENERATED_DEFAULT_VALUE);
        }
        return CHANNEL_LIST_LAST_GENERATED_DEFAULT_VALUE;
    }

    @ApplozicInternal
    public boolean isLoggedUserDeletedFromDashboard() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(LOGGED_USER_DELETE_FROM_DASHBOARD, false);
        }
        return false;
    }

    @ApplozicInternal(warningLevel = ApplozicInternal.WarningLevel.DO_NOT_USE)
    public void setLoggedUserDeletedFromDashboard(boolean deletedFromDashboard) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(LOGGED_USER_DELETE_FROM_DASHBOARD, deletedFromDashboard).commit();
        }
    }
}
