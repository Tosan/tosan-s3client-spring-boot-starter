package com.tosan.client.s3.starter.util;

import software.amazon.awssdk.http.HttpStatusCode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpStatusMessages {
    private static final Map<Integer, String> STATUS_MESSAGES = new ConcurrentHashMap<>();

    static {
        // 2xx Success
        STATUS_MESSAGES.put(200, "OK");
        STATUS_MESSAGES.put(201, "CREATED");
        STATUS_MESSAGES.put(202, "ACCEPTED");
        STATUS_MESSAGES.put(203, "NON_AUTHORITATIVE_INFORMATION");
        STATUS_MESSAGES.put(204, "NO_CONTENT");
        STATUS_MESSAGES.put(205, "RESET_CONTENT");
        STATUS_MESSAGES.put(206, "PARTIAL_CONTENT");

        // 3xx Redirection
        STATUS_MESSAGES.put(300, "MULTIPLE_CHOICES");
        STATUS_MESSAGES.put(301, "MOVED_PERMANENTLY");
        STATUS_MESSAGES.put(302, "FOUND");
        STATUS_MESSAGES.put(303, "SEE_OTHER");
        STATUS_MESSAGES.put(304, "NOT_MODIFIED");
        STATUS_MESSAGES.put(307, "TEMPORARY_REDIRECT");
        STATUS_MESSAGES.put(308, "PERMANENT_REDIRECT");

        // 4xx Client Errors
        STATUS_MESSAGES.put(400, "BAD_REQUEST");
        STATUS_MESSAGES.put(401, "UNAUTHORIZED");
        STATUS_MESSAGES.put(402, "PAYMENT_REQUIRED");
        STATUS_MESSAGES.put(403, "FORBIDDEN");
        STATUS_MESSAGES.put(404, "NOT_FOUND");
        STATUS_MESSAGES.put(405, "METHOD_NOT_ALLOWED");
        STATUS_MESSAGES.put(406, "NOT_ACCEPTABLE");
        STATUS_MESSAGES.put(407, "PROXY_AUTHENTICATION_REQUIRED");
        STATUS_MESSAGES.put(408, "REQUEST_TIMEOUT");
        STATUS_MESSAGES.put(409, "CONFLICT");
        STATUS_MESSAGES.put(410, "GONE");
        STATUS_MESSAGES.put(411, "LENGTH_REQUIRED");
        STATUS_MESSAGES.put(412, "PRECONDITION_FAILED");
        STATUS_MESSAGES.put(413, "PAYLOAD_TOO_LARGE");
        STATUS_MESSAGES.put(414, "URI_TOO_LONG");
        STATUS_MESSAGES.put(415, "UNSUPPORTED_MEDIA_TYPE");
        STATUS_MESSAGES.put(416, "RANGE_NOT_SATISFIABLE");
        STATUS_MESSAGES.put(417, "EXPECTATION_FAILED");
        STATUS_MESSAGES.put(418, "I_AM_A_TEAPOT");
        STATUS_MESSAGES.put(422, "UNPROCESSABLE_ENTITY");
        STATUS_MESSAGES.put(423, "LOCKED");
        STATUS_MESSAGES.put(424, "FAILED_DEPENDENCY");
        STATUS_MESSAGES.put(425, "TOO_EARLY");
        STATUS_MESSAGES.put(426, "UPGRADE_REQUIRED");
        STATUS_MESSAGES.put(428, "PRECONDITION_REQUIRED");
        STATUS_MESSAGES.put(429, "TOO_MANY_REQUESTS");
        STATUS_MESSAGES.put(431, "REQUEST_HEADER_FIELDS_TOO_LARGE");
        STATUS_MESSAGES.put(451, "UNAVAILABLE_FOR_LEGAL_REASONS");

        // 5xx Server Errors
        STATUS_MESSAGES.put(500, "INTERNAL_SERVER_ERROR");
        STATUS_MESSAGES.put(501, "NOT_IMPLEMENTED");
        STATUS_MESSAGES.put(502, "BAD_GATEWAY");
        STATUS_MESSAGES.put(503, "SERVICE_UNAVAILABLE");
        STATUS_MESSAGES.put(504, "GATEWAY_TIMEOUT");
        STATUS_MESSAGES.put(505, "HTTP_VERSION_NOT_SUPPORTED");
        STATUS_MESSAGES.put(506, "VARIANT_ALSO_NEGOTIATES");
        STATUS_MESSAGES.put(507, "INSUFFICIENT_STORAGE");
        STATUS_MESSAGES.put(508, "LOOP_DETECTED");
        STATUS_MESSAGES.put(510, "NOT_EXTENDED");
        STATUS_MESSAGES.put(511, "NETWORK_AUTHENTICATION_REQUIRED");
    }

    public static String getStatusMessage(int statusCode) {
        return STATUS_MESSAGES.getOrDefault(statusCode, "UNKNOWN");
    }

    public static String getFullStatus(int statusCode) {
        return statusCode + " " + getStatusMessage(statusCode);
    }

    public static boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    public static boolean isClientError(int statusCode) {
        return statusCode >= 400 && statusCode < 500;
    }

    public static boolean isServerError(int statusCode) {
        return statusCode >= 500 && statusCode < 600;
    }
}
