/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.example;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import static java.nio.charset.StandardCharsets.UTF_8;

@Contract(name = "TpccJavaContract",
    info = @Info(title = "TpccJava contract",
                description = "My Smart Contract",
                version = "0.0.1",
                license =
                        @License(name = "Apache-2.0",
                                url = ""),
                                contact =  @Contact(email = "tpccJava@example.com",
                                                name = "tpccJava",
                                                url = "http://tpccJava.me")))
@Default
public class TpccJavaContract implements ContractInterface {
    public  TpccJavaContract() {

    }
    @Transaction()
    public boolean tpccJavaExists(Context ctx, String tpccJavaId) {
        byte[] buffer = ctx.getStub().getState(tpccJavaId);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction()
    public void createTpccJava(Context ctx, String tpccJavaId, String value) {
        boolean exists = tpccJavaExists(ctx,tpccJavaId);
        if (exists) {
            throw new RuntimeException("The asset "+tpccJavaId+" already exists");
        }
        TpccJava asset = new TpccJava();
        asset.setValue(value);
        ctx.getStub().putState(tpccJavaId, asset.toJSONString().getBytes(UTF_8));
    }

    @Transaction()
    public TpccJava readTpccJava(Context ctx, String tpccJavaId) {
        boolean exists = tpccJavaExists(ctx,tpccJavaId);
        if (!exists) {
            throw new RuntimeException("The asset "+tpccJavaId+" does not exist");
        }

        TpccJava newAsset = TpccJava.fromJSONString(new String(ctx.getStub().getState(tpccJavaId),UTF_8));
        return newAsset;
    }

    @Transaction()
    public void updateTpccJava(Context ctx, String tpccJavaId, String newValue) {
        boolean exists = tpccJavaExists(ctx,tpccJavaId);
        if (!exists) {
            throw new RuntimeException("The asset "+tpccJavaId+" does not exist");
        }
        TpccJava asset = new TpccJava();
        asset.setValue(newValue);

        ctx.getStub().putState(tpccJavaId, asset.toJSONString().getBytes(UTF_8));
    }

    @Transaction()
    public void deleteTpccJava(Context ctx, String tpccJavaId) {
        boolean exists = tpccJavaExists(ctx,tpccJavaId);
        if (!exists) {
            throw new RuntimeException("The asset "+tpccJavaId+" does not exist");
        }
        ctx.getStub().delState(tpccJavaId);
    }

}
