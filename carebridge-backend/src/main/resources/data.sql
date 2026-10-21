-- CareBridge demo seed data. Stable business identifiers are used for providers, slots, and appointments.
INSERT INTO rooms (id, name, available)
VALUES (1, 'Room 101', TRUE),
       (2, 'Room 102', TRUE),
       (3, 'Room 103', TRUE);

INSERT INTO providers (id, name, specialty, status)
VALUES ('PRV-001', 'Dr. Sarah Chen', 'GENERAL', 'BUSY'),
       ('PRV-002', 'Dr. James Okonkwo', 'SPECIALIST', 'AVAILABLE'),
       ('PRV-003', 'Dr. Priya Sharma', 'PEDIATRIC', 'BUSY'),
       ('PRV-004', 'Dr. Rahul Verma', 'GENERAL', 'AVAILABLE'),
       ('PRV-005', 'Dr. Ananya Iyer', 'SPECIALIST', 'BUSY');

INSERT INTO slots (id, provider_id, room_id, start_time, duration_minutes, status, version)
VALUES ('SLT-001', 'PRV-001', 1, DATEADD('MINUTE', 60, CURRENT_TIMESTAMP), 30, 'BOOKED', 0),
       ('SLT-002', 'PRV-001', 1, DATEADD('MINUTE', 120, CURRENT_TIMESTAMP), 30, 'BOOKED', 0),
       ('SLT-003', 'PRV-003', 2, DATEADD('MINUTE', 90, CURRENT_TIMESTAMP), 45, 'BOOKED', 0),
       ('SLT-004', 'PRV-002', 2, DATEADD('MINUTE', 180, CURRENT_TIMESTAMP), 30, 'OPEN', 0),
       ('SLT-005', 'PRV-004', 3, DATEADD('MINUTE', 150, CURRENT_TIMESTAMP), 30, 'OPEN', 0),
       ('SLT-006', 'PRV-005', 3, DATEADD('MINUTE', 240, CURRENT_TIMESTAMP), 60, 'BOOKED', 0);

INSERT INTO appointments (id, patient_name, type, provider_id, slot_id, status, created_at, updated_at, version)
VALUES ('APT-001', 'John Martinez', 'ROUTINE', 'PRV-001', 'SLT-001', 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        0),
       ('APT-002', 'Emily Watson', 'FOLLOW_UP', 'PRV-001', 'SLT-002', 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        0),
       ('APT-003', 'Aisha Patel', 'URGENT', 'PRV-003', 'SLT-003', 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
       ('APT-004', 'Robert Kim', 'ROUTINE', 'PRV-005', 'SLT-006', 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
