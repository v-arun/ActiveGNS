/*
 * Copyright (C) 2014
 * University of Massachusetts
 * All Rights Reserved 
 *
 * Initial developer(s): Westy.
 */
package edu.umass.cs.gns.gnsApp.clientCommandProcessor.commands.account;

import edu.umass.cs.gns.gnsApp.clientCommandProcessor.commandSupport.AccountAccess;
import edu.umass.cs.gns.gnsApp.clientCommandProcessor.commandSupport.AccountInfo;
import edu.umass.cs.gns.gnsApp.clientCommandProcessor.commandSupport.CommandResponse;
import static edu.umass.cs.gns.gnsApp.clientCommandProcessor.commandSupport.GnsProtocolDefs.*;
import edu.umass.cs.gns.gnsApp.clientCommandProcessor.commands.CommandModule;
import edu.umass.cs.gns.gnsApp.clientCommandProcessor.commands.GnsCommand;
import edu.umass.cs.gns.gnsApp.clientCommandProcessor.demultSupport.ClientRequestHandlerInterface;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author westy
 */
public class LookupAccountRecord extends GnsCommand {

  /**
   * Creates a LookupAccountRecord instance.
   * 
   * @param module
   */
  public LookupAccountRecord(CommandModule module) {
    super(module);
  }

  @Override
  public String[] getCommandParameters() {
    return new String[]{GUID};
  }

  @Override
  public String getCommandName() {
    return LOOKUPACCOUNTRECORD;
  }

  @Override
  public CommandResponse<String> execute(JSONObject json, ClientRequestHandlerInterface handler) throws JSONException {
//    if (CommandDefs.handleAcccountCommandsAtNameServer) {
//      return LNSToNSCommandRequestHandler.sendCommandRequest(json);
//    } else {
      String guid = json.getString(GUID);
      AccountInfo acccountInfo;
      if ((acccountInfo = AccountAccess.lookupAccountInfoFromGuid(guid, handler)) == null) {
        return new CommandResponse<>(BADRESPONSE + " " + BADACCOUNT + " " + guid);
      }
      if (acccountInfo != null) {
        try {
          return new CommandResponse<>(acccountInfo.toJSONObject().toString());
        } catch (JSONException e) {
          return new CommandResponse<>(BADRESPONSE + " " + JSONPARSEERROR);
        }
      } else {
        return new CommandResponse<>(BADRESPONSE + " " + BADGUID + " " + guid);
      }
   // }
  }

  @Override
  public String getCommandDescription() {
    return "Returns the account info associated with the given GUID. "
            + "Returns " + BADGUID + " if the GUID has not been registered.";

  }
}
