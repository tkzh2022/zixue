package com.library.domain.shared.exception;

public enum ResultCode {

    SUCCESS(0, "success"),

    PARAM_INVALID(8001, "参数校验失败"),
    DATA_INTEGRITY(8002, "数据完整性冲突"),
    RATE_LIMITED(8003, "操作过于频繁，请稍后再试"),

    READER_DISABLED(8101, "读者被禁用"),
    READER_BORROW_LIMIT_EXCEEDED(8102, "在借数超过上限"),
    READER_HAS_OVERDUE(8103, "存在逾期未归还记录"),
    READER_NOT_FOUND(8104, "读者不存在"),
    READER_NO_DUPLICATED(8105, "读者证号已存在"),

    BORROWING_NOT_RETURNABLE(8201, "借阅记录状态不可归还"),
    BORROWING_FINE_UNPAID(8202, "未付清罚款不能进一步操作"),
    BORROWING_NO_AVAILABLE_COPY(8203, "未找到可借/可还复本"),

    BORROWING_NOT_RENEWABLE(8301, "该套记录不可续借"),
    BORROWING_RENEW_LIMIT_EXCEEDED(8302, "续借次数超过上限"),

    AUTH_TOKEN_MISSING(8401, "未提供有效 token"),
    AUTH_ACCESS_TOKEN_EXPIRED(8402, "登录已过期，请重新登录"),
    AUTH_REFRESH_TOKEN_INVALID(8403, "登录凭证无效"),
    AUTH_FORBIDDEN(8404, "权限不足"),
    AUTH_ACCOUNT_LOCKED(8405, "账号已被锁定，请稍后再试"),
    AUTH_USERNAME_OR_PASSWORD_INCORRECT(8406, "用户名或密码错误"),
    AUTH_USERNAME_DUPLICATED(8407, "用户名已存在"),
    USER_NOT_FOUND(8408, "用户不存在"),

    BOOK_ISBN_DUPLICATED(8501, "ISBN 已存在"),
    BOOK_COPY_BARCODE_DUPLICATED(8502, "复本条码已存在"),
    BOOK_HAS_UNRETURNED_COPY(8503, "存在未归还复本，不能删除"),
    BOOK_COPY_BORROWED(8504, "复本被借出，不能报损/删除"),

    POLICY_PARAM_INVALID(8601, "策略参数越界"),

    FINE_AMOUNT_MISMATCH(8701, "付款金额不一致"),

    SYSTEM_ERROR(9000, "系统内部错误"),
    DEPENDENCY_ERROR(9001, "依赖服务调用失败");

    private final int code;
    private final String defaultMessage;

    ResultCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
