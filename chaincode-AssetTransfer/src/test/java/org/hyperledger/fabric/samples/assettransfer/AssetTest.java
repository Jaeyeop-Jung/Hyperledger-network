package org.hyperledger.fabric.samples.assettransfer;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.*;

public class AssetTest {

    @Test
    public void 같은_Asset_을_테스트한다(){
        HashMap<String, String> coin = new HashMap<>();
        Asset asset = Asset.of("asset1", "0","test", coin, UserRole.ROLE_STUDENT.name(),null, null, null);

        assertThat(asset).isEqualTo(asset);
    }

    @Test
    public void 같은_값을_가진_Asset_을_테스트한다(){
        HashMap<String, String> coin = new HashMap<>();
        Asset asset1 = Asset.of("asset1", "0","test", coin, UserRole.ROLE_STUDENT.name(),null, null, null);
        Asset asset2 = Asset.of("asset1", "0", "test", coin, UserRole.ROLE_STUDENT.name(),null, null, null);

        assertThat(asset1).isEqualTo(asset2);
    }

    @Test
    public void 다른_Assets_을_테스트한다(){
        HashMap<String, String> coin = new HashMap<>();
        Asset asset1 = Asset.of("asset1", "0", "test1", coin, UserRole.ROLE_STUDENT.name(),null, null, null);
        Asset asset2 = Asset.of("asset2", "0", "test2", coin, UserRole.ROLE_STUDENT.name(),null, null, null);

        assertThat(asset1).isNotEqualTo(asset2);
    }
}
