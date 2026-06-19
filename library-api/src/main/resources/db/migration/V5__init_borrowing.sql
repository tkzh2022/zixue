CREATE TABLE borrow_rule (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reader_type VARCHAR(32) NOT NULL,
    max_borrow_days INT NOT NULL,
    max_borrow_count INT NOT NULL,
    max_renew_count INT NOT NULL,
    fine_per_day DECIMAL(10, 2) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_rule_reader_type (reader_type)
) COMMENT='借阅规则表';

CREATE TABLE borrow_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reader_id BIGINT NOT NULL,
    book_copy_id BIGINT NOT NULL,
    borrow_time DATETIME(3) NOT NULL,
    due_date DATE NOT NULL,
    return_time DATETIME(3) NULL,
    renew_count INT NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_borrow_reader (reader_id),
    KEY idx_borrow_copy (book_copy_id),
    KEY idx_borrow_status (status)
) COMMENT='借阅记录表';

CREATE TABLE fine_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    borrow_record_id BIGINT NOT NULL,
    reader_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    reason VARCHAR(128) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    paid_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_fine_borrow (borrow_record_id),
    KEY idx_fine_reader (reader_id)
) COMMENT='罚款记录表';

-- 插入默认规则
INSERT INTO borrow_rule (reader_type, max_borrow_days, max_borrow_count, max_renew_count, fine_per_day, created_at, updated_at)
VALUES ('DEFAULT', 30, 5, 1, 0.50, NOW(), NOW());
