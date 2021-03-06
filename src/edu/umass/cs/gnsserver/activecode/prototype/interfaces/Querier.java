package edu.umass.cs.gnsserver.activecode.prototype.interfaces;

import org.json.JSONObject;

import edu.umass.cs.gnsserver.activecode.prototype.ActiveException;
import edu.umass.cs.gnsserver.utils.ValuesMap;

/**
 * This interface is used for active code to send back queries
 * to GNS from its worker. It contains the following interfaces:
 * readGuid
 * writeGuid
 * @author gaozy
 *
 */
public interface Querier {
	
	/**
	 * This method allows customer's active code to read some other guid's value.
	 * It allows querierGuid to read the values from the fields of queriedGuid.
	 * 
	 * <p>Queried guid must be provided for ACL check. The implementation also needs
	 * to provide the querier guid.
	 * A user could query all fields by setting field to null, but it is not guaranteed that
	 * every read could get a value to return as the field may not exist or
	 * the read fails the ACL check for some field. GNS will return as many 
	 * values as possible of fields being read from the queriedGuid, and put the 
	 * values into the returned map object.
	 * 
	 * <p>To implement this method, a ttl value is also needed as the depth constrain
	 * may apply. But the ttl value should be provided by ActiveQuerier, but not
	 * user's code.
	 * 
	 * @param queriedGuid
	 * @param field
	 * @return the ValuesMap read from the field of the guid
	 * @throws ActiveException throws an exception if any parameter is null or response indicates the query fails
	 */
	public JSONObject readGuid(String queriedGuid, String field) throws ActiveException;
	
	
	/**
	 * This method allows customer's active code to update a field of some other guid.
	 * It allows querierGuid to write value into a field of the queriedGuid.
	 * 
	 * <p>Queried guid and field must be provided 
	 * for ACL check. GNS returns true if the write operation succeeds on the GNS side, 
	 * otherwise it returns false.
	 * 
	 * <p>To implement this method, a ttl value is also needed as the depth constrain
	 * may apply. But the ttl value should be provided by ActiveQuerier, but not
	 * user's code.
	 * 
	 * @param queriedGuid
	 * @param field
	 * @param value
	 * @throws ActiveException throws an exception if any parameter is null or response indicates the query fails
	 */
	public void writeGuid(String queriedGuid, String field, JSONObject value) throws ActiveException;
	
	/**
	 * This method allows customer's active code to look up a public key of a targetGuid.
	 * 
	 * @param targetGuid
	 * @return a JSONObject object that contains the targetGuid's public key with the key {@ActiveCode.PUBLICKEY_FIELD}.
	 * @throws ActiveException throws an ActiveException if the targetGuid does not exist.
	 */
	public JSONObject lookupPublicKeyForGuid(String targetGuid) throws ActiveException;
}
