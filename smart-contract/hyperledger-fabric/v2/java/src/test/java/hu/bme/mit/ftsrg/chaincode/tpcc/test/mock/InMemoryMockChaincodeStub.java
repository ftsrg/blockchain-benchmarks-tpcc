package hu.bme.mit.ftsrg.chaincode.tpcc.test.mock;

import org.hyperledger.fabric.protos.peer.ChaincodeEvent;
import org.hyperledger.fabric.protos.peer.SignedProposal;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.*;

import java.time.Instant;
import java.util.*;

public final class InMemoryMockChaincodeStub implements ChaincodeStub {

  private final NavigableMap<String, byte[]> db = new TreeMap<String, byte[]>();

  @Override
  public List<byte[]> getArgs() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public List<String> getStringArgs() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public String getFunction() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public List<String> getParameters() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public String getTxId() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public String getChannelId() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public Chaincode.Response invokeChaincode(String chaincodeName, List<byte[]> args, String channel) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public byte[] getState(String key) {
    return db.get(key);
  }

  @Override
  public byte[] getStateValidationParameter(String key) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void putState(String key, byte[] value) {
    db.put(key, value);
  }

  @Override
  public void setStateValidationParameter(String key, byte[] value) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void delState(String key) {
    db.remove(key);
  }

  @Override
  public QueryResultsIterator<KeyValue> getStateByRange(String startKey, String endKey) {
    return iteratorToQRI(db.subMap(startKey, endKey).entrySet().iterator());
  }

  @Override
  public QueryResultsIteratorWithMetadata<KeyValue> getStateByRangeWithPagination(String startKey, String endKey, int pageSize, String bookmark) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(String compositeKey) {
    List<Map.Entry<String, byte[]>> matches = new ArrayList<Map.Entry<String, byte[]>>();
    for (Map.Entry<String, byte[]> entry : db.entrySet()) {
      if (entry.getKey().startsWith(compositeKey)) {
        matches.add(entry);
      }
    }

    return iteratorToQRI(matches.iterator());
  }

  @Override
  public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(String objectType, String... attributes) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(CompositeKey compositeKey) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public QueryResultsIteratorWithMetadata<KeyValue> getStateByPartialCompositeKeyWithPagination(CompositeKey compositeKey, int pageSize, String bookmark) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public CompositeKey createCompositeKey(String objectType, String... attributes) {
    return new CompositeKey(objectType, attributes);
  }

