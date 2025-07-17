-- 1. 챌린지 카테고리 (예: 소비 줄이기, 저축 늘리기 등)
DROP TABLE IF EXISTS `challenge_category`;
CREATE TABLE `challenge_category` (
    `ID` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`ID`)
);

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

-- 3. 유저-챌린지 매핑 (참여 내역)
DROP TABLE IF EXISTS `user_challenge`;
CREATE TABLE `user_challenge` (
    `ID` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `challenge_id` BIGINT NOT NULL,
    `joined_at` DATE DEFAULT now(),
    `is_creator` BOOLEAN NOT NULL,
    `is_completed` BOOLEAN NOT NULL, -- 종료 여부
    `actual_value` INT DEFAULT 0,
    `is_success` BOOLEAN,
    `updated_at` DATETIME DEFAULT now(),
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
