/* SPDX-License-Identifier: Apache-2.0 */

package hu.bme.mit.ftsrg.chaincode.tpcc.middleware;

import hu.bme.mit.ftsrg.chaincode.dataaccess.ChaincodeStubMiddlewareBase;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.hyperledger.fabric.shim.ChaincodeStub;

public final class WriteBackCachedChaincodeStubMiddleware extends ChaincodeStubMiddlewareBase {

  private final Map<String, CachedItem> cache = new HashMap<>();

  WriteBackCachedChaincodeStubMiddleware(final ChaincodeStub next) {
    super(next);
  }

  @Override
  public byte[] getState(final String key) {
    CachedItem cached = cache.get(key);

    // New read, add to cache
    if (cached == null) {
      final byte[] value = this.nextLayer.getState(key);
      cached = new CachedItem(key, value);
      cache.put(key, cached);
    }

    // Already marked for deletion
    if (cached.isToDelete()) return null;

    return cached.getValue();
  }

  @Override
  public void putState(final String key, final byte[] value) {
    CachedItem cached = cache.get(key);

    // Blind write!
    if (cached == null) {
      cached = new CachedItem(key, null); // Initial value set later
      cache.put(key, cached);
    }

    if (cached.isToDelete())
      throw new Error("Ledger entry " + key + " is already marked for deletion");

    cached.setValue(value); // Sets the dirty flag if needed
  }

  @Override
  public void delState(final String key) {
    CachedItem cached = cache.get(key);

    // Blind delete!
    if (cached == null) {
      cached = new CachedItem(key, null);
      cache.put(key, cached);
    }

    cached.delete();
  }

  public void dispose() {
    for (final Map.Entry<String, CachedItem> entry : cache.entrySet()) {
      final CachedItem item = entry.getValue();

      if (item == null || !item.isDirty() || item.getValue() == null) continue;

      if (item.isToDelete()) this.nextLayer.delState(item.getKey());
      else this.nextLayer.putState(item.getKey(), item.getValue());
    }
  }

  private static final class CachedItem {

    private final String key;
    private byte[] value;
    private boolean toDelete = false;
    private boolean dirty = false;

    CachedItem(final String key, final byte[] value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return this.key;
    }

    public byte[] getValue() {
      return this.value;
    }

    public void setValue(final byte[] value) {
      if (Arrays.equals(this.value, value)) return;

      this.value = value;
      this.dirty = true;
    }

    public boolean isDirty() {
      return this.dirty;
    }

    public boolean isToDelete() {
      return this.toDelete;
    }

    public void delete() {
      if (!this.toDelete) {
        this.toDelete = true;
        this.dirty = true;
      }
    }

    public boolean hasValue() {
      return this.value != null;
    }
  }
}
