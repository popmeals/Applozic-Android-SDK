package com.applozic.mobicomkit.uiwidgets.conversation.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.RegisteredUsersAsyncTask;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.broadcast.ConnectivityReceiver;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.feed.ErrorResponseFeed;
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicomkit.feed.RegisteredUsersApiResponse;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.alphanumbericcolor.AlphaNumberColorUtil;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.MobiComKitBroadcastReceiver;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.ChannelMetadata;
import com.applozic.mobicommons.people.channel.ChannelUserMapper;
import com.applozic.mobicommons.people.channel.ChannelUtils;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.task.AlAsyncTask;
import com.applozic.mobicommons.task.AlTask;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sunil on 7/3/16.
 */
public class ChannelInfoActivity extends AppCompatActivity {

    public static final String GROUP_UPDTAE_INFO = "GROUP_UPDTAE_INFO";
    public static final String CHANNEL_UPDATE_RECEIVER = "CHANNEL_UPDATE_RECEIVER";
    public static final String CHANNEL_KEY = "CHANNEL_KEY";
    public static final String USERID = "USERID";
    public static final String CHANNEL_NAME = "CHANNEL_NAME";
    public static final int REQUEST_CODE_FOR_CONTACT = 1;
    public static final int REQUEST_CODE_FOR_CHANNEL_NEW_NAME = 2;
    public static final int REQUEST_CODE_FOR_CHANNEL_NEW_DESCRIPTION = 3;
    private static final String TAG = "ChannelInfoActivity";
    private static final String SUCCESS = "success";
    protected ListView mainListView;
    protected ContactsAdapter contactsAdapter;
    CollapsingToolbarLayout collapsingToolbarLayout;
    boolean isUserPresent;
    Contact contact;
    BaseContactService baseContactService;
    MobiComKitBroadcastReceiver mobiComKitBroadcastReceiver;
    MobiComUserPreference userPreference;
    AlCustomizationSettings alCustomizationSettings;
    ConnectivityReceiver connectivityReceiver;
    private ActionBar mActionBar;
    private ImageLoader contactImageLoader, channelImageLoader;
    public List<ChannelUserMapper> channelUserMapperList;
    public Channel channel;
    private ImageView channelImage;
    private TextView createdBy, groupParticipantsTexView, textViewGroupDescription;
    private CardView cardViewGroupDescriptionContainer;
    private Button exitChannelButton, deleteChannelButton;
    private RelativeLayout channelDeleteRelativeLayout, channelExitRelativeLayout;
    private Integer channelKey;
    private RefreshBroadcast refreshBroadcast;
    private NestedScrollView nestedScrollView;
    private ResultReceiver channelUpdateReceiver;

