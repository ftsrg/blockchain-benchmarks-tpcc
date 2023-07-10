package hu.bme.mit.ftsrg.tpcc.entities;


import java.nio.charset.StandardCharsets;
import hu.bme.mit.ftsrg.tpcc.utils.Common;
//import java.util.Arrays;

import org.hyperledger.fabric.contract.annotation.DataType;

import com.google.gson.Gson;

// import hu.bme.mit.ftsrg.tpcc.utils.JsonUtils;

@DataType()
public class EntityBase implements EntityInterface{
    Gson gson = new Gson();

    @Override
    public String getType() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getType'");
    }

    @Override
    public String[] getKeyParts() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getKeyParts'");
    }

    @Override
    public byte[] toBuffer() {
        //return StandardCharsets.UTF_8.encode(gson.toJson(this)).array();
        //return gson.toJson(this).getBytes(StandardCharsets.UTF_8);
        
        String entityToJson = gson.toJson(this);
        return entityToJson.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void fromBuffer(byte[] buffer) {
        this.fromJson(new String(buffer, StandardCharsets.UTF_8));
    }

    @Override
    public String toJson() {
        return gson.toJson(this);
        
    }

    @Override
    public void fromJson(String json) {
        //Object.assign(this, Common.robustJsonParse(json));
        EntityBase entity = new EntityBase();
        entity = (EntityBase) Common.robustJsonParse(json);
        
    }
}
