package com.library.domain.shared;

import com.library.domain.shared.exception.ResultCode;

public final class Result<T> {

    private final int code;
    private final String message;
    private final T data;
    private final String traceId;

    private Result(int code, String message, T data, String traceId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = traceId;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS.code(), ResultCode.SUCCESS.defaultMessage(), data, null);
    }

    public static <T> Result<T> ok(T data, String traceId) {
        return new Result<>(ResultCode.SUCCESS.code(), ResultCode.SUCCESS.defaultMessage(), data, traceId);
    }

    public static <T> Result<T> fail(ResultCode code, String message, String traceId) {
        return new Result<>(code.code(), message != null ? message : code.defaultMessage(), null, traceId);
    }

    public static <T> Result<T> fail(int code, String message, String traceId) {
        return new Result<>(code, message, null, traceId);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getTraceId() {
        return traceId;
    }

    public Result<T> withTraceId(String value) {
        return new Result<>(code, message, data, value);
    }
}
