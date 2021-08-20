
## Changelog

**Version  5.102.0** - TThursday, 20 August 2021

* MQTT Client Retry Policy Change:
- The MQTT client now will retry 3 times per connection session. Each will happen after a random interval between 1-40 minutes.
* Public API reference updated.
* Some refactoring.

**Version  5.101.0** - Tuesday, 29 June 2021

Features🔥:
* Added Gif/Sticker support 🎉
* Added utility logger class

Bugs/Fixes🔨:
* Fix Link Preview bug

**Version  5.100.0** - Friday, 11 June 2021

Features🔥:
* Update to Android API Level 30 🎉
* Added Mentions support❕
* Migrate JobIntentService classes to WorkerManager
* Video attachments will now show thumbnails

Bugs/Fixes🔨:
* UI Kit: Sender cannot interact suggested replies and submit button rich message

**Version  5.99.0** - Sunday, 9 May 2021

* Added client SDK side channel descriptions support.
* Data for channel decription goes in the channel metadata and is recognized by the UIKit.
* Added client SDK side support for admin only chat functionality in channel.
* Flag for request goes in the Channel metadata.
* Added config files for Jfrog Artifactory.
* Added support for custom fragment transitions.

Bug Fixes:
* Fixed bug with contact profile images dissapearing.

**Version  5.98** - Friday, 26 Feb 2021

* Deletion of logged in user will now be detected and appropriate message will be displayed.
* Name of sender will be shown for the received messages in the conversation list for groups. (OPTIONAL)
* Group lists will now also show group members, below the group name.
* Support for custom date/time formats in the UI.
* Notification color can now be set, from the manifest.
* BUG FIX: link preview not working for certain shortened URLs fixed.

**Version  5.97.1** - Thursday, 18 Feb 2021

* add option to pin contact to top in contact screen
* add retry download conversation view in case of network fail in conversation list
* add option to hide exit conversation in conversation screen
* fix app closing in case of no parent activity for conversation activity on takeOrder false

**Version  5.97** - Thursday, 28 Jan 2021

* Changed target to Android API level 29
* Clicking on contact profiles now sends a profile click broadcast
* Internal updates for supporting audio video calls

**Version  5.96.1** - Tuesday, 12 Jan 2021

* Bug fixes

**Version  5.96** - Wednesday, 30 Dec 2020

* Bug fixes
* Added AlTask for fetching total unread count from server

**Version  5.95.1** - Thursday, 17 Dec 2020

* Bug fixes
* Internal support for calls

**Version  5.95** - Thursday, 10 Dec 2020

* Fixed s3 file upload issue.
* Fixed user block bug in group of two.
* **Async tasks replaced with custom executor implementation.**

**Version  5.94** - Monday, 30 Nov 2020

* Enabled report message in open channel
* Fixed security bugs

**Version  5.93** - Friday, 6 Nov 2020

### Enhancement :
* Added support for deleting messages for all in the open group.
* Added API method and task for group list fetching.
* Security Improvement in KeyGenerator for key pair.

### Fixes :
* Fixed the NPE in Keystore.

**Version  5.92** - Wednesday, 14 Oct 2020
* Fixed the crash which was happening while creating a group and launching group chat.

**Version 5.91** - Friday, 9 Oct 2020
* Added support for link preview for URLs in chat.
* Added support for user activate and deactivate.
* Fixed the issue in full view of image orientation was ignored in some devices.

**Version 5.90** - Friday, 4 Sep 2020
* Security improvements in shared preferences 

**Version 5.80** - Tuesday, 23 June 2020

* Security enhancements: 
   1) MQTT connection will now require a user name and password to publish data to any topic.
   2) The password for MQTT will be refreshed in every 30 days, this is handled for pre-built UI. For customUI users, if you want to use the default progress dialog when the token refreshes, replace `Applozic.connectPublish(context)` method with `Applozic.connectPublishWithVerifyToken(context, loadingMessageString)` in your code. If you want to use your own dialog, refer to `Applozic.connectPublishWithVerifyToken` method implementation.
* Added methods to use custom MQTT topic in the SDK.
* Performance improvement: The read status of the message will be updated via MQTT instead of http call.
* Added restricted words support for attachment caption text.
* Added APIs to get support message list based on custom statuses.
* Fixed issue where send button was not visible on click deleting the message with attachment.
* Fixed issue where blank screen was displaying on back press of contact screen when hideGroupTab setting is enabled.
* Fixed issue where contact status was not dispaying when the conversation was switched from notification.

