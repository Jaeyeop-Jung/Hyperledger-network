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
import exception.*;
import io.grpc.netty.shaded.io.netty.util.internal.StringUtil;
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

    /**
     * methodName : InitLedger
     * author : 공용
     * description : 체인코드 테스트용 메소드
     *
     * @param ctx the ctx
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {

        CreateAsset(ctx, "asset1", "안규보");
        CreateAsset(ctx, "asset2", "정재엽");
        CreateAsset(ctx, "asset3", "최영창");

    }

    /**
     * methodName : AssetExists
     * author : 공용
     * description : Asset 존재 확인
     *
     * @param ctx     the ctx
     * @param assetId the asset id
     * @return the boolean
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AssetExists(final Context ctx, final String assetId) {
        try {
            if (assetId == null || assetId.contains(" ") || assetId.isEmpty() || assetId.isBlank()) {
                String errorMessage = "assetId is null or empty or blank";
                throw new EmptyValueException(errorMessage);
            }

            ChaincodeStub stub = ctx.getStub();
            String assetJSON = stub.getStringState(assetId);

            if (assetJSON == null || assetJSON.isEmpty()) {
                return false;
            }

            return true;
        } catch (EmptyValueException e){
            System.out.println(e.getMessage());
        }

        return false;
    }

    /**
     * methodName : CreateAsset
     * author : 공용
     * description : Asset 생성
     *
     * @param ctx     the ctx
     * @param assetId the asset id
     * @param owner   the owner
     * @return created Asset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset CreateAsset (
            final Context ctx,
            final String assetId,
            final String owner
    )
    {

        try {

            ChaincodeStub stub = ctx.getStub();

            if(AssetExists(ctx, assetId)){
                String errorMessage = String.format("Asset %s already exists", assetId);
                throw new AlreadyExistAssetException(errorMessage);
            }

            HashMap<String, String> coin = new HashMap<>();

            Asset asset = Asset.of(assetId, owner, coin, null, null, null);
            String assetJSON = objectMapper.writeValueAsString(asset);
            stub.putStringState(assetId, assetJSON);

            return asset;

        } catch (AlreadyExistAssetException e){
            System.out.println(e.getMessage());
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
            if(!AssetExists(ctx, assetId)){
                String errorMessage = String.format("Asset %s does not exist", assetId);
                throw new AssetNotFoundException(errorMessage);
            }

            ChaincodeStub stub = ctx.getStub();
            String assetJSON = stub.getStringState(assetId);

            Asset asset = objectMapper.readValue(assetJSON, Asset.class);

            return asset;

        } catch (AssetNotFoundException e){
            System.out.println(e.getMessage());
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
     * @param newOwner the new owner name
     * @return 수정된 Asset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset ChangeOwner(
            final Context ctx,
            final String assetId,
            final String newOwner
    )
    {
        try {
            if(!AssetExists(ctx, assetId)){
                String errorMessage = String.format("Asset %s does not exist", assetId);
                throw new AssetNotFoundException(errorMessage);
            }

            ChaincodeStub stub = ctx.getStub();

            Asset asset = objectMapper.readValue(stub.getStringState(assetId), Asset.class);
            asset.changeOwner(newOwner);

            stub.putStringState(assetId, objectMapper.writeValueAsString(asset));

            return asset;

        } catch (AssetNotFoundException e){
            System.out.println(e.getMessage());
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
     * @param assetId the asset id
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public boolean DeleteAsset(final Context ctx, final String assetId) {
        try {
            if (!AssetExists(ctx, assetId)) {
                String errorMessage = String.format("Asset %s does not exist", assetId);
                throw new AssetNotFoundException(errorMessage);
            }

            ChaincodeStub stub = ctx.getStub();

            stub.delState(assetId);

            return true;
        } catch (AssetNotFoundException e){
            System.out.println(e.getMessage());
        }

        return false;
    }


    /**
     * methodName : GetAllAssets
     * author : 공용
     * description : 모든 Asset 조회
     *
     * @param ctx the ctx
     * @return the string
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
            }

            return objectMapper.writeValueAsString(queryResults);

        } catch (JsonProcessingException e) {
            System.out.println("Object to Json Exception: " + e.getMessage());
        }

        return null;
    }

    /**
     * methodName : CoinExists
     * author : Jaeyeop Jung
     * description : 현재 존재하는 코인 확인
     *
     * @param ctx      the ctx
     * @param coinName the coin name
     * @return the exists
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean CoinExists(final Context ctx, final String coinName) {
        try {
            if(coinName == null || coinName.contains(" ") || coinName.isEmpty() || coinName.isBlank()){
                String errorMessage = "coinName is null or empty or blank";
                throw new EmptyValueException(errorMessage);
            }

            ChaincodeStub stub = ctx.getStub();

            Asset root = objectMapper.readValue(stub.getStringState("asset1"), Asset.class);
            if(!root.getCoin().containsKey(coinName)){
                return false;
            }

            return true;

        } catch (EmptyValueException e){
            System.out.println(e.getMessage());
        } catch (JsonProcessingException e){
            System.out.println(e.getMessage());
        }

        return false;
    }

    /**
     * methodName : CreateCoin
     * author : Jaeyeop Jung
     * description : 모든 Asset의 코인 생성
     *
     * @param ctx      the ctx
     * @param coinName the coin name
     * @return 생성 여부
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public boolean CreateCoin(final Context ctx, final String coinName) {
        try {

            if(CoinExists(ctx, coinName)){
                String errorMessage = String.format("Coin %s is already exists", coinName);
                throw new AlreadyExistsCoinException(errorMessage);
            }

            ChaincodeStub stub = ctx.getStub();

            QueryResultsIterator<KeyValue> assetIter = stub.getStateByRange("", "");

            for (KeyValue keyValue : assetIter) {
                Asset asset = objectMapper.readValue(keyValue.getStringValue(), Asset.class);
                asset.createCoin(coinName);
                stub.putStringState(asset.getAssetId(), objectMapper.writeValueAsString(asset));
            }

            return true;

        } catch (AlreadyExistsCoinException e){
            System.out.println(e.getMessage());
        } catch (JsonProcessingException e) {
            System.out.println("Object to Json Exception: " + e.getMessage());
        }

        return false;
    }

    /**
     * methodName : RemoveCoin
     * author : Jaeyeop Jung
     * description : 모든 Asset의 코인 삭제
     *
     * @param ctx      the ctx
     * @param coinName the coin name
     * @return the boolean
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public boolean RemoveCoin(final Context ctx, final String coinName) {
        try {
            if(!CoinExists(ctx, coinName)){
                String errorMessage = String.format("Coin %s is does not exists", coinName);
                throw new CoinNotFoundException(errorMessage);
            }

            ChaincodeStub stub = ctx.getStub();

            QueryResultsIterator<KeyValue> assetIdIter = stub.getStateByRange("", "");

            for (KeyValue keyValue : assetIdIter) {
                Asset asset = objectMapper.readValue(keyValue.getStringValue(), Asset.class);
                asset.removeCoin(coinName);
                stub.putStringState(asset.getAssetId(), objectMapper.writeValueAsString(asset));
            }

            return true;

        } catch (CoinNotFoundException e){
            System.out.println(e.getMessage());
        } catch (JsonProcessingException e) {
            System.out.println("Object to Json Exception: " + e.getMessage());
        }

        return false;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public boolean UpdateAllAssetCoin(
            final Context ctx,
            final String coinName,
            final String coinValue
    ) {
        try {
            if(!CoinExists(ctx, coinName)){
                String errorMessage = String.format("Coin %s is does not exists", coinName);
                throw new CoinNotFoundException(errorMessage);
            }

            ChaincodeStub stub = ctx.getStub();

            QueryResultsIterator<KeyValue> assetIdIter = stub.getStateByRange("", "");

            for (KeyValue keyValue : assetIdIter) {
                Asset asset = objectMapper.readValue(keyValue.getStringValue(), Asset.class);
                asset.modifyCoinValue(null, null, coinName, coinValue);
                stub.putStringState(asset.getAssetId(), objectMapper.writeValueAsString(asset));
            }

            return true;

        } catch (CoinNotFoundException e){
            System.out.println(e.getMessage());
        } catch (JsonProcessingException e) {
            System.out.println("Object to Json Exception: " + e.getMessage());
        }

        return false;
    }
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public boolean UpdateAssetCoin(
            final Context ctx,
            final String assetId,
            final String coinName,
            final String coinValue
    ) {
        try {

            if(!AssetExists(ctx, assetId)){
                String errorMessage = String.format("Asset %s is does not exists", assetId);
                throw new AssetNotFoundException(errorMessage);
            }
            if(!CoinExists(ctx, coinName)){
                String errorMessage = String.format("Coin %s is does not exists", coinName);
                throw new CoinNotFoundException(errorMessage);
            }

            ChaincodeStub stub = ctx.getStub();

            AssetExists(ctx, assetId);

            Asset asset = objectMapper.readValue(stub.getStringState(assetId), Asset.class);
            asset.modifyCoinValue(null, null, coinName, coinValue);

            stub.putStringState(assetId, objectMapper.writeValueAsString(asset));

            return true;

        } catch (AssetNotFoundException e){
            System.out.println(e.getMessage());
        } catch (CoinNotFoundException e){
            System.out.println(e.getMessage());
        } catch (JsonProcessingException e) {
            System.out.println("Object to Json Exception: " + e.getMessage());
        }

        return false;
    }


    /**
     * methodName : TransferCoin
     * author : YoungChang Choi
     * description :
     *
     * @param ctx         the ctx
     * @param senderAssetId the from asset id
     * @param receiverAssetId   the to asset id
     * @param coinName    the coin name
     * @param amount      the amount
     * @return the coin
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset TransferCoin(final Context ctx, final String senderAssetId, final String receiverAssetId, final String coinName, final String amount) {
        try{
            if(!AssetExists(ctx, senderAssetId)){
                String errorMessage = String.format("Asset %s is does not exists", senderAssetId);
                throw new AssetNotFoundException(errorMessage);
            }
            if(!AssetExists(ctx, receiverAssetId)){
                String errorMessage = String.format("Asset %s is does not exists", receiverAssetId);
                throw new AssetNotFoundException(errorMessage);
            }
            if(!CoinExists(ctx, coinName)){
                String errorMessage = String.format("Coin %s is does not exists", coinName);
                throw new CoinNotFoundException(errorMessage);
            }

            ChaincodeStub stub = ctx.getStub();

            Asset senderAsset = objectMapper.readValue(stub.getStringState(senderAssetId), Asset.class);
            Asset receiverAsset = objectMapper.readValue(stub.getStringState(receiverAssetId), Asset.class);

            senderAsset.modifyCoinValue(senderAssetId, receiverAssetId, coinName, "-" + amount);
            receiverAsset.modifyCoinValue(senderAssetId, receiverAssetId, coinName, amount);

            stub.putStringState(senderAssetId, objectMapper.writeValueAsString(senderAsset));
            stub.putStringState(receiverAssetId, objectMapper.writeValueAsString(receiverAsset));

            return senderAsset;
        } catch (AssetNotFoundException e){
            System.out.println(e.getMessage());
        } catch (CoinNotFoundException e){
            System.out.println(e.getMessage());
        } catch (NotEnoughCoinValueException e){
            System.out.println(e.getMessage());
        } catch (JsonProcessingException e) {
            System.out.println("Object to Json Exception: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("NumberFormatException: " + e.getMessage());
        }

        return null;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetHistoryForAssetId(final Context ctx, final String assetId) {
        try {
            if(!AssetExists(ctx, assetId)){
                String errorMessage = String.format("Asset %s is does not exists", assetId);
                throw new AssetNotFoundException(errorMessage);
            }

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

        } catch (AssetNotFoundException e){
            System.out.println(e.getMessage());
        } catch (JsonProcessingException e) {
            System.out.println("Object to Json Exception: " + e.getMessage());
        } catch (Exception e){
            System.out.println("QueryResultsIterator close Excepiton: " + e.getMessage());
        }

        return null;
    }
}
