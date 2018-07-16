CREATE PROCEDURE fix_patient_events_between_range (start_date DATE, end_date DATE)
BEGIN
  DECLARE patient_uuid VARCHAR(255);
  DECLARE done INT DEFAULT FALSE;
  DECLARE cursor_ids CURSOR FOR  SELECT p.uuid FROM person p INNER JOIN patient pa ON pa.patient_id = p.person_id WHERE CAST(p.date_created AS DATE) BETWEEN start_date AND end_date;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursor_ids;
  	SET done = 0;
  	REPEAT
      FETCH cursor_ids INTO patient_uuid;
      call publish_patient_events(patient_uuid);
  	UNTIL done END REPEAT;
  	CLOSE cursor_ids;
END;