  @Override
  public CompositeKey splitCompositeKey(String compositeKey) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public QueryResultsIterator<KeyValue> getQueryResult(String query) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public QueryResultsIteratorWithMetadata<KeyValue> getQueryResultWithPagination(String query, int pageSize, String bookmark) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public QueryResultsIterator<KeyModification> getHistoryForKey(String key) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public byte[] getPrivateData(String collection, String key) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public byte[] getPrivateDataHash(String collection, String key) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public byte[] getPrivateDataValidationParameter(String collection, String key) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void putPrivateData(String collection, String key, byte[] value) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void setPrivateDataValidationParameter(String collection, String key, byte[] value) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void delPrivateData(String collection, String key) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void purgePrivateData(String collection, String key) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public QueryResultsIterator<KeyValue> getPrivateDataByRange(String collection, String startKey, String endKey) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection, String compositeKey) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection, CompositeKey compositeKey) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection, String objectType, String... attributes) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public QueryResultsIterator<KeyValue> getPrivateDataQueryResult(String collection, String query) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void setEvent(String s, byte[] bytes) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public ChaincodeEvent getEvent() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public SignedProposal getSignedProposal() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public Instant getTxTimestamp() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public byte[] getCreator() {
    // This value was obtained at runtime from the test network
    return new byte[]{10, 7, 79, 114, 103, 49, 77, 83, 80, 18, -11, 5, 45, 45, 45, 45, 45, 66, 69, 71, 73, 78, 32, 67, 69, 82, 84, 73, 70, 73, 67, 65, 84, 69, 45, 45, 45, 45, 45, 10, 77, 73, 73, 67, 65, 106, 67, 67, 65, 97, 105, 103, 65, 119, 73, 66, 65, 103, 73, 85, 100, 51, 74, 104, 53, 49, 49, 121, 110, 90, 47, 74, 90, 82, 107, 72, 119, 85, 116, 118, 73, 118, 79, 116, 99, 113, 52, 119, 67, 103, 89, 73, 75, 111, 90, 73, 122, 106, 48, 69, 65, 119, 73, 119, 10, 99, 122, 69, 76, 77, 65, 107, 71, 65, 49, 85, 69, 66, 104, 77, 67, 86, 86, 77, 120, 69, 122, 65, 82, 66, 103, 78, 86, 66, 65, 103, 84, 67, 107, 78, 104, 98, 71, 108, 109, 98, 51, 74, 117, 97, 87, 69, 120, 70, 106, 65, 85, 66, 103, 78, 86, 66, 65, 99, 84, 68, 86, 78, 104, 10, 98, 105, 66, 71, 99, 109, 70, 117, 89, 50, 108, 122, 89, 50, 56, 120, 71, 84, 65, 88, 66, 103, 78, 86, 66, 65, 111, 84, 69, 71, 57, 121, 90, 122, 69, 117, 90, 88, 104, 104, 98, 88, 66, 115, 90, 83, 53, 106, 98, 50, 48, 120, 72, 68, 65, 97, 66, 103, 78, 86, 66, 65, 77, 84, 10, 69, 50, 78, 104, 76, 109, 57, 121, 90, 122, 69, 117, 90, 88, 104, 104, 98, 88, 66, 115, 90, 83, 53, 106, 98, 50, 48, 119, 72, 104, 99, 78, 77, 106, 81, 119, 78, 84, 77, 120, 77, 84, 81, 122, 77, 122, 65, 119, 87, 104, 99, 78, 77, 106, 85, 119, 78, 84, 77, 120, 77, 84, 81, 48, 10, 79, 68, 65, 119, 87, 106, 65, 104, 77, 81, 56, 119, 68, 81, 89, 68, 86, 81, 81, 76, 69, 119, 90, 106, 98, 71, 108, 108, 98, 110, 81, 120, 68, 106, 65, 77, 66, 103, 78, 86, 66, 65, 77, 84, 66, 87, 70, 107, 98, 87, 108, 117, 77, 70, 107, 119, 69, 119, 89, 72, 75, 111, 90, 73, 10, 122, 106, 48, 67, 65, 81, 89, 73, 75, 111, 90, 73, 122, 106, 48, 68, 65, 81, 99, 68, 81, 103, 65, 69, 112, 112, 113, 116, 86, 47, 97, 81, 107, 102, 113, 52, 118, 120, 109, 112, 76, 97, 102, 84, 48, 88, 109, 110, 100, 79, 49, 110, 83, 121, 108, 100, 102, 87, 56, 104, 122, 43, 56, 68, 10, 81, 122, 66, 99, 84, 103, 87, 105, 67, 67, 55, 48, 120, 74, 122, 43, 76, 110, 51, 97, 90, 89, 50, 66, 121, 83, 85, 78, 68, 51, 118, 76, 104, 112, 70, 103, 52, 71, 56, 68, 50, 122, 66, 76, 106, 97, 78, 115, 77, 71, 111, 119, 68, 103, 89, 68, 86, 82, 48, 80, 65, 81, 72, 47, 10, 66, 65, 81, 68, 65, 103, 101, 65, 77, 65, 119, 71, 65, 49, 85, 100, 69, 119, 69, 66, 47, 119, 81, 67, 77, 65, 65, 119, 72, 81, 89, 68, 86, 82, 48, 79, 66, 66, 89, 69, 70, 67, 71, 118, 98, 82, 89, 51, 69, 113, 108, 103, 65, 107, 80, 90, 73, 66, 88, 54, 66, 47, 74, 74, 10, 106, 119, 67, 78, 77, 67, 115, 71, 65, 49, 85, 100, 73, 119, 81, 107, 77, 67, 75, 65, 73, 71, 55, 98, 115, 84, 68, 83, 83, 118, 67, 54, 118, 118, 86, 102, 70, 108, 106, 43, 83, 75, 65, 122, 68, 49, 65, 77, 74, 76, 113, 117, 98, 47, 51, 77, 83, 54, 52, 67, 68, 84, 88, 50, 10, 77, 65, 111, 71, 67, 67, 113, 71, 83, 77, 52, 57, 66, 65, 77, 67, 65, 48, 103, 65, 77, 69, 85, 67, 73, 81, 68, 108, 78, 51, 47, 86, 103, 85, 69, 65, 83, 97, 118, 76, 99, 48, 85, 50, 65, 88, 104, 106, 65, 75, 67, 78, 86, 65, 69, 55, 51, 101, 110, 71, 119, 107, 122, 54, 10, 50, 77, 109, 114, 66, 103, 73, 103, 67, 107, 108, 55, 57, 81, 74, 105, 69, 98, 51, 115, 53, 101, 73, 120, 113, 104, 107, 106, 87, 122, 71, 85, 76, 55, 52, 111, 49, 106, 113, 74, 118, 86, 52, 79, 47, 52, 83, 76, 57, 56, 99, 61, 10, 45, 45, 45, 45, 45, 69, 78, 68, 32, 67, 69, 82, 84, 73, 70, 73, 67, 65, 84, 69, 45, 45, 45, 45, 45, 10};
  }

  @Override
  public Map<String, byte[]> getTransient() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public byte[] getBinding() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public String getMspId() {
    throw new UnsupportedOperationException("not implemented");
  }

  private KeyValue entryToKV(Map.Entry<String, byte[]> entry) {
    return new KeyValue() {
      @Override
      public String getKey() {
        return entry.getKey();
      }

      @Override
      public byte[] getValue() {
        return entry.getValue();
      }

      @Override
      public String getStringValue() {
        return Arrays.toString(getValue());
      }
    };
  }

  private QueryResultsIterator<KeyValue> iteratorToQRI(Iterator<Map.Entry<String, byte[]>> iterator) {
    return new QueryResultsIterator<KeyValue>() {
      @Override
      public void close() {
        System.out.println("CLOSE");
        return;
      }

      @Override
      public Iterator<KeyValue> iterator() {
        return new Iterator<KeyValue>() {
          @Override
          public boolean hasNext() {
            return iterator.hasNext();
          }

          @Override
          public KeyValue next() {
            return entryToKV(iterator.next());
          }
        };
      }
    };
  }
}
