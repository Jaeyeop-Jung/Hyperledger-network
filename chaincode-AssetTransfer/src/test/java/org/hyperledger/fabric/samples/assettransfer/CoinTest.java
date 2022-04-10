package org.hyperledger.fabric.samples.assettransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.owlike.genson.Genson;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class CoinTest {

    private final Genson genson = new Genson();

    @Test
    void createCoin() throws JsonProcessingException {
        HashMap<String, String> coin = new HashMap<>();
        coin.put("init", "-1");
        Asset asset = new Asset("asset1", "blue", 5, "Tomoko", 300, coin);
        ObjectMapper objectMapper = new ObjectMapper();

        String tojson = objectMapper.writeValueAsString(asset);

        Map assetValueMap = objectMapper.readValue(tojson, Map.class);

        Object coin1 = assetValueMap.get("coin");
        HashMap hashMap = objectMapper.convertValue(coin1, HashMap.class);
        hashMap.put("HBC", "100");
        Asset newAsset = new Asset(
                "asset1",
                String.valueOf(assetValueMap.get("color")),
                Integer.parseInt(String.valueOf(assetValueMap.get("size"))),
                String.valueOf(assetValueMap.get("owner")),
                Integer.parseInt(String.valueOf(assetValueMap.get("appraisedValue"))),
                hashMap
        );

        System.out.println(objectMapper.writeValueAsString(newAsset));
    }
}