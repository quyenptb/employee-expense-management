
-- ROLES
INSERT INTO roles (name) VALUES ('ROLE_EMPLOYEE');
INSERT INTO roles (name) VALUES ('ROLE_MANAGER');
INSERT INTO roles (name) VALUES ('ROLE_FINANCE');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- USERS
INSERT INTO users (full_name, email, password, role_id, manager_id) VALUES ('Admin User', 'admin@example.com', '$2a$12$RBqttzESi6H72IAVKwloF.XYwuWOoueTQPvtsYVmnExTxCJm8BfFe', 4, NULL);
INSERT INTO users (full_name, email, password, role_id, manager_id) VALUES ('Linh Manager', 'manager@example.com', '$2a$12$RBqttzESi6H72IAVKwloF.XYwuWOoueTQPvtsYVmnExTxCJm8BfFe', 2, NULL);
INSERT INTO users (full_name, email, password, role_id, manager_id) VALUES ('An Employee', 'employee@example.com', '$2a$12$RBqttzESi6H72IAVKwloF.XYwuWOoueTQPvtsYVmnExTxCJm8BfFe', 1, 2);
INSERT INTO users (full_name, email, password, role_id, manager_id) VALUES ('Tai Finance', 'finance@example.com', '$2a$12$RBqttzESi6H72IAVKwloF.XYwuWOoueTQPvtsYVmnExTxCJm8BfFe', 3, NULL);

-- EXPENSE REQUESTS
INSERT INTO expense_requests (title, description, amount, status, employee_id, receipt_image_url, created_at, updated_at) VALUES ('Văn phòng phẩm (Draft)', 'Mua bút và sổ cho team', 150000.00, 'DRAFT', 3, 'https://i.imgur.com/receipt1.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO expense_requests (title, description, amount, status, employee_id, receipt_image_url, created_at, updated_at) VALUES ('Tiếp khách hàng (Pending Manager)', 'Ăn trưa với khách hàng ABC', 800000.00, 'PENDING_MANAGER', 3, 'https://i.imgur.com/receipt2.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO expense_requests (title, description, amount, status, employee_id, receipt_image_url, created_at, updated_at) VALUES ('Vé máy bay công tác (Pending Finance)', 'Vé khứ hồi SGN-HAN', 3500000.00, 'PENDING_FINANCE', 3, 'https://i.imgur.com/receipt3.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO expense_requests (title, description, amount, status, employee_id, receipt_image_url, created_at, updated_at) VALUES ('Grab đi sân bay (Paid)', 'Grab từ văn phòng ra sân bay', 250000.00, 'PAID', 3, 'https://i.imgur.com/receipt4.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- REQUEST HISTORY
INSERT INTO request_history (request_id, actor_id, action, comment, created_at) VALUES (1, 3, 'CREATED', 'Tạo nháp', CURRENT_TIMESTAMP);
INSERT INTO request_history (request_id, actor_id, action, comment, created_at) VALUES (2, 3, 'CREATED', 'Tạo nháp', CURRENT_TIMESTAMP);
INSERT INTO request_history (request_id, actor_id, action, comment, created_at) VALUES (2, 3, 'SUBMITTED', 'Gửi duyệt cho Manager', CURRENT_TIMESTAMP);
INSERT INTO request_history (request_id, actor_id, action, comment, created_at) VALUES (3, 3, 'CREATED', 'Tạo nháp', CURRENT_TIMESTAMP);
INSERT INTO request_history (request_id, actor_id, action, comment, created_at) VALUES (3, 3, 'SUBMITTED', 'Gửi duyệt cho Manager', CURRENT_TIMESTAMP);
INSERT INTO request_history (request_id, actor_id, action, comment, created_at) VALUES (3, 2, 'MANAGER_APPROVED', 'OK An', CURRENT_TIMESTAMP);
INSERT INTO request_history (request_id, actor_id, action, comment, created_at) VALUES (4, 3, 'CREATED', 'Tạo nháp', CURRENT_TIMESTAMP);
INSERT INTO request_history (request_id, actor_id, action, comment, created_at) VALUES (4, 3, 'SUBMITTED', 'Gửi duyệt cho Manager', CURRENT_TIMESTAMP);
INSERT INTO request_history (request_id, actor_id, action, comment, created_at) VALUES (4, 2, 'MANAGER_APPROVED', 'OK', CURRENT_TIMESTAMP);
INSERT INTO request_history (request_id, actor_id, action, comment, created_at) VALUES (4, 4, 'FINANCE_APPROVED', 'Đã duyệt chi', CURRENT_TIMESTAMP);
INSERT INTO request_history (request_id, actor_id, action, comment, created_at) VALUES (4, 4, 'PAID', 'Đã thanh toán qua chuyển khoản', CURRENT_TIMESTAMP);