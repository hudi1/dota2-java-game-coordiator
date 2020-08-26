package org.tomass.dota.gc.handlers.features;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.tomass.dota.gc.handlers.Dota2ClientGCMsgHandler;
import org.tomass.dota.gc.handlers.callbacks.chat.ChatChannelLeft;
import org.tomass.dota.gc.handlers.callbacks.chat.ChatJoinedChannelCallback;
import org.tomass.dota.gc.handlers.callbacks.chat.ChatMembersJoinUpdateCallback;
import org.tomass.dota.gc.handlers.callbacks.chat.ChatMembersLeftUpdateCallback;
import org.tomass.dota.gc.handlers.callbacks.chat.ChatMembersUpdateCallback;
import org.tomass.dota.gc.handlers.callbacks.chat.ChatMessageCallback;
import org.tomass.dota.gc.util.Tuple2;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTAChatChannelMemberUpdate;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTAChatChannelMemberUpdate.JoinedMember;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTAChatMember;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTAChatMessage;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTAJoinChatChannel;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTAJoinChatChannelResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTAJoinChatChannelResponse.Result;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTALeaveChatChannel;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTAOtherJoinedChatChannel;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTAOtherLeftChatChannel;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTARequestChatChannelList;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby;
import org.tomass.protobuf.dota.DotaGcmessagesMsgid.EDOTAGCMsg;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTAChatChannelType_t;

import in.dragonbra.javasteam.base.ClientGCMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketGCMsg;
import in.dragonbra.javasteam.util.compat.Consumer;

public class Dota2Chat extends Dota2ClientGCMsgHandler {

    private Map<Integer, Consumer<IPacketGCMsg>> dispatchMap;

    private Map<Long, ChatChannel> channels;
    private Map<Tuple2<String, DOTAChatChannelType_t>, ChatChannel> channelsByName;

    public Dota2Chat() {
        channels = new ConcurrentHashMap<>();
        channelsByName = new ConcurrentHashMap<>();
        dispatchMap = new HashMap<>();
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCJoinChatChannelResponse_VALUE, packetMsg -> handleJoinResponse(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCChatMessage_VALUE, packetMsg -> handleMessage(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCOtherJoinedChannel_VALUE, packetMsg -> handleMembersJoinUpdate(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCOtherLeftChannel_VALUE, packetMsg -> handleMembersLeftUpdate(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgDOTAChatChannelMemberUpdate_VALUE,
                packetMsg -> handleMembersUpdate(packetMsg));
    }

    public void cleanup() {
        this.channels.clear();
        this.channelsByName.clear();
    }

    public void removeChannel(Long channelId) {
        ChatChannel channel = this.channels.remove(channelId);
        this.channelsByName.remove(new Tuple2<>(channel.getName(), channel.getType()));
    }

    public ChatChannel getLobbyChatChannel(String name) {
        return this.channelsByName.get(new Tuple2<>("Lobby_" + name, DOTAChatChannelType_t.DOTAChannelType_Lobby));
    }

    public Map<Tuple2<String, DOTAChatChannelType_t>, ChatChannel> getChannelsByName() {
        return channelsByName;
    }

    private void handleJoinResponse(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgDOTAJoinChatChannelResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTAJoinChatChannelResponse.class, msg);
        Tuple2<String, DOTAChatChannelType_t> key = new Tuple2<>(protobuf.getBody().getChannelName(),
                protobuf.getBody().getChannelType());

        if (protobuf.getBody().getResult() == Result.JOIN_SUCCESS) {
            ChatChannel channel;
            if (channels.containsKey(protobuf.getBody().getChannelId())) {
                channel = channels.get(protobuf.getBody().getChannelId());
            } else {
                channel = new ChatChannel(protobuf.getBody().build(), this);
                channels.put(channel.getId(), channel);
                channelsByName.put(key, channel);
            }
            client.postCallback(new ChatJoinedChannelCallback(channel));
        } else {
            getLogger().warn("handleJoinResponse: " + protobuf.getBody());
        }
    }

    private void handleMessage(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgDOTAChatMessage.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgDOTAChatMessage.class,
                msg);
        if (channels.containsKey(protobuf.getBody().getChannelId())) {
            client.postCallback(
                    new ChatMessageCallback(protobuf.getBody(), channels.get(protobuf.getBody().getChannelId())));
        }
    }

    public void handleMembersLeftUpdate(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgDOTAOtherLeftChatChannel.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTAOtherLeftChatChannel.class, msg);
        if (channels.containsKey(protobuf.getBody().getChannelId())) {
            ChatChannel channel = channels.get(protobuf.getBody().getChannelId());
            Set<Long> left = new HashSet<>();
            if (protobuf.getBody().getSteamId() != 0) {
                left.add(protobuf.getBody().getSteamId());
            }
            channel.processMembers(protobuf.getBody().build());
            if (!left.isEmpty()) {
                client.postCallback(new ChatMembersLeftUpdateCallback(channel, left));
            }
        }
    }

