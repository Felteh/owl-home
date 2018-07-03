package com.owl.owlyhome.server;

import static akka.event.Logging.InfoLevel;

import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.Rejection;
import akka.http.javadsl.server.RejectionHandler;
import akka.http.javadsl.server.directives.LogEntry;
import akka.http.scaladsl.server.ValidationRejection;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandlingDirectives extends AllDirectives {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorHandlingDirectives.class);

    protected RejectionHandler rejectionHandlerLogAndReturnNotFound() {
        return RejectionHandler
                .newBuilder()
                .handleNotFound(
                        extractUnmatchedPath(unmatchedPath
                                        -> extractRequest(
                                request -> {
                                    LOG.warn("Rejected: Unmatched path UnmatchedUri={} Method={} Uri={} Headers={}", unmatchedPath, request.method().value(), request.getUri().toString(), request.getHeaders());
                                    return complete(StatusCodes.NOT_FOUND);
                                }
                                )
                        )
                )
                .handle(ValidationRejection.class, rejection -> extractUnmatchedPath(unmatchedPath
                                -> extractRequest(
                        request -> {
                            LOG.warn("ValidationRejection: ValidationError={} Unmatched path UnmatchedUri={} Method={} Uri={} Headers={}", rejection.message(), unmatchedPath, request.method().value(), request.getUri().toString(), request.getHeaders());
                            return complete(StatusCodes.BAD_REQUEST);
                        }
                        )
                ))
                .build();
    }

    public LogEntry requestMethodAsInfo(HttpRequest request, HttpResponse response) {
        String headers = headersToJson(request.getHeaders());

        return LogEntry.create(
                "--> REQUEST {"
                        + request.method().name() + " : " + request.getUri().toString() + ", "
                        + headers
                        + "}\n"
                        + "<-- RESPONSE {"
                        + response.status() + " "
                        + "Content-Type:" + response.entity().getContentType().toString() + ", "
                        + "Content-Length: " + response.entity().getContentLengthOption().orElse(-1)
                        + "}",
                InfoLevel());
    }

    public LogEntry rejectionsAsInfo(HttpRequest request, List<Rejection> rejections) {
        String headers = headersToJson(request.getHeaders());

        return LogEntry.create(
                "Server has received a request\n"
                        + request.method().name() + " " + request.getUri().toString() + "\n"
                        + headers + "\n"
                        + "Server responded with a rejection\n"
                        + rejections.stream().map(Rejection::toString).collect(Collectors.joining("\n")),
                InfoLevel());
    }

    public ExceptionHandler exceptionHandlerLogAndReturnInternalError() {
        return ExceptionHandler
                .newBuilder()
                .matchAny(throwable -> extractRequest(request -> {
                            LOG.warn("Error on route: " + request.method().value() + " " + request.getUri().toString() + " " + throwable.getMessage(), throwable);
                            return complete(StatusCodes.INTERNAL_SERVER_ERROR);
                        })
                ).build();
    }

    public String headersToJson(Iterable<HttpHeader> headers) {
        return StreamSupport
                .stream(headers.spliterator(), false)
                .map(header -> {
                    return header.name() + ": " + header.value();
                })
                .collect(Collectors.joining(","));
    }

}
