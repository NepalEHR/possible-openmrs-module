CREATE PROCEDURE fix_encounter_events_between_range (start_date DATE, end_date DATE)
BEGIN
  DECLARE encounter_uuid VARCHAR(255);
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