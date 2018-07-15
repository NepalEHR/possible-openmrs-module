CREATE PROCEDURE publish_patient_events (patient_uuid VARCHAR)
BEGIN
  INSERT INTO event_records (uuid, title, uri, object,category, date_created, tags) VALUES
  (uuid(), 'Patient', '', concat('/openmrs/ws/rest/v1/patient/', patient_uuid, '?v=full'), 'patient', now(), 'patient');
END;

CREATE PROCEDURE publish_encounter_events (encounter_uuid VARCHAR)
BEGIN
  INSERT INTO event_records (uuid, title, uri, object,category, date_created, tags) VALUES
  (uuid(), 'Encounter', '', concat('/openmrs/ws/rest/v1/bahmnicore/bahmniencounter/', encounter_uuid, '?includeAll=true'), 'Encounter', now(), 'Encounter');
END;

CREATE PROCEDURE republish_all_events_between_range (start_date DATE, end_date DATE)
BEGIN
  DECLARE v_title VARCHAR(255);
  DECLARE v_uri VARCHAR(500);
  DECLARE v_object VARCHAR(255);
  DECLARE v_category VARCHAR(255);
  DECLARE v_tags VARCHAR(255);
  DECLARE done INT DEFAULT FALSE;
  DECLARE cursors CURSOR FOR SELECT title, uri, object, category, tags FROM event_records WHERE CAST(date_created AS DATE) BETWEEN start_date AND end_date;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursors;
  	SET done = 0;
  	read_loop: LOOP
      FETCH cursors INTO v_bed_type_id, v_name, disp_name, bed_desc;
      IF done THEN
      	LEAVE read_loop;
      END IF;
      INSERT INTO event_records (uuid, title, uri, object,category, date_created, tags) VALUES (uuid(), v_title, '', v_object, v_category, now(), v_tags);
  	END LOOP read_loop;
  	CLOSE cursors;
END;

CREATE PROCEDURE fix_encounters_between_range (start_date DATE, end_date DATE)
BEGIN
  DECLARE encounter_uuid VARCHAR;
  DECLARE done INT DEFAULT FALSE;
  DECLARE cursor_ids CURSOR FOR  SELECT uuid FROM encounter WHERE CAST(date_created AS DATE) BETWEEN start_date AND end_date;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor_ids;
  	SET done = 0;
  	REPEAT
      FETCH cursor_ids INTO encounter_uuid;
      call publish_encounter_events(encounter_uuid);
  	UNTIL done END REPEAT;
  	CLOSE cursor_ids;
END;

CREATE PROCEDURE fix_patient_between_range (start_date DATE, end_date DATE)
BEGIN
  DECLARE patient_uuid INT;
  DECLARE done INT DEFAULT FALSE;
  DECLARE cursor_ids CURSOR FOR  SELECT uuid FROM person WHERE CAST(date_created AS DATE) BETWEEN start_date AND end_date;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor_ids;
  	SET done = 0;
  	REPEAT
      FETCH cursor_ids INTO patient_uuid;
      call publish_patient_events(patient_uuid);
  	UNTIL done END REPEAT;
  	CLOSE cursor_ids;
END;