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
 *  Initial developer(s): Westy, Emmanuel Cecchet
 *
 */
package edu.umass.cs.gnsclient.client;

import edu.umass.cs.gnscommon.GNSCommandProtocol;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;
import static edu.umass.cs.gnsclient.client.CommandUtils.*;
import edu.umass.cs.gnsclient.client.util.GuidUtils;
import edu.umass.cs.gnsclient.client.util.KeyPairUtils;
import edu.umass.cs.gnsclient.client.util.Password;
import edu.umass.cs.gnscommon.SharedGuidUtils;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.ACCESSER;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.ACCOUNT_GUID;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.ACL_ADD;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.ACL_REMOVE;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.ACL_RETRIEVE;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.ACL_TYPE;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.AC_ACTION;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.AC_CLEAR;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.AC_CODE;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.AC_GET;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.AC_SET;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.ADD_ALIAS;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.ADD_GUID;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.ADD_MULTIPLE_GUIDS;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.ADD_TAG;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.ADD_TO_GROUP;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.ADMIN;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.ALL_FIELDS;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.ALL_USERS;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.APPEND;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.APPEND_LIST;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.APPEND_LIST_WITH_DUPLICATION;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.APPEND_OR_CREATE;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.APPEND_OR_CREATE_LIST;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.APPEND_WITH_DUPLICATION;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.CLEAR;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.CLEAR_TAGGED;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.CODE;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.CREATE;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.CREATE_INDEX;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.CREATE_LIST;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.DUMP;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.FIELD;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.FIELDS;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.GET_GROUPS;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.GET_GROUP_MEMBERS;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.GET_PARAMETER;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.GROUP_ACL;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.GUID;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.GUIDCNT;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.GUID_RECORD_PUBLICKEY;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.INTERVAL;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.LOCATION_FIELD_NAME;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.LOOKUP_ACCOUNT_RECORD;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.LOOKUP_GUID;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.LOOKUP_GUID_RECORD;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.LOOKUP_PRIMARY_GUID;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.MAX_DISTANCE;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.MEMBER;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.MEMBERS;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.N;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.NAME;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.NAMES;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.NEAR;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.OLD_VALUE;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.PASSKEY;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.PASSWORD;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.PUBLIC_KEY;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.PUBLIC_KEYS;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.QUERY;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.READ;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.READER;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.READ_ARRAY;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.READ_ARRAY_ONE;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.REGISTER_ACCOUNT;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.REMOVE_ACCOUNT;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.REMOVE_ALIAS;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.REMOVE_FIELD;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.REMOVE_FROM_GROUP;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.REMOVE_GUID;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.REMOVE_LIST;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.REMOVE_TAG;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.REPLACE;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.REPLACE_LIST;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.REPLACE_OR_CREATE;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.REPLACE_OR_CREATE_LIST;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.REPLACE_USER_JSON;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.RETRIEVE_ALIASES;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.RSA_ALGORITHM;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.SELECT;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.SELECT_GROUP;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.SET;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.SET_FIELD_NULL;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.SET_PARAMETER;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.SUBSTITUTE;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.SUBSTITUTE_LIST;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.USER_JSON;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.VALUE;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.VERIFY_ACCOUNT;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.WITHIN;
import static edu.umass.cs.gnscommon.GNSCommandProtocol.WRITER;
import edu.umass.cs.gnscommon.exceptions.client.EncryptionException;
import org.json.JSONArray;
import org.json.JSONObject;
import edu.umass.cs.gnscommon.exceptions.client.ClientException;
import edu.umass.cs.gnscommon.exceptions.client.FieldNotFoundException;
import edu.umass.cs.gnscommon.exceptions.client.InvalidGuidException;
import edu.umass.cs.gnscommon.utils.Base64;
import edu.umass.cs.gnsserver.gnsapp.clientCommandProcessor.commands.CommandType;
import edu.umass.cs.gnsserver.main.GNSConfig;
import edu.umass.cs.utils.DelayProfiler;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.json.JSONException;

/**
 * This class defines a client to communicate with a GNS instance over TCP.
 * This class adds single field list based commands to the {@link AbstractGNSClient}'s JSONObject based commands.
 *
 * This class contains a concise subset of all available server operations.
 * For a more complete set see {@link UniversalTcpClientExtended}.
 *
 * @author <a href="mailto:westy@cs.umass.edu">Westy</a>
 * @version 1.0
 */
public class GNSClientCommands extends GNSClient implements GNSClientInterface {

  public GNSClientCommands() throws IOException {
    super(null);
  }

  /**
   * Creates a new <code>UniversalTcpClient</code> object
   *
   * @param anyReconfigurator
   * @throws java.io.IOException
   */
  public GNSClientCommands(InetSocketAddress anyReconfigurator) throws IOException {
    super(anyReconfigurator);
  }

