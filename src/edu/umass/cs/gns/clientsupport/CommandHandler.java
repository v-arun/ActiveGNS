/*
 * Copyright (C) 2013
 * University of Massachusetts
 * All Rights Reserved 
 */
package edu.umass.cs.gns.clientsupport;

import edu.umass.cs.gns.commands.CommandModule;
import edu.umass.cs.gns.commands.GnsCommand;
import edu.umass.cs.gns.clientCommandProcessor.ClientRequestHandlerInterface;
import static edu.umass.cs.gns.clientsupport.Defs.*;
import edu.umass.cs.gns.main.GNS;
import edu.umass.cs.gns.newApp.NewApp;
import edu.umass.cs.gns.nio.JSONNIOTransport;
import edu.umass.cs.gns.nsdesign.Config;
import edu.umass.cs.gns.nsdesign.packet.CommandPacket;
import edu.umass.cs.gns.nsdesign.packet.CommandValueReturnPacket;
import edu.umass.cs.gns.util.CanonicalJSON;
import edu.umass.cs.gns.util.NetworkUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Handles sending and receiving of commands.
 *
 * @author westy
 */
public class CommandHandler {

  // handles command processing
  private static final CommandModule commandModule = new CommandModule();

  /**
   * Handles command packets coming in from the client.
   *
   * @param incomingJSON
   * @param handler
   * @throws JSONException
   * @throws UnknownHostException
   */
  public static void handlePacketCommandRequest(JSONObject incomingJSON, final ClientRequestHandlerInterface handler)
          throws JSONException, UnknownHostException {
    final Long receiptTime = System.currentTimeMillis(); // instrumentation
    if (handler.getParameters().isDebugMode()) {
      GNS.getLogger().info("<<<<<<<<<<<<<<<<< COMMAND PACKET RECEIVED: " + incomingJSON);
    }
    final CommandPacket packet = new CommandPacket(incomingJSON);
    // FIXME: Don't do this every time. 
    // Set the host field. Used by the help command and email module. 
    commandModule.setHTTPHost(handler.getNodeAddress().getHostString() + ":8080");
    final JSONObject jsonFormattedCommand = packet.getCommand();
    // Adds a field to the command to allow us to process the authentication of the signature
    addMessageWithoutSignatureToCommand(jsonFormattedCommand, handler);
    final GnsCommand command = commandModule.lookupCommand(jsonFormattedCommand);

    try {
      CommandResponse returnValue = executeCommand(command, jsonFormattedCommand, handler);
      // the last arguments here in the call below are instrumentation that the client can use to determine LNS load
      CommandValueReturnPacket returnPacket = new CommandValueReturnPacket(packet.getRequestId(), returnValue,
              handler.getReceivedRequests(), handler.getRequestsPerSecond(),
              System.currentTimeMillis() - receiptTime);
      if (handler.getParameters().isDebugMode()) {
        GNS.getLogger().info("SENDING VALUE BACK TO " + packet.getSenderAddress() + "/" + packet.getSenderPort() + ": " + returnPacket.toString());
      }
      handler.sendToAddress(returnPacket.toJSONObject(), packet.getSenderAddress(), packet.getSenderPort());
    } catch (JSONException e) {
      GNS.getLogger().severe("Problem  executing command: " + e);
      e.printStackTrace();
    }
    // This separate thread  makes it actually work.
    // I thought we were in a separate worker thread from 
    // the NIO message handling thread. Turns out not.
//    (new Thread() {
//      @Override
//      public void run() {
//        try {
//          CommandResponse returnValue = executeCommand(command, jsonFormattedCommand, handler);
//          // the last arguments here in the call below are instrumentation that the client can use to determine LNS load
//          CommandValueReturnPacket returnPacket = new CommandValueReturnPacket(packet.getRequestId(), returnValue,
//                  handler.getReceivedRequests(), handler.getRequestsPerSecond(),
//          System.currentTimeMillis() - receiptTime);
//          if (handler.getParameters().isDebugMode()) {
//            GNS.getLogger().info("SENDING VALUE BACK TO " + packet.getSenderAddress() + "/" + packet.getSenderPort() + ": " + returnPacket.toString());
//          }
//          handler.sendToAddress(returnPacket.toJSONObject(), packet.getSenderAddress(), packet.getSenderPort());
//        } catch (JSONException e) {
//          GNS.getLogger().severe("Problem  executing command: " + e);
//          e.printStackTrace();
//        }
//      }
//    }).start();
  }

