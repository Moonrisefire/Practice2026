CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL
);

CREATE TABLE students (
    id          BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    fio         VARCHAR(255) NOT NULL,
    group_name  VARCHAR(255) NOT NULL
);

CREATE TABLE teachers (
    id          BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    fio         VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    phone       VARCHAR(50)  UNIQUE
);

CREATE TABLE admins (
    id          BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE teacher_groups (
    teacher_id  BIGINT       NOT NULL REFERENCES teachers (id) ON DELETE CASCADE,
    group_name  VARCHAR(255) NOT NULL,
    PRIMARY KEY (teacher_id, group_name)
);

CREATE TABLE registration_requests (
    id          BIGSERIAL PRIMARY KEY,
    fio         VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    group_name  VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP    NOT NULL,
    status      VARCHAR(50)  NOT NULL
);

CREATE INDEX idx_registration_requests_email ON registration_requests (email);
CREATE INDEX idx_registration_requests_status ON registration_requests (status);
CREATE INDEX idx_registration_requests_expires_at ON registration_requests (expires_at);
