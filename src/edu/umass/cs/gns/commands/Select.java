/*
 * Copyright (C) 2014
 * University of Massachusetts
 * All Rights Reserved 
 *
 * Initial developer(s): Westy.
 */
package edu.umass.cs.gns.commands;

import edu.umass.cs.gns.client.FieldAccess;
import static edu.umass.cs.gns.clientprotocol.Defs.*;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author westy
 */
public class Select extends GnsCommand {

  public Select(CommandModule module) {
    super(module);
  }

  @Override
  public String[] getCommandParameters() {
    return new String[]{FIELD, VALUE};
  }

  @Override
  public String getCommandName() {
    return SELECT;
  }

  @Override
  public String execute(JSONObject json) throws JSONException {
    String field = json.getString(FIELD);
    String value = json.getString(VALUE);
    return FieldAccess.select(field, value);
  }

  @Override
  public String getCommandDescription() {
    return "Returns all records that have a field with the given value.";
  }
}
