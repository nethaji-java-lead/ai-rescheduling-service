-- CareBridge demo seed data. Stable business identifiers are used for providers, slots, and appointments.
INSERT INTO rooms (id, name, available) VALUES
                                            (1, 'Room 101', TRUE),
                                            (2, 'Room 102', TRUE),
                                            (3, 'Room 103', TRUE);

INSERT INTO providers (id, name, specialty, status) VALUES
                                                        (1, 'Dr. Sarah Chen',    'GENERAL',    'AVAILABLE'),
                                                        (2, 'Dr. James Okonkwo', 'SPECIALIST', 'AVAILABLE'),
                                                        (3, 'Dr. Priya Sharma',  'PEDIATRIC',  'AVAILABLE'),
                                                        (4, 'Dr. Rahul Verma',   'GENERAL',    'AVAILABLE'),
                                                        (5, 'Dr. Ananya Iyer',   'SPECIALIST', 'AVAILABLE');

INSERT INTO slots (id, provider_id, room_id, start_time, duration_minutes, status, version) VALUES
                                                                                                (1, 1, 1, DATEADD('MINUTE',  60, CURRENT_TIMESTAMP), 30, 'BOOKED', 0),
                                                                                                (2, 1, 1, DATEADD('MINUTE', 120, CURRENT_TIMESTAMP), 30, 'BOOKED', 0),
                                                                                                (3, 3, 2, DATEADD('MINUTE',  90, CURRENT_TIMESTAMP), 45, 'BOOKED', 0),
                                                                                                (4, 2, 2, DATEADD('MINUTE', 360, CURRENT_TIMESTAMP), 30, 'OPEN',   0),
                                                                                                (5, 4, 3, DATEADD('MINUTE', 150, CURRENT_TIMESTAMP), 30, 'OPEN',   0),
                                                                                                (6, 5, 3, DATEADD('MINUTE', 240, CURRENT_TIMESTAMP), 60, 'BOOKED', 0);

INSERT INTO appointments (id, patient_name, type, provider_id, slot_id, status, created_at, updated_at, version) VALUES
                                                                                                                     (1, 'John Martinez', 'ROUTINE',   1, 1, 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
                                                                                                                     (2, 'Emily Watson',  'FOLLOW_UP', 1, 2, 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
                                                                                                                     (3, 'Aisha Patel',   'URGENT',    3, 3, 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
                                                                                                                     (4, 'Robert Kim',    'ROUTINE',   5, 6, 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
