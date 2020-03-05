package org.tomass.dota.gc.handlers.features;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomass.dota.gc.handlers.Dota2ClientGCMsgHandler;
import org.tomass.dota.gc.handlers.callbacks.NotReadyCallback;
import org.tomass.dota.gc.handlers.callbacks.party.PartyInvitationCreatedCallback;
import org.tomass.dota.gc.handlers.callbacks.party.PartyInviteCallback;
import org.tomass.dota.gc.handlers.callbacks.party.PartyNewCallback;
import org.tomass.dota.gc.handlers.callbacks.party.PartyRemovedCallback;
import org.tomass.dota.gc.handlers.callbacks.party.PartyUpdatedCallback;
import org.tomass.dota.gc.handlers.callbacks.shared.SingleObjectNewParty;
import org.tomass.dota.gc.handlers.callbacks.shared.SingleObjectRemovedParty;
import org.tomass.dota.gc.handlers.callbacks.shared.SingleObjectUpdatedParty;
import org.tomass.dota.gc.util.CSOTypes;
import org.tomass.dota.steam.handlers.Dota2SteamGameCoordinator;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgInvitationCreated;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgInviteToParty;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgKickFromParty;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgLeaveParty;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgPartyInviteResponse;
import org.tomass.protobuf.dota.BaseGcmessages.EGCBaseMsg;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgDOTAPartyMemberSetCoach;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgDOTASetGroupLeader;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CMsgPartyReadyCheckAcknowledge;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CMsgPartyReadyCheckRequest;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTAParty;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTAPartyInvite;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.EReadyCheckStatus;
import org.tomass.protobuf.dota.DotaGcmessagesMsgid.EDOTAGCMsg;

import com.google.protobuf.ByteString;

import in.dragonbra.javasteam.base.ClientGCMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketGCMsg;
import in.dragonbra.javasteam.util.compat.Consumer;