    private void updateChannelDescriptionUIFrom(Map<String, String> channelMetadata) {
        String channelDescription = ChannelMetadata.getChannelDescriptionFrom(channelMetadata);
        if(textViewGroupDescription != null) {
            textViewGroupDescription.setText(!TextUtils.isEmpty(channelDescription) ? channelDescription : getString(R.string.no_description_tap_to_add));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_info_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String jsonString = FileUtils.loadSettingsJsonFile(getApplicationContext());
        if (!TextUtils.isEmpty(jsonString)) {
            alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
        } else {
            alCustomizationSettings = new AlCustomizationSettings();
        }
        refreshBroadcast = new RefreshBroadcast();
        baseContactService = new AppContactService(getApplicationContext());
        channelImage = (ImageView) findViewById(R.id.channelImage);
        userPreference = MobiComUserPreference.getInstance(this);
        createdBy = (TextView) findViewById(R.id.created_by);
        groupParticipantsTexView = (TextView) findViewById(R.id.groupParticipantsTexView);
        exitChannelButton = (Button) findViewById(R.id.exit_channel);
        deleteChannelButton = (Button) findViewById(R.id.delete_channel_button);
        channelDeleteRelativeLayout = (RelativeLayout) findViewById(R.id.channel_delete_relativeLayout);
        channelExitRelativeLayout = (RelativeLayout) findViewById(R.id.channel_exit_relativeLayout);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        textViewGroupDescription = findViewById(R.id.groupDescriptionTexView);
        cardViewGroupDescriptionContainer = findViewById(R.id.groupDescriptionCardView);

        collapsingToolbarLayout.setContentScrimColor(Color.parseColor(alCustomizationSettings.getCollapsingToolbarLayoutColor()));
        groupParticipantsTexView.setTextColor(Color.parseColor(alCustomizationSettings.getGroupParticipantsTextColor()));
        deleteChannelButton.setBackgroundColor(Color.parseColor((alCustomizationSettings.getGroupDeleteButtonBackgroundColor())));
        exitChannelButton.setBackgroundColor(Color.parseColor(alCustomizationSettings.getGroupExitButtonBackgroundColor()));

        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        mainListView = (ListView) findViewById(R.id.mainList);
        mainListView.setLongClickable(true);
        mainListView.setSmoothScrollbarEnabled(true);
        if (Utils.hasLollipop()) {
            mainListView.setNestedScrollingEnabled(true);
        }
        nestedScrollView.post(new Runnable() {
            @Override
            public void run() {
                nestedScrollView.scrollTo(nestedScrollView.getLeft(), groupParticipantsTexView.getTop());
            }
        });
        connectivityReceiver = new ConnectivityReceiver();
        mobiComKitBroadcastReceiver = new MobiComKitBroadcastReceiver(this);

        registerForContextMenu(mainListView);

        if (alCustomizationSettings.isHideGroupExitButton()) {
            channelExitRelativeLayout.setVisibility(View.GONE);
        }
        if (alCustomizationSettings.isHideGroupDeleteButton()) {
            channelDeleteRelativeLayout.setVisibility(View.GONE);
        }

        if (getIntent().getExtras() != null) {
            channelKey = getIntent().getIntExtra(CHANNEL_KEY, 0);
            channelUpdateReceiver = getIntent().getParcelableExtra(CHANNEL_UPDATE_RECEIVER);
            channel = ChannelService.getInstance(this).getChannelByChannelKey(channelKey);
            isUserPresent = ChannelService.getInstance(this).processIsUserPresentInChannel(channelKey);
            if (channel != null) {
                String title = ChannelUtils.getChannelTitleName(channel, userPreference.getUserId());
                if (!TextUtils.isEmpty(channel.getAdminKey())) {
                    contact = baseContactService.getContactById(channel.getAdminKey());
                    mActionBar.setTitle(title);
                    if (userPreference.getUserId().equals(contact.getUserId())) {
                        createdBy.setText(getString(R.string.channel_created_by) + " " + getString(R.string.you_string));
                    } else {
                        createdBy.setText(getString(R.string.channel_created_by) + " " + contact.getDisplayName());
                    }
                }
                updateChannelDescriptionUIFrom(channel.getMetadata());
                if (!isUserPresent) {
                    channelExitRelativeLayout.setVisibility(View.GONE);
                    channelDeleteRelativeLayout.setVisibility(View.VISIBLE);
                }
            }
        }

        if (channel != null && channel.getType() != null) {
            if (Channel.GroupType.BROADCAST.getValue().equals(channel.getType())) {
                deleteChannelButton.setText(R.string.broadcast_delete_button);
                exitChannelButton.setText(R.string.broadcast_exit_button);
                channelExitRelativeLayout.setVisibility(View.GONE);
                channelDeleteRelativeLayout.setVisibility(View.VISIBLE);
            } else {
                deleteChannelButton.setText(R.string.channel_delete_group_button);
                exitChannelButton.setText(R.string.channel_exit_button);
            }
        }

        contactImageLoader = new ImageLoader(getApplicationContext(), getListPreferredItemHeight()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return baseContactService.downloadContactImage(getApplicationContext(), (Contact) data);
            }
        };
        contactImageLoader.setLoadingImage(R.drawable.applozic_ic_contact_picture_holo_light);
        contactImageLoader.addImageCache(this.getSupportFragmentManager(), 0.1f);
        contactImageLoader.setImageFadeIn(false);
        channelImageLoader = new ImageLoader(getApplicationContext(), getListPreferredItemHeight()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return baseContactService.downloadGroupImage(getApplicationContext(), (Channel) data);
            }
        };

        channelImageLoader.setLoadingImage(R.drawable.applozic_group_icon);
        channelImageLoader.addImageCache(this.getSupportFragmentManager(), 0.1f);
        channelImageLoader.setImageFadeIn(false);

        if (channelImage != null && !channel.isBroadcastMessage()) {
            channelImageLoader.loadImage(channel, channelImage);
        } else {
            channelImage.setImageResource(R.drawable.applozic_ic_applozic_broadcast);
        }

        channelUserMapperList = ChannelService.getInstance(this).getListOfUsersFromChannelUserMapper(channel.getKey());

        contactsAdapter = new ContactsAdapter(this);
        mainListView.setAdapter(contactsAdapter);
        Helper.getListViewSize(mainListView);

        mainListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause image loader to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    contactImageLoader.setPauseWork(true);
                } else {
                    contactImageLoader.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        });
        exitChannelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveChannel(channel);
            }
        });

        deleteChannelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteChannel(channel);
            }
        });

        cardViewGroupDescriptionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserPresent && alCustomizationSettings.isEditChannelDescriptionAllowed() && channel != null) {
                    Intent editChannelNameIntent = new Intent(ChannelInfoActivity.this, EditChannelDescriptionActivity.class);
                    GroupInfoUpdate groupInfoUpdate = new GroupInfoUpdate(channel.getMetadata(), channel.getKey());
                    String groupJson = GsonUtils.getJsonFromObject(groupInfoUpdate, GroupInfoUpdate.class);
                    editChannelNameIntent.putExtra(GROUP_UPDTAE_INFO, groupJson);
                    startActivityForResult(editChannelNameIntent, REQUEST_CODE_FOR_CHANNEL_NEW_DESCRIPTION);
                } else {
                    Toast.makeText(ChannelInfoActivity.this, alCustomizationSettings.isEditChannelDescriptionAllowed() ? getString(R.string.channel_edit_description_alert) : getString(R.string.editing_channel_description_is_not_allowed), Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mobiComKitBroadcastReceiver);
        if (refreshBroadcast != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshBroadcast);
        }
        BroadcastService.currentInfoId = null;
        contactImageLoader.setPauseWork(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mobiComKitBroadcastReceiver, BroadcastService.getIntentFilter());
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshBroadcast, getIntentFilter());
        if (channel != null) {
            BroadcastService.currentInfoId = String.valueOf(channel.getKey());
            Channel newChannel = ChannelService.getInstance(this).getChannelByChannelKey(channel.getKey());
            if (newChannel != null && TextUtils.isEmpty(newChannel.getImageUrl())) {
                if (!channel.isBroadcastMessage()) {
                    channelImage.setImageResource(R.drawable.applozic_group_icon);
                } else {
                    channelImage.setImageResource(R.drawable.applozic_ic_applozic_broadcast);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        boolean isUserAlreadyPresent;
        if (data != null) {
            if (requestCode == REQUEST_CODE_FOR_CONTACT && resultCode == Activity.RESULT_OK) {
                isUserAlreadyPresent = ChannelService.getInstance(this).isUserAlreadyPresentInChannel(channel.getKey(), data.getExtras().getString(USERID));
                if (!isUserAlreadyPresent) {
                    addChannelUser(data.getExtras().getString(USERID), channel);
                } else {
                    Toast toast = Toast.makeText(this, getString(R.string.user_is_already_exists), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
            if ((requestCode == REQUEST_CODE_FOR_CHANNEL_NEW_NAME || requestCode == REQUEST_CODE_FOR_CHANNEL_NEW_DESCRIPTION) && resultCode == Activity.RESULT_OK) {
                GroupInfoUpdate groupInfoUpdate = (GroupInfoUpdate) GsonUtils.getObjectFromJson(data.getExtras().getString(GROUP_UPDTAE_INFO), GroupInfoUpdate.class);
                System.out.println("GroupInfoUpdate ::: " + data.getExtras().getString(GROUP_UPDTAE_INFO));
                if (channel.getName().equals(groupInfoUpdate.getNewName())) {
                    groupInfoUpdate.setNewName(null);
                }
                AlTask.execute(new ChannelAsync(groupInfoUpdate, ChannelInfoActivity.this, channelUpdateReceiver));
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        if (channelUserMapperList.size() <= position) {
            return true;
        }
        if (channel == null) {
            return true;
        }

        ChannelUserMapper channelUserMapper = channelUserMapperList.get(position);
        switch (item.getItemId()) {
            case 0:
                Intent startConversationIntent = new Intent(ChannelInfoActivity.this, ConversationActivity.class);
                startConversationIntent.putExtra(ConversationUIService.USER_ID, channelUserMapper.getUserKey());
                startActivity(startConversationIntent);
                finish();
                break;
            case 1:
                removeChannelUser(channel, channelUserMapper);
                break;
            case 2:
                if (Utils.isInternetAvailable(getApplicationContext())) {
                    AlTask.execute(new ChannelUserRoleAsyncTask(this, channelUserMapper, 1));
                } else {
                    Toast toast = Toast.makeText(this, getString(R.string.you_dont_have_any_network_access_info), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.channel_menu_option, menu);
        if (channel == null) {
            return true;
        }

        ChannelUserMapper loggedInUserMapper = ChannelService.getInstance(this).getChannelUserMapperByUserId(channel.getKey(), MobiComUserPreference.getInstance(ChannelInfoActivity.this).getUserId());
        if (alCustomizationSettings.isHideGroupAddMembersButton() || loggedInUserMapper != null && ChannelUserMapper.UserRole.MEMBER.getValue().equals(loggedInUserMapper.getRole()) || (!ChannelUtils.isAdminUserId(userPreference.getUserId(), channel) && loggedInUserMapper != null && Integer.valueOf(0).equals(loggedInUserMapper.getRole()))) {
            menu.removeItem(R.id.add_member_to_channel);
        }
        if (ApplozicSetting.getInstance(this).isHideGroupNameEditButton() || alCustomizationSettings.isHideGroupNameUpdateButton() || channel.isBroadcastMessage()) {
            menu.removeItem(R.id.edit_channel_name);
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int positionInList = info.position;
        if (positionInList < 0 || channelUserMapperList.isEmpty()) {
            return;
        }
        ChannelUserMapper channelUserMapper = channelUserMapperList.get(positionInList);
        if (MobiComUserPreference.getInstance(ChannelInfoActivity.this).getUserId().equals(channelUserMapper.getUserKey())) {
            return;
        }
        boolean isHideRemove = alCustomizationSettings.isHideGroupRemoveMemberOption();
        ChannelUserMapper loggedInUserMapper = ChannelService.getInstance(this).getChannelUserMapperByUserId(channelUserMapper.getKey(), MobiComUserPreference.getInstance(ChannelInfoActivity.this).getUserId());
        String[] menuItems = getResources().getStringArray(R.array.channel_users_menu_option);
        Contact contact = baseContactService.getContactById(channelUserMapper.getUserKey());
        for (int i = 0; i < menuItems.length; i++) {
            if (menuItems[i].equals(getString(R.string.make_admin_text_info)) && loggedInUserMapper != null && ChannelUserMapper.UserRole.MEMBER.getValue().equals(loggedInUserMapper.getRole())) {
                continue;
            }
            if (menuItems[i].equals(getString(R.string.remove_member)) && (isHideRemove || !isUserPresent || !ChannelUtils.isAdminUserId(userPreference.getUserId(), channel) && loggedInUserMapper != null && Integer.valueOf(0).equals(loggedInUserMapper.getRole()) || loggedInUserMapper != null && ChannelUserMapper.UserRole.MEMBER.getValue().equals(loggedInUserMapper.getRole()))) {
                continue;
            }
            if (menuItems[i].equals(getString(R.string.make_admin_text_info)) && (!isUserPresent || ChannelUserMapper.UserRole.ADMIN.getValue().equals(channelUserMapper.getRole()) || (channel != null && Channel.GroupType.BROADCAST.getValue().equals(channel.getType())))) {
                continue;
            }
            if (menuItems[i].equals(getString(R.string.make_admin_text_info))) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            } else {
                menu.add(Menu.NONE, i, i, menuItems[i] + " " + contact.getDisplayName());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        boolean isUserPresent = false;
        if (channel != null) {
            isUserPresent = ChannelService.getInstance(this).processIsUserPresentInChannel(channel.getKey());
        }
        if (id == R.id.add_member_to_channel) {
            if (isUserPresent) {
                Utils.toggleSoftKeyBoard(ChannelInfoActivity.this, true);
                if (alCustomizationSettings.getTotalRegisteredUserToFetch() > 0 && (alCustomizationSettings.isRegisteredUserContactListCall() || ApplozicSetting.getInstance(this).isRegisteredUsersContactCall()) && !userPreference.getWasContactListServerCallAlreadyDone()) {
                    processLoadRegisteredUsers();
                } else {
                    Intent addMemberIntent = new Intent(ChannelInfoActivity.this, ContactSelectionActivity.class);
                    addMemberIntent.putExtra(ContactSelectionActivity.CHECK_BOX, true);
                    addMemberIntent.putExtra(ContactSelectionActivity.CHANNEL_OBJECT, channel);
                    startActivityForResult(addMemberIntent, REQUEST_CODE_FOR_CONTACT);
                }

            } else {
                Toast.makeText(this, getString(R.string.channel_add_alert), Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.edit_channel_name) {
            if (isUserPresent) {
                Intent editChannelNameIntent = new Intent(ChannelInfoActivity.this, ChannelNameActivity.class);
                GroupInfoUpdate groupInfoUpdate = new GroupInfoUpdate(channel);
                String groupJson = GsonUtils.getJsonFromObject(groupInfoUpdate, GroupInfoUpdate.class);
                editChannelNameIntent.putExtra(GROUP_UPDTAE_INFO, groupJson);
                startActivityForResult(editChannelNameIntent, REQUEST_CODE_FOR_CHANNEL_NEW_NAME);
            } else {
                Toast.makeText(this, getString(R.string.channel_edit_alert), Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }


    public void processLoadRegisteredUsers() {
        final ProgressDialog progressDialog = ProgressDialog.show(ChannelInfoActivity.this, "",
                getString(R.string.applozic_contacts_loading_info), true);

        RegisteredUsersAsyncTask.TaskListener usersAsyncTaskTaskListener = new RegisteredUsersAsyncTask.TaskListener() {
            @Override
            public void onSuccess(RegisteredUsersApiResponse registeredUsersApiResponse, String[] userIdArray) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                userPreference.setWasContactListServerCallAlreadyDone(true);
                Intent addMemberIntent = new Intent(ChannelInfoActivity.this, ContactSelectionActivity.class);
                addMemberIntent.putExtra(ContactSelectionActivity.CHECK_BOX, true);
                addMemberIntent.putExtra(ContactSelectionActivity.CHANNEL_OBJECT, channel);
                startActivityForResult(addMemberIntent, REQUEST_CODE_FOR_CONTACT);
            }

            @Override
            public void onFailure(RegisteredUsersApiResponse registeredUsersApiResponse, String[] userIdArray, Exception exception) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                String error = getString(Utils.isInternetAvailable(ChannelInfoActivity.this) ? R.string.applozic_server_error : R.string.you_need_network_access_for_block_or_unblock);
                Toast toast = Toast.makeText(ChannelInfoActivity.this, error, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            @Override
            public void onCompletion() {

            }
        };
        RegisteredUsersAsyncTask usersAsyncTask = new RegisteredUsersAsyncTask(ChannelInfoActivity.this, usersAsyncTaskTaskListener, alCustomizationSettings.getTotalRegisteredUserToFetch(), userPreference.getRegisteredUsersLastFetchTime(), null, null, true);
        AlTask.execute(usersAsyncTask);

    }

    private int getListPreferredItemHeight() {
        final TypedValue typedValue = new TypedValue();

        getTheme().resolveAttribute(
                android.R.attr.listPreferredItemHeight, typedValue, true);
        final DisplayMetrics metrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return (int) typedValue.getDimension(metrics);
    }


    public void updateChannelList() {
        if (contactsAdapter != null && channel != null) {
            channelUserMapperList.clear();
            channelUserMapperList = ChannelService.getInstance(this).getListOfUsersFromChannelUserMapper(channel.getKey());
            contactsAdapter.notifyDataSetChanged();
            Helper.getListViewSize(mainListView);
            String oldChannelName = channel.getName();
            channel = ChannelService.getInstance(this).getChannelByChannelKey(channel.getKey());
            if (!oldChannelName.equals(channel.getName())) {
                mActionBar.setTitle(channel.getName());
                collapsingToolbarLayout.setTitle(channel.getName());
            }
        }
    }

    public void removeChannelUser(final Channel channel, final ChannelUserMapper channelUserMapper) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this).
                setPositiveButton(R.string.remove_member, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlTask.execute(new ChannelMember(channelUserMapper, channel, ChannelInfoActivity.this));

                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        String name = "";
        String channelName = "";
        Contact contact;
        if (!TextUtils.isEmpty(channelUserMapper.getUserKey())) {
            contact = baseContactService.getContactById(channelUserMapper.getUserKey());
            name = contact.getDisplayName();
            channelName = channel.getName();
        }

        alertDialog.setMessage(getString(R.string.dialog_remove_group_user).replace(getString(R.string.user_name_info), name).replace(getString(R.string.group_name_info), channelName));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    public void addChannelUser(final String userId, final Channel channel) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this).
                setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlTask.execute(new ChannelMemberAdd(channel, userId, ChannelInfoActivity.this));

                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        String name = "";
        String channelName = "";
        Contact contact;
        if (channel != null) {
            contact = baseContactService.getContactById(userId);
            name = contact.getDisplayName();
            channelName = channel.getName();
        }
        alertDialog.setMessage(getString(R.string.dialog_add_group_user).replace(getString(R.string.user_name_info), name).replace(getString(R.string.group_name_info), channelName));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    public void leaveChannel(final Channel channel) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this).
                setPositiveButton(R.string.channel_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlTask.execute(new ChannelAsync(channel, ChannelInfoActivity.this, channelUpdateReceiver));
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        if (channel.getType() != null) {
            alertDialog.setMessage(getString(R.string.leave_channel).replace(getString(R.string.groupType_info), Channel.GroupType.BROADCAST.getValue().equals(channel.getType()) ? getString(R.string.broadcast_string) : getString(R.string.group_string)));
        }
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    public void deleteChannel(final Channel channel) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this).
                setPositiveButton(R.string.channel_deleting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlTask.execute(new ChannelMemberAdd(channel, ChannelInfoActivity.this));
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        if (channel.getType() != null) {
            alertDialog.setMessage(getString(R.string.delete_channel_messages_and_channel_info).replace(getString(R.string.group_name_info), channel.getName()).replace(getString(R.string.groupType_info), Channel.GroupType.BROADCAST.getValue().equals(channel.getType()) ? getString(R.string.broadcast_string) : getString(R.string.group_string)));
        }
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (connectivityReceiver != null) {
                unregisterReceiver(connectivityReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ContactsAdapter extends BaseAdapter {
        Context context;
        private LayoutInflater mInflater;

        public ContactsAdapter(Context context) {
            this.context = context;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String contactNumber;
            char firstLetter;
            ContactViewHolder holder;
            ChannelUserMapper channelUserMapper = channelUserMapperList.get(position);
            final Contact contact = baseContactService.getContactById(channelUserMapper.getUserKey());
            if (convertView == null) {
                convertView =
                        mInflater.inflate(R.layout.contact_users_layout, parent, false);
                holder = new ContactViewHolder();
                holder.displayName = (TextView) convertView.findViewById(R.id.displayName);
                holder.alphabeticImage = (TextView) convertView.findViewById(R.id.alphabeticImage);
                holder.circleImageView = (CircleImageView) convertView.findViewById(R.id.contactImage);
                holder.adminTextView = (TextView) convertView.findViewById(R.id.adminTextView);
                holder.lastSeenAtTextView = (TextView) convertView.findViewById(R.id.lastSeenAtTextView);
                holder.displayName.setSelected(true);
                holder.displayName.requestFocus();
                convertView.setTag(holder);
            } else {
                holder = (ContactViewHolder) convertView.getTag();
            }

            holder.circleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (contact != null) {
                        BroadcastService.sendContactProfileClickBroadcast(ChannelInfoActivity.this.getApplicationContext(), contact.getUserId());
                    }
                }
            });

            holder.alphabeticImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (contact != null) {
                        BroadcastService.sendContactProfileClickBroadcast(ChannelInfoActivity.this.getApplicationContext(), contact.getUserId());
                    }
                }
            });

            GradientDrawable bgShapeAdminText = (GradientDrawable) holder.adminTextView.getBackground();
            bgShapeAdminText.setColor(Color.parseColor(alCustomizationSettings.getAdminBackgroundColor()));
            bgShapeAdminText.setStroke(2, Color.parseColor(alCustomizationSettings.getAdminBorderColor()));
            holder.adminTextView.setTextColor(Color.parseColor(alCustomizationSettings.getAdminTextColor()));

            if (userPreference.getUserId().equals(contact.getUserId())) {
                holder.displayName.setText(getString(R.string.you_string));
            } else {
                holder.displayName.setText(contact.getDisplayName());
            }
            if (ChannelUtils.isAdminUserId(channelUserMapper.getUserKey(), channel) && Integer.valueOf(0).equals(channelUserMapper.getRole()) || ChannelUserMapper.UserRole.ADMIN.getValue().equals(channelUserMapper.getRole())) {
                holder.adminTextView.setVisibility(View.VISIBLE);
            } else {
                holder.adminTextView.setVisibility(View.GONE);
            }
            if (!userPreference.getUserId().equals(contact.getUserId())) {
                if (contact.isConnected()) {
                    holder.lastSeenAtTextView.setVisibility(View.VISIBLE);
                    holder.lastSeenAtTextView.setText(getString(R.string.user_online));
                } else if (contact.getLastSeenAt() != 0) {
                    holder.lastSeenAtTextView.setVisibility(View.VISIBLE);
                    holder.lastSeenAtTextView.setText(getString(R.string.subtitle_last_seen_at_time) + " " + String.valueOf(DateUtils.getDateAndTimeForLastSeen(ChannelInfoActivity.this, contact.getLastSeenAt(), alCustomizationSettings.getDateFormatCustomization().getTimeAndDateTemplate(), R.string.JUST_NOW, R.plurals.MINUTES_AGO, R.plurals.HOURS_AGO, R.string.YESTERDAY)));
                } else {
                    holder.lastSeenAtTextView.setVisibility(View.GONE);
                    holder.lastSeenAtTextView.setText("");
                }
            } else {
                holder.lastSeenAtTextView.setVisibility(View.GONE);
                holder.lastSeenAtTextView.setText("");
            }

            if (contact != null && !TextUtils.isEmpty(contact.getDisplayName())) {
                contactNumber = contact.getDisplayName().toUpperCase();
                firstLetter = contact.getDisplayName().toUpperCase().charAt(0);
                if (firstLetter != '+') {
                    holder.alphabeticImage.setText(String.valueOf(firstLetter));
                } else if (contactNumber.length() >= 2) {
                    holder.alphabeticImage.setText(String.valueOf(contactNumber.charAt(1)));
                }
                Character colorKey = AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter) ? firstLetter : null;
                GradientDrawable bgShape = (GradientDrawable) holder.alphabeticImage.getBackground();
                bgShape.setColor(context.getResources().getColor(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey)));
            }
            holder.alphabeticImage.setVisibility(View.GONE);
            holder.circleImageView.setVisibility(View.VISIBLE);
            if (contact != null) {
                if (contact.isDrawableResources()) {
                    int drawableResourceId = context.getResources().getIdentifier(contact.getrDrawableName(), "drawable", context.getPackageName());
                    holder.circleImageView.setImageResource(drawableResourceId);
                } else {
                    contactImageLoader.loadImage(contact, holder.circleImageView, holder.alphabeticImage);
                }
            }
            return convertView;
        }

        @Override
        public int getCount() {
            return channelUserMapperList.size();
        }

        @Override
        public Object getItem(int position) {
            return channelUserMapperList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }


    }


    public static class Helper {
        public static void getListViewSize(ListView myListView) {
            ListAdapter myListAdapter = myListView.getAdapter();
            if (myListAdapter == null) {
                //do nothing return null
                return;
            }
            int totalHeight = 0;
            if (myListAdapter.getCount() > 0) {
                View listItem = myListAdapter.getView(0, null, myListView);
                listItem.measure(0, 0);
                totalHeight = listItem.getMeasuredHeight() * myListAdapter.getCount();

                ViewGroup.LayoutParams params = myListView.getLayoutParams();
                params.height = totalHeight + (myListView.getDividerHeight() * (myListAdapter.getCount() - 1));
                myListView.setLayoutParams(params);
            }
        }
    }

    public class ChannelMember extends AlAsyncTask<Integer, Long> {
        String responseForRemove;
        private ChannelUserMapper channelUserMapper;
        private ChannelService channelService;
        private ProgressDialog progressDialog;
        private Context context;
        private Channel channel;


        public ChannelMember(ChannelUserMapper channelUserMapper, Channel channel, Context context) {
            this.channelUserMapper = channelUserMapper;
            this.channel = channel;
            this.context = context;
            this.channelService = ChannelService.getInstance(context);

        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, "",
                    context.getString(R.string.removing_channel_user), true);
        }

        @Override
        protected Long doInBackground() {
            if (channel != null && channelUserMapper != null) {
                responseForRemove = channelService.removeMemberFromChannelProcess(channel.getKey(), channelUserMapper.getUserKey());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!Utils.isInternetAvailable(context)) {
                Toast toast = Toast.makeText(context, getString(R.string.you_dont_have_any_network_access_info), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            if (SUCCESS.equals(responseForRemove) && contactsAdapter != null) {
                if (channelUserMapperList != null && channelUserMapperList.size() > 0) {
                    channelUserMapperList.remove(channelUserMapper);
                    contactsAdapter.notifyDataSetChanged();
                    Helper.getListViewSize(mainListView);
                }
            }
        }

    }

    private class ContactViewHolder {
        public TextView displayName, alphabeticImage, adminTextView, lastSeenAtTextView;
        public CircleImageView circleImageView;

        public ContactViewHolder() {
        }

    }

    public class ChannelMemberAdd extends AlAsyncTask<Integer, Long> {
        ApiResponse apiResponse;
        String responseForDeleteGroup;
        String userId;
        private ChannelService channelService;
        private ProgressDialog progressDialog;
        private Context context;
        private Channel channel;


        public ChannelMemberAdd(Channel channel, String userId, Context context) {
            this.channel = channel;
            this.context = context;
            this.userId = userId;
            this.channelService = ChannelService.getInstance(context);
        }

        public ChannelMemberAdd(Channel channel, Context context) {
            this.channel = channel;
            this.context = context;
            this.channelService = ChannelService.getInstance(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!TextUtils.isEmpty(userId)) {
                progressDialog = ProgressDialog.show(context, "",
                        context.getString(R.string.adding_channel_user), true);
            } else {
                progressDialog = ProgressDialog.show(context, "",
                        context.getString(R.string.deleting_channel_user), true);
            }
        }

        @Override
        protected Long doInBackground() {
            if (channel != null && !TextUtils.isEmpty(userId)) {
                apiResponse = channelService.addMemberToChannelWithResponseProcess(channel.getKey(), userId);
            }
            if (channel != null && TextUtils.isEmpty(userId)) {
                responseForDeleteGroup = channelService.processChannelDeleteConversation(channel, context);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!Utils.isInternetAvailable(context)) {
                Toast toast = Toast.makeText(context, getString(R.string.you_dont_have_any_network_access_info), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            if (apiResponse != null) {
                if (apiResponse.isSuccess()) {
                    ChannelUserMapper channelUserMapper = new ChannelUserMapper(channel.getKey(), userId);
                    channelUserMapperList.add(channelUserMapper);
                    contactsAdapter.notifyDataSetChanged();
                    Helper.getListViewSize(mainListView);
                } else {
                    List<ErrorResponseFeed> error = apiResponse.getErrorResponse();
                    if (error != null && error.size() > 0) {
                        ErrorResponseFeed errorResponseFeed = error.get(0);
                        String errorDescription = errorResponseFeed.getDescription();
                        if (!TextUtils.isEmpty(errorDescription)) {
                            if (MobiComKitConstants.GROUP_USER_LIMIT_EXCEED.equalsIgnoreCase(errorDescription)) {
                                Toast.makeText(context, R.string.group_members_limit_exceeds, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, R.string.applozic_server_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
            if (!TextUtils.isEmpty(responseForDeleteGroup) && SUCCESS.equals(responseForDeleteGroup)) {
                try {
                    if (!TextUtils.isEmpty(alCustomizationSettings.getGroupDeletePackageName())) {
                        Class HomeActivity = Class.forName(alCustomizationSettings.getGroupDeletePackageName().trim());
                        if (HomeActivity != null) {
                            userPreference.setDeleteChannel(true);
                            Intent intent = new Intent(ChannelInfoActivity.this, HomeActivity);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra(ConversationUIService.FROM_GROUP_DELETE, true);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Intent intent = new Intent(ChannelInfoActivity.this, ConversationActivity.class);
                        if (ApplozicClient.getInstance(ChannelInfoActivity.this).isContextBasedChat()) {
                            intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                        }
                        startActivity(intent);
                        userPreference.setDeleteChannel(true);
                        finish();
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class ChannelAsync extends AlAsyncTask<Integer, Long> {
        GroupInfoUpdate groupInfoUpdate;
        String responseForExit;
        String responseForChannelUpdate;
        private ChannelService channelService;
        private ProgressDialog progressDialog;
        private Context context;
        private Channel channel;
        private ResultReceiver channelUpdateReceiver;

        public ChannelAsync(Channel channel, Context context, ResultReceiver channelUpdateReceiver) {
            this.channel = channel;
            this.context = context;
            this.channelService = ChannelService.getInstance(context);
            this.channelUpdateReceiver = channelUpdateReceiver;
        }

        public ChannelAsync(GroupInfoUpdate groupInfoUpdate, Context context, ResultReceiver channelUpdateReceiver) {
            this.groupInfoUpdate = groupInfoUpdate;
            this.context = context;
            this.channelService = ChannelService.getInstance(context);
            this.channelUpdateReceiver = channelUpdateReceiver;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (groupInfoUpdate != null) {
                progressDialog = ProgressDialog.show(context, "",
                        context.getString(R.string.channel_update), true);
            }
            if (channel != null) {
                progressDialog = ProgressDialog.show(context, "",
                        context.getString(R.string.channel_member_exit), true);
            }

        }

        @Override
        protected Long doInBackground() {
            if (groupInfoUpdate != null) {
                if (!TextUtils.isEmpty(groupInfoUpdate.getNewlocalPath())) {
                    try {
                        String response = new FileClientService(context).uploadProfileImage(groupInfoUpdate.getNewlocalPath());
                        groupInfoUpdate.setImageUrl(response);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    groupInfoUpdate.setImageUrl(null);
                }
                responseForChannelUpdate = channelService.updateChannel(groupInfoUpdate);
            }
            if (channel != null) {
                responseForExit = channelService.leaveMemberFromChannelProcess(channel.getKey(), userPreference.getUserId());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (channel != null && !Utils.isInternetAvailable(context)) {
                Toast toast = Toast.makeText(context, getString(R.string.failed_to_leave_group), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            if (groupInfoUpdate != null && !Utils.isInternetAvailable(context)) {
                Toast toast = Toast.makeText(context, getString(R.string.internet_connection_for_group_name_info), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            if (!TextUtils.isEmpty(responseForExit) && SUCCESS.equals(responseForExit)) {
                ChannelInfoActivity.this.finish();
            }
            if (!TextUtils.isEmpty(responseForChannelUpdate) && SUCCESS.equals(responseForChannelUpdate)) {
                if (!TextUtils.isEmpty(groupInfoUpdate.getNewName())) {
                    mActionBar.setTitle(groupInfoUpdate.getNewName());
                    collapsingToolbarLayout.setTitle(groupInfoUpdate.getNewName());
                }
                //File has been updated..rename new file to oldfile
                if (!TextUtils.isEmpty(groupInfoUpdate.getNewlocalPath()) && !TextUtils.isEmpty(groupInfoUpdate.getImageUrl()) && !TextUtils.isEmpty(groupInfoUpdate.getContentUri())) {
                    File file = new File(groupInfoUpdate.getNewlocalPath());
                    channel = ChannelInfoActivity.this.channel;
                    if (!TextUtils.isEmpty(channel.getLocalImageUri())) {
                        file.renameTo(new File(channel.getLocalImageUri()));
                    } else {
                        file.renameTo(FileClientService.getFilePath(channel.getKey() + "_profile.jpeg", context.getApplicationContext(), "image"));
                    }
                    channel.setLocalImageUri(file.getAbsolutePath());
                    channelService.updateChannel(channel);
                    channelImage.setImageURI(Uri.parse(groupInfoUpdate.getContentUri()));
                }
                Map<String, String> metadata = groupInfoUpdate.getMetadata();
                if (metadata != null && metadata.containsKey(ChannelMetadata.AL_CHANNEL_DESCRIPTION)) {
                    updateChannelDescriptionUIFrom(metadata);
                }
            }
            if (channelUpdateReceiver != null) {
                channelUpdateReceiver.send(1, null);
            }
            //update channel object for activity
            ChannelInfoActivity.this.channel = ChannelService.getInstance(ChannelInfoActivity.this).getChannelByChannelKey(channelKey);
        }
    }


    public class RefreshBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateChannelList();
        }
    }

    static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastService.INTENT_ACTIONS.UPDATE_GROUP_INFO.toString());
        intentFilter.addAction(BroadcastService.INTENT_ACTIONS.UPDATE_USER_DETAIL.toString());
        return intentFilter;
    }

    public class ChannelUserRoleAsyncTask extends AlAsyncTask<Integer, Long> {
        private final ChannelService channelService;
        private ProgressDialog progressDialog;
        private final Context context;
        private final ChannelUserMapper channelUserMapper;
        private final int newRole;
        private String response;

        public ChannelUserRoleAsyncTask(Context context, ChannelUserMapper channelUserMapper, int newRole) {
            this.channelUserMapper = channelUserMapper;
            this.context = context;
            this.channelService = ChannelService.getInstance(context);
            this.newRole = newRole;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, "",
                    context.getString(R.string.please_wait_info), true);
        }

        @Override
        protected Long doInBackground() {
            boolean updated = channelService.updateRoleForUserInChannel(channelUserMapper.getKey(), channelUserMapper.getUserKey(), newRole);
            if (updated) {
                channelUserMapper.setRole(newRole);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!TextUtils.isEmpty(response) && MobiComKitConstants.SUCCESS.equals(response)) {
                if (channelUserMapper != null && channelUserMapperList != null) {
                    try {
                        int index = channelUserMapperList.indexOf(channelUserMapper);
                        channelUserMapperList.remove(channelUserMapper);
                        channelUserMapperList.add(index, channelUserMapper);
                        contactsAdapter.notifyDataSetChanged();
                        Helper.getListViewSize(mainListView);
                    } catch (Exception e) { }
                }
            }
        }
    }
}