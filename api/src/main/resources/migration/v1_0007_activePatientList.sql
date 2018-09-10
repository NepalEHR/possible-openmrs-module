DELETE FROM global_property
WHERE property IN (
  'emrapi.sqlSearch.activePatients'
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.activePatients',
        'select distinct
          concat(pn.given_name,\' \', pn.family_name) as name,
          pi.identifier as identifier,
          concat("",p.uuid) as uuid,
          concat("",v.uuid) as activeVisitUuid,
          IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted,
		  b.bed_number AS bedNumber,
		  bt.display_name AS bedType
        from visit v
        join person_name pn on v.patient_id = pn.person_id and pn.voided = 0 AND pn.preferred= 1
        join patient_identifier pi on v.patient_id = pi.patient_id and pi.preferred = 1 and pi.voided = 0
        join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
        join global_property gp on gp.property="bahmni.primaryIdentifierType" and gp.property_value=pit.uuid
        join person p on p.person_id = v.patient_id
        join location l on l.uuid = ${visit_location_uuid} and v.location_id = l.location_id
		left join bed_patient_assignment_map bpam ON bpam.patient_id = pn.person_id and bpam.date_stopped IS NULL
        left join bed b ON b.bed_id = bpam.bed_id
		left join bed_type bt ON bt.bed_type_id = b.bed_type_id
        left outer join visit_attribute va on va.visit_id = v.visit_id and va.attribute_type_id = (
          select visit_attribute_type_id from visit_attribute_type where name="Admission Status"
        ) and va.voided = 0
        where v.date_stopped is null AND v.voided = 0',
        'Sql query to get list of active patients',
        uuid()
);