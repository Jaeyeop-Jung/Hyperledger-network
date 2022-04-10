/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

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

import com.owlike.genson.Genson;

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
     * Creates some initial assets on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) throws JsonProcessingException {
        ChaincodeStub stub = ctx.getStub();

        HashMap<String, String> initCoin = new HashMap<>();
        initCoin.put("init", "-1");

        CreateAsset(ctx, "asset1", "안규보", initCoin, "None", "None", "0");
        CreateAsset(ctx, "asset2", "정재엽", initCoin, "None", "None", "0");
        CreateAsset(ctx, "asset3", "최영창", initCoin, "None", "None", "0");

    }

    /**
     * Creates a new asset on the ledger.
     *
     * @param ctx the transaction context
     * @param assetID the ID of the new asset
     * @param owner the owner of the new asset
     * @param coin the coin of the new asset
     * @param from the from of the new asset
     * @param to the to of the new asset
     * @param amount the amount of the new asset
     * @return the created asset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset CreateAsset(
            final Context ctx,
            final String assetID,
            final String owner,
            final HashMap coin,
            final String from,
            final String to,
            final String amount
    ) throws JsonProcessingException
    {
        ChaincodeStub stub = ctx.getStub();

        if (AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s already exists", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        Asset asset = Asset.of(assetID, owner, coin, from, to, amount);
        String assetJSON = objectMapper.writeValueAsString(asset);
        stub.putStringState(assetID, assetJSON);

        return asset;
    }

    /**
     * Retrieves an asset with the specified ID from the ledger.
     *
     * @param ctx the transaction context
     * @param assetID the ID of the asset
     * @return the asset found on the ledger if there was one
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Asset ReadAsset(final Context ctx, final String assetID) throws JsonProcessingException {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);

        if (assetJSON == null || assetJSON.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Asset asset = objectMapper.readValue(assetJSON, Asset.class);
        return asset;
    }

    /**
     * Updates the properties of an asset on the ledger.
     *
     * @param ctx the transaction context
     * @param assetID the ID of the asset being updated
     * @param owner the owner of the asset being updated
     * @return the transferred asset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset UpdateAsset(
            final Context ctx,
            final String assetID,
            final String owner,
            final HashMap coin,
            final String from,
            final String to,
            final String amount
    ) throws JsonProcessingException
    {
        ChaincodeStub stub = ctx.getStub();

        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Asset newAsset = Asset.of(assetID, owner, coin, from, to, amount);
        String newAssetJSON = objectMapper.writeValueAsString(newAsset);
        stub.putStringState(assetID, newAssetJSON);

        return newAsset;
    }

    /**
     * Deletes asset on the ledger.
     *
     * @param ctx the transaction context
     * @param assetID the ID of the asset being deleted
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
     * Checks the existence of the asset on the ledger
     *
     * @param ctx the transaction context
     * @param assetID the ID of the asset
     * @return boolean indicating the existence of the asset
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AssetExists(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);

        return (assetJSON != null && !assetJSON.isEmpty());
    }

    /**
     * Retrieves all assets from the ledger.
     *
     * @param ctx the transaction context
     * @return array of assets found on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllAssets(final Context ctx) throws JsonProcessingException {
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