**Version 5.77** - Friday, 15 May 2020

* Added a listener to filter attachment based on custom logic
* Fixed the issue where the old conversations list was not loading on a scroll in some cases.

**Version 5.76** - Friday, 27 March 2020
* Fixed the update display name issue in case of launching a chat with another user.

**Version 5.75** - Friday, 13 March 2020

* Added dynamic setting to hide/show attachment options

**Version 5.74** - Friday, 31 January 2020

* Removed device package upload code
* Removed code related to message scheduling, contact access and call phone
* Moved assignee and status methods to Channel and Message classes
* Removed device contact sync feature and related code

**Version 5.51.4 (Support library version. Without androidX)** - Friday, 27 March 2020

* Fixed the update display name issue in case of launching a chat with another user.
* You can refer this branch for updates on SDK without android X
https://github.com/AppLozic/Applozic-Android-SDK/tree/without-androidX

**Version 5.51.1 (Support library version. Without androidX)** - Monday, 3 February 2020

* Removed read contact and phone state permission
* Removed device contact sync feature
* Removed contact sharing feature
* Fixed attachment upload issue on first install.

**Version 5.73** - Friday, 31 January 2020

* Fixed file access issue for apps targetting 29
* Fixed issue where files where not uploading on first install

**Version 5.72** - Wednesday, 29 January 2020

* Added setting to set the parent activity of chat programmatically.
Example:
`ApplozicSetting.getInstance(context).setParentActivity(MainActivity.class.getName());`
* Added Mute all notifications for user from platform API

**Version 5.71** - Tuesday, 16 January 2020

* Removed PHONE_STATE_PERMISSION from the sdk

**Version 5.70** - Friday, 10 January 2020

* Removed contact sharing feature.
* Removed device contact sync feature.
* Removed all permissions related to contacts.

**Version 5.66** - Tuesday, 31 December 2019

* MQTT message encryption.
* Added 2 more paramters in delete group API: updateClientGroupId and resetCount
* RichMessage UI fixes on the Sender side. Support for Image type and multiple buttons type rich messages added.
* isDeepLink option in link type rich messages
* Changed the authority of applozic file provider to applozic.provider in order to avoid conflicts with the same authority.

**Version 5.65** - Monday, 25 November 2019

* Fix notification issues for support groups
* Other crash fixes and optimization

**Version 5.64** - Friday, 18 October 2019

* Added dynamic setting to hide group name edit button
* Added setting in user to hide action messages
* New Rich message support

**Version 5.62** - Friday, 27 September 2019

**Features**
* Updated glide version to 4.9.0
* Added Message search APIs

**Improvements**
* Fixed typing indicator issue.
* Fixed image not sending issue when regex in settings file is empty.
* Fixed video not sending issue.
* Fixed MQTT not connecting when device goes idle for long time with chat open.

**Version 5.60** - Thursday, 5 September 2019

**Features**
* AndroidX migration.
* Option to enable/disable one to one chat.
* Option to set regex to block message that matches the pattern.
* UserId validation before login.
* Option to report a message.

**Fixes**
* Fixed issue where logout was taking too long to respond.
* Other crash fixes.

**Version 5.51**  - Friday, 19 July 2019
* Fixed issue where user email was not updating from the server.

**Version 5.50**  - Tuesday, 16 July 2019

**Features**
* New UI callbacks. You can now register multiple listeners with unique IDs. Then unregister the listeners usig those IDs.
   Registering:
   ```
  AlEventManager.getInstance().registerUIListener("mListener1", this);
   ```
   Unregistering:
    ```
  AlEventManager.getInstance().unregisterUIListener("mListener2");
    ```
* New update user method. The update user now accepts emailId as well. Admin can update the details of other users by passing the userId of the user(Call the below method in a background thread).
```
  User user = new User();
  user.setEmail("email@email.com");
  UserService.getInstance(this).updateUser(user);
```
* Added delete Group method.
* EmailID priority in display name. If user does'nt have display name and has emaild, emailId will be displayed.
* Added contribution doc. Please refer to the contribution doc before you contribute to Applozic SDK.

**Internal changes**
* Added Of-User-Id header for agents
* Added access token header for all authentication types
* updated getConversationList methods and DB calls for agents (Conversation sectioning)

**Fixes**
Fixed crash in personalized messages.

