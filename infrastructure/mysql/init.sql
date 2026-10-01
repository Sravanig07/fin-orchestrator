-- ==============================================================
-- FinOrchestrator Production Database Schema & Seed Data
-- ==============================================================

CREATE DATABASE IF NOT EXISTS fin_orchestrator CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fin_orchestrator;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. User Preferences Table
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    monthly_budget DECIMAL(15, 2) NOT NULL DEFAULT 80000.00,
    risk_tolerance VARCHAR(30) NOT NULL DEFAULT 'MODERATE',
    savings_target_percentage DECIMAL(5, 2) NOT NULL DEFAULT 20.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Accounts Table (Optimistic locking with version)
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_number VARCHAR(64) NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(50) NOT NULL, -- CHECKING, SAVINGS, CREDIT_CARD, INVESTMENT
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_accounts_user (user_id),
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL, -- NULL means system default category
    name VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL, -- INCOME, EXPENSE
    icon VARCHAR(50) DEFAULT 'tag',
    color VARCHAR(20) DEFAULT '#4F46E5',
    budget_limit DECIMAL(15, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_categories_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    type VARCHAR(30) NOT NULL, -- DEBIT, CREDIT
    description VARCHAR(255) NOT NULL,
    merchant VARCHAR(150),
    transaction_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
    idempotency_key VARCHAR(100) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tx_user_date (user_id, transaction_date DESC),
    INDEX idx_tx_account (account_id),
    INDEX idx_tx_category (category_id),
    CONSTRAINT fk_tx_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_tx_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_tx_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Recurring Payments Table
CREATE TABLE IF NOT EXISTS recurring_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    merchant VARCHAR(150) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    frequency VARCHAR(30) NOT NULL, -- WEEKLY, MONTHLY, QUARTERLY, YEARLY
    next_due_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, PAUSED, CANCELLED
    auto_debit BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_recurring_user_due (user_id, next_due_date),
    CONSTRAINT fk_rec_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_rec_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_rec_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Contracts Table
CREATE TABLE IF NOT EXISTS contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    contract_type VARCHAR(50) NOT NULL, -- LOAN, INSURANCE, CREDIT_CARD, LEASE, SUBSCRIPTION
    counterparty VARCHAR(150) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    renewal_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, EXPIRED, TERMINATED
    version INT NOT NULL DEFAULT 1,
    document_id VARCHAR(100),
    file_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_contracts_user (user_id, status),
    CONSTRAINT fk_contracts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Contract Terms Table
CREATE TABLE IF NOT EXISTS contract_terms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    term_key VARCHAR(100) NOT NULL, -- INTEREST_RATE, PRINCIPAL, PREMIUM, GRACE_PERIOD, EARLY_REPAYMENT_FEE, LATE_PENALTY
    term_value VARCHAR(255) NOT NULL,
    unit VARCHAR(50),
    description TEXT,
    confidence_score DECIMAL(5, 2) DEFAULT 1.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_terms_contract (contract_id),
    CONSTRAINT fk_terms_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Contract Clauses Table (For RAG Source Citation)
CREATE TABLE IF NOT EXISTS contract_clauses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    clause_title VARCHAR(200) NOT NULL,
    section_number VARCHAR(50),
    page_number INT NOT NULL,
    chunk_id VARCHAR(100) NOT NULL,
    clause_type VARCHAR(50) NOT NULL, -- PENALTY, FEE, OBLIGATION, TERMINATION, RENEWAL
    clause_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_clauses_contract (contract_id),
    CONSTRAINT fk_clauses_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Risk Events Table
CREATE TABLE IF NOT EXISTS risk_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    risk_type VARCHAR(100) NOT NULL, -- LIQUIDITY_BUFFER_LOW, SPENDING_SPIKE, DEBT_BURDEN_HIGH, UPCOMING_PAYMENT_RISK
    severity VARCHAR(30) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    description TEXT NOT NULL,
    metrics_json JSON,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, ACKNOWLEDGED, RESOLVED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_risk_user_status (user_id, status),
    CONSTRAINT fk_risk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Notifications & Reminders Table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL, -- REMINDER, RISK_ALERT, CONTRACT_EXPIRY, PAYMENT_DUE
    priority VARCHAR(30) NOT NULL DEFAULT 'NORMAL', -- LOW, NORMAL, HIGH, URGENT
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    trigger_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notif_user_read (user_id, is_read),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Audit Traces Table
CREATE TABLE IF NOT EXISTS audit_traces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trace_id VARCHAR(100) NOT NULL UNIQUE,
    correlation_id VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    user_query TEXT NOT NULL,
    intent VARCHAR(100) NOT NULL,
    plan_steps_json JSON,
    tools_called_json JSON,
    validation_status VARCHAR(50) NOT NULL,
    final_response TEXT,
    duration_ms BIGINT NOT NULL,
    tokens_used INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_corr (correlation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================
-- Initial Seed Data
-- ==============================================================

-- Demo User: password is 'Password123!' (BCrypt hashed)
INSERT INTO users (id, username, email, password_hash, full_name, role, status)
VALUES (1, 'sravani', 'demo@finorchestrator.com', '$2a$10$wN1e1FfLqj0eWnL55tJgE.b86fUvYx1z/e9mI9c/bOa7Wq9N9T37O', 'Sravani Gummudu', 'ROLE_USER', 'ACTIVE')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO user_preferences (user_id, currency, monthly_budget, risk_tolerance, savings_target_percentage)
VALUES (1, 'INR', 85000.00, 'MODERATE', 25.00)
ON DUPLICATE KEY UPDATE user_id=user_id;

-- Accounts
INSERT INTO accounts (id, user_id, account_number, account_name, account_type, balance, currency, status, version) VALUES
(1, 1, 'HDFC-77348912', 'HDFC Salary Checking', 'CHECKING', 145000.00, 'INR', 'ACTIVE', 0),
(2, 1, 'ICICI-99482155', 'ICICI High-Yield Savings', 'SAVINGS', 420000.00, 'INR', 'ACTIVE', 0),
(3, 1, 'AMEX-44120934', 'Amex Platinum Reserve', 'CREDIT_CARD', -18500.00, 'INR', 'ACTIVE', 0)
ON DUPLICATE KEY UPDATE id=id;

-- Categories
INSERT INTO categories (id, user_id, name, type, icon, color, budget_limit) VALUES
(1, NULL, 'Salary & Income', 'INCOME', 'briefcase', '#10B981', 0.00),
(2, NULL, 'Housing & Rent', 'EXPENSE', 'home', '#6366F1', 35000.00),
(3, NULL, 'Groceries & Supplies', 'EXPENSE', 'shopping-cart', '#F59E0B', 15000.00),
(4, NULL, 'Dining & Food Delivery', 'EXPENSE', 'coffee', '#EC4899', 8000.00),
(5, NULL, 'Utilities & Bills', 'EXPENSE', 'zap', '#3B82F6', 6000.00),
(6, NULL, 'Healthcare & Medical', 'EXPENSE', 'heart-pulse', '#EF4444', 5000.00),
(7, NULL, 'Entertainment & Subscriptions', 'EXPENSE', 'tv', '#8B5CF6', 4000.00),
(8, NULL, 'Transport & Fuel', 'EXPENSE', 'car', '#14B8A6', 7000.00)
ON DUPLICATE KEY UPDATE id=id;

-- Recurring Payments
INSERT INTO recurring_payments (id, user_id, account_id, category_id, merchant, amount, frequency, next_due_date, status, auto_debit) VALUES
(1, 1, 1, 2, 'Prestige Estates Home EMI', 32000.00, 'MONTHLY', '2026-10-05', 'ACTIVE', TRUE),
(2, 1, 1, 7, 'Netflix Premium 4K', 649.00, 'MONTHLY', '2026-09-28', 'ACTIVE', TRUE),
(3, 1, 1, 7, 'Cult.fit Gym Membership', 2500.00, 'MONTHLY', '2026-10-01', 'ACTIVE', TRUE),
(4, 1, 1, 5, 'Airtel Broadband & Fiber', 1199.00, 'MONTHLY', '2026-09-25', 'ACTIVE', TRUE)
ON DUPLICATE KEY UPDATE id=id;

-- Transactions (Current & Recent)
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, merchant, transaction_date, status, idempotency_key) VALUES
(1, 1, 1, 185000.00, 'CREDIT', 'Monthly Salary Credit', 'TechCorp India Pvt Ltd', '2026-09-01', 'COMPLETED', 'TX-SEED-001'),
(1, 1, 2, 32000.00, 'DEBIT', 'Prestige Home Loan EMI', 'HDFC Bank Loans', '2026-09-05', 'COMPLETED', 'TX-SEED-002'),
(1, 1, 3, 4250.00, 'DEBIT', 'Nature Basket Weekly Groceries', 'Nature Basket', '2026-09-07', 'COMPLETED', 'TX-SEED-003'),
(1, 1, 4, 1850.00, 'DEBIT', 'Dinner at Smoke House Deli', 'Smoke House Deli', '2026-09-10', 'COMPLETED', 'TX-SEED-004'),
(1, 1, 3, 3100.00, 'DEBIT', 'Blinkit Grocery Order', 'Blinkit', '2026-09-14', 'COMPLETED', 'TX-SEED-005'),
(1, 1, 5, 2450.00, 'DEBIT', 'BESCOM Electricity Bill', 'BESCOM', '2026-09-15', 'COMPLETED', 'TX-SEED-006'),
(1, 3, 4, 3400.00, 'DEBIT', 'Weekend Dining Out', 'Toit Brewery', '2026-09-18', 'COMPLETED', 'TX-SEED-007'),
(1, 1, 8, 2500.00, 'DEBIT', 'Shell Petrol Refill', 'Shell Fuels', '2026-09-19', 'COMPLETED', 'TX-SEED-008');

-- Contracts
INSERT INTO contracts (id, user_id, title, contract_type, counterparty, start_date, end_date, renewal_date, status, version, document_id, file_path) VALUES
(1, 1, 'HDFC Home Loan Facility Agreement', 'LOAN', 'HDFC Bank Ltd', '2024-03-15', '2044-03-15', NULL, 'ACTIVE', 1, 'DOC-HDFC-HL-2024', '/contracts/hdfc_home_loan.pdf'),
(2, 1, 'Star Health Comprehensive Insurance Policy', 'INSURANCE', 'Star Health Allied Insurance', '2025-11-01', '2026-10-31', '2026-10-15', 'ACTIVE', 1, 'DOC-STAR-HEALTH-2025', '/contracts/star_health_policy.pdf'),
(3, 1, 'American Express Cardmember Agreement', 'CREDIT_CARD', 'American Express India', '2023-06-01', NULL, '2027-06-01', 'ACTIVE', 1, 'DOC-AMEX-PLAT-2023', '/contracts/amex_agreement.pdf')
ON DUPLICATE KEY UPDATE id=id;

-- Contract Terms
INSERT INTO contract_terms (contract_id, term_key, term_value, unit, description, confidence_score) VALUES
(1, 'INTEREST_RATE', '8.45', '% p.a.', 'Floating interest linked to EBLR repo benchmark', 0.98),
(1, 'PRINCIPAL_SANCTIONED', '4500000', 'INR', 'Total sanctioned loan amount', 1.00),
(1, 'MONTHLY_EMI', '32000', 'INR', 'Monthly scheduled installment', 0.99),
(1, 'PREPAYMENT_PENALTY', '0.00', '%', 'Nil charges for floating rate home loan to individual borrowers', 0.95),
(1, 'LATE_PAYMENT_PENALTY', '24.00', '% p.a.', 'Penal interest on overdue installments payable monthly', 0.97),
(2, 'SUM_INSURED', '1500000', 'INR', 'Comprehensive family floater coverage', 1.00),
(2, 'ANNUAL_PREMIUM', '24800', 'INR', 'Annual payable renewal premium', 0.99),
(2, 'GRACE_PERIOD', '30', 'Days', 'Grace period for policy renewal before coverage lapse', 0.95),
(2, 'CO_PAYMENT', '10', '%', 'Co-payment on specific modern treatment procedures', 0.92),
(3, 'ANNUAL_FEE', '60000', 'INR', 'Annual card membership fee plus applicable taxes', 1.00),
(3, 'FINANCE_CHARGES', '3.75', '% per month', 'Monthly interest on unpaid card balance (45% p.a.)', 0.98),
(3, 'LATE_PAYMENT_FEE', '1200', 'INR', 'Late fee for unpaid minimum amount due', 0.95)
ON DUPLICATE KEY UPDATE id=id;

-- Contract Clauses with Section and Page Citation
INSERT INTO contract_clauses (contract_id, clause_title, section_number, page_number, chunk_id, clause_type, clause_text) VALUES
(1, 'Prepayment Terms and Charges', 'Section 8.2', 14, 'CHUNK-HL-014-02', 'PENALTY', 'As per Reserve Bank of India (RBI) guidelines, no prepayment charges or penalty shall be levied on early repayment of floating rate term loans sanctioned to individual borrowers, whether in partial or full settlement.'),
(1, 'Default Interest & Penalties', 'Section 11.4', 19, 'CHUNK-HL-019-01', 'PENALTY', 'In the event of default in payment of EMI on the scheduled due date, the Borrower shall be liable to pay penal interest at the rate of 2% per month (24% per annum) on the overdue sum from the due date until actual payment.'),
(2, 'Policy Renewal and Grace Period', 'Section 4.1', 8, 'CHUNK-SH-008-03', 'RENEWAL', 'A grace period of thirty (30) days from the date of expiry is permissible for renewal of the policy. During the grace period, payment shall maintain continuity of waiting periods, but claims incurred during the unpaid grace interval will not be admissible.'),
(2, 'Pre-Existing Disease Waiting Period', 'Section 3.2', 6, 'CHUNK-SH-006-01', 'OBLIGATION', 'Cover for pre-existing conditions shall commence only after twenty-four (24) months of continuous coverage without break with the Company.'),
(3, 'Interest on Revolving Credit and Late Fee', 'Clause 5(b)', 4, 'CHUNK-AMEX-004-02', 'FEE', 'If the Total Amount Due is not paid by the Payment Due Date, finance charges at the rate of 3.75% per month (annualized rate 45.00%) shall apply on daily balances from the date of transaction until settlement, in addition to a Late Payment Fee of INR 1,200.')
ON DUPLICATE KEY UPDATE id=id;

-- Risk Events
INSERT INTO risk_events (user_id, risk_type, severity, description, metrics_json, status) VALUES
(1, 'UPCOMING_EXPENSES_VS_BUFFER', 'LOW', 'Healthy cash buffer: Liquid savings cover 4.8 months of average expenses.', '{"liquidCash": 565000, "monthlyBurnRate": 118000, "bufferMonths": 4.78}', 'ACTIVE'),
(1, 'CONTRACT_RENEWAL_ALERT', 'MEDIUM', 'Star Health Insurance renewal due in 40 days (2026-10-15). Ensure payment within grace period.', '{"contractId": 2, "renewalDate": "2026-10-15", "premium": 24800}', 'ACTIVE')
ON DUPLICATE KEY UPDATE id=id;

-- Notifications
INSERT INTO notifications (user_id, title, message, type, priority, is_read, trigger_time) VALUES
(1, 'Contract Renewal: Star Health Policy', 'Your health insurance policy renewal is due on 2026-10-15. Annual premium: ₹24,800.', 'CONTRACT_EXPIRY', 'HIGH', FALSE, '2026-09-20 09:00:00'),
(1, 'Upcoming Bill: Airtel Broadband', 'Airtel Broadband bill of ₹1,199 is due on 2026-09-25.', 'PAYMENT_DUE', 'NORMAL', FALSE, '2026-09-21 00:00:00'),
(1, 'Salary Credited', '₹1,85,000 credited to HDFC Salary Checking from TechCorp India.', 'REMINDER', 'NORMAL', TRUE, '2026-09-01 10:30:00')
ON DUPLICATE KEY UPDATE id=id;
