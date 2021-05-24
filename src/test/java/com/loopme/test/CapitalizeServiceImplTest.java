package com.loopme.test;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class CapitalizeServiceImplTest {
    private static final long DEADLINE_MS = 10000L;
    private static final String HUGE_STRING = "I want to work for LoopMe. LoopMe is the world’s largest mobile video DSP, reaching over one billion consumers worldwide via integration with programmatic ad exchanges and direct publishers.";

    @Rule
    public GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private CapitalizeServiceGrpc.CapitalizeServiceBlockingStub blockingStub;

    @Before
    public void setUp() throws IOException {
        String serverName = InProcessServerBuilder.generateName();

        grpcCleanup.register(InProcessServerBuilder.forName(serverName)
                .addService(new CapitalizeServiceImpl()).build()
                .start());

        blockingStub = CapitalizeServiceGrpc.newBlockingStub(
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()))
                .withDeadlineAfter(DEADLINE_MS, TimeUnit.MILLISECONDS);

    }

    @Test
    public void shouldThrownInvalidArgumentException() {
        CapitalizeRequest request = CapitalizeRequest.newBuilder().setStr(HUGE_STRING).build();

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> blockingStub.capitalize(request));

        assertEquals(Status.Code.INVALID_ARGUMENT, exception.getStatus().getCode());
    }

    @Test
    public void shouldThrownDeadlineExceededException() throws InterruptedException {
        CapitalizeRequest request = CapitalizeRequest.newBuilder().setStr("some data").build();

        // Added delay
        Thread.sleep(DEADLINE_MS);
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> blockingStub.capitalize(request));

        assertEquals(Status.Code.DEADLINE_EXCEEDED, exception.getStatus().getCode());
    }

    @Test
    public void shouldCapitaliseAllStringsCorrectly() {
        List<String> initialStrings = getInitialStrings();
        List<String> expectedStrings = getExpectedStrings();

        List<String> resultStrings = initialStrings.stream()
                .map(str -> blockingStub.capitalize(buildRequest(str)).getStr())
                .collect(Collectors.toList());

        assertIterableEquals(expectedStrings, resultStrings);
    }

    private CapitalizeRequest buildRequest(String str) {
        return CapitalizeRequest.newBuilder().setStr(str).build();
    }

    private static List<String> getInitialStrings() {
        return Arrays.asList(
                "",
                " ",
                "i",
                "1. i",
                "i want to work for LoopMe",
                "1. i want to work for LoopMe"
        );
    }

    private static List<String> getExpectedStrings() {
        return Arrays.asList(
                "",
                " ",
                "I",
                "1. I",
                "I want to work for LoopMe",
                "1. I want to work for LoopMe"
        );
    }
}