**Version 4.78**  - Thursday, 2 March 2017
   
  - Group of two support 
  - Group member limit in group create and group member add
  - Launch one to one chat in group member name click or profile click 
  - Sending meta data in message by adding settings 
  - Group and profile image upload bug fix
  - Other bug fixes 
 

 **Version 4.76**  - Friday,10 February 2017
   
  - Android N contact sharing bug fix  .
 

 **Version 4.75**  - Wednesday,8 February 2017
   
  - Group Delete support: deleted group will be disabled.
 
 **Version 4.74**  - Sunday, 5 February 2017
   
  - Support for version 7.1 
  - Support for support-library version 25.1.0
  - Chat not syncing bug fix for android 7.1
  
 **Version 4.73**  - Sunday, 5 January 2017
 
 - Support for version 7.0 
 - Group mute
 
   
 **Version 4.71**  - Thursday, 5 January 2017
 
 - Attachments options settings 
 - Restricted words settings 
 
  
   
 **Version 4.64**  - Friday, 18 November 2016
   
  Group info members context menu
 
  Smart messaging with message meta data : Push notification,Archive
  
  Group silent notifications 

  Bug fixes and Improvements
  
### Steps for upgrading from 4.63 to 4.64


**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.64'`


**Version 4.63**  - Tuesday, 25 October 2016
   
  Broadcast messageing 
  
  Smart messaging with message meta data : Hidden messages

  Bug fixes and Improvements


### Steps for upgrading from 4.62 to 4.63


**Step 1: Add the following in your Top-level/Proejct level build.gradle file change the version according to your app**:   

 ```
ext.googlePlayServicesVersion = '9.0.2'
ext.supportLibraryVersion = '23.1.1'
 ```

**Step 2: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.63'`


 **Version 4.62**  - Tuesday, 11 October 2016

  
  Change in Settings config now added json file 
  
  Bug fixes and Improvements


### Steps for upgrading from 4.61 to 4.62


**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.62'`


 **Version 4.61**  - wednesday, 5 October 2016

   Contact list selection added search option ,UI change

   Bug fixes and Improvements


### Steps for upgrading from 4.60 to 4.61


**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.61'`


 **Version 4.60**  - Friday, 30 September 2016
 
 Bug fixes and Improvements
 
  
### Steps for upgrading from 4.59 to 4.60


**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.60'`

 
 **Version 4.59**  - Saturday, 17 September 2016
 
 Bug fixes and Improvements
 
 
###  Steps for upgrading from 4.58 to 4.59

**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.59'`
 

 **Version 4.58**  - Thursday, 15 September 2016
 
 Unread count bug fix
 
 Open group issues
 
 Improvements  

 
###  Steps for upgrading from 4.57 to 4.58

**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.58'`


 **Version 4.57**  - Wednesday, 7 September 2016
 
 Block and unblock fix
 
 Message list pagination
 
 Message Encryption
 
 Group add,remove,exit,delete group,group icon change meta data supports 
 
 Improvements and bug fixs
 

 
###  Steps for upgrading from 4.56 to 4.57

**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.57'`
 
 **Version 4.56**  - Tuesday, 30 August 2016
 
 Improvements and bug fixs
 
 
###  Steps for upgrading from 4.55 to 4.56

**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.56'`

 
**Version 4.55**  - Tuesday, 23 August 2016

User block and unblock bug fix

Unread message count fix

Code improvements 

###  Steps for upgrading from 4.53 to 4.55

**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.55'`


**Step 2: In Your Android manifest add below code**

```
<service android:name="com.applozic.mobicomkit.api.conversation.ConversationReadService"
          android:exported="false" />

```

**Version 4.53**  - Monday,1 August 2016

Bug fixes and improvement

###  Steps for upgrading from 4.52 to 4.53

**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.53'`


**Version 4.52**  - Wednesday,27 July 2016

User and Group image upload and change

Group typing status 

Typing status is moved from bottom to App Bar

User status message change

Bug fixes and performance improvement

###  Steps for upgrading from 4.51 to 4.52

**Step 1: Add the following in your build.gradle dependency**

`compile 'com.applozic.communication.uiwidget:mobicomkitui:4.52'`


**Step 2: In Your Android manifest add below code**

```
 <activity android:name="com.soundcloud.android.crop.CropImageActivity" />

```

**Version 3.31**

Bug fixes and improvements

**Version 3.30**

 Contact search bug fix
 
 Group name sync changes
 
 Read Count bug fix 
 
**Version 3.29**

User Block

Grid layout for attachment options

Contact Search

Group Change notification
