package com.kyobo.server.common;

public class ApiResponse<T> {
    private final String statusCode;
    private final String statusMessage;
    private final T data;

    private ApiResponse(String statusCode, String statusMessage, T data) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("00", "완료되었습니다.", data);
    }

    public static <T> ApiResponse<T> fail(String statusCode, String statusMessage) {
        return new ApiResponse<>(statusCode, statusMessage, null);
    }

    public static <T> ApiResponse<T> error(String statusMessage) {
        return new ApiResponse<>("500", statusMessage, null);
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public T getData() {
        return data;
    }
}
