INSERT INTO providers (id, name, specialty, status, version) VALUES
 (1, 'Dr. Anita Rao', 'GENERAL', 'AVAILABLE'),
 (2, 'Dr. David Chen', 'GENERAL', 'AVAILABLE'),
 (3, 'Dr. Maya Patel', 'PEDIATRIC', 'AVAILABLE');
INSERT INTO rooms (id, name, available) VALUES (1, 'Room 101', TRUE), (2, 'Room 102', TRUE);
INSERT INTO slots (id, start_time, duration_minutes, provider_id, room_id, status, version) VALUES
 (1, DATEADD('DAY', 1, CURRENT_TIMESTAMP), 30, 1, 1, 'BOOKED', 0),
 (2, DATEADD('DAY', 1, CURRENT_TIMESTAMP) + INTERVAL '1' HOUR, 30, 1, 1, 'OPEN', 0),
 (3, DATEADD('DAY', 1, CURRENT_TIMESTAMP) + INTERVAL '2' HOUR, 30, 2, 2, 'OPEN', 0),
 (4, DATEADD('DAY', 1, CURRENT_TIMESTAMP) + INTERVAL '3' HOUR, 30, 3, 2, 'OPEN', 0);
INSERT INTO appointments (id, patient_name, type, provider_id, slot_id, status, created_at, updated_at, version)
 VALUES (1, 'Jordan Lee', 'ROUTINE', 1, 1, 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
