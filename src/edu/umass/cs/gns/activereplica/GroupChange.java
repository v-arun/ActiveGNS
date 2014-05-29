package edu.umass.cs.gns.activereplica;

import edu.umass.cs.gns.main.GNS;
import edu.umass.cs.gns.nio.MessagingTask;
import edu.umass.cs.gns.nio.NIOTransport;
import edu.umass.cs.gns.nsdesign.Config;
import edu.umass.cs.gns.nsdesign.packet.NewActiveSetStartupPacket;
import edu.umass.cs.gns.nsdesign.packet.OldActiveSetStopPacket;
import edu.umass.cs.gns.reconfigurator.Add;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * @author V. Arun
 * Based on code created by abhigyan on 2/27/14. 
 * 
 * Helper class for ActiveReplica.
 * 
 * Implements functionality at an active replica for a changing the set of active replicas for a name. This class
 * contains two types of methods: those executed at old active replicas, and those executed at new active replicas.
 *
 * The old active replica takes following actions during group change:
 * (1) When replica controller contacts an active replica receives a message to stop current active set, it proposes
 * this stop request to all active replicas.
 * (2) After active replicas agree to stop, the app for each active replica backs up its state at the time of executing
 * the stop request. The active replica contacted by replica controller send back a confirmation to replica controller
 * that actives have stopped.
 * (3) When a new active replica contacts an old active replicas for copying state for a name, the old active replica
 * sends the backed up state, if available, to the new active replica.
 * (4) When replica controller asks old active replicas to clear any state related to this name, they do so. In case an
 * old active replica is also a member of new active replica set, it only deletes the backed up state from the time of
 * executing the stop request.
 *
 * The new active replica takes following actions during group change:
 *
 * (1) When a replica controller informs one of the new active replicas of its membership in the new group, that
 * active replica to informs all new active replicas of their membership.
 * (2) When an active replica learns about its membership in the new group from another active replica, it requests a
 * state transfer from any of the old active replicas who have executed the stop request.
 * (3) Once an active replica obtains state from an old active replica, it becomes functional as a new active replica,
 * and informs the active replica who had informed it about its membership in new set.
 * (4) When the active replica first contact by replica controller receives confirmation from a majority of new active
 * replicas that they are functional, it confirms to the replica controller that the new active replica set is
 * functional.
 *
 * Created by abhigyan on 2/27/14.
 */
public class GroupChange {

	private static Logger log = NIOTransport.LOCAL_LOGGER ? Logger.getLogger(Add.class.getName()) : GNS.getLogger();


	/********************   BEGIN: methods executed at old active replicas. *********************/

	public static void handleOldActiveStopFromReplicaController(OldActiveSetStopPacket stopPacket, ActiveReplica<?> replica)
			throws JSONException{
		replica.getCoordinator().coordinateRequest(stopPacket.toJSONObject());
		// do the check and propose to replica controller.
	}

