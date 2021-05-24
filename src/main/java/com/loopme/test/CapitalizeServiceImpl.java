package com.loopme.test;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class CapitalizeServiceImpl extends CapitalizeServiceGrpc.CapitalizeServiceImplBase {
    private static final int MAX_STRING_LENGTH = 100;
    private static final String INVALID_ARGUMENT_DESC = "String length is greater than 100 symbols";

    @Override
    public void capitalize(CapitalizeRequest request, StreamObserver<CapitalizeResponse> responseObserver) {
        if (request.getStr().length() > MAX_STRING_LENGTH) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(INVALID_ARGUMENT_DESC).asRuntimeException());
            return;
        }

        CapitalizeResponse response = CapitalizeResponse.newBuilder().setStr(capitalizeString(request.getStr())).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private static String capitalizeString(String str) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(str.charAt(i))) {
                result.append(Character.toUpperCase(str.charAt(i)));
                result.append(str.substring(i + 1));
                break;
            }
            result.append(str.charAt(i));
        }

        return result.toString();
    }
}
