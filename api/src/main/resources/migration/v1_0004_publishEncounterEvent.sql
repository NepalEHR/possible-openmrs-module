CREATE PROCEDURE publish_encounter_events (encounter_uuid VARCHAR(255))
BEGIN
  INSERT INTO event_records (uuid, title, uri, object,category, date_created, tags) VALUES
  (uuid(), 'Encounter', '', concat('/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/', encounter_uuid, '?includeAll=true'), 'Encounter', now(), 'Encounter');
END;