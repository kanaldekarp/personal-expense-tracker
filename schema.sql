-- ============================================================
-- QuickExpense — Cloud Database Schema (Neon PostgreSQL)
-- Run this SQL in the Neon SQL Editor to set up all tables
-- ============================================================

-- 1. Users table
CREATE TABLE IF NOT EXISTS users (
    id             SERIAL          PRIMARY KEY,
    username       VARCHAR(50)     NOT NULL UNIQUE,
    password       VARCHAR(100)    NOT NULL,
    email          VARCHAR(100)    NOT NULL,
    currency       VARCHAR(10)     DEFAULT 'INR',
    profile_pic    TEXT,
    created_at     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 2. Expenses table
CREATE TABLE IF NOT EXISTS expenses (
    id             SERIAL          PRIMARY KEY,
    user_id        INTEGER         REFERENCES users(id),
    title          VARCHAR(100),
    category       VARCHAR(50),
    amount         NUMERIC(10,2),
    date           DATE,
    description    TEXT,
    tags           TEXT
);

-- 3. Budgets table
CREATE TABLE IF NOT EXISTS budgets (
    id              SERIAL          PRIMARY KEY,
    user_id         INTEGER         NOT NULL REFERENCES users(id),
    category        VARCHAR(50)     NOT NULL,
    budget_amount   NUMERIC(10,2)   NOT NULL,
    month           INTEGER         NOT NULL,
    year            INTEGER         NOT NULL,
    UNIQUE (user_id, category, month, year)
);

-- 4. Income table
CREATE TABLE IF NOT EXISTS income (
    id             SERIAL          PRIMARY KEY,
    user_id        INTEGER         REFERENCES users(id),
    source         VARCHAR(100)    NOT NULL,
    amount         NUMERIC(10,2)   NOT NULL,
    date           DATE            NOT NULL,
    is_recurring   BOOLEAN         DEFAULT FALSE,
    notes          TEXT
);

-- 5. Recurring expenses table
CREATE TABLE IF NOT EXISTS recurring_expenses (
    id             SERIAL          PRIMARY KEY,
    user_id        INTEGER         REFERENCES users(id),
    title          VARCHAR(100)    NOT NULL,
    category       VARCHAR(50)     NOT NULL,
    amount         NUMERIC(10,2)   NOT NULL,
    frequency      VARCHAR(20)     DEFAULT 'monthly',
    next_due       DATE            NOT NULL,
    description    TEXT,
    is_active      BOOLEAN         DEFAULT TRUE
);

-- 6. Savings goals table
CREATE TABLE IF NOT EXISTS savings_goals (
    id              SERIAL          PRIMARY KEY,
    user_id         INTEGER         REFERENCES users(id),
    name            VARCHAR(100)    NOT NULL,
    target_amount   NUMERIC(10,2)   NOT NULL,
    saved_amount    NUMERIC(10,2)   DEFAULT 0,
    deadline        DATE,
    icon            VARCHAR(50)     DEFAULT 'fa-bullseye',
    color           VARCHAR(20)     DEFAULT '#4f46e5',
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 7. Tags table
CREATE TABLE IF NOT EXISTS tags (
    id             SERIAL          PRIMARY KEY,
    user_id        INTEGER         REFERENCES users(id),
    name           VARCHAR(50)     NOT NULL,
    color          VARCHAR(20)     DEFAULT '#6366f1',
    UNIQUE (user_id, name)
);

-- 8. Expense-Tags join table
CREATE TABLE IF NOT EXISTS expense_tags (
    expense_id     INTEGER         REFERENCES expenses(id) ON DELETE CASCADE,
    tag_id         INTEGER         REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (expense_id, tag_id)
);

-- 9. Password reset tokens table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id             SERIAL          PRIMARY KEY,
    user_id        INTEGER         NOT NULL REFERENCES users(id),
    token          VARCHAR(255)    NOT NULL,
    expires_at     TIMESTAMP       NOT NULL,
    used           BOOLEAN         DEFAULT FALSE
);

-- Done! All tables created successfully.
