CREATE PROCEDURE publish_patient_events (patient_uuid VARCHAR(255))
BEGIN
  INSERT INTO event_records (uuid, title, uri, object,category, date_created, tags) VALUES
  (uuid(), 'Patient', '', concat('/openmrs/ws/rest/v1/patient/', patient_uuid, '?v=full'), 'patient', now(), 'patient');
END;