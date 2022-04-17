
package org.hyperledger.fabric.samples.assettransfer;

import java.util.HashMap;
import java.util.Objects;

import lombok.*;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.hyperledger.fabric.shim.ChaincodeException;

@DataType()
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Asset {

    @Property()
    private String assetId;

    @Property()
    private String owner;

    @Property()
    private HashMap<String, String> coin;

    @Property()
    private String sender;

    @Property()
    private String receiver;

    @Property()
    private String amount;

    public static Asset of(final String assetId, final String owner, final HashMap<String, String> coin, final String sender, final String receiver, final String amount) {
        return new Asset(assetId, owner, coin, sender, receiver, amount);
    }

    public Asset(String assetId, String owner, HashMap<String, String> coin, String sender, String receiver, String amount) {
        this.assetId = assetId;
        this.owner = owner;
        this.coin = coin;
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

    public void createCoin(String coinName){
        coin.put(coinName, "0");
    }

    public void removeCoin(String coinName){
        coin.remove(coinName);
    }

    public void increaseCoinValue(String coinName, String amount){
        String coinValue = coin.get(coinName);
        int modifiedCoinValue = Integer.parseInt(coinValue) + Integer.parseInt(amount);

        coin.put(coinName, String.valueOf(modifiedCoinValue));
    }

    public void decreaseCoinValue(String coinName, String amount) throws ChaincodeException {
        String coinValue = coin.get(coinName);

        if(Integer.parseInt(coin.get(coinName)) - Integer.parseInt(amount) < 0){
            System.out.println("Asset" + assetId + "does not have enough coin");
            throw new ChaincodeException("Asset" + assetId + "does not have enough coin", "ASSET_NOTENOUGH_COINVALUE");
        }

        int modifiedCoinValue = Integer.parseInt(coinValue) - Integer.parseInt(amount);
        coin.put(coinName, String.valueOf(modifiedCoinValue));
    }

}
