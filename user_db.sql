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
