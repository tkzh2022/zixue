CREATE TABLE reader (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_account_id BIGINT NOT NULL,
    reader_no VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    phone VARCHAR(32) NULL,
    email VARCHAR(128) NULL,
    status VARCHAR(16) NOT NULL,
    register_date DATE NOT NULL,
    deleted_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reader_no (reader_no),
    UNIQUE KEY uk_reader_user_account (user_account_id),
    KEY idx_reader_name (name)
) COMMENT='读者信息表';
