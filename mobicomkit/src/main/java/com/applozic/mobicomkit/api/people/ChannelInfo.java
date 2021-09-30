package com.applozic.mobicomkit.api.people;


import com.applozic.mobicommons.json.JsonMarker;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.ChannelMetadata;
import com.applozic.mobicommons.people.channel.ChannelUserMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores {@link Channel} information. Passed to <i>channel</i> related API requests.
 */
public class ChannelInfo extends JsonMarker {

    private String clientGroupId;
    private String groupName;
    List<GroupUser> users;
    private List<String> groupMemberList;
    private String imageUrl;
    private int type = Channel.GroupType.PUBLIC.getValue().intValue();
    private Map<String, String> metadata;
    private String admin;
    private Integer parentKey;
    private String parentClientGroupId;
    private ChannelMetadata channelMetadata;

    public ChannelInfo() {
        this.metadata = new HashMap<>();
    }

    public ChannelInfo(String groupName, List<String> groupMemberList) {
        this();
        this.groupName = groupName;
        this.groupMemberList = groupMemberList;
    }

    public ChannelInfo(String groupName, List<String> groupMemberList, String imageLink) {
        this(groupName, groupMemberList);
        this.imageUrl = imageLink;
    }

    /**
     * @see #setClientGroupId(String)
     */
    public String getClientGroupId() {
        return clientGroupId;
    }

    /**
     * Use this if you want to set your own <i>id</i> for the channel.
     *
     * @see Channel#getClientGroupId()
     */
    public void setClientGroupId(String clientGroupId) {
        this.clientGroupId = clientGroupId;
    }

    /**
     * @see #setGroupName(String)
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * The display name for your <i>channel</i>.
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * @see #setGroupMemberList(List)
     */
    public List<String> getGroupMemberList() {
        return groupMemberList;
    }

    /**
     * When you create a new {@link Channel} you have the option to specify the initial members for that channel.
     * Pass the list of <i>user-ids</i> here.
     */
    public void setGroupMemberList(List<String> groupMemberList) {
        this.groupMemberList = groupMemberList;
    }

    /**
     * @see #setImageUrl(String)
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Set the remote URL to the display picture for the <i>channel</i>.
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * @see com.applozic.mobicommons.people.channel.Channel.GroupType
     */
    public int getType() {
        return type;
    }

    /**
     * @see com.applozic.mobicommons.people.channel.Channel.GroupType
     */
    public void setType(int type) {
        this.type = type;
    }

    public ChannelMetadata getChannelMetadata() {
        return channelMetadata;
    }

    /**
     * Gets the user-id(or user-ids separated by commas) string for channel {@link ChannelUserMapper.UserRole admin/s}.
     */
    public String getAdmin() {
        return admin;
    }

    /**
     * Set the user-id of the user you wish to make admin when the channel is created.
     *
     * <p>Note: The user-id must be present in {@link #getGroupMemberList()}.</p>
     */
    public void setAdmin(String admin) {
        this.admin = admin;
    }

    /**
     * Gets details for each member of the channel.
     */
    public List<GroupUser> getUsers() {
        return users;
    }

    /**
     * This is an internal method. Do not use.
     */
    public void setUsers(List<GroupUser> users) {
        this.users = users;
    }

    /**
     * This is an internal method.
     */
    public Integer getParentKey() {
        return parentKey;
    }

    /**
     * This is an internal method. Do not use.
     */
    public void setParentKey(Integer parentKey) {
        this.parentKey = parentKey;
    }

    /**
     * This is an internal method.
     */
    public String getParentClientGroupId() {
        return parentClientGroupId;
    }

    /**
     * This is an internal method. Do not use.
     */
    public void setParentClientGroupId(String parentClientGroupId) {
        this.parentClientGroupId = parentClientGroupId;
    }

    /**
     * @see ChannelMetadata
     */
    public void setChannelMetadata(ChannelMetadata channelMetadata){
        this.channelMetadata = channelMetadata;
        this.metadata = channelMetadata.getMetadata();
    }

    /**
     * @see #setMetadata(Map)
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Custom key-value data for the channel.
     *
     * @see Channel#getMetadata()
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Member of a <i>channel</i>.
     */
    public class GroupUser extends JsonMarker {
        String userId;
        int groupRole;

        public String getUserId() {
            return userId;
        }

        public GroupUser setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        /**
         * A <code>groupRole</code> of <i>1</i> is for an admin. A <code>groupRole</code> of <i>0</i> is for a non-admin.
         */
        public int getGroupRole() {
            return groupRole;
        }

        /**
         * @see #getGroupRole()
         */
        public GroupUser setGroupRole(int groupRole) {
            this.groupRole = groupRole;
            return this;
        }

        @Override
        public String toString() {
            return "GroupUser{" +
                    "userId='" + userId + '\'' +
                    ", groupRole=" + groupRole +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ChannelInfo{" +
                "clientGroupId='" + clientGroupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", users=" + users +
                ", groupMemberList=" + groupMemberList +
                ", imageUrl='" + imageUrl + '\'' +
                ", type=" + type +
                ", metadata=" + metadata +
                ", admin='" + admin + '\'' +
                ", parentKey=" + parentKey +
                ", channelMetadata=" + channelMetadata +
                '}';
    }
}
