package hu.bme.mit.ftsrg.tpcc.registry;

import com.google.gson.Gson;
import hu.bme.mit.ftsrg.tpcc.entries.Warehouse;
import hu.bme.mit.ftsrg.tpcc.utils.Common;
import hu.bme.mit.ftsrg.tpcc.utils.Common.TABLES;
import hu.bme.mit.ftsrg.tpcc.utils.ParseUtils;
import java.nio.charset.StandardCharsets;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ledger.CompositeKey;

public class Create {
  static Gson gson = new Gson();

  public static Warehouse getWarehouse(Context ctx, int w_id) throws Exception {
    String entry = getEntry(ctx, TABLES.WAREHOUSE, new String[] {Common.pad(w_id)});
    if (entry == null) {
      throw new Exception("Could not retrieve Warehouse(" + w_id + ")");
    }
    return entry != null ? ParseUtils.parseWarehouse(entry) : null;
  }

  public static String getEntry(Context ctx, String type, String[] keyParts) throws Exception {
    CompositeKey key = ctx.getStub().createCompositeKey(type, keyParts);   
    byte[] data = ctx.getStub().getState(key.toString());
  
    if (data.length > 0) {
      String entry = new String(data, StandardCharsets.UTF_8);
      return entry;
    }
    return null;
  }



  
  public Object getWarehouseEntry(){
    
    return null;
  }
}
