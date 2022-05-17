package org.hyperledger.fabric.samples.assettransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
        boolean response = contract.AssetExists(ctx, "rootAsset");

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
        when(stub.getStringState("rootAsset"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("rootAsset", "0", "test", new HashMap<>(), null, null, null)));

        //when
       boolean response = contract.AssetExists(ctx, "rootAsset");

        //then
        verify(stub).getStringState("rootAsset");
        assertThat(response).isTrue();

    }

    @Test
    public void CreateAsset_을_테스트한다() throws JsonProcessingException {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        HashMap<String, String> coin = new HashMap<>();
        coin.put("test", "0");

        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStringState("rootAsset"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("rootAsset", "0", "rootOwner", coin, null, null, null)));

        //when
        Asset asset = contract.CreateAsset(ctx, "testAsset", "0","test");

        //then
        assertThat(asset).isEqualTo(Asset.of("testAsset", "0", "test", coin, null, null, null));
    }

    @Test
    public void GetAsset_으로_없는_Asset_을_테스트한다() throws JsonProcessingException {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStringState("rootAsset"))
                .thenReturn(null);

        //when
        Asset asset = contract.GetAsset(ctx, "rootAsset");

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
        when(stub.getStringState("rootAsset"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("rootAsset", "0", "test", new HashMap<>(), null, null, null)));

        //when
        Asset asset = contract.GetAsset(ctx, "rootAsset");

        //then
        verify(stub, times(2)).getStringState("rootAsset");
        assertThat(asset).isEqualTo(Asset.of("rootAsset", "0", "test", new HashMap<>(), null, null, null));
    }

    @Test
    public void ChangeOwner_을_테스트한다() throws JsonProcessingException {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStringState("rootAsset"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("rootAsset", "0", "test", new HashMap<>(), null, null, null)));

        //when
        Asset asset = contract.ChangeOwner(ctx, "rootAsset", "newTest");

        //then
        verify(stub, times(2)).getStringState("rootAsset");
        assertThat(asset.getOwner()).isEqualTo("newTest");
    }

    @Test
    public void DeleteAsset_을_테스트한다() throws JsonProcessingException {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStringState("rootAsset"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("rootAsset", "0", "test", new HashMap<>(), null, null, null)));

        //when
        boolean response = contract.DeleteAsset(ctx, "rootAsset");

        //then
        verify(stub).getStringState("rootAsset");
        verify(stub).delState("rootAsset");
        assertThat(response).isTrue();

    }

    @Test
    public void CoinExists_으로_없는_코인_을_테스트한다() throws JsonProcessingException {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStringState("rootAsset"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("rootAsset", "0", "test", new HashMap<>(), null, null, null)));

        //when
        boolean response = contract.CoinExists(ctx, "test");

        //then
        verify(stub).getStringState("rootAsset");
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
        when(stub.getStringState("rootAsset"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("rootAsset", "0", "test", coin, null, null, null)));

        //when
        boolean response = contract.CoinExists(ctx, "test");

        //then
        verify(stub).getStringState("rootAsset");
        assertThat(response).isTrue();
    }

    @Test
    public void CreateCoin_을_테스트한다() throws JsonProcessingException {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);

        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStringState("rootAsset"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("rootAsset", "0", "test", new HashMap<>(), null, null, null)));
        when(stub.getStateByRange("", "")).thenReturn(new QueryResultsIterator<KeyValue>() {
            @Override
            public void close() throws Exception {

            }

            @Override
            public Iterator<KeyValue> iterator() {
                Iterator<KeyValue> keyValueIterator = new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public KeyValue next() {
                        KeyValue keyValue = new KeyValue() {
                            @Override
                            public String getKey() {
                                return null;
                            }

                            @Override
                            public byte[] getValue() {
                                return new byte[0];
                            }

                            @Override
                            public String getStringValue() {
                                try {
                                    return objectMapper.writeValueAsString(Asset.of("rootAsset", "0", "test", new HashMap<>(), null, null, null));
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        };
                        return null;
                    }
                };
                return keyValueIterator;
            }
        });
        //when
        boolean response = contract.CreateCoin(ctx, "test");

        //then
        verify(stub).getStringState("rootAsset");
        assertThat(response).isTrue();

    }

    @Test
    public void TransferCoin_을_테스트한다() throws JsonProcessingException {
        //given
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);

        HashMap<String, String> coin = new HashMap<>();
        coin.put("test", "100");
        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStringState("rootAsset"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("rootAsset", "0", "test1", coin, null, null, null)));
        when(stub.getStringState("asset2"))
                .thenReturn(objectMapper.writeValueAsString(Asset.of("asset2", "0", "test2", coin, null, null, null)));

        //when
        TransferResponse transferResponse = contract.TransferCoin(ctx, "rootAsset", "asset2", "test", "100");

        //then
        verify(stub, times(3)).getStringState("rootAsset");
        verify(stub, times(2)).getStringState("asset2");
        assertThat(transferResponse.getCoinName()).isEqualTo("test");
        assertThat(transferResponse.getAmount()).isEqualTo("100");

    }


}
