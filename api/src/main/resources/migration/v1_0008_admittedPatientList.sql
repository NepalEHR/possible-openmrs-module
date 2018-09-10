DELETE FROM global_property
WHERE property IN (
  'emrapi.sqlSearch.admittedPatients'
);

INSERT INTO global_property (property,property_value,description,uuid) 
	VALUES ('emrapi.sqlSearch.admittedPatients',
            'SELECT DISTINCT
    CONCAT(pn.given_name, \' \', pn.family_name) AS name,
    pi.identifier AS identifier,
    CONCAT("", p.uuid) AS uuid,
    CONCAT("", v.uuid) AS activeVisitUuid,
    IF(va.value_reference = "Admitted", "true", "false") AS hasBeenAdmitted,
    b.bed_number AS bedNumber,
    bt.display_name AS bedType
	FROM
    	visit v
    JOIN person_name pn ON v.patient_id = pn.person_id AND pn.voided = 0
    JOIN patient_identifier pi ON v.patient_id = pi.patient_id
    JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id
    JOIN global_property gp ON gp.property = "bahmni.primaryIdentifierType" AND gp.property_value = pit.uuid
    JOIN person p ON v.patient_id = p.person_id
    JOIN visit_attribute va ON v.visit_id = va.visit_id AND va.value_reference = "Admitted" AND va.voided = 0
    JOIN visit_attribute_type vat ON vat.visit_attribute_type_id = va.attribute_type_id
        AND vat.name = "Admission Status"
    LEFT JOIN bed_patient_assignment_map bpam ON bpam.patient_id = pn.person_id AND bpam.date_stopped IS NULL
    LEFT JOIN bed b ON b.bed_id = bpam.bed_id
	LEFT JOIN bed_type bt ON bt.bed_type_id = b.bed_type_id
    JOIN location l on l.uuid = ${visit_location_uuid} and v.location_id = l.location_id
	WHERE
    v.date_stopped IS NULL AND v.voided = 0',
    'SQL Query to get admittedPatients',
    uuid()
);