/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.util.HashMap;
import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Asset {

    @Property()
    private final String assetID;

    @Property()
    private final String color;

    @Property()
    private final int size;

    @Property()
    private final String owner;

    @Property()
    private final int appraisedValue;

    @Property()
    private final HashMap<String, String> coin;

    public String getAssetID() {
        return assetID;
    }

    public String getColor() {
        return color;
    }

    public int getSize() {
        return size;
    }

    public String getOwner() {
        return owner;
    }

    public int getAppraisedValue() {
        return appraisedValue;
    }

    public HashMap<String, String> getCoin() {
        return coin;
    }

    public Asset(@JsonProperty("assetID") final String assetID, @JsonProperty("color") final String color,
                 @JsonProperty("size") final int size, @JsonProperty("owner") final String owner,
                 @JsonProperty("appraisedValue") final int appraisedValue, @JsonProperty("coin") final HashMap<String, String> coin
    ) {
        this.assetID = assetID;
        this.color = color;
        this.size = size;
        this.owner = owner;
        this.appraisedValue = appraisedValue;
        this.coin = coin;
    }

    @Override
    public String toString() {
        return "Asset{" + "assetID='" + assetID + '\'' + ", color='" + color + '\'' + ", size=" + size + ", owner='" + owner + '\'' + ", appraisedValue=" + appraisedValue + ", coin=" + coin + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Asset asset = (Asset) o;
        return getSize() == asset.getSize() && getAppraisedValue() == asset.getAppraisedValue() && Objects.equals(getAssetID(), asset.getAssetID()) && Objects.equals(getColor(), asset.getColor()) && Objects.equals(getOwner(), asset.getOwner()) && Objects.equals(coin, asset.coin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAssetID(), getColor(), getSize(), getOwner(), getAppraisedValue(), coin);
    }
}
