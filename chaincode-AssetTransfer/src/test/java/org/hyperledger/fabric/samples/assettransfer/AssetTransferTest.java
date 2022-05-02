package org.hyperledger.fabric.samples.assettransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exception.AssetNotFoundException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("체인코드 테스트")
public class AssetTransferTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void 없는_체인코드_실행_을_테스트한다(){
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);

        //when

        //then
        assertThatThrownBy(() -> contract.unknownTransaction(ctx))
                .isInstanceOf(Exception.class);
        verifyNoMoreInteractions(ctx);
    }

    @Test
    public void AssetExists_으로_없는_Asset_을_테스트한다() {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(ctx.getStub()).thenReturn(stub);

        //when
        boolean response = contract.AssetExists(ctx, "asset1");

        //then
        assertThat(response).isFalse();
    }

    @Test
    public void AssetExists_으로_있는_Asset_을_테스트한다() throws JsonProcessingException {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStringState("asset1"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("asset1", "test", new HashMap<>(), null, null, null)));

        //when
       boolean response = contract.AssetExists(ctx, "asset1");

        //then
        verify(stub).getStringState("asset1");
        assertThat(response).isTrue();

    }

    @Test
    public void CreateAsset_을_테스트한다() throws JsonProcessingException {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(ctx.getStub()).thenReturn(stub);

        //when
        Asset asset = contract.CreateAsset(ctx, "asset1", "test");

        //then
        verify(stub).putStringState("asset1", objectMapper.writeValueAsString(asset));
        assertThat(asset).isEqualTo(Asset.of("asset1", "test", new HashMap<>(), null, null, null));
    }

    @Test
    public void GetAsset_으로_없는_Asset_을_테스트한다() throws JsonProcessingException {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStringState("asset1"))
                .thenReturn(null);

        //when
        Asset asset = contract.GetAsset(ctx, "asset1");

        //then
        assertThat(asset).isNull();

    }

    @Test
    public void GetAsset_으로_있는_Asset_을_테스트한다() throws JsonProcessingException {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStringState("asset1"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("asset1", "test", new HashMap<>(), null, null, null)));

        //when
        Asset asset = contract.GetAsset(ctx, "asset1");

        //then
        verify(stub, times(2)).getStringState("asset1");
        assertThat(asset).isEqualTo(Asset.of("asset1", "test", new HashMap<>(), null, null, null));
    }

    @Test
    public void ChangeOwner_을_테스트한다() throws JsonProcessingException {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStringState("asset1"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("asset1", "test", new HashMap<>(), null, null, null)));

        //when
        Asset asset = contract.ChangeOwner(ctx, "asset1", "newTest");

        //then
        verify(stub, times(2)).getStringState("asset1");
        assertThat(asset.getOwner()).isEqualTo("newTest");
    }

    @Test
    public void DeleteAsset_을_테스트한다() throws JsonProcessingException {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStringState("asset1"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("asset1", "test", new HashMap<>(), null, null, null)));

        //when
        boolean response = contract.DeleteAsset(ctx, "asset1");

        //then
        verify(stub).getStringState("asset1");
        verify(stub).delState("asset1");
        assertThat(response).isTrue();
        
    }
    
    @Test
    public void CoinExists_으로_없는_코인_을_테스트한다() throws JsonProcessingException {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStringState("asset1"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("asset1", "test", new HashMap<>(), null, null, null)));

        //when
        boolean response = contract.CoinExists(ctx, "test");

        //then
        verify(stub).getStringState("asset1");
        assertThat(response).isFalse();
    }

    @Test
    public void CoinExists_으로_있는_코인_을_테스트한다() throws JsonProcessingException {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);

        HashMap<String, String> coin = new HashMap<>();
        coin.put("test", "0");
        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStringState("asset1"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("asset1", "test", coin, null, null, null)));

        //when
        boolean response = contract.CoinExists(ctx, "test");

        //then
        verify(stub).getStringState("asset1");
        assertThat(response).isTrue();
    }


}
