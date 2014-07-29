/*
 * Copyright (C) 2014
 * University of Massachusetts
 * All Rights Reserved 
 *
 * Initial developer(s): Westy.
 */
package edu.umass.cs.gns.commands.group;

import edu.umass.cs.gns.clientsupport.CommandResponse;
import static edu.umass.cs.gns.clientsupport.Defs.*;
import edu.umass.cs.gns.clientsupport.GroupAccess;
import edu.umass.cs.gns.commands.CommandModule;
import edu.umass.cs.gns.commands.GnsCommand;
import edu.umass.cs.gns.util.NSResponseCode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author westy
 */
public class AddToGroup extends GnsCommand {

  public AddToGroup(CommandModule module) {
    super(module);
  }

  @Override
  public String[] getCommandParameters() {
    return new String[]{GUID, MEMBER, WRITER, SIGNATURE, SIGNATUREFULLMESSAGE};
  }

  @Override
  public String getCommandName() {
    return ADDTOGROUP;
  }

  @Override
  public CommandResponse execute(JSONObject json) throws InvalidKeyException, InvalidKeySpecException,
          JSONException, NoSuchAlgorithmException, SignatureException {
    String guid = json.getString(GUID);
    String member = json.getString(MEMBER);
    // writer might be same as guid
    String writer = json.optString(WRITER, guid);
    // signature and message can be empty for unsigned cases
    String signature = json.optString(SIGNATURE, null);
    String message = json.optString(SIGNATUREFULLMESSAGE, null);
    NSResponseCode responseCode;
    if (!(responseCode = GroupAccess.addToGroup(guid, member, writer, signature, message)).isAnError()) {
      return new CommandResponse(OKRESPONSE);
    } else {
      return new CommandResponse(BADRESPONSE + " " + responseCode.getProtocolCode());
    }
  }

  @Override
  public String getCommandDescription() {
    return "Adds the member guid to the group specified by guid. Writer guid needs to have write access and sign the command.";
  }
}
