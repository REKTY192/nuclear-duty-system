-- ============================================================
--  Информационная система планирования дежурств
--  Атомная отрасль — MVP schema (PostgreSQL)
-- ============================================================

-- Типы смен (день/ночь/сутки и т.д.)
CREATE TABLE shift_types (
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,          -- напр. "Дневная", "Ночная", "Суточная"
    start_time    TIME        NOT NULL,            -- время начала смены
    duration_hours INT        NOT NULL             -- продолжительность в часах
);

-- Сотрудники
CREATE TABLE employees (
    id               SERIAL PRIMARY KEY,
    full_name        VARCHAR(255) NOT NULL,
    position         VARCHAR(150),                 -- должность
    clearance_level  INT         NOT NULL,         -- уровень допуска (1..3)
    medical_expiry   DATE        NOT NULL,         -- дата окончания медосмотра
    is_active        BOOLEAN     NOT NULL DEFAULT TRUE  -- FALSE = уволен/отстранён
);

-- Отсутствия (отпуск, больничный, командировка)
CREATE TABLE absences (
    id          SERIAL PRIMARY KEY,
    employee_id INT  NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    reason      VARCHAR(50) NOT NULL               -- 'VACATION' | 'SICK_LEAVE' | 'BUSINESS_TRIP'
);

-- Назначенные дежурства
CREATE TABLE shifts (
    id             SERIAL PRIMARY KEY,
    employee_id    INT         NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    shift_type_id  INT         NOT NULL REFERENCES shift_types(id),
    shift_date     DATE        NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED | COMPLETED | CANCELLED
    notes          TEXT
);

-- ============================================================
--  Тестовые данные
-- ============================================================

INSERT INTO shift_types (name, start_time, duration_hours) VALUES
    ('Дневная',   '08:00', 12),
    ('Ночная',    '20:00', 12),
    ('Суточная',  '08:00', 24);

INSERT INTO employees (full_name, position, clearance_level, medical_expiry, is_active) VALUES
    ('Иванов Иван Иванович',       'Оператор реакторного отделения', 2, '2026-12-01', TRUE),
    ('Петрова Мария Сергеевна',    'Дозиметрист',                    1, '2025-03-15', TRUE),  -- просрочен!
    ('Сидоров Алексей Петрович',   'Начальник смены',                3, '2026-09-30', TRUE),
    ('Козлова Елена Дмитриевна',   'Оператор БЩУ',                   2, '2026-06-20', TRUE),
    ('Новиков Дмитрий Олегович',   'Физик-ядерщик',                  3, '2025-04-30', FALSE); -- неактивен

INSERT INTO absences (employee_id, start_date, end_date, reason) VALUES
    (1, '2025-04-10', '2025-04-20', 'VACATION'),       -- Иванов в отпуске
    (3, '2025-04-05', '2025-04-08', 'SICK_LEAVE');     -- Сидоров был на больничном
