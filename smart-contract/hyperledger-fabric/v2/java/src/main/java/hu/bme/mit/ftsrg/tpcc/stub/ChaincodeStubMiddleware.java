package hu.bme.mit.ftsrg.tpcc.stub;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.hyperledger.fabric.protos.peer.ChaincodeEvent;
import org.hyperledger.fabric.protos.peer.SignedProposal;
import org.hyperledger.fabric.shim.Chaincode.Response;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;

public class ChaincodeStubMiddleware implements ChaincodeStub {

  ChaincodeStubMiddleware(ChaincodeStub nextLayer) {
    this.nextLayer = nextLayer;
  }

  private ChaincodeStub nextLayer;

  @Override
  public List<byte[]> getArgs() {

    return this.nextLayer.getArgs();
  }

  @Override
  public List<String> getStringArgs() {
    return this.nextLayer.getStringArgs();
  }

  @Override
  public String getFunction() {
    return this.nextLayer.getFunction();
  }

  @Override
  public List<String> getParameters() {
    return this.nextLayer.getParameters();
  }

  @Override
  public String getTxId() {
    return this.nextLayer.getTxId();
  }

  @Override
  public String getChannelId() {
    return this.nextLayer.getChannelId();
  }

  @Override
  public Response invokeChaincode(String chaincodeName, List<byte[]> args, String channel) {
    return this.nextLayer.invokeChaincode(chaincodeName, args, channel);
  }

  @Override
  public byte[] getState(String key) {
    return this.nextLayer.getState(key);
  }

  @Override
  public byte[] getStateValidationParameter(String key) {
    return this.nextLayer.getStateValidationParameter(key);
  }

  @Override
  public void putState(String key, byte[] value) {
    // return;
  }

  @Override
  public void setStateValidationParameter(String key, byte[] value) {
    // return;
  }

  @Override
  public void delState(String key) {}

  @Override
  public QueryResultsIterator<KeyValue> getStateByRange(String startKey, String endKey) {
    return this.nextLayer.getStateByRange(startKey, endKey);
  }

  @Override
  public QueryResultsIteratorWithMetadata<KeyValue> getStateByRangeWithPagination(
      String startKey, String endKey, int pageSize, String bookmark) {

    return this.nextLayer.getStateByRangeWithPagination(startKey, endKey, pageSize, bookmark);
  }

  @Override
  public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(String compositeKey) {
    return this.nextLayer.getStateByPartialCompositeKey(compositeKey);
  }

  @Override
  public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(
      String objectType, String... attributes) {
    return this.nextLayer.getStateByPartialCompositeKey(objectType, attributes);
  }

  @Override
  public QueryResultsIteratorWithMetadata<KeyValue> getStateByPartialCompositeKeyWithPagination(
      CompositeKey compositeKey, int pageSize, String bookmark) {
    return this.nextLayer.getStateByPartialCompositeKeyWithPagination(
        compositeKey, pageSize, bookmark);
  }

  @Override
  public CompositeKey createCompositeKey(String objectType, String... attributes) {
    return this.nextLayer.createCompositeKey(objectType, attributes);
  }

  @Override
  public CompositeKey splitCompositeKey(String compositeKey) {
    return this.nextLayer.splitCompositeKey(compositeKey);
  }

  @Override
  public QueryResultsIterator<KeyValue> getQueryResult(String query) {
    return this.nextLayer.getQueryResult(query);
  }

  @Override
  public QueryResultsIteratorWithMetadata<KeyValue> getQueryResultWithPagination(
      String query, int pageSize, String bookmark) {
    return this.nextLayer.getQueryResultWithPagination(query, pageSize, bookmark);
  }

  @Override
  public QueryResultsIterator<KeyModification> getHistoryForKey(String key) {
    return this.getHistoryForKey(key);
  }

  @Override
  public byte[] getPrivateData(String collection, String key) {
    return this.nextLayer.getPrivateData(collection, key);
  }

  @Override
  public byte[] getPrivateDataHash(String collection, String key) {
    return this.nextLayer.getPrivateDataHash(collection, key);
  }

  @Override
  public byte[] getPrivateDataValidationParameter(String collection, String key) {
    return this.nextLayer.getPrivateDataValidationParameter(collection, key);
  }

  @Override
  public void putPrivateData(String collection, String key, byte[] value) {}

  @Override
  public void setPrivateDataValidationParameter(String collection, String key, byte[] value) {}

  @Override
  public void delPrivateData(String collection, String key) {}

  @Override
  public void purgePrivateData(String collection, String key) {}

  @Override
  public QueryResultsIterator<KeyValue> getPrivateDataByRange(
      String collection, String startKey, String endKey) {
    return this.nextLayer.getPrivateDataByRange(collection, startKey, endKey);
  }

  @Override
  public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(
      String collection, String compositeKey) {
    return this.nextLayer.getPrivateDataByPartialCompositeKey(collection, compositeKey);
  }

  @Override
  public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(
      String collection, CompositeKey compositeKey) {
    return this.nextLayer.getPrivateDataByPartialCompositeKey(collection, compositeKey);
  }

  @Override
  public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(
      String collection, String objectType, String... attributes) {
    return this.nextLayer.getPrivateDataByPartialCompositeKey(collection, objectType, attributes);
  }

  @Override
  public QueryResultsIterator<KeyValue> getPrivateDataQueryResult(String collection, String query) {
    return this.nextLayer.getPrivateDataQueryResult(collection, query);
  }

  @Override
  public void setEvent(String name, byte[] payload) {}

  @Override
  public ChaincodeEvent getEvent() {
    return this.nextLayer.getEvent();
  }

  @Override
  public SignedProposal getSignedProposal() {
    return this.nextLayer.getSignedProposal();
  }

  @Override
  public Instant getTxTimestamp() {
    return this.nextLayer.getTxTimestamp();
  }

  @Override
  public byte[] getCreator() {
    return this.nextLayer.getCreator();
  }

  @Override
  public Map<String, byte[]> getTransient() {
    return this.nextLayer.getTransient();
  }

  @Override
  public byte[] getBinding() {
    return this.nextLayer.getBinding();
  }

  @Override
  public String getMspId() {
    return this.nextLayer.getMspId();
  }
}
