/*
 * Copyright (C) 2014
 * University of Massachusetts
 * All Rights Reserved 
 *
 * Initial developer(s): Westy.
 */
package edu.umass.cs.gns.commands.admin;

import static edu.umass.cs.gns.clientsupport.Defs.*;
import edu.umass.cs.gns.clientsupport.PerformanceTests;
import edu.umass.cs.gns.commands.CommandModule;
import edu.umass.cs.gns.commands.GnsCommand;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author westy
 */
public class RTTQuick extends GnsCommand {

  public RTTQuick(CommandModule module) {
    super(module);
  }

  @Override
  public String[] getCommandParameters() {
    return new String[]{GUIDCNT};
  }

  @Override
  public String getCommandName() {
    return RTTTEST;
  }

  @Override
  public String execute(JSONObject json) throws JSONException {
    if (module.isAdminMode()) {
      String guidCntString = json.getString(GUIDCNT);
      int guidCnt = Integer.parseInt(guidCntString);
      return PerformanceTests.runRttPerformanceTest(5, guidCnt, false);
    } else {
      return BADRESPONSE + " " + OPERATIONNOTSUPPORTED + " Don't understand " + getCommandName();
    }

  }

  @Override
  public String getCommandDescription() {
    return "Runs the round trip test with 5 reads and only shows bad results.";
  }
}