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
    `id` INT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL,
    `summary` TEXT NOT NULL,
    `content` TEXT NOT NULL,
    `thumbnail_url` VARCHAR(255) NOT NULL,
    `published_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`)
);

-- 3. 말풍선 (사용자 감정 표현용)
DROP TABLE IF EXISTS `bubble`;
CREATE TABLE `bubble` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `message` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`)
);
