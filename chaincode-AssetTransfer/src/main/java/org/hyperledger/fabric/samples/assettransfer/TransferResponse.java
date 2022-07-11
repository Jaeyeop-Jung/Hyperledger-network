package org.hyperledger.fabric.samples.assettransfer;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class TransferResponse {

    private String transactionId;
    private String senderStudentId;

    private String receiverStudentIdOrPhoneNumber;

    private String coinName;

    private String amount;

    @Builder
    public TransferResponse(String transactionId, String senderStudentId, String receiverStudentIdOrPhoneNumber, String coinName, String amount) {
        this.transactionId = transactionId;
        this.senderStudentId = senderStudentId;
        this.receiverStudentIdOrPhoneNumber = receiverStudentIdOrPhoneNumber;
        this.coinName = coinName;
        this.amount = amount;
    }
}
