drop table if exists user;
drop table if exists team;
drop table if exists company;

create table company
(
    id   TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(50)      NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

create table team
(
    id         TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name       VARCHAR(50)      NOT NULL UNIQUE,
    company_id TINYINT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    INDEX IDX_COMPANY_ID (company_id),
    FOREIGN KEY (company_id) REFERENCES company (id)
);

create table user
(
    id      BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    name    VARCHAR(50)         NOT NULL,
    email   VARCHAR(50)         NOT NULL,
    team_id TINYINT UNSIGNED    NOT NULL,
    PRIMARY KEY (id),
    INDEX IDX_TEAM_ID (team_id),
    FOREIGN KEY (team_id) REFERENCES team (id)
);