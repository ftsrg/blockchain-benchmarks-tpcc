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

    ChaincodeStubMiddleware(ChaincodeStub nextLayer){
        this.nextLayer = nextLayer;    }

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
        //return;
    }

    @Override
    public void setStateValidationParameter(String key, byte[] value) {
        //return;
    }

    @Override
    public void delState(String key) {
        
    }

    @Override
    public QueryResultsIterator<KeyValue> getStateByRange(String startKey, String endKey) {
        return this.nextLayer.getStateByRange(startKey, endKey);
    }

    @Override
    public QueryResultsIteratorWithMetadata<KeyValue> getStateByRangeWithPagination(String startKey, String endKey,
            int pageSize, String bookmark) {
                
        return this.nextLayer.getStateByRangeWithPagination(startKey, endKey, pageSize, bookmark);
    }

    @Override
    public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(String compositeKey) {
        return this.nextLayer.getStateByPartialCompositeKey(compositeKey);
        
      }

    @Override
    public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(String objectType, String... attributes) {
        return this.nextLayer.getStateByPartialCompositeKey(objectType, attributes);
    }

    @Override
    public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(CompositeKey compositeKey) {
        return this.nextLayer.getStateByPartialCompositeKey(compositeKey);
    }

    @Override
    public QueryResultsIteratorWithMetadata<KeyValue> getStateByPartialCompositeKeyWithPagination(
            CompositeKey compositeKey, int pageSize, String bookmark) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStateByPartialCompositeKeyWithPagination'");
    }

    @Override
    public CompositeKey createCompositeKey(String objectType, String... attributes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createCompositeKey'");
    }

    @Override
    public CompositeKey splitCompositeKey(String compositeKey) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'splitCompositeKey'");
    }

    @Override
    public QueryResultsIterator<KeyValue> getQueryResult(String query) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getQueryResult'");
    }

    @Override
    public QueryResultsIteratorWithMetadata<KeyValue> getQueryResultWithPagination(String query, int pageSize,
            String bookmark) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getQueryResultWithPagination'");
    }

    @Override
    public QueryResultsIterator<KeyModification> getHistoryForKey(String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHistoryForKey'");
    }

    @Override
    public byte[] getPrivateData(String collection, String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPrivateData'");
    }

    @Override
    public byte[] getPrivateDataHash(String collection, String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPrivateDataHash'");
    }

    @Override
    public byte[] getPrivateDataValidationParameter(String collection, String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPrivateDataValidationParameter'");
    }

    @Override
    public void putPrivateData(String collection, String key, byte[] value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'putPrivateData'");
    }

    @Override
    public void setPrivateDataValidationParameter(String collection, String key, byte[] value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setPrivateDataValidationParameter'");
    }

    @Override
    public void delPrivateData(String collection, String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delPrivateData'");
    }

    @Override
    public void purgePrivateData(String collection, String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'purgePrivateData'");
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByRange(String collection, String startKey, String endKey) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPrivateDataByRange'");
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection, String compositeKey) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPrivateDataByPartialCompositeKey'");
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection,
            CompositeKey compositeKey) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPrivateDataByPartialCompositeKey'");
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection, String objectType,
            String... attributes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPrivateDataByPartialCompositeKey'");
    }

    @Override
    public QueryResultsIterator<KeyValue> getPrivateDataQueryResult(String collection, String query) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPrivateDataQueryResult'");
    }

    @Override
    public void setEvent(String name, byte[] payload) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setEvent'");
    }

    @Override
    public ChaincodeEvent getEvent() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEvent'");
    }

    @Override
    public SignedProposal getSignedProposal() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSignedProposal'");
    }

    @Override
    public Instant getTxTimestamp() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTxTimestamp'");
    }

    @Override
    public byte[] getCreator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCreator'");
    }

    @Override
    public Map<String, byte[]> getTransient() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTransient'");
    }

    @Override
    public byte[] getBinding() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBinding'");
    }

    @Override
    public String getMspId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMspId'");
    }
    
}