public class Dota2Party extends Dota2ClientGCMsgHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, Consumer<IPacketGCMsg>> dispatchMap;

    private CSODOTAParty party;

    public Dota2Party() {
        dispatchMap = new HashMap<>();
        dispatchMap.put(EGCBaseMsg.k_EMsgGCInvitationCreated_VALUE, packetMsg -> handleInvitationCreated(packetMsg));
    }

    @Override
    public void setup(Dota2SteamGameCoordinator gameCoordinator) {
        super.setup(gameCoordinator);
        client.getManager().subscribe(SingleObjectNewParty.class, this::onSingleObjectNew);
        client.getManager().subscribe(SingleObjectUpdatedParty.class, this::onSingleObjectUpdated);
        client.getManager().subscribe(SingleObjectRemovedParty.class, this::onSingleObjectRemoved);
        client.getManager().subscribe(NotReadyCallback.class, this::onNotReady);
    }

    private void onNotReady(NotReadyCallback callback) {
        partyCleanup();
    }

    private void onSingleObjectNew(SingleObjectNewParty callback) {
        switch (callback.getTypeId()) {
        case CSOTypes.PARTY_INVITE_VALUE:
            handlePartyInvite(callback.getData());
            break;
        case CSOTypes.PARTY_VALUE:
            handlePartyNew(callback.getData());
            break;
        default:
            logger.debug("!!onSingleObjectNew with type: " + callback.getTypeId());
            break;
        }
    }

    private void onSingleObjectUpdated(SingleObjectUpdatedParty callback) {
        switch (callback.getTypeId()) {
        case CSOTypes.PARTY_VALUE:
            handlePartyUpdated(callback.getData());
            break;
        default:
            logger.debug("!!onSingleObjectUpdated with type: " + callback.getTypeId());
            break;
        }
    }

    private void onSingleObjectRemoved(SingleObjectRemovedParty callback) {
        switch (callback.getTypeId()) {
        case CSOTypes.PARTY_VALUE:
            handlePartyRemoved(callback.getData());
            break;
        default:
            logger.debug("!!onSingleObjectRemoved with type: " + callback.getTypeId());
            break;
        }
    }

    public void partyCleanup() {
        this.party = null;
    }

    public void handlePartyInvite(ByteString data) {
        try {
            CSODOTAPartyInvite partyInvite = CSODOTAPartyInvite.parseFrom(data);
            logger.trace(">>handlePartyInvite: " + partyInvite);
            client.postCallback(new PartyInviteCallback(partyInvite));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handlePartyNew(ByteString data) {
        try {
            CSODOTAParty party = CSODOTAParty.parseFrom(data);
            logger.trace(">>handlePartyNew: " + party);
            this.party = party;
            client.postCallback(new PartyNewCallback(party));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handlePartyUpdated(ByteString data) {
        try {
            CSODOTAParty party = CSODOTAParty.parseFrom(data);
            logger.trace(">>handlePartyUpdated: " + party);
            this.party = party;
            client.postCallback(new PartyUpdatedCallback(party));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handlePartyRemoved(ByteString data) {
        try {
            CSODOTAParty party = CSODOTAParty.parseFrom(data);
            logger.trace(">>handlePartyRemoved: " + party);
            this.party = null;
            client.postCallback(new PartyRemovedCallback(party));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleInvitationCreated(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgInvitationCreated.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgInvitationCreated.class, msg);
        if (party != null && party.getPartyId() == protobuf.getBody().getGroupId()) {
            logger.trace(">>handleInvitationCreated: " + protobuf.getBody());
            client.postCallback(new PartyInvitationCreatedCallback(protobuf.getBody()));
        }
    }

    // actions

    public void respondToPartyInvite(long partyId, boolean accept) {
        ClientGCMsgProtobuf<CMsgPartyInviteResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPartyInviteResponse.class, EGCBaseMsg.k_EMsgGCPartyInviteResponse_VALUE);
        protobuf.getBody().setPartyId(partyId);
        protobuf.getBody().setAccept(accept);
        logger.trace(">>respondToPartyInvite: " + protobuf.getBody());
        send(protobuf);
    }

    public void leaveParty() {
        if (party == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgLeaveParty.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgLeaveParty.class,
                EGCBaseMsg.k_EMsgGCLeaveParty_VALUE);
        logger.trace(">>leaveParty: " + protobuf.getBody());
        send(protobuf);
    }

    public void setPartyLeader(long steamId) {
        if (party == null) {
            return;
        }

        ClientGCMsgProtobuf<CMsgDOTASetGroupLeader.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTASetGroupLeader.class, EDOTAGCMsg.k_EMsgClientToGCSetPartyLeader_VALUE);
        protobuf.getBody().setNewLeaderSteamid(steamId);
        logger.trace(">>setPartyLeader: " + protobuf.getBody());
        send(protobuf);
    }

    public void setPartyCoach(boolean coach) {
        if (party == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgDOTAPartyMemberSetCoach.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTAPartyMemberSetCoach.class, EDOTAGCMsg.k_EMsgGCPartyMemberSetCoach_VALUE);
        protobuf.getBody().setWantsCoach(coach);
        logger.trace(">>setPartyCoach: " + protobuf.getBody());
        send(protobuf);
    }

    public void inviteToParty(long steamId) {
        ClientGCMsgProtobuf<CMsgInviteToParty.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgInviteToParty.class,
                EGCBaseMsg.k_EMsgGCInviteToParty_VALUE);
        protobuf.getBody().setSteamId(steamId);
        logger.trace(">>inviteToParty: " + protobuf.getBody());
        send(protobuf);
    }

    public void kickFromParty(long steamId) {
        if (party == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgKickFromParty.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgKickFromParty.class,
                EGCBaseMsg.k_EMsgGCKickFromParty_VALUE);
        protobuf.getBody().setSteamId(steamId);
        logger.trace(">>kickFromParty: " + protobuf.getBody());
        send(protobuf);
    }

    public void readyCheckAcknowledgeReady() {
        if (party == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPartyReadyCheckAcknowledge.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPartyReadyCheckAcknowledge.class, EDOTAGCMsg.k_EMsgPartyReadyCheckAcknowledge_VALUE);
        protobuf.getBody().setReadyStatus(EReadyCheckStatus.k_EReadyCheckStatus_Ready);
        logger.trace(">>readyCheckAcknowledgeReady: " + protobuf.getBody());
        send(protobuf);
    }

    public void readyCheck() {
        if (party == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPartyReadyCheckRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPartyReadyCheckRequest.class, EDOTAGCMsg.k_EMsgPartyReadyCheckRequest_VALUE);
        logger.trace(">>readyCheck: " + protobuf.getBody());
        send(protobuf);
    }

    @Override
    public void handleGCMsg(IPacketGCMsg packetGCMsg) {
        Consumer<IPacketGCMsg> dispatcher = dispatchMap.get(packetGCMsg.getMsgType());
        if (dispatcher != null) {
            logger.trace(">>handleGCMsg party msg: " + packetGCMsg.getMsgType());
            dispatcher.accept(packetGCMsg);
        }
    }

    public CSODOTAParty getParty() {
        return party;
    }

}
