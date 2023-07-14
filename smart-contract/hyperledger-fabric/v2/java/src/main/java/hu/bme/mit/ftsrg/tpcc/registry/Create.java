package hu.bme.mit.ftsrg.tpcc.registry;

import java.nio.charset.StandardCharsets;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ledger.CompositeKey;

import com.google.gson.Gson;

import hu.bme.mit.ftsrg.tpcc.entries.Warehouse;
import hu.bme.mit.ftsrg.tpcc.utils.Common;
import hu.bme.mit.ftsrg.tpcc.utils.ParseUtils;
import hu.bme.mit.ftsrg.tpcc.utils.Common.TABLES;

public class Create {
    static Gson gson = new Gson();
    public static void createWarehouse(Context ctx, Object entry) throws Exception {
        Warehouse warehouse = entry instanceof String ? 
                        ParseUtils.parseWarehouse((String) entry) : (Warehouse) entry;
        createEntry(ctx, TABLES.WAREHOUSE, new String[] {Common.pad(warehouse.w_id)}, entry);
        }
      
    public static void createEntry(Context ctx, String type, String[] keyParts, Object entry)
          throws Exception {
        CompositeKey key = ctx.getStub().createCompositeKey(type, keyParts);
        String entryString = entry instanceof String ? (String) entry : gson.toJson(entry);    
        byte[] buffer = entryString.getBytes(StandardCharsets.UTF_8);
        ctx.getStub().putState(key.toString(), buffer);
    }
    
    
}
