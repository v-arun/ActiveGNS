/*
 *
 *  Copyright (c) 2015 University of Massachusetts
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you
 *  may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 *  Initial developer(s): Abhigyan Sharma, Westy
 *
 */
package edu.umass.cs.gnsserver.gnsApp.clientSupport;

import edu.umass.cs.gnscommon.GnsProtocol;
import edu.umass.cs.gnsserver.database.ColumnFieldType;
import edu.umass.cs.gnscommon.exceptions.server.FailedDBOperationException;
import edu.umass.cs.gnscommon.exceptions.server.FieldNotFoundException;
import edu.umass.cs.gnscommon.exceptions.server.RecordNotFoundException;
import edu.umass.cs.gnsserver.main.GNS;
import edu.umass.cs.gnsserver.gnsApp.AppReconfigurableNodeOptions;
import edu.umass.cs.gnsserver.gnsApp.GnsApplicationInterface;
import edu.umass.cs.gnsserver.gnsApp.clientCommandProcessor.commandSupport.ActiveCode;
import edu.umass.cs.gnsserver.gnsApp.clientCommandProcessor.commandSupport.InternalField;
import edu.umass.cs.gnsserver.gnsApp.recordmap.BasicRecordMap;
import edu.umass.cs.gnsserver.gnsApp.recordmap.NameRecord;
import edu.umass.cs.gnsserver.utils.ResultValue;
import edu.umass.cs.gnsserver.utils.ValuesMap;
import java.util.List;

/**
 * Methods for reading field information in guids on NameServers.
 *
 * Similar code for getting values from the database exists in GnsReconLookup.
 *
 * @author westy
 */
public class NSFieldAccess {

  public static ValuesMap lookupFieldLocalNoAuth(String guid, String field, ColumnFieldType returnFormat,
          BasicRecordMap database) throws FailedDBOperationException {
    NameRecord nameRecord = null;
    if (AppReconfigurableNodeOptions.debuggingEnabled) {
      GNS.getLogger().info("XXXXXXXXXXXXXXXXXXXXX LOOKUP_FIELD_LOCALLY: " + guid + " : " + field);
    }
    // Try to look up the value in the database
    try {
      // Check for the case where we're returning all the fields the entire record.
      if (GnsProtocol.ALL_FIELDS.equals(field)) {
        if (AppReconfigurableNodeOptions.debuggingEnabled) {
          GNS.getLogger().fine("Field=" + field + " Format=" + returnFormat);
        }
        // need everything so just grab all the fields
        nameRecord = NameRecord.getNameRecord(database, guid);
        // Otherwise if field is specified we're just looking up that single field.
      } else if (field != null) {
        if (AppReconfigurableNodeOptions.debuggingEnabled) {
          GNS.getLogger().fine("Field=" + field + " Format=" + returnFormat);
        }
        // otherwise grab a few system fields we need plus the field the user wanted
        nameRecord = NameRecord.getNameRecordMultiField(database, guid,
                null, returnFormat, field);
      }
      if (nameRecord != null) {
        return nameRecord.getValuesMap();
      }
    } catch (RecordNotFoundException e) {
      GNS.getLogger().fine("Record not found for name: " + guid + " Key = " + field);
    } catch (FieldNotFoundException e) {
      if (AppReconfigurableNodeOptions.debuggingEnabled) {
        GNS.getLogger().info("Field not found " + guid + " : " + e);
      }
    }
    return null;
  }

  /**
   * Looks up the value of a field in the guid on this NameServer.
   *
   * Returns the value of a field in a GUID as a ValuesMap.
   *
   * @param guid
   * @param field
   * @param gnsApp
   * @return ResultValue
   * @throws edu.umass.cs.gnscommon.exceptions.server.FailedDBOperationException
   */
  public static ValuesMap lookupJSONFieldLocalNoAuth(String guid, String field,
          GnsApplicationInterface<String> gnsApp)
          throws FailedDBOperationException {
    ValuesMap valuesMap = lookupFieldLocalNoAuth(guid, field, ColumnFieldType.USER_JSON, gnsApp.getDB());
    valuesMap = NSFieldAccess.handleActiveCode(GnsProtocol.ALL_FIELDS, guid, valuesMap, gnsApp);
    return valuesMap;
  }

