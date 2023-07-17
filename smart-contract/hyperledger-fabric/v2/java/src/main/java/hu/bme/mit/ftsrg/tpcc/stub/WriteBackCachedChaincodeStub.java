package hu.bme.mit.ftsrg.tpcc.stub;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.hyperledger.fabric.shim.ChaincodeStub;

public class WriteBackCachedChaincodeStub extends ChaincodeStubMiddlewareBase {
  private Map<String, CachedItem> cache;

  WriteBackCachedChaincodeStub(ChaincodeStub nextLayer) {
    super(nextLayer);
    this.cache = new HashMap<String, CachedItem>();
  }

  public byte[] read(String key) {
    CachedItem cached = cache.get(key);

    // New read, add to cache
    if (cached == null) {
      byte[] value = this.nextLayer.getState(key);
      cached = new CachedItem(key, value);
      cache.put(key, cached);
    }

    // Already marked for deletion
    if (cached.isToDelete()) {
      return null;
    }

    return cached.getValue();
  }

  public void write(String key, byte[] value) {
    CachedItem cached = cache.get(key);

    // Blind write!
    if (cached == null) {
      cached = new CachedItem(key, null); // Initial value set later
      cache.put(key, cached);
    }

    if (cached.isToDelete()) {
      throw new Error("Ledger entry " + key + " is already marked for deletion");
    }

    cached.setValue(value); // Sets the dirty flag if needed
  }

  public void delete(String key) {
    CachedItem cached = cache.get(key);

    // Blind delete!
    if (cached == null) {
      cached = new CachedItem(key, null);
      cache.put(key, cached);
    }

    cached.delete();
  }

  public void dispose() {
    for (Map.Entry<String, CachedItem> entry : cache.entrySet()) {
      CachedItem item = entry.getValue();

      if (item == null || !item.isDirty() || item.getValue() == null) {
        continue;
      }

      if (item.isToDelete()) {
        this.nextLayer.delState(item.getKey());
      } else {
        this.nextLayer.putState(item.getKey(), item.getValue());
      }
    }
  }
}

class CachedItem {
  private String _key;
  private byte[] _value;
  private boolean _toDelete;
  private boolean _isDirty;

  CachedItem(String key, byte[] value) {
    this._key = key;
    this._value = value;
    this._toDelete = false;
    this._isDirty = false;
  }

  public String getKey() {
    return this._key;
  }

  public byte[] getValue() {
    return this._value;
  }

  public void setValue(byte[] value) {
    if (this._value == null && value == null) {
      return;
    }

    if (this._value != null && value != null && Arrays.equals(this._value, value)) {
      return;
    }

    this._value = value;
    this._isDirty = true;
  }

  public boolean isDirty() {
    return this._isDirty;
  }

  public boolean isToDelete() {
    return this._toDelete;
  }

  public void delete() {
    if (!this._toDelete) {
      this._toDelete = true;
      this._isDirty = true;
    }
  }

  public boolean hasValue() {
    return this._value != null;
  }
}
