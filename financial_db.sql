-- 1. 계좌 정보
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT,
                           `user_id` BIGINT NOT NULL,
                           `bank_name` VARCHAR(255) NOT NULL,
                           `account_number` VARCHAR(255) NOT NULL,
                           `pin_account_number` VARCHAR(255) NOT NULL,
                           `account_type` VARCHAR(255) NOT NULL,
                           `balance` DECIMAL(10,2) NOT NULL,
                           `connected_at` DATETIME DEFAULT now(),
                           PRIMARY KEY (`id`),
                           FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 2. 거래 내역
DROP TABLE IF EXISTS `transaction`;
CREATE TABLE `transaction` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT,
                               `user_id` BIGINT NOT NULL,
                               `account_id` BIGINT NOT NULL,
                               `place` VARCHAR(255) NOT NULL,
                               `date` DATETIME NOT NULL,
                               `category` VARCHAR(255),
                               `type` ENUM('INCOME', 'EXPENSE') NOT NULL,
                               `amount` DECIMAL(10,2) NOT NULL,
                               `memo` TEXT,
                               PRIMARY KEY (`id`),
                               FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                               FOREIGN KEY (`account_id`) REFERENCES `account`(`id`) ON DELETE CASCADE
);

-- 3. 월간 리포트
DROP TABLE IF EXISTS `monthreport`;
CREATE TABLE `monthreport` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `month` VARCHAR(255) NOT NULL,
    `total_expense` DECIMAL(10,2) NOT NULL,
    `total_saving` DECIMAL(10,2) NOT NULL,
    `saving_rate` DECIMAL(10,2) NOT NULL,
    `compare_expense` DECIMAL(10,2) NOT NULL,
    `compare_saving` DECIMAL(10,2) NOT NULL,
    `category_chart` TEXT NOT NULL,
    `six_month_chart` TEXT NOT NULL,
    `feedback` TEXT NOT NULL,
    `next_goal` TEXT NOT NULL,
    `created_at` DATETIME DEFAULT now(),
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 4. 예금 상품 목록
DROP TABLE IF EXISTS `deposit_list`;
CREATE TABLE `deposit_list` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `deposit_name` VARCHAR(255) NOT NULL,
    `bank_name` VARCHAR(255) NOT NULL,
    `interest_rate` FLOAT(10,2) NOT NULL,
    `period_month` INT NOT NULL,
    `description` TEXT NOT NULL,
    `deposit_link` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`)
);

-- 5. 펀드 상품 목록
DROP TABLE IF EXISTS `fund_list`;
CREATE TABLE `fund_list` (
    `id` VARCHAR(255) NOT NULL,
    `fund_name` VARCHAR(255) NOT NULL,
    `manager_name` VARCHAR(255) NOT NULL,
    `standard_price` FLOAT(10,2) NOT NULL,
    `return_1month` FLOAT(10,2) NOT NULL,
    `return_3month` FLOAT(10,2) NOT NULL,
    `return_6month` FLOAT(10,2) NOT NULL,
    `return_9month` FLOAT(10,2) NOT NULL,
    `return_12month` FLOAT(10,2) NOT NULL,
    `fund_type` ENUM('BOND', 'STOCK', 'MIXED', 'ETF') NOT NULL,
    `risk_rating` ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL,
    `description` TEXT NOT NULL,
    `fund_link` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`)
);

-- 6. 주식 상품 목록
DROP TABLE IF EXISTS `stock_list`;
CREATE TABLE `stock_list` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `stock_code` VARCHAR(255) NOT NULL,
  `stock_name` VARCHAR(255) NOT NULL,
  `market_type` ENUM('KOSPI', 'KOSDAQ') NOT NULL,
  `stock_sector` VARCHAR(255) NOT NULL,
  `stock_price` INT NOT NULL,
  `stock_per` FLOAT(10,2) NOT NULL,
  `dividend_yield` FLOAT(10,2) NOT NULL,
  PRIMARY KEY (`id`)
);

-- 7. 찜한 상품 (유저별)
DROP TABLE IF EXISTS `wishlist`;
CREATE TABLE `wishlist` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `product_type` ENUM('DEPOSIT', 'FUND', 'STOCK') NOT NULL,
    `product_id` INT NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);
