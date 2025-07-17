-- ShopX Database Creation Script
-- PostgreSQL Database Schema for ShopX E-commerce Platform

-- Create database (run this separately if needed)
-- CREATE DATABASE core_service;

-- Create schemas
CREATE SCHEMA IF NOT EXISTS "sync-catalog";

-- Create sequences
CREATE SEQUENCE IF NOT EXISTS user_sequence START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS store_sequence START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS subscription_plan_sequence START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS store_subscription_sequence START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS "sync-catalog".history_product_store_id_seq START 1 INCREMENT 1;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY DEFAULT nextval('user_sequence'),
    username VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    provider VARCHAR(50) DEFAULT 'local',
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sessions table
CREATE TABLE IF NOT EXISTS sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    token VARCHAR(1024),
    refresh_token VARCHAR(1024),
    user_agent VARCHAR(512),
    ip_address VARCHAR(45),
    device_name VARCHAR(255),
    location VARCHAR(255),
    expires_at TIMESTAMP,
    last_activity_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Store table
CREATE TABLE IF NOT EXISTS store (
    id BIGINT PRIMARY KEY DEFAULT nextval('store_sequence'),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE,
    subdomain VARCHAR(255),
    description TEXT,
    logo VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    owner_id INTEGER NOT NULL,
    facebook_business_id VARCHAR(255),
    facebook_catalog_id VARCHAR(255),
    facebook_token VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Store staff many-to-many table
CREATE TABLE IF NOT EXISTS store_staff (
    store_id BIGINT NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (store_id, user_id),
    FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Store roles table
CREATE TABLE IF NOT EXISTS store_role (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL,
    user_id INTEGER NOT NULL,
    role VARCHAR(50) NOT NULL,
    permissions TEXT[],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Store invitations table
CREATE TABLE IF NOT EXISTS store_invitation (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    invited_by INTEGER NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE,
    FOREIGN KEY (invited_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Product types table
CREATE TABLE IF NOT EXISTS product_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Product field definitions table
CREATE TABLE IF NOT EXISTS product_field_definitions (
    id BIGSERIAL PRIMARY KEY,
    product_type_id BIGINT NOT NULL,
    field_name VARCHAR(255) NOT NULL,
    field_type VARCHAR(50) NOT NULL,
    required BOOLEAN DEFAULT false,
    order_position INTEGER NOT NULL,
    regex VARCHAR(500),
    min_value VARCHAR(255),
    max_value VARCHAR(255),
    multiple_values BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_type_id) REFERENCES product_types(id) ON DELETE CASCADE
);

-- Variant field definitions table
CREATE TABLE IF NOT EXISTS variant_field_definitions (
    id BIGSERIAL PRIMARY KEY,
    product_type_id BIGINT NOT NULL,
    option_name VARCHAR(255) NOT NULL,
    required BOOLEAN DEFAULT false,
    order_position INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_type_id) REFERENCES product_types(id) ON DELETE CASCADE
);

-- Variant field allowed values table
CREATE TABLE IF NOT EXISTS variant_field_allowed_values (
    variant_field_definition_id BIGINT NOT NULL,
    allowed_value VARCHAR(255) NOT NULL,
    PRIMARY KEY (variant_field_definition_id, allowed_value),
    FOREIGN KEY (variant_field_definition_id) REFERENCES variant_field_definitions(id) ON DELETE CASCADE
);

-- Product table
CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sku VARCHAR(255),
    price DECIMAL(10,2),
    store_id BIGINT NOT NULL,
    product_type_id BIGINT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE,
    FOREIGN KEY (product_type_id) REFERENCES product_types(id) ON DELETE SET NULL
);

-- Product field values table
CREATE TABLE IF NOT EXISTS product_field_values (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    field_name VARCHAR(255) NOT NULL,
    value TEXT,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- Product variants table
CREATE TABLE IF NOT EXISTS product_variant (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    name VARCHAR(255),
    sku VARCHAR(255),
    price DECIMAL(10,2),
    stock_quantity INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- Product images table
CREATE TABLE IF NOT EXISTS product_image (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    is_primary BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- Subscription plans table
CREATE TABLE IF NOT EXISTS subscription_plans (
    id BIGINT PRIMARY KEY DEFAULT nextval('subscription_plan_sequence'),
    name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(1000),
    monthly_price DECIMAL(10,2) NOT NULL,
    yearly_price DECIMAL(10,2) NOT NULL,
    max_products INTEGER NOT NULL,
    trial_period_days INTEGER,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Subscription plan features table
CREATE TABLE IF NOT EXISTS subscription_plan_features (
    plan_id BIGINT NOT NULL,
    feature VARCHAR(255) NOT NULL,
    PRIMARY KEY (plan_id, feature),
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(id) ON DELETE CASCADE
);

-- Store subscriptions table
CREATE TABLE IF NOT EXISTS store_subscriptions (
    id BIGINT PRIMARY KEY DEFAULT nextval('store_subscription_sequence'),
    store_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    trial_end_date TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    billing_cycle VARCHAR(20) NOT NULL,
    auto_renew BOOLEAN DEFAULT true,
    next_billing_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(id) ON DELETE CASCADE
);

-- Subscription invoices table
CREATE TABLE IF NOT EXISTS subscription_invoice (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    invoice_date TIMESTAMP NOT NULL,
    due_date TIMESTAMP NOT NULL,
    paid_date TIMESTAMP,
    stripe_invoice_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subscription_id) REFERENCES store_subscriptions(id) ON DELETE CASCADE
);

-- Usage metrics table
CREATE TABLE IF NOT EXISTS usage_metric (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL,
    metric_type VARCHAR(50) NOT NULL,
    value INTEGER NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE
);

-- Revoked tokens table
CREATE TABLE IF NOT EXISTS revoked_token (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    revoked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);

-- History product store table (in sync-catalog schema)
CREATE TABLE IF NOT EXISTS "sync-catalog".history_product_store (
    id BIGINT PRIMARY KEY DEFAULT nextval('"sync-catalog".history_product_store_id_seq'),
    product_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    method VARCHAR(20),
    sync_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    synced_at TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_token ON sessions(token);
CREATE INDEX IF NOT EXISTS idx_sessions_refresh_token ON sessions(refresh_token);

CREATE UNIQUE INDEX IF NOT EXISTS idx_store_subdomain ON store (subdomain);
CREATE INDEX IF NOT EXISTS idx_store_owner_id ON store(owner_id);
CREATE INDEX IF NOT EXISTS idx_store_slug ON store(slug);

CREATE INDEX IF NOT EXISTS idx_product_store_id ON product(store_id);
CREATE INDEX IF NOT EXISTS idx_product_type_id ON product(product_type_id);
CREATE INDEX IF NOT EXISTS idx_product_sku ON product(sku);

CREATE INDEX IF NOT EXISTS idx_product_variant_product_id ON product_variant(product_id);
CREATE INDEX IF NOT EXISTS idx_product_variant_sku ON product_variant(sku);

CREATE INDEX IF NOT EXISTS idx_product_field_values_product_id ON product_field_values(product_id);
CREATE INDEX IF NOT EXISTS idx_product_field_values_field_name ON product_field_values(field_name);

CREATE INDEX IF NOT EXISTS idx_product_image_product_id ON product_image(product_id);

CREATE INDEX IF NOT EXISTS idx_store_subscriptions_store_id ON store_subscriptions(store_id);
CREATE INDEX IF NOT EXISTS idx_store_subscriptions_plan_id ON store_subscriptions(plan_id);
CREATE INDEX IF NOT EXISTS idx_store_subscriptions_status ON store_subscriptions(status);

CREATE INDEX IF NOT EXISTS idx_subscription_invoice_subscription_id ON subscription_invoice(subscription_id);
CREATE INDEX IF NOT EXISTS idx_subscription_invoice_status ON subscription_invoice(status);

CREATE INDEX IF NOT EXISTS idx_usage_metric_store_id ON usage_metric(store_id);
CREATE INDEX IF NOT EXISTS idx_usage_metric_type ON usage_metric(metric_type);

CREATE INDEX IF NOT EXISTS idx_revoked_token_hash ON revoked_token(token_hash);
CREATE INDEX IF NOT EXISTS idx_revoked_token_expires_at ON revoked_token(expires_at);

-- Create the public store view
DROP VIEW IF EXISTS public_store_view;
CREATE OR REPLACE VIEW public_store_view AS
SELECT
    s.id,
    s.name,
    s.slug,
    s.description,
    s.logo,
    s.is_active,
    u.id as owner_id,
    u.username as owner_name
FROM
    store s
    JOIN users u ON s.owner_id = u.id
WHERE
    s.is_active = true;

-- Insert default subscription plans
INSERT INTO subscription_plans (name, description, monthly_price, yearly_price, max_products, trial_period_days) 
VALUES 
    ('Free', 'Basic plan for small stores', 0.00, 0.00, 10, 14),
    ('Basic', 'Perfect for growing businesses', 29.99, 299.99, 100, 7),
    ('Pro', 'For established businesses', 79.99, 799.99, 1000, 7),
    ('Enterprise', 'For large scale operations', 199.99, 1999.99, -1, 14)
ON CONFLICT (name) DO NOTHING;

-- Insert default subscription plan features
INSERT INTO subscription_plan_features (plan_id, feature)
SELECT p.id, f.feature
FROM subscription_plans p
CROSS JOIN (
    VALUES 
        ('Basic store management'),
        ('Product catalog'),
        ('Order management'),
        ('Customer support'),
        ('Analytics dashboard'),
        ('Multi-channel sync'),
        ('Advanced analytics'),
        ('Priority support'),
        ('Custom integrations'),
        ('White label solution')
) f(feature)
WHERE 
    (p.name = 'Free' AND f.feature IN ('Basic store management', 'Product catalog'))
    OR (p.name = 'Basic' AND f.feature IN ('Basic store management', 'Product catalog', 'Order management', 'Customer support'))
    OR (p.name = 'Pro' AND f.feature IN ('Basic store management', 'Product catalog', 'Order management', 'Customer support', 'Analytics dashboard', 'Multi-channel sync'))
    OR (p.name = 'Enterprise' AND f.feature IN ('Basic store management', 'Product catalog', 'Order management', 'Customer support', 'Analytics dashboard', 'Multi-channel sync', 'Advanced analytics', 'Priority support', 'Custom integrations', 'White label solution'))
ON CONFLICT (plan_id, feature) DO NOTHING;

-- Insert default product types
INSERT INTO product_types (name, slug) 
VALUES 
    ('Physical Product', 'physical-product'),
    ('Digital Product', 'digital-product'),
    ('Service', 'service'),
    ('Subscription', 'subscription')
ON CONFLICT (slug) DO NOTHING;

COMMIT;