  public static ValuesMap lookupFieldsLocalNoAuth(String guid, List<String> fields,
          ColumnFieldType returnFormat, BasicRecordMap database)
          throws FailedDBOperationException {
    // Try to look up the value in the database
    try {
      if (AppReconfigurableNodeOptions.debuggingEnabled) {
        GNS.getLogger().fine("Fields=" + fields + " Format=" + returnFormat);
      }
      String[] fieldArray = new String[fields.size()];
      fieldArray = fields.toArray(fieldArray);
      // Grab a few system fields and the fields the user wanted
      NameRecord nameRecord = NameRecord.getNameRecordMultiField(database, guid,
              null, returnFormat, fieldArray);
      if (nameRecord != null) {
        return nameRecord.getValuesMap();
      }
    } catch (RecordNotFoundException e) {
      GNS.getLogger().fine("Record not found for name: " + guid);
    } catch (FieldNotFoundException e) {
      if (AppReconfigurableNodeOptions.debuggingEnabled) {
        GNS.getLogger().info("Field not found for " + guid + " : " + e);
      }
    }
    return null;
  }

  /**
   * Looks up the value of an old-style list field in the guid on this NameServer.
   * Returns the value of a field in a GUID as a ResultValue or an empty ResultValue if field cannot be found.
   *
   * @param guid
   * @param field
   * @param database
   * @return ResultValue
   */
  public static ResultValue lookupListFieldLocallyNoAuth(String guid, String field,
          BasicRecordMap database) {
    ResultValue result = null;
    try {
      NameRecord nameRecord = NameRecord.getNameRecordMultiField(database, guid, null, ColumnFieldType.LIST_STRING, field);
      if (AppReconfigurableNodeOptions.debuggingEnabled) {
        GNS.getLogger().fine("LOOKUPFIELDONTHISSERVER: " + guid + " : " + field + "->" + nameRecord);
      }
      result = nameRecord.getKeyAsArray(field);
    } catch (FieldNotFoundException e) {
      if (AppReconfigurableNodeOptions.debuggingEnabled) {
        GNS.getLogger().info("Field not found " + guid + " : " + field);
      }
    } catch (RecordNotFoundException e) {
      if (AppReconfigurableNodeOptions.debuggingEnabled) {
        GNS.getLogger().info("Record not found " + guid + " : " + field);
      }
    } catch (FailedDBOperationException e) {
      if (AppReconfigurableNodeOptions.debuggingEnabled) {
        GNS.getLogger().info("Failed DB operation " + guid + " : " + field);
      }
    }
    if (result != null) {
      return result;
    } else {
      return new ResultValue();
    }
  }

  /**
   * Looks up the first element of field in the guid on this NameServer as a String.
   * Returns null if the field or the record cannot be found.
   *
   * @param guid
   * @param field
   * @param database
   * @return a string representing the first value in field
   * @throws edu.umass.cs.gnscommon.exceptions.server.FailedDBOperationException
   */
  public static String lookupSingletonFieldOnThisServer(String guid, String field,
          BasicRecordMap database) throws FailedDBOperationException {
    ResultValue guidResult = lookupListFieldLocallyNoAuth(guid, field, database);
    if (guidResult != null && !guidResult.isEmpty()) {
      return (String) guidResult.get(0);
    } else {
      return null;
    }
  }

