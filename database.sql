
-- USER
-- 1. 사용자 정보
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT,
                        `email` VARCHAR(255) NOT NULL,
                        `password` VARCHAR(255) NOT NULL,
                        `user_name` VARCHAR(255),
                        `phone_num` VARCHAR(255),
                        `birthday` DATE,
                        `gender` ENUM('MALE', 'FEMALE') DEFAULT NULL,
                        `created_at` DATETIME DEFAULT now(),
                        PRIMARY KEY (`id`)
);

ALTER TABLE `user`
    ADD COLUMN `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`,
    ADD COLUMN `last_pw_change_at` DATETIME DEFAULT CURRENT_TIMESTAMP AFTER `updated_at`;

ALTER TABLE `user`
    ADD COLUMN `is_verified` BOOLEAN NOT NULL DEFAULT 0 AFTER `last_pw_change_at`;

ALTER TABLE `user`
    ADD COLUMN `is_active` BOOLEAN DEFAULT TRUE;

ALTER TABLE `user`
    ADD CONSTRAINT uq_user_email UNIQUE (`email`);


-- 2. 유저 상태 (닉네임, 레벨)
DROP TABLE IF EXISTS `user_status`;
CREATE TABLE `user_status` (
                               `id` BIGINT NOT NULL,
                               `nickname` VARCHAR(255) NOT NULL,
                               `level` VARCHAR(255) NOT NULL,
                               PRIMARY KEY (`id`),
                               FOREIGN KEY (`id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 3. 유저 투자 성향
DROP TABLE IF EXISTS `investment_types`;
CREATE TABLE `investment_types` (
                                    `id` BIGINT NOT NULL,
                                    `total_score` INT NOT NULL,
                                    `propensity_type` VARCHAR(255) NOT NULL,
                                    `question1` ENUM('A','B','C') NOT NULL,
                                    `question2` ENUM('A','B','C') NOT NULL,
                                    `question3` ENUM('A','B','C') NOT NULL,
                                    `question4` ENUM('A','B','C') NOT NULL,
                                    PRIMARY KEY (`id`),
                                    FOREIGN KEY (`id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 4. 유저의 약관 동의
DROP TABLE IF EXISTS `agree`;
CREATE TABLE `agree` (
                         `id` BIGINT NOT NULL,
                         `login_agreed` BOOLEAN NOT NULL,
                         `mydata_agreed` BOOLEAN NOT NULL,
                         `push_agreed` BOOLEAN NOT NULL,
                         `login_agreed_at` DATETIME,
                         `mydata_agreed_at` DATETIME,
                         `push_agreed_at` DATETIME,
                         PRIMARY KEY (`id`),
                         FOREIGN KEY (`id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 5. mydata 정보
DROP TABLE IF EXISTS `mydata`;
CREATE TABLE `mydata` (
                          `id` BIGINT NOT NULL,
                          `job` VARCHAR(255) NOT NULL,
                          `monthly_income` DECIMAL(10,2) NOT NULL,
                          PRIMARY KEY (`id`),
                          FOREIGN KEY (`id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 6. 아바타
DROP TABLE IF EXISTS `avatar`;
CREATE TABLE `avatar` (
                          `id` BIGINT NOT NULL,
                          `avatar_image` VARCHAR(255) NOT NULL,
                          `top_id` INT,
                          `bottom_id` INT,
                          `shoes_id` INT,
                          `accessory_id` INT,
                          `hat_id` INT,
                          `background_id` INT,
                          PRIMARY KEY (`id`),
                          FOREIGN KEY (`id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 7. 아이템
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT,
                        `type` ENUM('TOP', 'BOTTOM', 'SHOES', 'ACCESSORY', 'HAT', 'BACKGROUND') NOT NULL,
                        `code` INT NOT NULL,
                        `cost` INT NOT NULL,
                        `image_url` VARCHAR(255) NOT NULL,
                        PRIMARY KEY (`id`)
);

-- 8. 착용 정보
DROP TABLE IF EXISTS `clothes`;
CREATE TABLE `clothes` (
                           `id` BIGINT NOT NULL,
                           `user_id` BIGINT NOT NULL,
                           `item_id` BIGINT NOT NULL,
                           `is_wearing` BOOLEAN NOT NULL,
                           PRIMARY KEY (`id`, `user_id`),
                           FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                           FOREIGN KEY (`item_id`) REFERENCES `item`(`id`) ON DELETE CASCADE
);

-- 9. 퀴즈
DROP TABLE IF EXISTS `quiz`;
CREATE TABLE `quiz` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT,
                        `question` TEXT NOT NULL,
                        `answer` CHAR(1) NOT NULL,
                        `explanation` TEXT NOT NULL,
                        PRIMARY KEY (`id`)
);

-- 10. 퀴즈 기록
DROP TABLE IF EXISTS `quiz_history`;
CREATE TABLE `quiz_history` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT,
                                `user_id` BIGINT NOT NULL,
                                `quiz_id` BIGINT NOT NULL,
                                `is_correct` BOOLEAN NOT NULL,
                                `submitted_at` DATETIME DEFAULT now(),
                                PRIMARY KEY (`id`),
                                FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                                FOREIGN KEY (`quiz_id`) REFERENCES `quiz`(`id`) ON DELETE CASCADE
);

-- 11. 말풍선 (감정 메시지 등)
DROP TABLE IF EXISTS `bubble`;
CREATE TABLE `bubble` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT,
                          `message` VARCHAR(255) NOT NULL,
                          PRIMARY KEY (`id`)
);

-- FINANCIAL
-- 1. 계좌 정보
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT,
                           `user_id` BIGINT NOT NULL,
                           `pin_account_number` VARCHAR(255) NOT NULL,
                           `bank_code` VARCHAR(20) NOT NULL,
                           `account_number` VARCHAR(255) NOT NULL,
                           `product_name` VARCHAR(255) NOT NULL,
                           `account_type` VARCHAR(50) NOT NULL,
                           `balance` DECIMAL(20,2) NOT NULL,
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`),
                           FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 2. 계좌 거래 내역
DROP TABLE IF EXISTS `account_transaction`;
CREATE TABLE `account_transaction` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT,
                                       `user_id` BIGINT NOT NULL,
                                       `account_id` BIGINT NOT NULL,
                                       `date` DATETIME NOT NULL,
                                       `type` ENUM('INCOME', 'EXPENSE') NOT NULL,
                                       `amount` DECIMAL(20,2) NOT NULL,
                                       `balance` DECIMAL(20,2) NOT NULL,
                                       `place` VARCHAR(255) NOT NULL,
                                       `is_cancelled` BOOLEAN NOT NULL DEFAULT FALSE,
                                       `tu_no` BIGINT NOT NULL,
                                       PRIMARY KEY (`id`),
                                       FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                                       FOREIGN KEY (`account_id`) REFERENCES `account`(`id`) ON DELETE CASCADE
);

-- 3. 카드 정보
DROP TABLE IF EXISTS `card`;
CREATE TABLE `card` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT,
                        `user_id` BIGINT NOT NULL,
                        `fin_card_number` VARCHAR(255) NOT NULL,
                        `back_code` VARCHAR(10) NOT NULL,
                        `bank_name` VARCHAR(50) NOT NULL,
                        `card_name` VARCHAR(100) NOT NULL,
                        `card_maskednum` VARCHAR(30) NOT NULL,
                        `card_member_type` ENUM('SELF', 'FAMILY') NOT NULL,
                        `card_type` ENUM('CREDIT', 'DEBIT') NOT NULL,
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                        FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 4. 카드 거래 내역
DROP TABLE IF EXISTS `card_transaction`;
CREATE TABLE `card_transaction` (
                                    `id` BIGINT NOT NULL AUTO_INCREMENT,
                                    `user_id` BIGINT NOT NULL,
                                    `card_id` BIGINT NOT NULL,
                                    `auth_number` VARCHAR(50) NOT NULL,
                                    `sales_type` ENUM('1','2','3','6','7','8') NOT NULL COMMENT '1:일시불 2:할부 3:현금서비스 6:해외일시불 7:해외할부 8:해외현금서비스',
                                    `approved_at` DATETIME NOT NULL,
                                    `payment_date` DATE NOT NULL,
                                    `amount` DECIMAL(20,2) NOT NULL,
                                    `is_cancelled` BOOLEAN NOT NULL DEFAULT FALSE,
                                    `cancel_amount` DECIMAL(20,2),
                                    `cancelled_at` DATETIME,
                                    `merchant_name` VARCHAR(100),
                                    `tpbcd` VARCHAR(20),
                                    `tpbcd_nm` VARCHAR(50),
                                    `installment_month` INT,
                                    `currency` VARCHAR(10),
                                    `foreign_amount` DECIMAL(20,2),
                                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`),
                                    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                                    FOREIGN KEY (`card_id`) REFERENCES `card`(`id`) ON DELETE CASCADE
);

-- 5. 거래내역 (통합 로그)
DROP TABLE IF EXISTS `ledger`;
CREATE TABLE `ledger` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT,
                          `user_id` BIGINT NOT NULL,
                          `source_id` BIGINT NOT NULL,
                          `account_id` BIGINT,
                          `card_id` BIGINT,
                          `source_type` ENUM('ACCOUNT', 'CARD') NOT NULL,
                          `source_name` VARCHAR(100),
                          `type` ENUM('INCOME', 'EXPENSE') NOT NULL,
                          `amount` DECIMAL(20,2) NOT NULL,
                          `category` VARCHAR(50),
                          `memo` TEXT,
                          `analysis` VARCHAR(255),
                          `date` DATETIME NOT NULL,
                          `merchant_name` VARCHAR(100),
                          `place` VARCHAR(100),
                          `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`),
                          FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 6. 월간 리포트
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

-- 7. 예금 상품 목록
DROP TABLE IF EXISTS `deposit_list`;
CREATE TABLE `deposit_list` (
                                `id` INT NOT NULL AUTO_INCREMENT,
                                `deposit_bank_name` VARCHAR(255) NOT NULL,
                                `deposit_product_name` VARCHAR(255) NOT NULL,
                                `deposit_contract_period` VARCHAR(50) NOT NULL,
                                `deposit_subscription_amount` VARCHAR(50) NOT NULL,
                                `deposit_basic_rate` FLOAT(10,2) NOT NULL,
                                `deposit_max_rate` FLOAT(10,2) NOT NULL,
                                `deposit_preferential_rate` TEXT NULL,
                                `deposit_product_features` TEXT NULL,
                                `deposit_summary` VARCHAR(255) NOT NULL,
                                `deposit_link` VARCHAR(255) NOT NULL,
                                PRIMARY KEY (`id`)
);

-- 8. 적금 상품 목록
DROP TABLE IF EXISTS `installment_list`;
CREATE TABLE `installment_list` (
                                    `id` INT NOT NULL AUTO_INCREMENT,
                                    `installment_bank_name` VARCHAR(255) NOT NULL,
                                    `installment_product_name` VARCHAR(255) NOT NULL,
                                    `installment_contract_period` VARCHAR(50) NOT NULL,
                                    `installment_type` VARCHAR(8) NOT NULL,
                                    `installment_subscription_amount` VARCHAR(50) NOT NULL,
                                    `installment_basic_rate` FLOAT(10,2) NOT NULL,
                                    `installment_max_rate` FLOAT(10,2) NOT NULL,
                                    `installment_preferential_rate` TEXT NULL,
                                    `installment_product_features` TEXT NULL,
                                    `installment_summary` VARCHAR(255) NOT NULL,
                                    `installment_link` VARCHAR(255) NULL,
                                    PRIMARY KEY (`id`)
);

-- 9. 펀드 상품 목록
DROP TABLE IF EXISTS `fund_list`;
CREATE TABLE `fund_list` (
                             `id` INT NOT NULL AUTO_INCREMENT,
                             `fund_manager` VARCHAR(255) NOT NULL,
                             `fund_product_name` VARCHAR(255) NOT NULL,
                             `fund_risk_level` VARCHAR(255) NOT NULL,
                             `fund_type` VARCHAR(20) NOT NULL,
                             `fund_returns_data` TEXT NOT NULL,
                             `fund_start_Date` VARCHAR(255) NOT NULL,
                             `fund_net_asset_value` VARCHAR(255) NOT NULL,
                             `fund_total_expense_ratio` VARCHAR(255) NOT NULL,
                             `fund_product_features` TEXT NOT NULL,
                             `fund_link` VARCHAR(255) NOT NULL,
                             PRIMARY KEY (`id`)
);

-- 10. 주식 상품 목록
DROP TABLE IF EXISTS `stock_list`;
CREATE TABLE `stock_list` (
                              `id` INT NOT NULL AUTO_INCREMENT,
                              `stock_name` VARCHAR(255) NOT NULL,
                              `stock_code` VARCHAR(255) NOT NULL,
                              `market_type` ENUM('KOSPI', 'KOSDAQ') NOT NULL,
                              `stock_summary` VARCHAR(20) NOT NULL,
                              PRIMARY KEY (`id`)
);

-- 11. 찜한 상품 (유저별)
DROP TABLE IF EXISTS `wishlist`;
CREATE TABLE `wishlist` (
                            `id` INT NOT NULL AUTO_INCREMENT,
                            `user_id` BIGINT NOT NULL,
                            `product_type` ENUM('DEPOSIT', 'INSTALLMENT', 'FUND', 'STOCK') NOT NULL,
                            `product_name` VARCHAR(255) NOT NULL,
                            PRIMARY KEY (`id`),
                            FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 12. 키움증권 rest api 접근 토큰
DROP TABLE IF EXISTS `user_kiwoom_access_token`;
CREATE TABLE `user_kiwoom_access_token` (
                                            `id`       BIGINT       NOT NULL,
                                            `user_account` VARCHAR(255) NOT NULL,
                                            `stock_access_token` VARCHAR(255) NOT NULL,
                                            `stock_token_expires_dt` VARCHAR(255) NOT NULL,
                                            PRIMARY KEY (`id`),
                                            FOREIGN KEY (`id`) REFERENCES `user` (`id`) ON DELETE CASCADE
);

-- 13. 주식 차트 데이터
DROP TABLE IF EXISTS `stock_chart_cache`;
CREATE TABLE `stock_chart_cache` (
                                    `stock_code` VARCHAR(20) NOT NULL,
                                    `json_data` TEXT NOT NULL,
                                    `base_date` VARCHAR(8) NOT NULL,
                                    PRIMARY KEY (`stock_code`)
);

-- CHALLENGE
-- 1. 챌린지 카테고리 (예: 소비 줄이기, 저축 늘리기 등)
DROP TABLE IF EXISTS `challenge_category`;
CREATE TABLE `challenge_category` (
                                      `ID` BIGINT NOT NULL AUTO_INCREMENT,
                                      `name` VARCHAR(255) NOT NULL,
                                      PRIMARY KEY (`ID`)
);

ALTER TABLE challenge_category ADD memo VARCHAR(255) DEFAULT NULL;


-- 2. 챌린지 메인 테이블
DROP TABLE IF EXISTS `challenge`;
CREATE TABLE `challenge` (
                             `ID` BIGINT NOT NULL AUTO_INCREMENT,
                             `title` VARCHAR(255) NOT NULL,
                             `category_id` BIGINT NOT NULL,
                             `start_date` DATE NOT NULL,
                             `end_date` DATE NOT NULL,
                             `description` TEXT NOT NULL,
                             `type` ENUM('COMMON', 'PERSONAL', 'GROUP') NOT NULL,
                             `max_participants` INT NOT NULL,
                             `password` INT,
                             `writer_id` BIGINT NOT NULL,
                             `status` ENUM('RECRUITING', 'IN_PROGRESS', 'COMPLETED') NOT NULL,
                             `goal_type` VARCHAR(255) NOT NULL,
                             `goal_value` INT NOT NULL,
                             `created_at` DATETIME DEFAULT now(),
                             PRIMARY KEY (`ID`),
                             FOREIGN KEY (`category_id`) REFERENCES `challenge_category`(`ID`) ON DELETE CASCADE,
                             FOREIGN KEY (`writer_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);
ALTER TABLE challenge ADD use_password BOOLEAN DEFAULT FALSE;
ALTER TABLE challenge ADD COLUMN participant_count INT DEFAULT 0;


-- 3. 유저-챌린지 매핑 (참여 내역)
DROP TABLE IF EXISTS `user_challenge`;
CREATE TABLE `user_challenge` (
                                  `ID` BIGINT NOT NULL AUTO_INCREMENT,
                                  `user_id` BIGINT NOT NULL,
                                  `challenge_id` BIGINT NOT NULL,
                                  `joined_at` DATETIME DEFAULT now(),
                                  `is_creator` BOOLEAN NOT NULL,
                                  `is_completed` BOOLEAN NOT NULL, -- 종료 여부
                                  `actual_value` INT DEFAULT 0,
                                  `is_success` BOOLEAN,
                                  `updated_at` DATETIME DEFAULT now(),
                                  PRIMARY KEY (`ID`),
                                  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                                  FOREIGN KEY (`challenge_id`) REFERENCES `challenge`(`ID`) ON DELETE CASCADE
);

ALTER TABLE user_challenge CHANGE joined_at created_at DATETIME DEFAULT CURRENT_TIMESTAMP;


-- 4. 챌린지 진행 통계 (유저별 집계)
DROP TABLE IF EXISTS `user_challenge_summary`;
CREATE TABLE `user_challenge_summary` (
                                          `id` BIGINT NOT NULL, -- user_id와 동일
                                          `total_challenges` INT NOT NULL,
                                          `success_count` INT NOT NULL,
                                          `achievement_rate` DECIMAL(5,2) NOT NULL DEFAULT 0,
                                          `updated_at` DATETIME DEFAULT now(),
                                          PRIMARY KEY (`id`),
                                          FOREIGN KEY (`id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 5. 챌린지 랭킹
DROP TABLE IF EXISTS `challenge_rank`;
CREATE TABLE `challenge_rank` (
                                  `ID` BIGINT NOT NULL AUTO_INCREMENT,
                                  `user_challenge_id` BIGINT NOT NULL,
                                  `rank` INT NOT NULL,
                                  `progress_rate` DECIMAL(5,2) NOT NULL DEFAULT 0,
                                  `updated_at` DATETIME DEFAULT now(),
                                  PRIMARY KEY (`ID`),
                                  FOREIGN KEY (`user_challenge_id`) REFERENCES `user_challenge`(`ID`) ON DELETE CASCADE
);


-- CONTENT
-- 1. 금융용어 사전
DROP TABLE IF EXISTS `dictionary`;
CREATE TABLE `dictionary` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT,
                              `term` VARCHAR(255) NOT NULL,      -- 용어
                              `definition` TEXT NOT NULL,        -- 설명
                              PRIMARY KEY (`id`)
);

-- 2. 핀픽 콘텐츠 피드 (뉴스/칼럼 등)
DROP TABLE IF EXISTS `finpik`;
CREATE TABLE `finpik` (
                          `id` INT NOT NULL AUTO_INCREMENT, -- 기사고유id
                          `title` VARCHAR(500) NOT NULL, -- 기사제목
                          `summary` TEXT NOT NULL, -- 기사요약
                          `link` varchar(500) NOT NULL, -- 기사링크
                          `published_at` DATETIME NOT NULL, -- 기사생성날짜
                          PRIMARY KEY (`id`)
);

-- 3. 말풍선 (사용자 감정 표현용)
DROP TABLE IF EXISTS `bubble`;
CREATE TABLE `bubble` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT,
                          `message` VARCHAR(255) NOT NULL,
                          PRIMARY KEY (`id`)
);