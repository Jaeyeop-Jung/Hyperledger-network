package org.hyperledger.fabric.samples.assettransfer;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class TransferResponse {

    private String transactionId;
    private String senderIdentifier;

    private String receiverIdentifier;

    private String coinName;

    private String amount;

    @Builder
    public TransferResponse(String transactionId, String senderIdentifier, String receiverIdentifier, String coinName, String amount) {
        this.transactionId = transactionId;
        this.senderIdentifier = senderIdentifier;
        this.receiverIdentifier = receiverIdentifier;
        this.coinName = coinName;
        this.amount = amount;
    }
}
