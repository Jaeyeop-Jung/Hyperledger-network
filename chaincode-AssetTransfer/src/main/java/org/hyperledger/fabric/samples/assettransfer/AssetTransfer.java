/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

@Contract(
        name = "basic",
        info = @Info(
                title = "Asset Transfer",
                description = "The hyperlegendary asset transfer",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "a.transfer@example.com",
                        name = "Adrian Transfer",
                        url = "https://hyperledger.example.com")))
@Default
public final class AssetTransfer implements ContractInterface {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    private enum AssetTransferErrors {
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS
    }


    /**
     * methodName : InitLedger
     * author : 공용
     * description : 체인코드 테스트용 메소드
     *
     * @param ctx the ctx
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {

        HashMap<String, String> initCoin = new HashMap<>();

        CreateAsset(ctx, "asset1", "안규보", initCoin, "None", "None", "0");
        CreateAsset(ctx, "asset2", "정재엽", initCoin, "None", "None", "0");
        CreateAsset(ctx, "asset3", "최영창", initCoin, "None", "None", "0");
    }

    /**
     * methodName : CreateAsset
     * author : 공용
     * description : Asset 생성
     *
     * @param ctx     the ctx
     * @param assetId the asset id
     * @param owner   the owner
     * @param coin    the coin
     * @param sender    the sender
     * @param receiver      the receiver
     * @param amount  the amount
     * @return created Asset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset CreateAsset (
            final Context ctx,
            final String assetId,
            final String owner,
            final HashMap coin,
            final String sender,
            final String receiver,
            final String amount
    )
    {

        try {

            ChaincodeStub stub = ctx.getStub();

            if (AssetExists(ctx, assetId)) {
                String errorMessage = String.format("Asset %s already exists", assetId);
                System.out.println(errorMessage);
                throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
            }

            Asset asset = Asset.of(assetId, owner, coin, sender, receiver, amount);
            String assetJSON = objectMapper.writeValueAsString(asset);
            stub.putStringState(assetId, assetJSON);

            return asset;

        } catch (JsonProcessingException e) {
            System.out.println("Object to Json Exception: " + e.getMessage());
        }

        return null;
    }


    /**
     * methodName : GetAsset
     * author : 공용
     * description : Asset 읽기
     *
     * @param ctx     the ctx
     * @param assetId the asset id
     * @return the asset
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Asset GetAsset(final Context ctx, final String assetId) {

        try{

            ChaincodeStub stub = ctx.getStub();
            String assetJSON = stub.getStringState(assetId);

            if (assetJSON == null || assetJSON.isEmpty()) {
                String errorMessage = String.format("Asset %s does not exist", assetId);
                System.out.println(errorMessage);
                throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
            }

            Asset asset = objectMapper.readValue(assetJSON, Asset.class);

            return asset;

        } catch (JsonProcessingException e) {
            System.out.println("Object to Json Exception: " + e.getMessage());
        }

        return null;
    }


    /**
     * methodName : ChangeOwner
     * author : Jaeyeop Jung
     * description : *
     *
     * @param ctx          the ctx
     * @param assetId      the asset id
     * @param newOwnerName the new owner name
     * @return 수정된 Asset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset ChangeOwner(
            final Context ctx,
            final String assetId,
            final String newOwnerName
    )
    {
        try {

            ChaincodeStub stub = ctx.getStub();

            if (!AssetExists(ctx, assetId)) {
                String errorMessage = String.format("Asset %s does not exist", assetId);
                System.out.println(errorMessage);
                throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
            }

            byte[] stateBytes = stub.getState(assetId);
            Map stateMap = objectMapper.readValue(stateBytes, Map.class);

            Asset modifiedAsset = Asset.of(
                    assetId,
                    newOwnerName,
                    objectMapper.convertValue(stateMap.get("coin"), HashMap.class),
                    stateMap.get("sender").toString(),
                    stateMap.get("receiver").toString(),
                    stateMap.get("amount").toString()
            );

            String modifiedAssetJSON = objectMapper.writeValueAsString(modifiedAsset);
            stub.putStringState(assetId, modifiedAssetJSON);

            return modifiedAsset;

        } catch (JsonProcessingException e) {
            System.out.println("Object to Json Exception: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("readValue Exception: " + e.getMessage());
        }

        return null;
    }


    /**
     * methodName : DeleteAsset
     * author : 공용
     * description : Asset 삭제
     *
     * @param ctx     the ctx
     * @param assetID the asset id
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteAsset(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();

        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        stub.delState(assetID);
    }


    /**
     * methodName : AssetExists
     * author : 공용
     * description : Asset 존재 확인
     *
     * @param ctx     the ctx
     * @param assetID the asset id
     * @return the boolean
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AssetExists(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);

        return (assetJSON != null && !assetJSON.isEmpty());
    }


    /**
     * methodName : GetAllAssets
     * author : 공용
     * description : 모든 Asset 조회
     *
     * @param ctx the ctx
     * @return the string
     * @throws JsonProcessingException the json processing exception
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllAssets(final Context ctx) {

        try {

            ChaincodeStub stub = ctx.getStub();

            List<Asset> queryResults = new ArrayList<>();

            // To retrieve all assets from the ledger use getStateByRange with empty startKey & endKey.
            // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
            // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
            // then getStateByRange will retrieve asset with keys between asset0 (inclusive) and asset9 (exclusive) in lexical order.
            QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

            for (KeyValue result: results) {
                Asset asset = objectMapper.readValue(result.getStringValue(), Asset.class);
                queryResults.add(asset);
                System.out.println(asset.toString());
            }

            return objectMapper.writeValueAsString(queryResults);

        } catch (JsonProcessingException e) {
            System.out.println("Object to Json Exception: " + e.getMessage());
        }

        return null;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String CreateCoin(final Context ctx, final String assetID, final String coinName) throws JsonProcessingException {
        ChaincodeStub stub = ctx.getStub();

        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        byte[] state = stub.getState(assetID);
        String stateToString = new String(state, StandardCharsets.UTF_8);

        Map assetValueMap = objectMapper.readValue(stateToString, Map.class);

        HashMap coin = objectMapper.convertValue(assetValueMap.get("coin"), HashMap.class);
        coin.put(coinName, 0);

        Asset newAsset = Asset.of(
                assetID,
                String.valueOf(assetValueMap.get("owner")),
                coin,
                String.valueOf(assetValueMap.get("from")),
                String.valueOf(assetValueMap.get("to")),
                String.valueOf(assetValueMap.get("amount"))
        );
        String newAssetJSON = objectMapper.writeValueAsString(newAsset);
        stub.putStringState(assetID, newAssetJSON);

        return newAssetJSON;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset UpdateCoin(
            final Context ctx,
            final String assetID,
            final String coinName,
            final String coinValue
    ) throws JsonProcessingException {
        ChaincodeStub stub = ctx.getStub();

        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        byte[] state = stub.getState(assetID);

        String stateToString = new String(state, StandardCharsets.UTF_8);

        Map assetValueMap = objectMapper.readValue(stateToString, Map.class);

        HashMap coin = objectMapper.convertValue(assetValueMap.get(coinName), HashMap.class);

        coin.replace(coinName, coinValue);

        Asset newAsset = Asset.of(
                assetID,
                String.valueOf(assetValueMap.get("owner")),
                coin,
                String.valueOf(assetValueMap.get("from")),
                String.valueOf(assetValueMap.get("to")),
                String.valueOf(assetValueMap.get("amount"))
        );

        String newAssetJSON = objectMapper.writeValueAsString(newAsset);
        stub.putStringState(assetID, newAssetJSON);

        return newAsset;
    }


    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetHistoryForAssedId(final Context ctx, final String assetId) throws Exception {
        ChaincodeStub stub = ctx.getStub();

        Map<Long, String> response = new HashMap<>();

        QueryResultsIterator<KeyModification> history = stub.getHistoryForKey(assetId);
        if (history == null) {
            String errorMessage = String.format("Product %s does not exist", assetId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, "Incorrect AssetId");
        }

        long cnt = 1;
        for (KeyModification keyModification : history) {
            String value = "TIMESTAMP = " + keyModification.getTimestamp() + " TxId = " + keyModification.getTxId() + " Value = " + keyModification.getStringValue();
            response.put(cnt++, value);
        }
        history.close();

        return objectMapper.writeValueAsString(response);
    }
}
