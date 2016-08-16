Program Auto Enrollment Module
==============================

Properties file
---------------

###The property file resides at path `/opt/openmrs/program-auto-enrolment.properties`

- ANC.diagnosis.uuid=\<program-indicator which is the diagnosis-uuid>
- ANC.program.uuid=\<program-uuid>
- ANC.VDCs=\<VDC1-name>,\<VDC2-name>,..\<VDCn-name>

**Sample:**

- ANC.diagnosis.uuid=f7e858d5-5328-4120-9198-974fc401a050
- ANC.program.uuid=473fb329-d74b-4d72-9a9b-4979c56eac27
- ANC.VDCs=Sanfebagar,Municipality,Baradadevi,Payal,Lunar,Janalikot

**While deploying**

Add this line in setup.yml.
<pre>
omods:
  - "https://github.com/Possiblehealth/program-auto-enrollment/blob/master/programautoenrolment-1.0-SNAPSHOT.omod"
</pre>
