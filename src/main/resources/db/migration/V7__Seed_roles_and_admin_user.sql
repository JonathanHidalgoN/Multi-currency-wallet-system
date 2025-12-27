-- Insert roles
INSERT INTO roles (role, updated_at) VALUES
    ('USER', CURRENT_TIMESTAMP),
    ('AUDITOR', CURRENT_TIMESTAMP),
    ('ADMIN', CURRENT_TIMESTAMP);

-- Insert admin user (password: admin123)
INSERT INTO users (email, password, full_name, enabled, created_at, updated_at) VALUES
    ('admin@payflow.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7Fo9nX5vCVHKJZ8RcJH.6ixW0FXgGfO', 'System Administrator', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Link admin user to ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@payflow.com' AND r.role = 'ADMIN';