  /**
   * Looks up the value of an old-style list field in the guid.
   * If allowQueryToOtherNSs is true and guid doesn't exists on this Name Server,
   * sends a DNS query from this Name Server to a Local Name Server.
   * Returns the value of a field in a GUID as a ResultValue.
   *
   * @param guid
   * @param field
   * @param allowRemoteQuery
   * @param database
   * @return ResultValue containing the value of the field or an empty ResultValue if field cannot be found
   * @throws edu.umass.cs.gnscommon.exceptions.server.FailedDBOperationException
   */
  public static ResultValue lookupListFieldAnywhere(String guid, String field,
          boolean allowRemoteQuery, BasicRecordMap database) throws FailedDBOperationException {
    ResultValue result = lookupListFieldLocallyNoAuth(guid, field, database);
    // if values wasn't found and the guid doesn't exist on this server and we're allowed then send a query to the LNS
    if (result.isEmpty() && !database.containsName(guid) && allowRemoteQuery) {
      try {
        String stringResult = new RemoteQuery().fieldReadArray(guid, field);
        result = new ResultValue(stringResult);
      } catch (Exception e) {
        GNS.getLogger().severe("Problem getting record from remote server: " + e);
      }
      //result = lookupListFieldQueryLNS(guid, field, activeReplica, lnsAddress);
      if (AppReconfigurableNodeOptions.debuggingEnabled) {
        if (!result.isEmpty()) {
          GNS.getLogger().info("@@@@@@ Field " + field + " in " + guid + " "
                  + "not found on this server but was found thru remote query. "
                  + "Returning " + result.toString());
        }
      }
    }
    return result;
  }

//  private static ResultValue lookupListFieldQueryLNS(String guid, String field, GnsApplicationInterface<String> activeReplica,
//          InetSocketAddress lnsAddress) {
//    QueryResult<String> queryResult = LNSQueryHandler.sendListFieldQuery(guid, field, activeReplica, lnsAddress);
//    if (!queryResult.isError()) {
//      return queryResult.getArray(field);
//    } else {
//      return new ResultValue();
//    }
//  }
  /**
   * Looks up the value of a field in the guid.
   * If allowQueryToOtherNSs is true and guid doesn't exists on this Name Server,
   * sends a DNS query from this Name Server to a Local Name Server.
   * Returns the value of a field in a GUID as a ResultValue.
   *
   * @param guid
   * @param field
   * @param gnsApp
   * @return ValuesMap containing the value of the field or null if field cannot be found
   * @throws edu.umass.cs.gnscommon.exceptions.server.FailedDBOperationException
   */
  public static ValuesMap lookupFieldAnywhere(String guid, String field,
          GnsApplicationInterface<String> gnsApp) throws FailedDBOperationException {
    ValuesMap result = lookupJSONFieldLocalNoAuth(guid, field, gnsApp);
    // if values wasn't found and the guid doesn't exist on this server and we're allowed then send a query to the LNS
    if (result == null && !gnsApp.getDB().containsName(guid)) {
      try {
        String stringResult = new RemoteQuery().fieldRead(guid, field);
        if (stringResult != null) {
          result = new ValuesMap();
          result.put(field, stringResult);
        }
      } catch (Exception e) {
        GNS.getLogger().severe("Problem getting record from remote server: " + e);
      }
      //result = lookupFieldQueryLNS(guid, field, activeReplica, lnsAddress);
      if (result != null) {
        GNS.getLogger().info("@@@@@@ Field " + field + " in " + guid + " not found on this server but was found thru remote query.");
      }
    }
    return result;
  }

  public static ValuesMap handleActiveCode(String field, String guid,
          ValuesMap originalValues, GnsApplicationInterface<String> gnsApp)
          throws FailedDBOperationException {
    ValuesMap newResult = originalValues;
    // Only do this for user fields.
    if (field == null || !InternalField.isInternalField(field)) {
      int hopLimit = 1;
      // Grab the code because it is of a different type
      NameRecord codeRecord = null;
      try {
        codeRecord = NameRecord.getNameRecordMultiField(gnsApp.getDB(), guid, null,
                ColumnFieldType.USER_JSON, ActiveCode.ON_READ);
      } catch (RecordNotFoundException e) {
        //GNS.getLogger().severe("Active code read record not found: " + e.getMessage());
      }

      if (codeRecord != null && originalValues != null
              && gnsApp.getActiveCodeHandler().hasCode(codeRecord, "read")) {
        try {
          String code64 = codeRecord.getValuesMap().getString(ActiveCode.ON_READ);
          if (AppReconfigurableNodeOptions.debuggingEnabled) {
            GNS.getLogger().info("AC--->>> " + guid + " " + field + " " + originalValues.toString());
          }
          newResult = gnsApp.getActiveCodeHandler().runCode(code64, guid, field,
                  "read", originalValues, hopLimit);
          if (AppReconfigurableNodeOptions.debuggingEnabled) {
            GNS.getLogger().info("AC--->>> " + newResult.toString());
          }
        } catch (Exception e) {
          GNS.getLogger().info("Active code error: " + e.getMessage());
        }
      }
    }
    return newResult;
  }
}