  // this little dance is because we need to remove the signature to get the message that was signed
  // alternatively we could have the client do it but that just means a longer message
  // OR we could put the signature outside the command in the packet, but some packets don't need a signature
  private static void addMessageWithoutSignatureToCommand(JSONObject command, ClientRequestHandlerInterface handler) throws JSONException {
    if (command.has(SIGNATURE)) {
      String signature = command.getString(SIGNATURE);
      command.remove(SIGNATURE);
      String commandSansSignature = CanonicalJSON.getCanonicalForm(command);
      //String commandSansSignature = JSONUtils.getCanonicalJSONString(command);
      if (handler.getParameters().isDebugMode()) {
        GNS.getLogger().fine("########CANONICAL JSON: " + commandSansSignature);
      }
      command.put(SIGNATURE, signature);
      command.put(SIGNATUREFULLMESSAGE, commandSansSignature);
    }
  }

  /**
   * Executes the given command with the parameters supplied in the JSONObject.
   *
   * @param command
   * @param json
   * @return
   */
  public static CommandResponse executeCommand(GnsCommand command, JSONObject json, ClientRequestHandlerInterface handler) {
    try {
      if (command != null) {
        //GNS.getLogger().info("Executing command: " + command.toString());
        GNS.getLogger().fine("Executing command: " + command.toString() + " with " + json);
        return command.execute(json, handler);
      } else {
        return new CommandResponse(BADRESPONSE + " " + OPERATIONNOTSUPPORTED + " - Don't understand " + json.toString());
      }
    } catch (JSONException e) {
      e.printStackTrace();
      return new CommandResponse(BADRESPONSE + " " + JSONPARSEERROR + " " + e + " while executing command.");
    } catch (NoSuchAlgorithmException e) {
      return new CommandResponse(BADRESPONSE + " " + QUERYPROCESSINGERROR + " " + e);
    } catch (InvalidKeySpecException e) {
      return new CommandResponse(BADRESPONSE + " " + QUERYPROCESSINGERROR + " " + e);
    } catch (SignatureException e) {
      return new CommandResponse(BADRESPONSE + " " + QUERYPROCESSINGERROR + " " + e);
    } catch (InvalidKeyException e) {
      return new CommandResponse(BADRESPONSE + " " + QUERYPROCESSINGERROR + " " + e);
    }
  }

  //
  // Code for handling commands at the app
  //
  static class CommandQuery {

    private String host;
    private int port;

    public CommandQuery(String host, int port) {
      this.host = host;
      this.port = port;
    }

    public String getHost() {
      return host;
    }

    public int getPort() {
      return port;
    }
  }

  private static final ConcurrentMap<Integer, CommandQuery> outStandingQueries = new ConcurrentHashMap<>(10, 0.75f, 3);

  private static InetSocketAddress cppAddress;

  static {
    try {
      cppAddress = new InetSocketAddress(NetworkUtils.getLocalHostLANAddress().getHostAddress(), GNS.DEFAULT_CPP_TCP_PORT);
      GNS.getLogger().info("CPP Address is " + cppAddress);
    } catch (UnknownHostException e) {
      GNS.getLogger().info("Unabled to determine CPP address: " + e);
      cppAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), GNS.DEFAULT_CPP_TCP_PORT);
    }
  }

  public static void handleCommandPacketForApp(JSONObject json, NewApp app) throws JSONException, IOException {
    CommandPacket packet = new CommandPacket(json);
    // Squirrel away the host and port so we know where to send the command return value
    outStandingQueries.put(packet.getRequestId(), new CommandQuery(packet.getSenderAddress(), packet.getSenderPort()));
    // remove these so the stamper will put new ones in so the packet will find it's way back here
    json.remove(JSONNIOTransport.DEFAULT_IP_FIELD);
    json.remove(JSONNIOTransport.DEFAULT_PORT_FIELD);
    // Send it to the client command handler
    app.getNioServer().sendToAddress(cppAddress, json);
  }

  public static void handleCommandReturnValuePacketForApp(JSONObject json, NewApp app) throws JSONException, IOException {
    CommandValueReturnPacket returnPacket = new CommandValueReturnPacket(json, app.getGNSNodeConfig());
    int id = returnPacket.getRequestId();
    CommandQuery sentInfo;
    if ((sentInfo = outStandingQueries.get(id)) != null) {
      outStandingQueries.remove(id);
      if (Config.debuggingEnabled) {
        GNS.getLogger().info("APP IS SENDING VALUE BACK TO "
                + sentInfo.getHost() + "/" + sentInfo.getPort() + ": " + returnPacket.toString());
      }
      app.getNioServer().sendToAddress(new InetSocketAddress(sentInfo.getHost(), sentInfo.getPort()),
              json);
    } else {
      GNS.getLogger().severe("Command packet info not found for " + id + ": " + json);
    }
  }
}
