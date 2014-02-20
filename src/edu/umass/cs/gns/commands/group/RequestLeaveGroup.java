/*
 * Copyright (C) 2014
 * University of Massachusetts
 * All Rights Reserved 
 *
 * Initial developer(s): Westy.
 */
package edu.umass.cs.gns.commands.group;

import edu.umass.cs.gns.client.AccountAccess;
import edu.umass.cs.gns.client.GroupAccess;
import edu.umass.cs.gns.client.GuidInfo;
import edu.umass.cs.gns.clientprotocol.AccessSupport;
import edu.umass.cs.gns.commands.CommandModule;
import edu.umass.cs.gns.commands.GnsCommand;
import static edu.umass.cs.gns.clientprotocol.Defs.*;
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
public class RequestLeaveGroup extends GnsCommand {

  public RequestLeaveGroup(CommandModule module) {
    super(module);
  }

  @Override
  public String[] getCommandParameters() {
    return new String[]{GUID, MEMBER, SIGNATURE, SIGNATUREFULLMESSAGE};
  }

  @Override
  public String getCommandName() {
    return REQUESTLEAVEGROUP;
  }

  @Override
  public String execute(JSONObject json) throws InvalidKeyException, InvalidKeySpecException,
          JSONException, NoSuchAlgorithmException, SignatureException {
    String guid = json.getString(GUID);
    String member = json.getString(MEMBER);
    // signature and message can be empty for unsigned cases
    String signature = json.optString(SIGNATURE, null);
    String message = json.optString(SIGNATUREFULLMESSAGE, null);
    GuidInfo guidInfo, memberInfo;
    if ((guidInfo = AccountAccess.lookupGuidInfo(guid)) == null) {
      return BADRESPONSE + " " + BADGUID + " " + guid;
    }
    if (member.equals(guid)) {
      memberInfo = guidInfo;
    } else if ((memberInfo = AccountAccess.lookupGuidInfo(member)) == null) {
      return BADRESPONSE + " " + BADREADERGUID + " " + member;
    }
    if (!AccessSupport.verifySignature(memberInfo, signature, message)) {
      return BADRESPONSE + " " + BADSIGNATURE;
    } else {
      if (GroupAccess.requestLeaveGroup(guid, member)) {
        return OKRESPONSE;
      } else {
        return BADRESPONSE + " " + GENERICEERROR;
      }
    }
  }

  @Override
  public String getCommandDescription() {
    return "Request revocation of membership in the group specified by guid. Member guid needs to sign the command.";
  }
}
