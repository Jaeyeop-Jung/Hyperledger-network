
package org.hyperledger.fabric.samples.assettransfer;

import java.util.HashMap;
import java.util.Objects;

import exception.NotEnoughCoinValueException;
import lombok.*;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Asset {

    @Property()
    private String assetId;

    @Property()
    private String identifier;

    @Property()
    private String owner;

    @Property()
    private HashMap<String, String> coin;

    @Property()
    private String userRole;

    @Property()
    private String sender;

    @Property()
    private String receiver;

    @Property()
    private String amount;

    public static Asset of(final String assetId, final String studentId, final String owner, final HashMap<String, String> coin, final String userRole, final String sender, final String receiver, final String amount) {
        return new Asset(assetId, studentId, owner, coin, userRole, sender, receiver, amount);
    }

    public Asset(String assetId, String identifier, String owner, HashMap<String, String> coin, String userRole, String sender, String receiver, String amount) {
        this.assetId = assetId;
        this.identifier = identifier;
        this.owner = owner;
        this.coin = coin;
        this.userRole = userRole;
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return Objects.equals(this.getAssetId(), asset.getAssetId()) && Objects.equals(getOwner(), asset.getOwner()) && Objects.equals(getCoin(), asset.getCoin()) && Objects.equals(getSender(), asset.getSender()) && Objects.equals(getReceiver(), asset.getReceiver()) && Objects.equals(getAmount(), asset.getAmount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getAssetId(), getOwner(), getCoin(), getSender(), getReceiver(), getAmount());
    }

    public void changeOwner(String owner){
        this.owner = owner;
    }

    public void createCoin(String coinName){
        coin.put(coinName, "0");
    }

    public void createCoin(String coinName, String coinValue){
        coin.put(coinName, coinValue);
    }

    public void removeCoin(String coinName){
        coin.remove(coinName);
    }

    public void modifyCoinValue(String senderAssetId, String receiverAssetId, String coinName, String amount) throws NotEnoughCoinValueException {
        int modifiedCoinValue = Integer.parseInt(coin.get(coinName)) + Integer.parseInt(amount);
        if (modifiedCoinValue < 0) {
            String errormessage = String.format("Asset %s does not have enough coin", senderAssetId);
            throw new NotEnoughCoinValueException(errormessage);
        }

        coin.put(coinName, String.valueOf(modifiedCoinValue));

        sender = senderAssetId;
        receiver = receiverAssetId;
        this.amount = amount;
    }
}