  // READ AND WRITE COMMANDS
  /**
   * Updates the JSONObject associated with targetGuid using the given JSONObject.
   * Top-level fields not specified in the given JSONObject are not modified.
   * The writer is the guid of the user attempting access. Signs the query using
   * the private key of the user associated with the writer guid.
   *
   * @param targetGuid
   * @param json
   * @param writer
   * @throws IOException
   * @throws ClientException
   */
  public void update(String targetGuid, JSONObject json, GuidEntry writer) throws IOException, ClientException {
    JSONObject command = createAndSignCommand(CommandType.ReplaceUserJSON,
            writer.getPrivateKey(),
            REPLACE_USER_JSON, GUID,
            targetGuid, USER_JSON, json.toString(), WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Replaces the JSON in guid with JSONObject. Signs the query using
   * the private key of the given guid.
   *
   * @param guid
   * @param json
   * @throws IOException
   * @throws ClientException
   */
  public void update(GuidEntry guid, JSONObject json) throws IOException, ClientException {
    update(guid.getGuid(), json, guid);
  }

  /**
   * Updates the field in the targetGuid. The writer is the guid
   * of the user attempting access. Signs the query using
   * the private key of the writer guid.
   *
   * @param targetGuid
   * @param field
   * @param value
   * @param writer
   * @throws IOException
   * @throws ClientException
   * @throws JSONException
   */
  public void fieldUpdate(String targetGuid, String field, Object value, GuidEntry writer)
          throws IOException, ClientException, JSONException {
    JSONObject json = new JSONObject();
    json.put(field, value);
    JSONObject command = createAndSignCommand(CommandType.ReplaceUserJSON,
            writer.getPrivateKey(), REPLACE_USER_JSON,
            GUID, targetGuid, USER_JSON, json.toString(), WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Creates an index for a field. The guid is only used for
   * authentication purposes.
   *
   * @param guid
   * @param field
   * @param index
   * @throws IOException
   * @throws ClientException
   * @throws JSONException
   */
  public void fieldCreateIndex(GuidEntry guid, String field, String index)
          throws IOException, ClientException, JSONException {
    JSONObject command = createAndSignCommand(CommandType.CreateIndex,
            guid.getPrivateKey(), CREATE_INDEX,
            GUID, guid.getGuid(), FIELD, field, VALUE, index, WRITER, guid.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Updates the field in the targetGuid.
   * Signs the query using the private key of the given guid.
   *
   * @param targetGuid
   * @param field
   * @param value
   * @throws IOException
   * @throws ClientException
   * @throws JSONException
   */
  public void fieldUpdate(GuidEntry targetGuid, String field, Object value)
          throws IOException, ClientException, JSONException {
    fieldUpdate(targetGuid.getGuid(), field, value, targetGuid);
  }

  /**
   * Reads the JSONObject for the given targetGuid.
   * The reader is the guid of the user attempting access. Signs the query using
   * the private key of the user associated with the reader guid (unsigned if
   * reader is null).
   *
   * @param targetGuid
   * @param reader if null guid must be all fields readable for all users
   * @return a JSONObject
   * @throws Exception
   */
  public JSONObject read(String targetGuid, GuidEntry reader) throws Exception {
    JSONObject command;
    if (reader == null) {
      // this one actually uses the old style read... for now
      command = createCommand(CommandType.ReadArrayUnsigned,
              READ_ARRAY, GUID, targetGuid, FIELD, ALL_FIELDS);
    } else {
      // this one actually uses the old style read... for now
      command = createAndSignCommand(CommandType.ReadArray,
              reader.getPrivateKey(), READ_ARRAY, GUID, targetGuid, FIELD, ALL_FIELDS,
              READER, reader.getGuid());
    }

    String response = sendCommandAndWait(command);

    return new JSONObject(checkResponse(command, response));
  }

  /**
   * Reads the entire record from the GNS server for the given guid.
   * Signs the query using the private key of the guid.
   *
   * @param guid
   * @return a JSONArray containing the values in the field
   * @throws Exception
   */
  public JSONObject read(GuidEntry guid) throws Exception {
    return read(guid.getGuid(), guid);
  }

  /**
   * Returns true if the field exists in the given targetGuid.
   * Field is a string the naming the field. Field can use dot
   * notation to indicate subfields. The reader is the guid of the
   * user attempting access. This method signs the query using the
   * private key of the user associated with the reader guid (unsigned if
   * reader is null).
   *
   * @param targetGuid
   * @param field
   * @param reader if null the field must be readable for all
   * @return a boolean indicating if the field exists
   * @throws Exception
   */
  public boolean fieldExists(String targetGuid, String field, GuidEntry reader) throws Exception {
    JSONObject command;
    if (reader == null) {
      command = createCommand(CommandType.ReadUnsigned,
              READ, GUID, targetGuid, FIELD, field);
    } else {
      command = createAndSignCommand(CommandType.Read,
              reader.getPrivateKey(), READ, GUID, targetGuid, FIELD, field,
              READER, reader.getGuid());
    }
    String response = sendCommandAndWait(command);
    try {
      checkResponse(command, response);
      return true;
    } catch (FieldNotFoundException e) {
      return false;
    }
  }

  /**
   * Returns true if the field exists in the given targetGuid.
   * Field is a string the naming the field. Field can use dot
   * notation to indicate subfields. This method signs the query using the
   * private key of the targetGuid.
   *
   * @param targetGuid
   * @param field
   * @return a boolean indicating if the field exists
   * @throws Exception
   */
  public boolean fieldExists(GuidEntry targetGuid, String field) throws Exception {
    return fieldExists(targetGuid.getGuid(), field, targetGuid);
  }

  /**
   * Reads the value of field for the given targetGuid.
   * Field is a string the naming the field. Field can use dot
   * notation to indicate subfields. The reader is the guid of the
   * user attempting access. This method signs the query using the
   * private key of the user associated with the reader guid (unsigned if
   * reader is null).
   *
   * @param targetGuid
   * @param field
   * @param reader if null the field must be readable for all
   * @return a string containing the values in the field
   * @throws Exception
   */
  public String fieldRead(String targetGuid, String field, GuidEntry reader) throws Exception {
    JSONObject command;
    if (reader == null) {
      command = createCommand(CommandType.ReadUnsigned,
              READ, GUID, targetGuid, FIELD, field);
    } else {
      command = createAndSignCommand(CommandType.Read,
              reader.getPrivateKey(), READ, GUID, targetGuid, FIELD, field,
              READER, reader.getGuid());
    }

    long t = System.currentTimeMillis();
    String response = sendCommandAndWait(command);
    DelayProfiler.updateDelay("fieldRead", t);
    GNSClientConfig.getLogger().fine(DelayProfiler.getStats());

    return checkResponse(command, response);
  }

  /**
   * Reads the value of field from the targetGuid.
   * Field is a string the naming the field. Field can use dot
   * notation to indicate subfields. This method signs the query using the
   * private key of the targetGuid.
   *
   * @param targetGuid
   * @param field
   * @return
   * @throws Exception
   */
  public String fieldRead(GuidEntry targetGuid, String field) throws Exception {
    return fieldRead(targetGuid.getGuid(), field, targetGuid);
  }

  /**
   * Reads the value of fields for the given targetGuid.
   * Fields is a list of strings the naming the field. Fields can use dot
   * notation to indicate subfields. The reader is the guid of the
   * user attempting access. This method signs the query using the
   * private key of the user associated with the reader guid (unsigned if
   * reader is null).
   *
   * @param targetGuid
   * @param fields
   * @param reader if null the field must be readable for all
   * @return a JSONArray containing the values in the fields
   * @throws Exception
   */
  public String fieldRead(String targetGuid, ArrayList<String> fields, GuidEntry reader) throws Exception {
    JSONObject command;
    if (reader == null) {
      command = createCommand(CommandType.ReadMultiFieldUnsigned,
              READ, GUID, targetGuid, FIELDS, fields);
    } else {
      command = createAndSignCommand(CommandType.ReadMultiField,
              reader.getPrivateKey(), READ, GUID, targetGuid,
              FIELDS, fields,
              READER, reader.getGuid());
    }

    String response = sendCommandAndWait(command);

    return checkResponse(command, response);
  }

  /**
   * Reads the value of fields for the given guid.
   * Fields is a list of strings the naming the field. Fields can use dot
   * notation to indicate subfields. This method signs the query using the
   * private key of the guid.
   *
   * @param targetGuid
   * @param fields
   * @return
   * @throws Exception
   */
  public String fieldRead(GuidEntry targetGuid, ArrayList<String> fields) throws Exception {
    return fieldRead(targetGuid.getGuid(), fields, targetGuid);
  }

  /**
   * Removes a field in the JSONObject record of the given targetGuid.
   * The writer is the guid of the user attempting access.
   * Signs the query using the private key of the user
   * associated with the writer guid.
   *
   * @param targetGuid
   * @param field
   * @param writer
   * @throws IOException
   * @throws InvalidKeyException
   * @throws NoSuchAlgorithmException
   * @throws SignatureException
   * @throws ClientException
   */
  public void fieldRemove(String targetGuid, String field, GuidEntry writer) throws IOException, InvalidKeyException,
          NoSuchAlgorithmException, SignatureException, ClientException {
    JSONObject command = createAndSignCommand(CommandType.RemoveField,
            writer.getPrivateKey(), REMOVE_FIELD, GUID, targetGuid,
            FIELD, field, WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);
    checkResponse(command, response);
  }

  // SELECT COMMANDS
  /**
   * Selects all records that match query.
   * Returns the result of the query as a JSONArray of guids.

 The query syntax is described here:
 https://gns.name/wiki/index.php?title=Query_Syntax

 Currently there are two predefined field names in the GNS client (this is in edu.umass.cs.gnsclient.client.GNSCommandProtocol):
 LOCATION_FIELD_NAME = "geoLocation"; Defined as a "2d" index in the database.
 IPADDRESS_FIELD_NAME = "netAddress";

 There are links in the wiki page abive to find the exact syntax for querying spacial coordinates.
   *
   * @param query - the query
   * @return - a JSONArray of guids
   * @throws Exception
   */
  public JSONArray selectQuery(String query) throws Exception {
    JSONObject command = createCommand(CommandType.SelectQuery,
            SELECT, QUERY, query);
    String response = sendCommandAndWait(command);

    return new JSONArray(checkResponse(command, response));
  }

  /**
   * Set up a context aware group guid using a query.
   * Requires a accountGuid and a publicKey which are used to set up the new guid
   * or look it up if it already exists.
   *
   * Also returns the result of the query as a JSONArray of guids.
   *
   * The query syntax is described here:
   * https://gns.name/wiki/index.php?title=Query_Syntax
   *
   * @param accountGuid
   * @param publicKey
   * @param query the query
   * @param interval - the refresh interval in seconds - default is 60 - (queries that happens quicker than
   * this will get stale results)
   * @return a JSONArray of guids
   * @throws Exception
   */
  public JSONArray selectSetupGroupQuery(GuidEntry accountGuid, String publicKey, String query, int interval) throws Exception {
    JSONObject command = createCommand(CommandType.SelectGroupSetupQuery,
            SELECT_GROUP, GUID, accountGuid.getGuid(),
            PUBLIC_KEY, publicKey, QUERY, query,
            INTERVAL, interval);
    String response = sendCommandAndWait(command);

    return new JSONArray(checkResponse(command, response));
  }

  /**
   * Look up the value of a context aware group guid using a query.
   * Returns the result of the query as a JSONArray of guids. The results will be
   * stale if the queries that happen more quickly than the refresh interval given during setup.
   *
   * @param guid
   * @return a JSONArray of guids
   * @throws Exception
   */
  public JSONArray selectLookupGroupQuery(String guid) throws Exception {
    JSONObject command = createCommand(CommandType.SelectGroupLookupQuery,
            SELECT_GROUP, GUID, guid);
    String response = sendCommandAndWait(command);

    return new JSONArray(checkResponse(command, response));
  }

  // ACCOUNT COMMANDS
  /**
   * Obtains the guid of the alias from the GNS server.
   *
   * @param alias
   * @return guid
   * @throws IOException
   * @throws UnsupportedEncodingException
   * @throws ClientException
   */
  public String lookupGuid(String alias) throws IOException, ClientException {
    JSONObject command = createCommand(CommandType.LookupGuid,
            LOOKUP_GUID, NAME, alias);
    String response = sendCommandAndWait(command);

    return checkResponse(command, response);
  }

  /**
   * If this is a sub guid returns the account guid it was created under.
   *
   * @param guid
   * @return
   * @throws UnsupportedEncodingException
   * @throws IOException
   * @throws ClientException
   */
  public String lookupPrimaryGuid(String guid) throws UnsupportedEncodingException, IOException, ClientException {
    JSONObject command = createCommand(CommandType.LookupPrimaryGuid,
            LOOKUP_PRIMARY_GUID, GUID, guid);
    String response = sendCommandAndWait(command);

    return checkResponse(command, response);
  }

  /**
   * Returns a JSON object containing all of the guid meta information.
   * This method returns meta data about the guid.
   * If you want any particular field or fields of the guid
   * you'll need to use one of the read methods.
   *
   * @param guid
   * @return
   * @throws IOException
   * @throws ClientException
   */
  @Override
  public JSONObject lookupGuidRecord(String guid) throws IOException, ClientException {
    JSONObject command = createCommand(CommandType.LookupGuidRecord,
            LOOKUP_GUID_RECORD, GUID, guid);
    String response = sendCommandAndWait(command);
    checkResponse(command, response);
    try {
      return new JSONObject(response);
    } catch (JSONException e) {
      throw new ClientException("Failed to parse LOOKUP_GUID_RECORD response", e);
    }
  }

  /**
   * Returns a JSON object containing all of the account meta information for an
   * account guid.
   * This method returns meta data about the account associated with this guid
   * if and only if the guid is an account guid.
   * If you want any particular field or fields of the guid
   * you'll need to use one of the read methods.
   *
   * @param accountGuid
   * @return
   * @throws IOException
   * @throws ClientException
   */
  public JSONObject lookupAccountRecord(String accountGuid) throws IOException, ClientException {
    JSONObject command = createCommand(CommandType.LookupAccountRecord,
            LOOKUP_ACCOUNT_RECORD, GUID, accountGuid);
    String response = sendCommandAndWait(command);
    checkResponse(command, response);
    try {
      return new JSONObject(response);
    } catch (JSONException e) {
      throw new ClientException("Failed to parse LOOKUP_ACCOUNT_RECORD response", e);
    }
  }

  /**
   * Get the public key for a given alias.
   *
   * @param alias
   * @return the public key registered for the alias
   * @throws InvalidGuidException
   * @throws ClientException
   * @throws IOException
   */
  public PublicKey publicKeyLookupFromAlias(String alias) throws InvalidGuidException, ClientException, IOException {

    String guid = lookupGuid(alias);
    return publicKeyLookupFromGuid(guid);
  }

  /**
   * Get the public key for a given GUID.
   *
   * @param guid
   * @return
   * @throws InvalidGuidException
   * @throws ClientException
   * @throws IOException
   */
  public PublicKey publicKeyLookupFromGuid(String guid) throws InvalidGuidException, ClientException, IOException {
    JSONObject guidInfo = lookupGuidRecord(guid);
    try {
      String key = guidInfo.getString(GUID_RECORD_PUBLICKEY);
      byte[] encodedPublicKey = Base64.decode(key);
      KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
      X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
      return keyFactory.generatePublic(publicKeySpec);
    } catch (JSONException e) {
      throw new ClientException("Failed to parse LOOKUP_USER response", e);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new EncryptionException("Public key encryption failed", e);
    }

  }

  /**
   * Register a new account guid with the corresponding alias on the GNS server.
   * This generates a new guid and a public / private key pair. Returns a
   * GuidEntry for the new account which contains all of this information.
   *
   * @param alias - a human readable alias to the guid - usually an email
   * address
   * @return
   * @throws Exception
   */
  @Override
  public GuidEntry accountGuidCreate(String alias, String password) throws Exception {

    KeyPair keyPair = KeyPairGenerator.getInstance(RSA_ALGORITHM).generateKeyPair();
    String guid = SharedGuidUtils.createGuidStringFromPublicKey(keyPair.getPublic().getEncoded());
    // Squirrel this away now just in case the call below times out.
    KeyPairUtils.saveKeyPair(getGNSInstance(), alias, guid, keyPair);
    String returnedGuid = accountGuidCreateHelper(alias, keyPair.getPublic(), keyPair.getPrivate(), password);
    assert returnedGuid.equals(guid);
    // Anything else we want to do here?
    if (!returnedGuid.equals(guid)) {
      GNSClientConfig.getLogger().log(Level.WARNING,
              "Returned guid {0} doesn''t match locally created guid {1}",
              new Object[]{returnedGuid, guid});
    }
    GuidEntry entry = new GuidEntry(alias, guid, keyPair.getPublic(), keyPair.getPrivate());

    return entry;
  }

  /**
   * Verify an account by sending the verification code back to the server.
   *
   * @param guid the account GUID to verify
   * @param code the verification code
   * @return ?
   * @throws Exception
   */
  @Override
  public String accountGuidVerify(GuidEntry guid, String code) throws Exception {
    JSONObject command = createAndSignCommand(CommandType.VerifyAccount,
            guid.getPrivateKey(), VERIFY_ACCOUNT, GUID, guid.getGuid(),
            CODE, code);
    String response = sendCommandAndWait(command);
    return checkResponse(command, response);
  }

  /**
   * Deletes the account given by name
   *
   * @param guid GuidEntry
   * @throws Exception
   */
  public void accountGuidRemove(GuidEntry guid) throws Exception {
    JSONObject command = createAndSignCommand(CommandType.RemoveAccount,
            guid.getPrivateKey(),
            REMOVE_ACCOUNT,
            GUID, guid.getGuid(),
            NAME, guid.getEntityName());
    String response = sendCommandAndWait(command);
    checkResponse(command, response);
  }

  /**
   * Creates an new GUID associated with an account on the GNS server.
   *
   * @param accountGuid
   * @param alias the alias
   * @return the newly created GUID entry
   * @throws Exception
   */
  @Override
  public GuidEntry guidCreate(GuidEntry accountGuid, String alias) throws Exception {

    long startTime = System.currentTimeMillis();
    GuidEntry entry = GuidUtils.createAndSaveGuidEntry(alias, getGNSInstance());
    DelayProfiler.updateDelay("updatePreferences", startTime);
    String returnedGuid = guidCreateHelper(accountGuid, alias, entry.getPublicKey());
    assert returnedGuid.equals(entry.getGuid());
    // Anything else we want to do here?
    if (!returnedGuid.equals(entry.getGuid())) {
      GNSClientConfig.getLogger().log(Level.WARNING,
              "Returned guid {0}doesn''t match locally created guid{1}",
              new Object[]{returnedGuid, entry.getGuid()});
    }
    DelayProfiler.updateDelay("guidCreateFromAlias", startTime);
    return entry;
  }

  /**
   * Batch create guids with the given aliases. If
   * createPublicKeys is true, key pairs will be created and saved
   * by the client for the guids. If not, bogus public keys will
   * be uses which will make the guids only accessible using the
   * account guid (which has ACL access to each guid).
   *
   * @param accountGuid
   * @param aliases
   * @return
   * @throws Exception
   */
  public String guidBatchCreate(GuidEntry accountGuid, Set<String> aliases) throws Exception {

    List<String> aliasList = new ArrayList<>(aliases);
    List<String> publicKeys = null;
    //if (createPublicKeys) {
      long publicKeyStartTime = System.currentTimeMillis();
      publicKeys = new ArrayList<>();
      for (String alias : aliasList) {
        long singleEntrystartTime = System.currentTimeMillis();
        GuidEntry entry = GuidUtils.createAndSaveGuidEntry(alias, getGNSInstance());
        DelayProfiler.updateDelay("updateOnePreference", singleEntrystartTime);
        byte[] publicKeyBytes = entry.getPublicKey().getEncoded();
        String publicKeyString = Base64.encodeToString(publicKeyBytes, false);
        publicKeys.add(publicKeyString);
      }
      DelayProfiler.updateDelay("batchCreatePublicKeys", publicKeyStartTime);
    //}

    System.out.println(DelayProfiler.getStats());
    JSONObject command;
    //if (createPublicKeys) {
      command = createAndSignCommand(CommandType.AddMultipleGuids,
              accountGuid.getPrivateKey(), ADD_MULTIPLE_GUIDS,
              GUID, accountGuid.getGuid(),
              NAMES, new JSONArray(aliasList),
              PUBLIC_KEYS, new JSONArray(publicKeys));
//    } else {
//      // This version creates guids that have bogus public keys
//      command = createAndSignCommand(CommandType.AddMultipleGuidsFast,
//              accountGuid.getPrivateKey(), ADD_MULTIPLE_GUIDS,
//              GUID, accountGuid.getGuid(),
//              NAMES, new JSONArray(aliasList));
//    }
    String result = checkResponse(command, sendCommandAndWait(command));
    return result;
  }

//  /**
//   * Batch create a number guids. These guids will have
//   * random aliases and can be accessed using the account guid.
//   *
//   * @param accountGuid
//   * @param guidCnt
//   * @return
//   * @throws Exception
//   */
//  public String guidBatchCreateFast(GuidEntry accountGuid, int guidCnt) throws Exception {
//    JSONObject command;
//    // This version creates guids that have bogus public keys
//    command = createAndSignCommand(CommandType.AddMultipleGuidsFastRandom,
//            accountGuid.getPrivateKey(), ADD_MULTIPLE_GUIDS,
//            GUID, accountGuid.getGuid(),
//            GUIDCNT, guidCnt);
//    String result = checkResponse(command, sendCommandAndWait(command));
//    return result;
//  }

  /**
   * Removes a guid (not for account Guids - use removeAccountGuid for them).
   *
   * @param guid the guid to remove
   * @throws Exception
   */
  public void guidRemove(GuidEntry guid) throws Exception {
    JSONObject command = createAndSignCommand(CommandType.RemoveGuidNoAccount,
            guid.getPrivateKey(),
            REMOVE_GUID, GUID, guid.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Removes a guid given the guid and the associated account guid.
   *
   * @param accountGuid
   * @param guidToRemove
   * @throws Exception
   */
  public void guidRemove(GuidEntry accountGuid, String guidToRemove) throws Exception {
    JSONObject command = createAndSignCommand(CommandType.RemoveGuid,
            accountGuid.getPrivateKey(),
            REMOVE_GUID,
            ACCOUNT_GUID, accountGuid.getGuid(),
            GUID, guidToRemove);
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  // GROUP COMMANDS
  /**
   * Return the list of guids that are members of the group. Signs the query
   * using the private key of the user associated with the guid.
   *
   * @param groupGuid the guid of the group to lookup
   * @param reader the guid of the entity doing the lookup
   * @return the list of guids as a JSONArray
   * @throws IOException if a communication error occurs
   * @throws ClientException if a protocol error occurs or the list cannot be
   * parsed
   * @throws InvalidGuidException if the group guid is invalid
   */
  public JSONArray groupGetMembers(String groupGuid, GuidEntry reader) throws IOException, ClientException,
          InvalidGuidException {
    JSONObject command = createAndSignCommand(CommandType.GetGroupMembers,
            reader.getPrivateKey(), GET_GROUP_MEMBERS, GUID, groupGuid,
            READER, reader.getGuid());
    String response = sendCommandAndWait(command);

    try {
      return new JSONArray(checkResponse(command, response));
    } catch (JSONException e) {
      throw new ClientException("Invalid member list", e);
    }
  }

  /**
   * Return a list of the groups that the guid is a member of. Signs the query
   * using the private key of the user associated with the guid.
   *
   * @param guid the guid we are looking for
   * @param reader the guid of the entity doing the lookup
   * @return the list of groups as a JSONArray
   * @throws IOException if a communication error occurs
   * @throws ClientException if a protocol error occurs or the list cannot be
   * parsed
   * @throws InvalidGuidException if the group guid is invalid
   */
  public JSONArray guidGetGroups(String guid, GuidEntry reader) throws IOException, ClientException,
          InvalidGuidException {
    JSONObject command = createAndSignCommand(CommandType.GetGroups,
            reader.getPrivateKey(), GET_GROUPS, GUID, guid,
            READER, reader.getGuid());
    String response = sendCommandAndWait(command);

    try {
      return new JSONArray(checkResponse(command, response));
    } catch (JSONException e) {
      throw new ClientException("Invalid member list", e);
    }
  }

  /**
   * Add a guid to a group guid. Any guid can be a group guid. Signs the query
   * using the private key of the user associated with the writer.
   *
   * @param groupGuid guid of the group
   * @param guidToAdd guid to add to the group
   * @param writer the guid doing the add
   * @throws IOException
   * @throws InvalidGuidException if the group guid does not exist
   * @throws ClientException
   */
  public void groupAddGuid(String groupGuid, String guidToAdd, GuidEntry writer) throws IOException,
          InvalidGuidException, ClientException {
    JSONObject command = createAndSignCommand(CommandType.AddToGroup,
            writer.getPrivateKey(), ADD_TO_GROUP, GUID, groupGuid,
            MEMBER, guidToAdd, WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Add multiple members to a group
   *
   * @param groupGuid guid of the group
   * @param members guids of members to add to the group
   * @param writer the guid doing the add
   * @throws IOException
   * @throws InvalidGuidException
   * @throws ClientException
   * @throws InvalidKeyException
   * @throws NoSuchAlgorithmException
   * @throws SignatureException
   */
  public void groupAddGuids(String groupGuid, JSONArray members, GuidEntry writer) throws IOException,
          InvalidGuidException, ClientException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
    JSONObject command = createAndSignCommand(CommandType.AddMembersToGroup,
            writer.getPrivateKey(), ADD_TO_GROUP, GUID, groupGuid,
            MEMBERS, members.toString(), WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Removes a guid from a group guid. Any guid can be a group guid. Signs the
   * query using the private key of the user associated with the writer.
   *
   * @param guid guid of the group
   * @param guidToRemove guid to remove from the group
   * @param writer the guid of the entity doing the remove
   * @throws IOException
   * @throws InvalidGuidException if the group guid does not exist
   * @throws ClientException
   */
  public void groupRemoveGuid(String guid, String guidToRemove, GuidEntry writer) throws IOException,
          InvalidGuidException, ClientException {
    JSONObject command = createAndSignCommand(CommandType.RemoveFromGroup,
            writer.getPrivateKey(), REMOVE_FROM_GROUP, GUID, guid,
            MEMBER, guidToRemove, WRITER, writer.getGuid());

    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Remove a list of members from a group
   *
   * @param guid guid of the group
   * @param members guids to remove from the group
   * @param writer the guid of the entity doing the remove
   * @throws IOException
   * @throws InvalidGuidException
   * @throws ClientException
   * @throws InvalidKeyException
   * @throws NoSuchAlgorithmException
   * @throws SignatureException
   */
  public void groupRemoveGuids(String guid, JSONArray members, GuidEntry writer) throws IOException,
          InvalidGuidException, ClientException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
    JSONObject command = createAndSignCommand(CommandType.RemoveMembersFromGroup,
            writer.getPrivateKey(), REMOVE_FROM_GROUP, GUID, guid,
            MEMBERS, members.toString(), WRITER, writer.getGuid());

    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Authorize guidToAuthorize to add/remove members from the group groupGuid.
   * If guidToAuthorize is null, everyone is authorized to add/remove members to
   * the group. Note that this method can only be called by the group owner
   * (private key required) Signs the query using the private key of the group
   * owner.
   *
   * @param groupGuid the group GUID entry
   * @param guidToAuthorize the guid to authorize to manipulate group membership
   * or null for anyone
   * @throws Exception
   */
  public void groupAddMembershipUpdatePermission(GuidEntry groupGuid, String guidToAuthorize) throws Exception {
    aclAdd(GNSCommandProtocol.AccessType.WRITE_WHITELIST, groupGuid, GROUP_ACL, guidToAuthorize);
  }

  /**
   * Unauthorize guidToUnauthorize to add/remove members from the group
   * groupGuid. If guidToUnauthorize is null, everyone is forbidden to
   * add/remove members to the group. Note that this method can only be called
   * by the group owner (private key required). Signs the query using the
   * private key of the group owner.
   *
   * @param groupGuid the group GUID entry
   * @param guidToUnauthorize the guid to authorize to manipulate group
   * membership or null for anyone
   * @throws Exception
   */
  public void groupRemoveMembershipUpdatePermission(GuidEntry groupGuid, String guidToUnauthorize) throws Exception {
    aclRemove(GNSCommandProtocol.AccessType.WRITE_WHITELIST, groupGuid, GROUP_ACL, guidToUnauthorize);
  }

  /**
   * Authorize guidToAuthorize to get the membership list from the group
   * groupGuid. If guidToAuthorize is null, everyone is authorized to list
   * members of the group. Note that this method can only be called by the group
   * owner (private key required). Signs the query using the private key of the
   * group owner.
   *
   * @param groupGuid the group GUID entry
   * @param guidToAuthorize the guid to authorize to manipulate group membership
   * or null for anyone
   * @throws Exception
   */
  public void groupAddMembershipReadPermission(GuidEntry groupGuid, String guidToAuthorize) throws Exception {
    aclAdd(GNSCommandProtocol.AccessType.READ_WHITELIST, groupGuid, GROUP_ACL, guidToAuthorize);
  }

  /**
   * Unauthorize guidToUnauthorize to get the membership list from the group
   * groupGuid. If guidToUnauthorize is null, everyone is forbidden from
   * querying the group membership. Note that this method can only be called by
   * the group owner (private key required). Signs the query using the private
   * key of the group owner.
   *
   * @param groupGuid the group GUID entry
   * @param guidToUnauthorize the guid to authorize to manipulate group
   * membership or null for anyone
   * @throws Exception
   */
  public void groupRemoveMembershipReadPermission(GuidEntry groupGuid, String guidToUnauthorize) throws Exception {
    aclRemove(GNSCommandProtocol.AccessType.READ_WHITELIST, groupGuid, GROUP_ACL, guidToUnauthorize);
  }

  // ACL COMMANDS
  /**
   * Adds to an access control list of the given field. The accesser can be a
   * guid of a user or a group guid or null which means anyone can access the
   * field. The field can be also be +ALL+ which means all fields can be read by
   * the reader. Signs the query using the private key of the user associated
   * with the guid.
   *
   * @param accessType a value from GnrsProtocol.AccessType
   * @param targetGuid guid of the field to be modified
   * @param field field name
   * @param accesserGuid guid to add to the ACL
   * @throws Exception
   * @throws ClientException if the query is not accepted by the server.
   */
  public void aclAdd(GNSCommandProtocol.AccessType accessType, GuidEntry targetGuid, String field, String accesserGuid)
          throws Exception {
    aclAdd(accessType.name(), targetGuid, field, accesserGuid);
  }

  /**
   * Removes a GUID from an access control list of the given user's field on the
   * GNS server to include the guid specified in the accesser param. The
   * accesser can be a guid of a user or a group guid or null which means anyone
   * can access the field. The field can be also be +ALL+ which means all fields
   * can be read by the reader. Signs the query using the private key of the
   * user associated with the guid.
   *
   * @param accessType
   * @param guid
   * @param field
   * @param accesserGuid
   * @throws Exception
   * @throws ClientException if the query is not accepted by the server.
   */
  public void aclRemove(GNSCommandProtocol.AccessType accessType, GuidEntry guid, String field, String accesserGuid)
          throws Exception {
    aclRemove(accessType.name(), guid, field, accesserGuid);
  }

  /**
   * Get an access control list of the given user's field on the GNS server to
   * include the guid specified in the accesser param. The accesser can be a
   * guid of a user or a group guid or null which means anyone can access the
   * field. The field can be also be +ALL+ which means all fields can be read by
   * the reader. Signs the query using the private key of the user associated
   * with the guid.
   *
   * @param accessType
   * @param guid
   * @param field
   * @param accesserGuid
   * @return list of GUIDs for that ACL
   * @throws Exception
   * @throws ClientException if the query is not accepted by the server.
   */
  public JSONArray aclGet(GNSCommandProtocol.AccessType accessType, GuidEntry guid, String field, String accesserGuid)
          throws Exception {
    return aclGet(accessType.name(), guid, field, accesserGuid);
  }

  // ALIASES
  /**
   * Creates an alias entity name for the given guid. The alias can be used just
   * like the original entity name.
   *
   * @param guid
   * @param name - the alias
   * @throws Exception
   */
  public void addAlias(GuidEntry guid, String name) throws Exception {
    JSONObject command = createAndSignCommand(CommandType.AddAlias,
            guid.getPrivateKey(), ADD_ALIAS, GUID, guid.getGuid(),
            NAME, name);
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Removes the alias for the given guid.
   *
   * @param guid
   * @param name - the alias
   * @throws Exception
   */
  public void removeAlias(GuidEntry guid, String name) throws Exception {
    JSONObject command = createAndSignCommand(CommandType.RemoveAlias,
            guid.getPrivateKey(), REMOVE_ALIAS, GUID, guid.getGuid(),
            NAME, name);
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Retrieve the aliases associated with the given guid.
   *
   * @param guid
   * @return - a JSONArray containing the aliases
   * @throws Exception
   */
  public JSONArray getAliases(GuidEntry guid) throws Exception {
    JSONObject command = createAndSignCommand(CommandType.RetrieveAliases,
            guid.getPrivateKey(), RETRIEVE_ALIASES, GUID, guid.getGuid());

    String response = sendCommandAndWait(command);
    try {
      return new JSONArray(checkResponse(command, response));
    } catch (JSONException e) {
      throw new ClientException("Invalid alias list", e);
    }
  }

  // TAGS
  /**
   * Creates a tag to the tags of the guid.
   *
   * @param guid
   * @param tag
   * @throws Exception
   */
  @Override
  public void addTag(GuidEntry guid, String tag) throws Exception {
    JSONObject command = createAndSignCommand(CommandType.AddTag,
            guid.getPrivateKey(), ADD_TAG,
            GUID, guid.getGuid(), NAME, tag);
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Removes a tag from the tags of the guid.
   *
   * @param guid
   * @param tag
   * @throws Exception
   */
  public void removeTag(GuidEntry guid, String tag) throws Exception {
    JSONObject command = createAndSignCommand(CommandType.RemoveTag,
            guid.getPrivateKey(), REMOVE_TAG,
            GUID, guid.getGuid(), NAME, tag);
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Retrieves GUIDs that have been tagged with tag
   *
   * @param tag
   * @return
   * @throws Exception
   */
  public JSONArray retrieveTagged(String tag) throws Exception {
    JSONObject command = createCommand(CommandType.Dump,
            DUMP, NAME, tag);
    String response = sendCommandAndWait(command);

    return new JSONArray(checkResponse(command, response));
  }

  /**
   * Removes all guids that have the corresponding tag. Removes the reverse
   * fields for the entity name and aliases. Note: doesn't remove all the
   * associated fields yet, though, so still a work in progress.
   *
   * @param tag
   * @throws Exception
   */
  public void clearTagged(String tag) throws Exception {
    JSONObject command = createCommand(CommandType.ClearTagged,
            CLEAR_TAGGED, NAME, tag);
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  // ///////////////////////////////
  // // PRIVATE METHODS BELOW /////
  // /////////////////////////////
  /**
   * Creates a new GUID associated with an account.
   *
   * @param accountGuid
   * @param name
   * @param publicKey
   * @return
   * @throws Exception
   */
  private String guidCreateHelper(GuidEntry accountGuid, String name, PublicKey publicKey) throws Exception {
    long startTime = System.currentTimeMillis();
    byte[] publicKeyBytes = publicKey.getEncoded();
    String publicKeyString = Base64.encodeToString(publicKeyBytes, false);
    JSONObject command = createAndSignCommand(CommandType.AddGuid,
            accountGuid.getPrivateKey(), ADD_GUID,
            GUID, accountGuid.getGuid(),
            NAME, name,
            PUBLIC_KEY, publicKeyString);
    String result = checkResponse(command, sendCommandAndWait(command));
    DelayProfiler.updateDelay("guidCreate", startTime);
    return result;
  }

  /**
   * Register a new account guid with the corresponding alias and the given
   * public key on the GNS server. Returns a new guid.
   *
   * @param alias the alias to register (usually an email address)
   * @param publicKey the public key associate with the account
   * @return guid the GUID generated by the GNS
   * @throws IOException
   * @throws UnsupportedEncodingException
   * @throws ClientException
   * @throws InvalidGuidException if the user already exists
   */
  private String accountGuidCreateHelper(String alias, PublicKey publicKey, PrivateKey privateKey, String password) throws UnsupportedEncodingException,
          IOException, ClientException, InvalidGuidException, NoSuchAlgorithmException {
    JSONObject command;
    long startTime = System.currentTimeMillis();
    if (password != null) {
      command = createAndSignCommand(CommandType.RegisterAccount,
              privateKey, REGISTER_ACCOUNT,
              NAME, alias,
              PUBLIC_KEY, Base64.encodeToString(publicKey.getEncoded(), false),
              PASSWORD, Base64.encodeToString(Password.encryptPassword(password, alias), false));
    } else {
      command = createAndSignCommand(CommandType.RegisterAccountSansPassword,
              privateKey, REGISTER_ACCOUNT,
              NAME, alias,
              PUBLIC_KEY, Base64.encodeToString(publicKey.getEncoded(), false));
    }
    String result = checkResponse(command, sendCommandAndWait(command));
    DelayProfiler.updateDelay("accountGuidCreate", startTime);
    return result;
  }

  private void aclAdd(String accessType, GuidEntry guid, String field, String accesserGuid) throws Exception {
    JSONObject command = createAndSignCommand(CommandType.AclAddSelf,
            guid.getPrivateKey(), ACL_ADD, ACL_TYPE, accessType,
            GUID, guid.getGuid(), FIELD, field,
            ACCESSER, accesserGuid == null ? ALL_USERS : accesserGuid);
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  private void aclRemove(String accessType, GuidEntry guid, String field, String accesserGuid) throws Exception {
    JSONObject command = createAndSignCommand(CommandType.AclRemoveSelf,
            guid.getPrivateKey(), ACL_REMOVE, ACL_TYPE, accessType,
            GUID, guid.getGuid(), FIELD, field,
            ACCESSER, accesserGuid == null ? ALL_USERS : accesserGuid);
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  private JSONArray aclGet(String accessType, GuidEntry guid, String field, String readerGuid) throws Exception {
    JSONObject command = createAndSignCommand(CommandType.AclRetrieve,
            guid.getPrivateKey(), ACL_RETRIEVE, ACL_TYPE, accessType,
            GUID, guid.getGuid(), FIELD, field,
            READER, readerGuid == null ? ALL_USERS : readerGuid);
    String response = sendCommandAndWait(command);
    try {
      return new JSONArray(checkResponse(command, response));
    } catch (JSONException e) {
      throw new ClientException("Invalid ACL list", e);
    }
  }

  // Extended commands
  /**
   * Creates a new field with value being the list. Allows a a different guid as
   * the writer. If the writer is different use addToACL first to allow other
   * the guid to write this field.
   *
   * @param targetGuid
   * @param field
   * @param value
   * @param writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldCreateList(String targetGuid, String field, JSONArray value, GuidEntry writer) throws IOException,
          ClientException {
    JSONObject command = createAndSignCommand(CommandType.CreateList,
            writer.getPrivateKey(), CREATE_LIST, GUID, targetGuid,
            FIELD, field, VALUE, value.toString(), WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);
    checkResponse(command, response);
  }

  /**
   * Appends the values of the field onto list of values or creates a new field
   * with values in the list if it does not exist.
   *
   * @param targetGuid
   * @param field
   * @param value
   * @param writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldAppendOrCreateList(String targetGuid, String field, JSONArray value, GuidEntry writer)
          throws IOException, ClientException {
    JSONObject command = createAndSignCommand(CommandType.AppendOrCreateList,
            writer.getPrivateKey(), APPEND_OR_CREATE_LIST, GUID, targetGuid,
            FIELD, field, VALUE, value.toString(), WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Replaces the values of the field with the list of values or creates a new
   * field with values in the list if it does not exist.
   *
   * @param targetGuid
   * @param field
   * @param value
   * @param writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldReplaceOrCreateList(String targetGuid, String field, JSONArray value, GuidEntry writer)
          throws IOException, ClientException {
    JSONObject command = createAndSignCommand(CommandType.ReplaceOrCreateList,
            writer.getPrivateKey(), REPLACE_OR_CREATE_LIST, GUID, targetGuid,
            FIELD, field, VALUE, value.toString(), WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);
    checkResponse(command, response);
  }

  /**
   * Appends a list of values onto a field.
   *
   * @param targetGuid GUID where the field is stored
   * @param field field name
   * @param value list of values
   * @param writer GUID entry of the writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldAppend(String targetGuid, String field, JSONArray value, GuidEntry writer) throws IOException,
          ClientException {
    JSONObject command = createAndSignCommand(CommandType.AppendListWithDuplication,
            writer.getPrivateKey(), APPEND_LIST_WITH_DUPLICATION, GUID,
            targetGuid, FIELD, field, VALUE, value.toString(), WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Replaces all the values of field with the list of values.
   *
   * @param targetGuid GUID where the field is stored
   * @param field field name
   * @param value list of values
   * @param writer GUID entry of the writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldReplaceList(String targetGuid, String field, JSONArray value, GuidEntry writer) throws IOException,
          ClientException {
    JSONObject command = createAndSignCommand(CommandType.ReplaceList,
            writer.getPrivateKey(), REPLACE_LIST, GUID, targetGuid,
            FIELD, field, VALUE, value.toString(), WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Removes all the values in the list from the field.
   *
   * @param targetGuid GUID where the field is stored
   * @param field field name
   * @param value list of values
   * @param writer GUID entry of the writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldClear(String targetGuid, String field, JSONArray value, GuidEntry writer) throws IOException,
          ClientException {
    JSONObject command = createAndSignCommand(CommandType.RemoveList,
            writer.getPrivateKey(), REMOVE_LIST, GUID, targetGuid,
            FIELD, field, VALUE, value.toString(), WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Removes all values from the field.
   *
   * @param targetGuid GUID where the field is stored
   * @param field field name
   * @param writer GUID entry of the writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldClear(String targetGuid, String field, GuidEntry writer) throws IOException, ClientException {
    JSONObject command = createAndSignCommand(CommandType.Clear,
            writer.getPrivateKey(), CLEAR, GUID, targetGuid,
            FIELD, field, WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Reads all the values for a key from the GNS server for the given guid. The
   * guid of the user attempting access is also needed. Signs the query using
   * the private key of the user associated with the reader guid (unsigned if
   * reader is null).
   *
   * @param guid
   * @param field
   * @param reader if null the field must be readable for all
   * @return a JSONArray containing the values in the field
   * @throws Exception
   */
  public JSONArray fieldReadArray(String guid, String field, GuidEntry reader) throws Exception {
    JSONObject command;
    if (reader == null) {
      command = createCommand(CommandType.ReadArrayUnsigned,
              READ_ARRAY, GUID, guid, FIELD, field);
    } else {
      command = createAndSignCommand(CommandType.ReadArray,
              reader.getPrivateKey(), READ_ARRAY, GUID, guid, FIELD, field,
              READER, reader.getGuid());
    }

    String response = sendCommandAndWait(command);

    return new JSONArray(checkResponse(command, response));
  }

  /**
   * Sets the nth value (zero-based) indicated by index in the list contained in
   * field to newValue. Index must be less than the current size of the list.
   *
   * @param targetGuid
   * @param field
   * @param newValue
   * @param index
   * @param writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldSetElement(String targetGuid, String field, String newValue, int index, GuidEntry writer)
          throws IOException, ClientException {
    JSONObject command = createAndSignCommand(CommandType.Set,
            writer.getPrivateKey(), SET, GUID, targetGuid, FIELD,
            field, VALUE, newValue, N, Integer.toString(index), WRITER,
            writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Sets a field to be null. That is when read field is called a null will be
   * returned.
   *
   * @param targetGuid
   * @param field
   * @param writer
   * @throws IOException
   * @throws InvalidKeyException
   * @throws NoSuchAlgorithmException
   * @throws SignatureException
   * @throws ClientException
   */
  public void fieldSetNull(String targetGuid, String field, GuidEntry writer) throws IOException, InvalidKeyException,
          NoSuchAlgorithmException, SignatureException, ClientException {
    JSONObject command = createAndSignCommand(CommandType.SetFieldNull,
            writer.getPrivateKey(), SET_FIELD_NULL, GUID, targetGuid,
            FIELD, field, WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  //
  // SELECT
  //
  /**
   * Returns all GUIDs that have a field that contains the given value as a
   * JSONArray containing guids.
   *
   * @param field
   * @param value
   * @return a JSONArray containing the guids of all the matched records
   * @throws Exception
   */
  public JSONArray select(String field, String value) throws Exception {
    JSONObject command = createCommand(CommandType.Select,
            SELECT, FIELD, field, VALUE, value);
    String response = sendCommandAndWait(command);

    return new JSONArray(checkResponse(command, response));
  }

  /**
   * If field is a GeoSpatial field queries the GNS server return all the guids
   * that have fields that are within value which is a bounding box specified as a nested
   * JSONArrays of paired tuples: [[LONG_UL, LAT_UL],[LONG_BR, LAT_BR]]
   *
   * @param field
   * @param value - [[LONG_UL, LAT_UL],[LONG_BR, LAT_BR]]
   * @return a JSONArray containing the guids of all the matched records
   * @throws Exception
   */
  public JSONArray selectWithin(String field, JSONArray value) throws Exception {
    JSONObject command = createCommand(CommandType.SelectWithin,
            SELECT, FIELD, field, WITHIN,
            value.toString());
    String response = sendCommandAndWait(command);

    return new JSONArray(checkResponse(command, response));
  }

  /**
   * If field is a GeoSpatial field queries the GNS server and returns all
   * the guids that have fields that are near value which is a point specified
   * as a two element JSONArray: [LONG, LAT]. Max Distance is in meters.
   *
   * @param field
   * @param value - [LONG, LAT]
   * @param maxDistance - distance in meters
   * @return a JSONArray containing the guids of all the matched records
   * @throws Exception
   */
  public JSONArray selectNear(String field, JSONArray value, Double maxDistance) throws Exception {
    JSONObject command = createCommand(CommandType.SelectNear,
            SELECT, FIELD, field, NEAR,
            value.toString(), MAX_DISTANCE, Double.toString(maxDistance));
    String response = sendCommandAndWait(command);

    return new JSONArray(checkResponse(command, response));
  }

  /**
   * Update the location field for the given GUID
   *
   * @param targetGuid
   * @param longitude the GUID longitude
   * @param latitude the GUID latitude
   * @param writer
   * @throws Exception if a GNS error occurs
   */
  public void setLocation(String targetGuid, double longitude, double latitude, GuidEntry writer) throws Exception {
    JSONArray array = new JSONArray(Arrays.asList(longitude, latitude));
    fieldReplaceOrCreateList(targetGuid, LOCATION_FIELD_NAME, array, writer);
  }

  /**
   * Update the location field for the given GUID
   *
   * @param longitude the GUID longitude
   * @param latitude the GUID latitude
   * @param guid the GUID to update
   * @throws Exception if a GNS error occurs
   */
  public void setLocation(GuidEntry guid, double longitude, double latitude) throws Exception {
    setLocation(guid.getGuid(), longitude, latitude, guid);
  }

  /**
   * Get the location of the target GUID as a JSONArray: [LONG, LAT]
   *
   * @param readerGuid the GUID issuing the request
   * @param targetGuid the GUID that we want to know the location
   * @return a JSONArray: [LONGITUDE, LATITUDE]
   * @throws Exception if a GNS error occurs
   */
  public JSONArray getLocation(String targetGuid, GuidEntry readerGuid) throws Exception {
    return fieldReadArray(targetGuid, LOCATION_FIELD_NAME, readerGuid);
  }

  /**
   * Get the location of the target GUID as a JSONArray: [LONG, LAT]
   *
   * @param guid
   * @return a JSONArray: [LONGITUDE, LATITUDE]
   * @throws Exception if a GNS error occurs
   */
  public JSONArray getLocation(GuidEntry guid) throws Exception {
    return fieldReadArray(guid.getGuid(), LOCATION_FIELD_NAME, guid);
  }

  // Active Code
  public void activeCodeClear(String guid, String action, GuidEntry writerGuid) throws ClientException, IOException {
    JSONObject command = createAndSignCommand(CommandType.ClearActiveCode,
            writerGuid.getPrivateKey(), AC_CLEAR,
            GUID, guid, AC_ACTION, action,
            WRITER, writerGuid.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  public void activeCodeSet(String guid, String action, byte[] code, GuidEntry writerGuid)
          throws ClientException, IOException {
    String code64 = Base64.encodeToString(code, true);
    JSONObject command = createAndSignCommand(CommandType.SetActiveCode,
            writerGuid.getPrivateKey(), AC_SET,
            GUID, guid, AC_ACTION, action,
            AC_CODE, code64, WRITER, writerGuid.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  public byte[] activeCodeGet(String guid, String action, GuidEntry readerGuid) throws Exception {
    JSONObject command = createAndSignCommand(CommandType.GetActiveCode,
            readerGuid.getPrivateKey(), AC_GET,
            GUID, guid, AC_ACTION, action,
            READER, readerGuid.getGuid());
    String response = sendCommandAndWait(command);

    String code64String = checkResponse(command, response);
    if (code64String != null) {
      return Base64.decode(code64String);
    } else {
      return null;
    }
  }

  // Extended commands
  /**
   * Creates a new field in the target guid with value being the list.
   *
   * @param target
   * @param field
   * @param value
   * @throws IOException
   * @throws ClientException
   */
  public void fieldCreateList(GuidEntry target, String field, JSONArray value) throws IOException,
          ClientException {
    fieldCreateList(target.getGuid(), field, value, target);
  }

  /**
   * Creates a new one element field with single element value being the string. Allows a a different guid as
   * the writer. If the writer is different use addToACL first to allow other
   * the guid to write this field.
   *
   * @param targetGuid
   * @param field
   * @param value
   * @param writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldCreateOneElementList(String targetGuid, String field, String value, GuidEntry writer) throws IOException,
          ClientException {
    JSONObject command = createAndSignCommand(CommandType.Create,
            writer.getPrivateKey(), CREATE, GUID, targetGuid,
            FIELD, field, VALUE, value, WRITER, writer.getGuid());
    try {

      String response = sendCommandAndWait(command);

      checkResponse(command, response);
    } catch (NullPointerException ne) {
      GNSConfig.getLogger().severe("NPE in field create");
      ne.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Creates a new one element field in the target guid with single element value being the string.
   *
   * @param target
   * @param field
   * @param value
   * @throws IOException
   * @throws ClientException
   */
  public void fieldCreateOneElementList(GuidEntry target, String field, String value) throws IOException,
          ClientException {
    fieldCreateOneElementList(target.getGuid(), field, value, target);
  }

  /**
   * Appends the single value of the field onto list of values or creates a new field
   * with a single value list if it does not exist.
   * If the writer is different use addToACL first to allow other
   * the guid to write this field.
   *
   * @param targetGuid
   * @param field
   * @param value
   * @param writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldAppendOrCreate(String targetGuid, String field, String value, GuidEntry writer)
          throws IOException, ClientException {
    JSONObject command = createAndSignCommand(CommandType.AppendOrCreate,
            writer.getPrivateKey(), APPEND_OR_CREATE, GUID, targetGuid,
            FIELD, field, VALUE, value, WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);
    checkResponse(command, response);
  }

  /**
   * Replaces the values of the field in targetGuid with the single value or creates a new
   * field with a single value list if it does not exist.
   * If the writer is different use addToACL first to allow other
   * the guid to write this field.
   *
   * @param targetGuid
   * @param field
   * @param value
   * @param writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldReplaceOrCreate(String targetGuid, String field, String value, GuidEntry writer)
          throws IOException, ClientException {
    JSONObject command = createAndSignCommand(CommandType.ReplaceOrCreate,
            writer.getPrivateKey(), REPLACE_OR_CREATE, GUID, targetGuid,
            FIELD, field, VALUE, value, WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);
    checkResponse(command, response);
  }

  /**
   * Replaces the values of the field with the list of values or creates a new
   * field with values in the list if it does not exist.
   *
   * @param targetGuid
   * @param field
   * @param value
   * @throws IOException
   * @throws ClientException
   */
  public void fieldReplaceOrCreateList(GuidEntry targetGuid, String field, JSONArray value)
          throws IOException, ClientException {
    fieldReplaceOrCreateList(targetGuid.getGuid(), field, value, targetGuid);
  }

  /**
   * Replaces the values of the field in the target guid with the single value or creates a new
   * field with a single value list if it does not exist.
   *
   * @param target
   * @param field
   * @param value
   * @throws IOException
   * @throws ClientException
   */
  public void fieldReplaceOrCreateList(GuidEntry target, String field, String value)
          throws IOException, ClientException {
    fieldReplaceOrCreate(target.getGuid(), field, value, target);
  }

  /**
   * Replaces all the values of field with the single value.
   * If the writer is different use addToACL first to allow other
   * the guid to write this field.
   *
   * @param targetGuid GUID where the field is stored
   * @param field field name
   * @param value the new value
   * @param writer GUID entry of the writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldReplace(String targetGuid, String field, String value, GuidEntry writer) throws IOException,
          ClientException {
    JSONObject command = createAndSignCommand(CommandType.Replace,
            writer.getPrivateKey(), REPLACE, GUID, targetGuid,
            FIELD, field, VALUE, value, WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Replaces all the values of field in target with with the single value.
   *
   * @param target
   * @param field
   * @param value
   * @throws IOException
   * @throws ClientException
   */
  public void fieldReplace(GuidEntry target, String field, String value) throws IOException,
          ClientException {
    fieldReplace(target.getGuid(), field, value, target);
  }

  /**
   * Replaces all the values of field in target with the list of values.
   *
   * @param target
   * @param field
   * @param value
   * @throws IOException
   * @throws ClientException
   */
  public void fieldReplace(GuidEntry target, String field, JSONArray value) throws IOException,
          ClientException {
    fieldReplaceList(target.getGuid(), field, value, target);
  }

  /**
   * Appends a single value onto a field.
   * If the writer is different use addToACL first to allow other
   * the guid to write this field.
   *
   * @param targetGuid
   * @param field
   * @param value
   * @param writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldAppend(String targetGuid, String field, String value, GuidEntry writer) throws IOException,
          ClientException {
    JSONObject command = createAndSignCommand(CommandType.AppendWithDuplication,
            writer.getPrivateKey(), APPEND_WITH_DUPLICATION, GUID, targetGuid,
            FIELD, field, VALUE, value, WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Appends a single value onto a field in the target guid.
   *
   * @param target
   * @param field
   * @param value
   * @throws IOException
   * @throws ClientException
   */
  public void fieldAppend(GuidEntry target, String field, String value) throws IOException,
          ClientException {
    fieldAppend(target.getGuid(), field, value, target);
  }

  /**
   * Appends a list of values onto a field but converts the list to set removing duplicates.
   * If the writer is different use addToACL first to allow other
   * the guid to write this field.
   *
   * @param targetGuid
   * @param field
   * @param value
   * @param writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldAppendWithSetSemantics(String targetGuid, String field, JSONArray value, GuidEntry writer) throws IOException,
          ClientException {
    JSONObject command = createAndSignCommand(CommandType.AppendList,
            writer.getPrivateKey(), APPEND_LIST, GUID, targetGuid,
            FIELD, field, VALUE, value.toString(), WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Appends a list of values onto a field in target but converts the list to set removing duplicates.
   *
   * @param target
   * @param field
   * @param value
   * @throws IOException
   * @throws ClientException
   */
  public void fieldAppendWithSetSemantics(GuidEntry target, String field, JSONArray value) throws IOException,
          ClientException {
    fieldAppendWithSetSemantics(target.getGuid(), field, value, target);
  }

  /**
   * Appends a single value onto a field but converts the list to set removing duplicates.
   * If the writer is different use addToACL first to allow other
   * the guid to write this field.
   *
   * @param targetGuid
   * @param field
   * @param value
   * @param writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldAppendWithSetSemantics(String targetGuid, String field, String value, GuidEntry writer) throws IOException,
          ClientException {
    JSONObject command = createAndSignCommand(CommandType.Append,
            writer.getPrivateKey(), APPEND, GUID, targetGuid,
            FIELD, field, VALUE, value.toString(), WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Appends a single value onto a field in target but converts the list to set removing duplicates.
   *
   * @param target
   * @param field
   * @param value
   * @throws IOException
   * @throws ClientException
   */
  public void fieldAppendWithSetSemantics(GuidEntry target, String field, String value) throws IOException,
          ClientException {
    fieldAppendWithSetSemantics(target.getGuid(), field, value, target);
  }

  /**
   * Replaces all the first element of field with the value.
   * If the writer is different use addToACL first to allow other
   * the guid to write this field. If writer is null the command
   * is sent unsigned.
   *
   * @param targetGuid
   * @param field
   * @param value
   * @param writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldReplaceFirstElement(String targetGuid, String field, String value, GuidEntry writer) throws IOException,
          ClientException {
    JSONObject command;
    if (writer == null) {
      command = createCommand(CommandType.ReplaceUnsigned,
              REPLACE, GUID, targetGuid,
              FIELD, field, VALUE, value);
    } else {
      command = createAndSignCommand(CommandType.Replace,
              writer.getPrivateKey(), REPLACE, GUID, targetGuid,
              FIELD, field, VALUE, value, WRITER, writer.getGuid());
    }
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Replaces the first element of field in target with the value.
   *
   * @param target
   * @param field
   * @param value
   * @throws IOException
   * @throws ClientException
   */
  public void fieldReplaceFirstElement(GuidEntry target, String field, String value) throws IOException,
          ClientException {
    fieldReplaceFirstElement(target.getGuid(), field, value, target);
  }

  /**
   * Substitutes the value for oldValue in the list of values of a field.
   * If the writer is different use addToACL first to allow other
   * the guid to write this field.
   *
   * @param targetGuid GUID where the field is stored
   * @param field field name
   * @param newValue
   * @param oldValue
   * @param writer GUID entry of the writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldSubstitute(String targetGuid, String field, String newValue,
          String oldValue, GuidEntry writer) throws IOException, ClientException {
    JSONObject command = createAndSignCommand(CommandType.Substitute,
            writer.getPrivateKey(), SUBSTITUTE, GUID,
            targetGuid, FIELD, field, VALUE, newValue,
            OLD_VALUE, oldValue, WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Substitutes the value for oldValue in the list of values of a field in the target.
   *
   * @param target
   * @param field
   * @param newValue
   * @param oldValue
   * @throws IOException
   * @throws ClientException
   */
  public void fieldSubstitute(GuidEntry target, String field, String newValue,
          String oldValue) throws IOException, ClientException {
    fieldSubstitute(target.getGuid(), field, newValue, oldValue, target);
  }

  /**
   * Pairwise substitutes all the values for the oldValues in the list of values of a field.
   * If the writer is different use addToACL first to allow other
   * the guid to write this field.
   *
   *
   * @param targetGuid GUID where the field is stored
   * @param field
   * @param newValue list of new values
   * @param oldValue list of old values
   * @param writer GUID entry of the writer
   * @throws IOException
   * @throws ClientException
   */
  public void fieldSubstitute(String targetGuid, String field,
          JSONArray newValue, JSONArray oldValue, GuidEntry writer) throws IOException, ClientException {
    JSONObject command = createAndSignCommand(CommandType.SubstituteList,
            writer.getPrivateKey(), SUBSTITUTE_LIST, GUID,
            targetGuid, FIELD, field, VALUE, newValue.toString(),
            OLD_VALUE, oldValue.toString(), WRITER, writer.getGuid());
    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  /**
   * Pairwise substitutes all the values for the oldValues in the list of values of a field in the target.
   *
   * @param target
   * @param field
   * @param newValue
   * @param oldValue
   * @throws IOException
   * @throws ClientException
   */
  public void fieldSubstitute(GuidEntry target, String field,
          JSONArray newValue, JSONArray oldValue) throws IOException, ClientException {
    fieldSubstitute(target.getGuid(), field, newValue, oldValue, target);
  }

  /**
   * Reads the first value for a key from the GNS server for the given
   * guid. The guid of the user attempting access is also needed. Signs the
   * query using the private key of the user associated with the reader guid
   * (unsigned if reader is null).
   *
   * @param guid
   * @param field
   * @param reader
   * @return
   * @throws Exception
   */
  public String fieldReadArrayFirstElement(String guid, String field, GuidEntry reader) throws Exception {
    JSONObject command;
    if (reader == null) {
      command = createCommand(CommandType.ReadArrayOneUnsigned,
              READ_ARRAY_ONE, GUID, guid, FIELD, field);
    } else {
      command = createAndSignCommand(CommandType.ReadArrayOne,
              reader.getPrivateKey(), READ_ARRAY_ONE, GUID, guid, FIELD, field,
              READER, reader.getGuid());
    }

    String response = sendCommandAndWait(command);

    return checkResponse(command, response);
  }

  /**
   * Reads the first value for a key in the guid.
   *
   * @param guid
   * @param field
   * @return
   * @throws Exception
   */
  public String fieldReadArrayFirstElement(GuidEntry guid, String field) throws Exception {
    return fieldReadArrayFirstElement(guid.getGuid(), field, guid);
  }

  /**
   * Removes a field in the JSONObject record of the given guid.
   * Signs the query using the private key of the guid.
   * A convenience method™.
   *
   * @param guid
   * @param field
   * @throws IOException
   * @throws InvalidKeyException
   * @throws NoSuchAlgorithmException
   * @throws SignatureException
   * @throws ClientException
   */
  public void fieldRemove(GuidEntry guid, String field) throws IOException, InvalidKeyException,
          NoSuchAlgorithmException, SignatureException, ClientException {
    fieldRemove(guid.getGuid(), field, guid);
  }

  public String adminEnable(String passkey) throws Exception {
    JSONObject command = createCommand(CommandType.Admin,
            ADMIN, PASSKEY, passkey);

    String response = sendCommandAndWait(command);

    return checkResponse(command, response);
  }

  public void parameterSet(String name, Object value) throws Exception {
    JSONObject command = createCommand(CommandType.SetParameter,
            SET_PARAMETER, NAME, name, VALUE, value);

    String response = sendCommandAndWait(command);

    checkResponse(command, response);
  }

  public String parameterGet(String name) throws Exception {
    JSONObject command = createCommand(CommandType.GetParameter,
            GET_PARAMETER, NAME, name);

    String response = sendCommandAndWait(command);

    return checkResponse(command, response);
  }

  @Override
  public void stop() {
    close();
  }
}
