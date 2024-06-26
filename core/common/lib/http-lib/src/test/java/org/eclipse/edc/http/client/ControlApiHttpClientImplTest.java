/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.http.client;

import okhttp3.Request;
import org.eclipse.edc.api.auth.spi.ControlClientAuthenticationProvider;
import org.eclipse.edc.http.spi.ControlApiHttpClient;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.spi.result.ServiceFailure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockserver.integration.ClientAndServer;

import java.util.Map;
import java.util.stream.Stream;

import static org.eclipse.edc.http.client.testfixtures.HttpTestUtils.testHttpClient;
import static org.eclipse.edc.junit.assertions.AbstractResultAssert.assertThat;
import static org.eclipse.edc.spi.result.ServiceFailure.Reason.BAD_REQUEST;
import static org.eclipse.edc.spi.result.ServiceFailure.Reason.CONFLICT;
import static org.eclipse.edc.spi.result.ServiceFailure.Reason.NOT_FOUND;
import static org.eclipse.edc.spi.result.ServiceFailure.Reason.UNAUTHORIZED;
import static org.eclipse.edc.spi.result.ServiceFailure.Reason.UNEXPECTED;
import static org.eclipse.edc.util.io.Ports.getFreePort;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.stop.Stop.stopQuietly;

class ControlApiHttpClientImplTest {

    private final int port = getFreePort();
    private final EdcHttpClient http = testHttpClient();
    private final ControlClientAuthenticationProvider authenticationProvider = mock();
    private ClientAndServer server;

    private final ControlApiHttpClient client = new ControlApiHttpClientImpl(http, authenticationProvider);

    @BeforeEach
    public void startServer() {
        server = ClientAndServer.startClientAndServer(port);
    }

    @AfterEach
    public void stopServer() {
        stopQuietly(server);
    }

    @Nested
    class Execute {
        @Test
        void shouldSucceed_whenServerResponseIsSuccessful() {
            server.when(request()).respond(response().withStatusCode(204));

            var request = new Request.Builder()
                    .url("http://localhost:" + port);

            var result = client.execute(request);

            assertThat(result).isSucceeded();
        }

        @Test
        void shouldIncludeAuthenticationHeaders() {
            server.when(request()).respond(response().withStatusCode(204));
            when(authenticationProvider.authenticationHeaders()).thenReturn(Map.of("Authorization", "authToken"));

            var request = new Request.Builder()
                    .url("http://localhost:" + port);

            client.execute(request);

            server.verify(request().withHeader("Authorization", "authToken"));
        }

        @ParameterizedTest
        @ArgumentsSource(FailingResponses.class)
        void shouldFail_whenServerResponseIsNotSuccessful(int statusCode, ServiceFailure.Reason reason) {
            server.when(request()).respond(response().withStatusCode(statusCode));

            var request = new Request.Builder()
                    .url("http://localhost:" + port);

            var result = client.execute(request);

            assertThat(result).isFailed().extracting(ServiceFailure::getReason).isEqualTo(reason);
        }

        private static class FailingResponses implements ArgumentsProvider {

            @Override
            public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
                return Stream.of(
                        arguments(400, BAD_REQUEST),
                        arguments(401, UNAUTHORIZED),
                        arguments(403, UNAUTHORIZED),
                        arguments(404, NOT_FOUND),
                        arguments(409, CONFLICT),
                        arguments(500, UNEXPECTED)
                );
            }
        }
    }

    @Nested
    class Response {

        @Test
        void shouldSucceed_whenServerResponseIsSuccessful() {
            server.when(request()).respond(response().withStatusCode(200).withBody("response body"));

            var request = new Request.Builder()
                    .url("http://localhost:" + port);

            var result = client.request(request);

            assertThat(result).isSucceeded().isEqualTo("response body");
        }

        @Test
        void shouldIncludeAuthenticationHeaders() {
            server.when(request()).respond(response().withStatusCode(204));
            when(authenticationProvider.authenticationHeaders()).thenReturn(Map.of("Authorization", "authToken"));

            var request = new Request.Builder()
                    .url("http://localhost:" + port);

            client.request(request);

            server.verify(request().withHeader("Authorization", "authToken"));
        }

        @ParameterizedTest
        @ArgumentsSource(FailingResponses.class)
        void shouldFail_whenServerResponseIsNotSuccessful(int statusCode, ServiceFailure.Reason reason) {
            server.when(request()).respond(response().withStatusCode(statusCode));

            var request = new Request.Builder()
                    .url("http://localhost:" + port);

            var result = client.request(request);

            assertThat(result).isFailed().extracting(ServiceFailure::getReason).isEqualTo(reason);
        }

        private static class FailingResponses implements ArgumentsProvider {

            @Override
            public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
                return Stream.of(
                        arguments(400, BAD_REQUEST),
                        arguments(401, UNAUTHORIZED),
                        arguments(403, UNAUTHORIZED),
                        arguments(404, NOT_FOUND),
                        arguments(409, CONFLICT),
                        arguments(500, UNEXPECTED)
                );
            }
        }

    }

}
