-- =================================================================
-- USER DOMAIN
-- =================================================================

-- 1. 사용자 기본 정보
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT,
                        `email` VARCHAR(255) NOT NULL,
                        `password` VARCHAR(255) NOT NULL,
                        `auth_pw` VARCHAR(255) NULL,
                        `user_name` VARCHAR(255) NULL,
                        `phone_num` VARCHAR(255) NULL,
                        `birthday` DATE NULL,
                        `gender` ENUM('MALE', 'FEMALE') DEFAULT NULL,
                        `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        `last_pw_change_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                        `is_verified` BOOLEAN NOT NULL DEFAULT 0,
                        `is_active` BOOLEAN DEFAULT TRUE,
                        PRIMARY KEY (`id`),
                        CONSTRAINT `uq_user_email` UNIQUE (`email`)
);

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
                                    `question1` VARCHAR(255) NOT NULL,
                                    `question2` VARCHAR (255) NOT NULL,
                                    `question3` VARCHAR(255) NOT NULL,
                                    `question4` VARCHAR(255)NOT NULL,
                                    `question5` VARCHAR(255) NOT NULL,
                                    `question6` VARCHAR(255) NULL,
                                    `question7` VARCHAR(255) NULL,
                                    `question8` VARCHAR(255) NULL,
                                    `question9` VARCHAR(255) NULL,
                                    `question10` VARCHAR(255) NULL,

                                    PRIMARY KEY (`id`),
                                    FOREIGN KEY (`id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 4. 유저 약관 동의
DROP TABLE IF EXISTS `agree`;
CREATE TABLE `agree` (
                         `id` BIGINT NOT NULL,
                         `open_banking_agreed` BOOLEAN DEFAULT false,
                         `personal_info_agreed` BOOLEAN DEFAULT false,
                         `ars_agreed` BOOLEAN DEFAULT false,
                         `open_banking_agreed_at` DATETIME NULL,
                         `personal_info_agreed_at` DATETIME NULL,
                         `ars_agreed_at` DATETIME NULL,
                         PRIMARY KEY (`id`),
                         CONSTRAINT `fk_agree_user_id` FOREIGN KEY (`id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 5. 마이데이터 정보
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
                          `level_id` BIGINT NOT NULL,
                          `top_id` BIGINT,
                          `shoes_id` BIGINT,
                          `accessory_id` BIGINT,
                          `gift_card_id` BIGINT,
                          PRIMARY KEY (`id`),
                          FOREIGN KEY (`id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 7. 아이템
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT,
                        `name` VARCHAR(255) NOT NULL,
                        `type` ENUM('level', 'top', 'shoes', 'accessory', 'giftCard') NOT NULL,
                        `cost` INT NOT NULL,
                        `image_url` VARCHAR(255) NOT NULL,
                        PRIMARY KEY (`id`)
);

insert into item values ('1','금융새싹','level',0,'s3://finpickbucket/level/SEEDLING/sprout.png'),
                        ('2','금융견습','level',0,'s3://finpickbucket/level/TRAINEE/beginner.png'),
                        ('3','금융법사','level',0,'s3://finpickbucket/level/WIZARD/wizardhat.png'),
                        ('4','금융도사','shoes',0,'s3://finpickbucket/level/MASTER/dosa.png'),
                        ('30','블러셔','accessory', 500, 's3://finpickbucket/accessory/blush.png'),
                        ('31','선글라스','accessory',500,'s3://finpickbucket/accessory/sunglasses.png');


-- 8. 옷장
DROP TABLE IF EXISTS `clothes`;
CREATE TABLE `clothes` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT,
                           `user_id` BIGINT NOT NULL,
                           `item_id` BIGINT NOT NULL,
                           `is_wearing` BOOLEAN NOT NULL,
                           PRIMARY KEY (`id`, `user_id`),
                           FOREIGN KEY (`user_id`) REFERENCES `avatar`(`id`) ON DELETE CASCADE,
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

INSERT INTO `quiz` (`question`, `answer`, `explanation`) VALUES
                                                             ('주식은 기업의 소유권을 나타내며, 채권은 기업에 대한 대여금을 나타낸다.', 'O', '주식은 기업의 소유 지분을 나타내는 반면, 채권은 기업이 투자자에게서 빌린 돈에 대한 채무 증서입니다.'),
                                                             ('예금은 원금 손실의 위험이 없는 금융 상품이다.', 'O', '예금자 보호법에 따라 일정 금액까지 원금이 보장되므로, 가장 안전한 금융 상품 중 하나입니다.'),
                                                             ('인플레이션이 발생하면 돈의 실질 가치는 상승한다.', 'X', '인플레이션(물가 상승)은 화폐의 구매력을 떨어뜨려 돈의 실질 가치를 하락시킵니다.'),
                                                             ('신용카드를 사용하고 대금을 갚지 못해 다음 달로 이월하는 것을 리볼빙이라고 한다.', 'O', '리볼빙은 신용카드 대금 중 일부만 결제하고 나머지를 다음 달로 이월하는 서비스입니다. 높은 이자가 부과될 수 있습니다.'),
                                                             ('분산 투자는 투자 위험을 줄이는 효과적인 방법이다.', 'O', '여러 자산에 나누어 투자함으로써 특정 종목의 손실이 전체 포트폴리오에 미치는 영향을 줄일 수 있습니다.'),
                                                             ('가계부를 작성하는 것은 개인 금융 관리에서 중요하지 않다.', 'X', '가계부 작성을 통해 지출을 파악하고 불필요한 소비를 줄이는 등 효율적인 자산 관리가 가능합니다.'),
                                                             ('만기일이 정해져 있지 않은 채권도 존재한다.', 'X', '채권은 발행 시점에 만기일과 이자 지급 방식이 정해지는 것이 일반적입니다.'),
                                                             ('기업이 처음으로 주식을 일반인에게 공개하고 상장하는 것을 IPO라고 한다.', 'O', 'IPO(Initial Public Offering)는 기업 공개를 의미하며, 비상장 기업이 주식 시장에 상장되는 절차입니다.'),
                                                             ('변동금리 대출은 시장 금리가 올라도 이자가 변하지 않는다.', 'X', '변동금리 대출은 시장 금리 변동에 따라 이자율이 달라지므로, 금리 상승 시 이자 부담이 커집니다.'),
                                                             ('펀드 투자는 원금 보장이 되지 않는다.', 'O', '펀드는 주식, 채권 등에 투자하여 수익을 추구하는 상품으로, 투자 성과에 따라 원금 손실이 발생할 수 있습니다.')
;

-- 10. 퀴즈 기록
DROP TABLE IF EXISTS `quiz_history`;
CREATE TABLE `quiz_history` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT,
                                `user_id` BIGINT NOT NULL,
                                `quiz_id` BIGINT NOT NULL,
                                `is_correct` BOOLEAN NOT NULL,
                                `submitted_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                                FOREIGN KEY (`quiz_id`) REFERENCES `quiz`(`id`) ON DELETE CASCADE
);

-- 11. 재화 정보
DROP TABLE IF EXISTS `coin`;
CREATE TABLE `coin` (
                        `id` BIGINT NOT NULL,
                        `amount` BIGINT NOT NULL,
                        `cumulative_amount` BIGINT NOT NULL DEFAULT 0,
                        `monthly_cumulative_amount` BIGINT NOT NULL DEFAULT 0,
                        `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                        FOREIGN KEY (`id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 12. 재화 내역
DROP TABLE IF EXISTS `coin_history`;
CREATE TABLE `coin_history` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT,
                                `user_id` BIGINT NOT NULL,
                                `amount` BIGINT NOT NULL,
                                `type` ENUM('plus', 'minus') NOT NULL,
                                `coin_type` ENUM('QUIZ', 'CHALLENGE', 'AVATAR', 'GIFTICON') NOT NULL,
                                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                FOREIGN KEY (`user_id`) REFERENCES `coin`(`id`) ON DELETE CASCADE
);

-- 13. 알람 내역
DROP TABLE IF EXISTS `alarms`;
CREATE TABLE `alarms` (
                          `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                          `user_id` BIGINT NOT NULL,
                          `message` VARCHAR(500) NOT NULL,
                          `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
                          `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT `fk_user_id` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);


-- =================================================================
-- FINANCIAL DOMAIN
-- =================================================================

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
                           `is_active` BOOLEAN DEFAULT TRUE,
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
                                       `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
                        `is_active` BOOLEAN DEFAULT TRUE,
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
                                    `cancel_amount` DECIMAL(20,2) NULL,
                                    `cancelled_at` DATETIME NULL,
                                    `merchant_name` VARCHAR(100) NULL,
                                    `tpbcd` VARCHAR(20) NULL,
                                    `tpbcd_nm` VARCHAR(50) NULL,
                                    `installment_month` INT NULL,
                                    `currency` VARCHAR(10) NULL,
                                    `foreign_amount` DECIMAL(20,2) NULL,
                                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uq_card_tx` (`user_id`,`card_id`,`auth_number`,`approved_at`),
                                    KEY `idx_card_last` (`card_id`,`approved_at`),
                                    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                                    FOREIGN KEY (`card_id`) REFERENCES `card`(`id`) ON DELETE CASCADE
);

-- 5. 가계부 카테고리
DROP TABLE IF EXISTS `tr_category`;
CREATE TABLE `tr_category` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT,
                               `name` VARCHAR(50) NOT NULL UNIQUE,
                               `label` VARCHAR(50) NOT NULL,
                               PRIMARY KEY (`id`)
);

-- 🎯 초기 카테고리 데이터
INSERT INTO `tr_category` (name, label) VALUES
                                            ('food', '식비'),
                                            ('cafe', '카페/간식'),
                                            ('shopping', '쇼핑/미용'),
                                            ('mart', '편의점/마트/잡화'),
                                            ('house', '주거/통신'),
                                            ('hobby', '취미/여가'),
                                            ('transport', '교통/자동차'),
                                            ('finance', '보험 및 기타 금융'),
                                            ('subscription', '구독'),
                                            ('transfer', '이체'),
                                            ('etc', '기타'),
                                            ('uncategorized', '카테고리 없음');

-- 6. 거래 내역 (통합 로그)
DROP TABLE IF EXISTS `ledger`;
CREATE TABLE `ledger` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT,
                          `user_id` BIGINT NOT NULL,
                          `source_id` BIGINT NOT NULL,
                          `account_id` BIGINT NULL,
                          `card_id` BIGINT NULL,
                          `source_type` ENUM('ACCOUNT', 'CARD') NOT NULL,
                          `source_name` VARCHAR(100) NULL,
                          `type` ENUM('INCOME', 'EXPENSE') NOT NULL,
                          `amount` DECIMAL(20,2) NOT NULL,
                          `category_id` BIGINT NOT NULL,
                          `memo` TEXT NULL,
                          `analysis` VARCHAR(255) NULL,
                          `date` DATETIME NOT NULL,
                          `merchant_name` VARCHAR(100) NULL,
                          `place` VARCHAR(100) NULL,
                          `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`),
                          KEY `idx_ledger_card_date` (`card_id`,`date`),
                          FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                          FOREIGN KEY (`category_id`) REFERENCES `tr_category`(`id`) ON DELETE RESTRICT
);

-- 7. 월간 리포트
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
                               `pattern_label` VARCHAR(50) NOT NULL,
                               `feedback` TEXT NOT NULL,
                               `next_goal` TEXT NOT NULL,
                               `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`),
                               FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 8. 월말 자산 스냅샷
DROP TABLE IF EXISTS `monthly_snapshot`;
CREATE TABLE IF NOT EXISTS `monthly_snapshot` (
                                                id BIGINT NOT NULL AUTO_INCREMENT,
                                                user_id BIGINT NOT NULL,
                                                month CHAR(7) NOT NULL,                          -- 'YYYY-MM'
                                                total_asset DECIMAL(20,2) NOT NULL DEFAULT 0,    -- 월말 총자산
                                                income DECIMAL(20,2) NOT NULL DEFAULT 0,        -- 월 총수입
                                                total_amount DECIMAL(20,2) NOT NULL DEFAULT 0,   -- 월 총지출
                                                computed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                PRIMARY KEY (id),
                                                UNIQUE KEY uniq_user_month (user_id, month),
                                                FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);


-- 9. 예금 상품 목록
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

-- 10. 적금 상품 목록
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

-- 11. 펀드 상품 목록
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

-- 12. 주식 상품 목록
DROP TABLE IF EXISTS `stock_list`;
CREATE TABLE `stock_list` (
                              `id` INT NOT NULL AUTO_INCREMENT,
                              `stock_name` VARCHAR(255) NOT NULL,
                              `stock_code` VARCHAR(255) NOT NULL,
                              `stock_returns_data` TEXT NULL,
                              `market_type` ENUM('KOSPI', 'KOSDAQ') NOT NULL,
                              `stock_summary` VARCHAR(20) NOT NULL,
                              `stock_div` VARCHAR(100) NULL,
                              PRIMARY KEY (`id`)
);

-- 13. 찜한 상품 (유저별)
DROP TABLE IF EXISTS `wishlist`;
CREATE TABLE `wishlist` (
                            `id` INT NOT NULL AUTO_INCREMENT,
                            `user_id` BIGINT NOT NULL,
                            `product_type` ENUM('DEPOSIT', 'INSTALLMENT', 'FUND', 'STOCK') NOT NULL,
                            `product_id` INT NOT NULL,
                            PRIMARY KEY (`id`),
                            FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 14. 키움증권 REST API 접근 토큰
DROP TABLE IF EXISTS `user_kiwoom_access_token`;
CREATE TABLE `user_kiwoom_access_token` (
                                            `id` BIGINT NOT NULL,
                                            `user_account` VARCHAR(255) NOT NULL,
                                            `stock_access_token` VARCHAR(255) NOT NULL,
                                            `stock_token_expires_dt` VARCHAR(255) NOT NULL,
                                            PRIMARY KEY (`id`),
                                            FOREIGN KEY (`id`) REFERENCES `user` (`id`) ON DELETE CASCADE
);

-- 15. 팩터값
DROP TABLE IF EXISTS `factor_list`;
CREATE TABLE `factor_list`(
                              `id` INT NOT NULL AUTO_INCREMENT,
                              `date` VARCHAR(8) NOT NULL,
                              `smb` DECIMAL(10, 6) NOT NULL,
                              `hml` DECIMAL(10, 6) NOT NULL,
                              `mom` DECIMAL(10, 6) NOT NULL,
                              `kospi` DECIMAL(10, 6) NOT NULL,
                              `kosdaq` DECIMAL(10, 6) NOT NULL,
                              PRIMARY KEY (`id`)
);


-- =================================================================
-- CHALLENGE DOMAIN
-- =================================================================

-- 1. 챌린지 카테고리
DROP TABLE IF EXISTS `challenge_category`;
CREATE TABLE `challenge_category` (
                                      `ID` BIGINT NOT NULL AUTO_INCREMENT,
                                      `name` VARCHAR(255) NOT NULL,
                                      `memo` VARCHAR(255) DEFAULT NULL,
                                      PRIMARY KEY (`ID`)
);

-- 🎯 챌린지 초기 카테고리 데이터
INSERT INTO `challenge_category` (name, memo) VALUES
                                                  ('total', '전체 소비 줄이기'),
                                                  ('food', '식비 줄이기'),
                                                  ('cafe', '카페/간식 줄이기'),
                                                  ('transport', '교통비 줄이기'),
                                                  ('shopping', '쇼핑/미용');

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
                             `password` INT NULL,
                             `use_password` BOOLEAN DEFAULT FALSE,
                             `writer_id` BIGINT NOT NULL,
                             `status` ENUM('RECRUITING', 'CLOSED','IN_PROGRESS', 'COMPLETED') NOT NULL,
                             `goal_type` VARCHAR(255) NOT NULL,
                             `goal_value` INT NOT NULL,
                             `reward_point` INT NOT NULL,
                             `participant_count` INT DEFAULT 0,
                             `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (`ID`),
                             FOREIGN KEY (`category_id`) REFERENCES `challenge_category`(`ID`) ON DELETE CASCADE,
                             FOREIGN KEY (`writer_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 3. 유저-챌린지 매핑 (참여 내역)
DROP TABLE IF EXISTS `user_challenge`;
CREATE TABLE `user_challenge` (
                                  `ID` BIGINT NOT NULL AUTO_INCREMENT,
                                  `user_id` BIGINT NOT NULL,
                                  `challenge_id` BIGINT NOT NULL,
                                  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  `is_creator` BOOLEAN NOT NULL,
                                  `is_completed` BOOLEAN NOT NULL,
                                  `actual_value` INT DEFAULT 0,
                                  `actual_reward_point` INT DEFAULT 0,
                                  `is_success` BOOLEAN NULL,
                                  `result_checked` TINYINT(1) DEFAULT 0,
                                  PRIMARY KEY (`ID`),
                                  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                                  FOREIGN KEY (`challenge_id`) REFERENCES `challenge`(`ID`) ON DELETE CASCADE
);

-- 4. 챌린지 진행 통계 (유저별 집계)
DROP TABLE IF EXISTS `user_challenge_summary`;
CREATE TABLE `user_challenge_summary` (
                                          `id` BIGINT NOT NULL, -- user_id와 동일
                                          `total_challenges` INT NOT NULL,
                                          `success_count` INT NOT NULL,
                                          `achievement_rate` DECIMAL(5,2) NOT NULL DEFAULT 0,
                                          `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                          PRIMARY KEY (`id`),
                                          FOREIGN KEY (`id`) REFERENCES `user`(`id`) ON DELETE CASCADE
);

-- 5. 챌린지 랭킹 (실시간)
DROP TABLE IF EXISTS `challenge_rank`;
CREATE TABLE `challenge_rank` (
                                  `ID` BIGINT NOT NULL AUTO_INCREMENT,
                                  `user_challenge_id` BIGINT NOT NULL,
                                  `rank` INT NOT NULL,
                                  `progress_rate` DECIMAL(5,2) NOT NULL DEFAULT 0,
                                  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`ID`),
                                  FOREIGN KEY (`user_challenge_id`) REFERENCES `user_challenge`(`ID`) ON DELETE CASCADE
);

-- 6. 챌린지 랭킹 스냅샷 (월별 최종 랭킹)
DROP TABLE IF EXISTS `challenge_rank_snapshot`;
CREATE TABLE `challenge_rank_snapshot` (
                                           `id` BIGINT NOT NULL AUTO_INCREMENT,
                                           `user_challenge_id` BIGINT NOT NULL,
                                           `month` VARCHAR(7) NOT NULL COMMENT '예: 2025-08',
                                           `rank` INT NOT NULL,
                                           `progress_rate` DECIMAL(5,2) NOT NULL DEFAULT 0,
                                           `is_checked` TINYINT(1) NOT NULL DEFAULT 0,
                                           `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                           PRIMARY KEY (`id`),
                                           FOREIGN KEY (`user_challenge_id`) REFERENCES `user_challenge`(`ID`) ON DELETE CASCADE
);

-- 7. 챌린지 누적 포인트 랭킹 (실시간)
DROP TABLE IF EXISTS `challenge_coin_rank`;
CREATE TABLE `challenge_coin_rank` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT,
                                       `user_id` BIGINT NOT NULL,
                                       `month` VARCHAR(7) NOT NULL COMMENT '예: 2025-08',
                                       `rank` INT NOT NULL,
                                       `cumulative_point` BIGINT NOT NULL,
                                       `challenge_count` INT NOT NULL,
                                       `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uniq_user_month` (`user_id`, `month`),
                                       FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
);

-- 8. 챌린지 누적 포인트 랭킹 스냅샷 (월별 최종 랭킹)
DROP TABLE IF EXISTS `challenge_coin_rank_snapshot`;
CREATE TABLE `challenge_coin_rank_snapshot` (
                                                `id` BIGINT NOT NULL AUTO_INCREMENT,
                                                `user_id` BIGINT NOT NULL,
                                                `month` VARCHAR(7) NOT NULL COMMENT '예: 2025-08',
                                                `rank` INT NOT NULL,
                                                `cumulative_point` BIGINT NOT NULL,
                                                `challenge_count` INT NOT NULL,
                                                `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                                PRIMARY KEY (`id`),
                                                UNIQUE KEY `uniq_user_month` (`user_id`, `month`),
                                                FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
);


-- =================================================================
-- CONTENT DOMAIN
-- =================================================================

-- 1. 금융 용어 사전
DROP TABLE IF EXISTS `dictionary`;
CREATE TABLE `dictionary` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT,
                              `term` VARCHAR(255) NOT NULL,
                              `definition` TEXT NOT NULL,
                              PRIMARY KEY (`id`)
);

-- 2. 핀픽 콘텐츠 피드 (뉴스/칼럼 등)
DROP TABLE IF EXISTS `finpik`;
CREATE TABLE `finpik` (
                          `id` INT NOT NULL AUTO_INCREMENT,
                          `title` VARCHAR(500) NOT NULL,
                          `summary` TEXT NOT NULL,
                          `link` VARCHAR(500) NOT NULL,
                          `published_at` DATETIME NOT NULL,
                          `keyword` VARCHAR(255) NOT NULL,
                          PRIMARY KEY (`id`)
);

-- 3. 말풍선 (감정 메시지 등)
DROP TABLE IF EXISTS `bubble`;
CREATE TABLE `bubble` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT,
                          `message` VARCHAR(255) NOT NULL,
                          PRIMARY KEY (`id`)
);