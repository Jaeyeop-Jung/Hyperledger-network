package org.hyperledger.fabric.samples.assettransfer;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class TransferResponse {

    private String senderStudentId;

    private String receiverStudentId;

    private String coinName;

    private String amount;

    @Builder
    public TransferResponse(String senderStudentId, String receiverStudentId, String coinName, String amount) {
        this.senderStudentId = senderStudentId;
        this.receiverStudentId = receiverStudentId;
        this.coinName = coinName;
        this.amount = amount;
    }
}
