CREATE PROCEDURE republish_all_events_between_range (start_date DATE, end_date DATE)
BEGIN
  DECLARE v_title VARCHAR(255);
  DECLARE v_uri VARCHAR(255);
  DECLARE v_object VARCHAR(1000);
  DECLARE v_category VARCHAR(255);
  DECLARE v_tags VARCHAR(255);
  DECLARE done INT DEFAULT FALSE;
  DECLARE cursors CURSOR FOR SELECT title, uri, object, category, tags FROM event_records WHERE CAST(date_created AS DATE) BETWEEN start_date AND end_date;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN cursors;
  	SET done = 0;
  	read_loop: LOOP
      FETCH cursors INTO v_title, v_uri, v_object, v_category, v_tags;
      IF done THEN
      	LEAVE read_loop;
      END IF;
      INSERT INTO event_records (uuid, title, uri, object, category, date_created, tags) VALUES (uuid(), v_title, v_uri, v_object, v_category, now(), v_tags);
  	END LOOP read_loop;
  	CLOSE cursors;
END;