    public void handleMembersJoinUpdate(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgDOTAOtherJoinedChatChannel.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTAOtherJoinedChatChannel.class, msg);
        if (channels.containsKey(Long.valueOf(protobuf.getBody().getChannelId()))) {
            ChatChannel channel = channels.get(protobuf.getBody().getChannelId());
            channel.processMembers(protobuf.getBody().build());
            if (protobuf.getBody().getSteamId() != 0) {
                client.postCallback(new ChatMembersJoinUpdateCallback(channel, protobuf.getBody().getSteamId()));
            }
        }
    }

    public void handleMembersUpdate(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgDOTAChatChannelMemberUpdate.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTAChatChannelMemberUpdate.class, msg);
        if (channels.containsKey(protobuf.getBody().getChannelId())) {
            ChatChannel channel = channels.get(protobuf.getBody().getChannelId());
            Set<Long> join = new HashSet<>();
            Set<Long> left = new HashSet<>();

            for (JoinedMember member : protobuf.getBody().getJoinedMembersList()) {
                if (member.getSteamId() != 0) {
                    join.add(member.getSteamId());
                }
            }

            for (Long steamId : protobuf.getBody().getLeftSteamIdsList()) {
                if (steamId != null) {
                    join.add(steamId);
                }
            }

            channel.processMembers(protobuf.getBody().build());
            if (!join.isEmpty() || !left.isEmpty()) {
                client.postCallback(new ChatMembersUpdateCallback(channel, join, left));
            }
        }
    }

    public void joinChannel(String channelName, DOTAChatChannelType_t channelType) {
        ClientGCMsgProtobuf<CMsgDOTAJoinChatChannel.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTAJoinChatChannel.class, EDOTAGCMsg.k_EMsgGCJoinChatChannel_VALUE);
        protobuf.getBody().setChannelName(channelName);
        protobuf.getBody().setChannelType(channelType);
        getLogger().trace(">>joinChannel: " + protobuf.getBody());
        send(protobuf);
    }

    public void joinLobbyChannel() {
        if (client.getLobbyHandler() != null && client.getLobbyHandler().getLobby() != null) {
            joinChannel("Lobby_" + client.getLobbyHandler().getLobby().getLobbyId(),
                    DOTAChatChannelType_t.DOTAChannelType_Lobby);
        }
    }

    public void joinLobbyChannel(CSODOTALobby lobby) {
        joinChannel("Lobby_" + lobby.getLobbyId(), DOTAChatChannelType_t.DOTAChannelType_Lobby);
    }

    public void joinPartyChannel() {
        if (client.getPartyHandler() != null && client.getPartyHandler().getParty() != null) {
            joinChannel("Party_" + client.getPartyHandler().getParty().getPartyId(),
                    DOTAChatChannelType_t.DOTAChannelType_Party);
        }
    }

    public void getChannelList() {
        ClientGCMsgProtobuf<CMsgDOTARequestChatChannelList.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTARequestChatChannelList.class, EDOTAGCMsg.k_EMsgGCRequestChatChannelList_VALUE);
        getLogger().trace(">>getChannelList: " + protobuf.getBody());
    }

    public void leaveChannels() {
        for (Long channelId : channels.keySet()) {
            leaveChannel(channelId);
        }
    }

    public void leaveChannel(Long channelId) {
        if (channels.containsKey(channelId)) {
            ChatChannel channel = channels.get(channelId);

            ClientGCMsgProtobuf<CMsgDOTALeaveChatChannel.Builder> protobuf = new ClientGCMsgProtobuf<>(
                    CMsgDOTALeaveChatChannel.class, EDOTAGCMsg.k_EMsgGCLeaveChatChannel_VALUE);
            protobuf.getBody().setChannelId(channelId);
            getLogger().trace(">>leaveChannel: " + protobuf.getBody());
            send(protobuf);

            removeChannel(channelId);
            client.postCallback(new ChatChannelLeft(channel));
        }
    }

    public void sendAllChannelMessage(String message) {
        channels.values().forEach(chat -> chat.send(message));
    }

    public void shareLobby() {
        channels.values().forEach(chat -> chat.shareLobby());
    }

    public void flipCoin() {
        channels.values().forEach(chat -> chat.flipCoin());
    }

    public void flipCoin(Integer min, Integer max) {
        channels.values().forEach(chat -> chat.rollDice(min, max));
    }

    @Override
    public void handleGCMsg(IPacketGCMsg packetGCMsg) {
        Consumer<IPacketGCMsg> dispatcher = dispatchMap.get(packetGCMsg.getMsgType());
        if (dispatcher != null) {
            getLogger().info(">>handleGCMsg chat msg: " + packetGCMsg.getMsgType());
            dispatcher.accept(packetGCMsg);
        }
    }

    // inner class
    public class ChatChannel {

        private Set<ChatMember> members;
        private long id;
        private String name;
        private DOTAChatChannelType_t type;
        private int userId;
        private int maxMembers;
        private Dota2Chat dota2chat;

        public ChatChannel(CMsgDOTAJoinChatChannelResponse joinData, Dota2Chat dota2chat) {
            this.members = new HashSet<>();
            this.id = joinData.getChannelId();
            this.name = joinData.getChannelName();
            this.type = joinData.getChannelType();
            this.userId = joinData.getChannelUserId();
            this.maxMembers = joinData.getMaxMembers();
            this.dota2chat = dota2chat;
            processMembers(joinData);
        }

        private void processMembers(Object joinData) {
            if (joinData instanceof CMsgDOTAOtherLeftChatChannel) {
                members.removeIf(m -> m.getSteamId().equals(((CMsgDOTAOtherLeftChatChannel) joinData).getSteamId())
                        || m.getChannelUserId().equals(((CMsgDOTAOtherLeftChatChannel) joinData).getChannelUserId()));
                return;
            } else if (joinData instanceof CMsgDOTAJoinChatChannelResponse) {
                for (CMsgDOTAChatMember member : ((CMsgDOTAJoinChatChannelResponse) joinData).getMembersList()) {
                    members.add(new ChatMember(member));
                }
            } else if (joinData instanceof CMsgDOTAOtherJoinedChatChannel) {
                members.add(new ChatMember((CMsgDOTAOtherJoinedChatChannel) joinData));
            } else if (joinData instanceof CMsgDOTAChatChannelMemberUpdate) {
                for (Long steamId : ((CMsgDOTAChatChannelMemberUpdate) joinData).getLeftSteamIdsList()) {
                    members.removeIf(m -> m.getSteamId().equals(steamId));
                }
                for (JoinedMember member : ((CMsgDOTAChatChannelMemberUpdate) joinData).getJoinedMembersList()) {
                    members.add(new ChatMember(member));
                }
            }
        }

        public void leave() {
            dota2chat.leaveChannel(id);
        }

        public void send(String message) {
            ClientGCMsgProtobuf<CMsgDOTAChatMessage.Builder> protobuf = new ClientGCMsgProtobuf<>(
                    CMsgDOTAChatMessage.class, EDOTAGCMsg.k_EMsgGCChatMessage_VALUE);
            protobuf.getBody().setChannelId(id);
            protobuf.getBody().setText(message);
            getLogger().trace(">>ChatChannelSend: " + protobuf.getBody());
            dota2chat.send(protobuf);
        }

        public void shareLobby() {
            if (dota2chat.client.getLobbyHandler() != null && dota2chat.client.getLobbyHandler().getLobby() != null) {
                ClientGCMsgProtobuf<CMsgDOTAChatMessage.Builder> protobuf = new ClientGCMsgProtobuf<>(
                        CMsgDOTAChatMessage.class, EDOTAGCMsg.k_EMsgGCChatMessage_VALUE);
                protobuf.getBody().setChannelId(id);
                protobuf.getBody().setShareLobbyId(dota2chat.client.getLobbyHandler().getLobby().getLobbyId());
                protobuf.getBody().setShareLobbyPasskey(dota2chat.client.getLobbyHandler().getLobby().getPassKey());
                getLogger().trace(">>ChatChannelshareLobby: " + protobuf.getBody());
                dota2chat.send(protobuf);
            }
        }

        public void flipCoin() {
            ClientGCMsgProtobuf<CMsgDOTAChatMessage.Builder> protobuf = new ClientGCMsgProtobuf<>(
                    CMsgDOTAChatMessage.class, EDOTAGCMsg.k_EMsgGCChatMessage_VALUE);
            protobuf.getBody().setChannelId(id);
            protobuf.getBody().setCoinFlip(true);
            getLogger().trace(">>ChatChannelflipCoin: " + protobuf.getBody());
            dota2chat.send(protobuf);
        }

        public void rollDice(Integer min, Integer max) {
            ClientGCMsgProtobuf<CMsgDOTAChatMessage.Builder> protobuf = new ClientGCMsgProtobuf<>(
                    CMsgDOTAChatMessage.class, EDOTAGCMsg.k_EMsgGCChatMessage_VALUE);
            protobuf.getBody().setChannelId(id);
            protobuf.getBody().getDiceRollBuilder().setRollMin(min != null ? min : 1)
                    .setRollMax(max != null ? max : 100).build();
            getLogger().trace(">>ChatChannelflipCoin: " + protobuf.getBody());
            dota2chat.send(protobuf);
        }

        public Set<ChatMember> getMembers() {
            return members;
        }

        public void setMembers(Set<ChatMember> members) {
            this.members = members;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public DOTAChatChannelType_t getType() {
            return type;
        }

        public void setType(DOTAChatChannelType_t type) {
            this.type = type;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getMaxMembers() {
            return maxMembers;
        }

        public void setMaxMembers(int maxMembers) {
            this.maxMembers = maxMembers;
        }

    }

    private class ChatMember {

        private Integer channelUserId;
        private String personaName;
        private Long steamId;
        private Integer status;

        public ChatMember(CMsgDOTAChatMember data) {
            this.channelUserId = data.getChannelUserId();
            this.personaName = data.getPersonaName();
            this.steamId = data.getSteamId();
            this.status = data.getStatus();
        }

        public ChatMember(CMsgDOTAOtherJoinedChatChannel data) {
            this.channelUserId = data.getChannelUserId();
            this.personaName = data.getPersonaName();
            this.steamId = data.getSteamId();
            this.status = data.getStatus();
        }

        public ChatMember(JoinedMember data) {
            this.channelUserId = data.getChannelUserId();
            this.personaName = data.getPersonaName();
            this.steamId = data.getSteamId();
            this.status = data.getStatus();
        }

        public Integer getChannelUserId() {
            return channelUserId;
        }

        public Long getSteamId() {
            return steamId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((channelUserId == null) ? 0 : channelUserId.hashCode());
            result = prime * result + ((steamId == null) ? 0 : steamId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ChatMember other = (ChatMember) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (channelUserId == null) {
                if (other.channelUserId != null)
                    return false;
            } else if (!channelUserId.equals(other.channelUserId))
                return false;
            if (steamId == null) {
                if (other.steamId != null)
                    return false;
            } else if (!steamId.equals(other.steamId))
                return false;
            return true;
        }

        private Dota2Chat getOuterType() {
            return Dota2Chat.this;
        }

        @Override
        public String toString() {
            return "ChatMember [channelUserId=" + channelUserId + ", personaName=" + personaName + ", steamId="
                    + steamId + ", status=" + status + "]";
        }

    }
}
