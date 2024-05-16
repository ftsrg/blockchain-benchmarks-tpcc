/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.chaincode.tpcc.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.experimental.UtilityClass;

/** A convenience facade for a concrete JSON-serializer. */
@UtilityClass
public final class JSON {

  private static final ObjectMapper mapper = JsonMapper.builder().enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY).build();

  /**
   * Serialize an object to a JSON string.
   *
   * @param obj The object to serialize
   * @return The JSON-serialization of <code>obj</code>
   */
  public static String serialize(final Object obj) throws JsonProcessingException {
    return mapper.writeValueAsString(obj);
  }

  /**
   * Deserialize a JSON string into an object.
   *
   * @param json The JSON string to deserialize
   * @param clazz The type of the object to interpret the JSON as
   * @return The resulting object
   */
  public static <T> T deserialize(final String json, final Class<T> clazz) throws JsonProcessingException {
    return mapper.readValue(json, clazz);
  }
}
