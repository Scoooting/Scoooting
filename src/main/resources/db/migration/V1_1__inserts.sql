INSERT INTO users (email, name, password, role) VALUES (
    '${adminEmail}', '${adminUsername}', '${adminPassword}', 'ADMIN'
);

INSERT INTO users (email, name, password, role)
SELECT
    'user' || gs || '@example.com',
    'User_' || gs,
    'password' || gs,
    'USER'
FROM generate_series(1, 102) AS gs;

INSERT INTO scooters (model, status)
    SELECT 'Urent 10A8E', 'NONACTIVE' FROM generate_series(1, 50);