	/**
	 * Send confirmation to replica controller that actives have stopped.
	 */
	public static MessagingTask handleStopProcessed(OldActiveSetStopPacket stopPacket, ActiveReplica<?> activeReplica) {
		MessagingTask confirmToRC = null;
		try {
			// confirm to primary name server that this set of actives has stopped
			if (stopPacket.getActiveReceiver() == activeReplica.getNodeID()) {
				// the active node who received this node, sends confirmation to primary
				// confirm to primary
				stopPacket.changePacketTypeToConfirm();
				confirmToRC = new MessagingTask(stopPacket.getPrimarySender(), stopPacket.toJSONObject());
				log.info("Active removed: Name Record updated. Sent confirmation to replica controller. Packet = " +
						stopPacket);
			} else {
				// other nodes do nothing.
				log.info("Active removed: Name Record updated. OldVersion = " + stopPacket.getVersion());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return confirmToRC;
	}

	/**
	 * Responds to a request from a new active replica regarding transferring state for a name.
	 */
	public static MessagingTask handlePrevValueRequest(NewActiveSetStartupPacket packet, String finalState, int activeID)
			throws JSONException, IOException {
		MessagingTask sendOldActiveState = null;
		if (Config.debugMode) log.info(" Received NEW_ACTIVE_START_PREV_VALUE_REQUEST at node " +
				activeID);
		// obtain name record
		if (finalState == null) {
			packet.changePreviousValueCorrect(false);
		} else {
			// update previous value
			packet.changePreviousValueCorrect(true);
			packet.changePreviousValue(finalState);
		}

		packet.changePacketTypeToPreviousValueResponse();

		if (Config.debugMode) log.info(" NEW_ACTIVE_START_PREV_VALUE_REQUEST reply sent to: " + packet.getSendingActive());
		// reply to sending active
		sendOldActiveState = new MessagingTask(packet.getSendingActive(), packet.toJSONObject());
		return sendOldActiveState;
	}


	/********************   END: methods executed at old active replicas. *********************/


	/********************   BEGIN: methods executed at new active replicas. *********************/

	/**
	 *  Handle message from replica controller informing of this node's membership in the new active replica set; this
	 *  node informs all new active replicas (including itself) of their membership in the new set.
	 *  This method also creates book-keeping state at active replica to records response from active replicas.
	 */
	public static MessagingTask handleNewActiveStart(NewActiveSetStartupPacket packet, int arID, HashMap<Integer,NewActiveStartInfo> activeStartups,
			ARProtocolTask[] protocolTask)
					throws JSONException, IOException{

		MessagingTask newActiveForward = null;
		// sanity check: am I in set? otherwise quit.
		if (!packet.getNewActiveNameServers().contains(arID)) {
			log.severe("ERROR: NewActiveSetStartupPacket reached a non-active name server." + packet.toString());
			return null;
		}
		// create name server
		NewActiveStartInfo activeStartInfo = new NewActiveStartInfo(new NewActiveSetStartupPacket(packet.toJSONObject()));
		int requestID = (int)Math.random()*Integer.MAX_VALUE;
		activeStartups.put(requestID, activeStartInfo);
		// send to all nodes, except self
		packet.changePacketTypeToForward();
		packet.setUniqueID(requestID); // this ID is set by active replica for identifying this packet.
		if (Config.debugMode) log.info("NEW_ACTIVE_START: forwarded msg to nodes; "
				+ packet.getNewActiveNameServers());
		for (int nodeID: packet.getNewActiveNameServers()) {
			if (arID != nodeID) { // exclude myself
				newActiveForward = new MessagingTask(nodeID, packet.toJSONObject());
			}
		}
		CopyStateFromOldActiveTask copyTask = new CopyStateFromOldActiveTask(packet);
		assert(protocolTask.length>0);
		protocolTask[0] = copyTask;
		return newActiveForward;
	}


	/**
	 * This active replica learns that it one of the new active replica, upon which it creates a task to copy
	 * state from one of the old active replicas who have executed the stop request.
	 */
	public static void handleNewActiveStartForward(NewActiveSetStartupPacket packet, ARProtocolTask[] protocolTasks)
			throws JSONException, IOException {

		CopyStateFromOldActiveTask copyTask = new CopyStateFromOldActiveTask(packet);
		assert(protocolTasks.length>0);
		protocolTasks[0] = copyTask;
	}

	/**
	 * One of the old active replica have responded to this node's request for transferring state for a name. If the
	 * response is valid, this node becomes functional as a new active replica and confirms back to that active replica
	 * who informed it of its membership in new group.
	 */
	public static MessagingTask handlePrevValueResponse(NewActiveSetStartupPacket originalPacket, int activeID)
			throws JSONException, IOException{
		MessagingTask replyToActive = null;
		
		if (originalPacket != null) {
			// send back response to the active who forwarded this packet to this node.
			originalPacket.changePacketTypeToResponse();
			int sendingActive = originalPacket.getSendingActive();
			originalPacket.changeSendingActive(activeID);

			if (Config.debugMode) log.info("Node "+activeID+" NEW_ACTIVE_START: replied to active sending the startup packet from node: " + sendingActive);

			replyToActive = new MessagingTask(sendingActive, originalPacket.toJSONObject());
		} else {
			if (Config.debugMode) log.info(" NewActiveSetStartupPacket not found for response.");
		}
		return replyToActive;
	}

	/**
	 * This method is executed at the new active replica first contact by replica controller.
	 * This message received confirms that one of the new active replica is functional now. This node checks if it has
	 * received such messages from a majority of new replicas. If so, it confirms to the replica controller
	 * that the new active replica set is functional.
	 */
	public static MessagingTask handleNewActiveStartResponse(NewActiveSetStartupPacket packet, HashMap<Integer,NewActiveStartInfo> activeStartups)
			throws JSONException, IOException {
		MessagingTask replyToRC = null;
		NewActiveStartInfo info = (NewActiveStartInfo) activeStartups.get(packet.getUniqueID());
		if (Config.debugMode) log.info("NEW_ACTIVE_START: received confirmation from node: " +
				packet.getSendingActive());
		if (info != null) {
			info.receivedResponseFromActive(packet.getSendingActive());
			if (info.haveMajorityActivesResponded()) {
				if (Config.debugMode) log.info("NEW_ACTIVE_START: received confirmation from majority. name = " + packet.getName());
				info.originalPacket.changePacketTypeToConfirmation();
				replyToRC = new MessagingTask(info.originalPacket.getSendingPrimary(), info.originalPacket.toJSONObject());
				activeStartups.remove(packet.getUniqueID());
			}
		}
		return replyToRC;
	}


	/********************   END: methods executed at new active replicas. *********************/
}