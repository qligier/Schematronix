<?xml version="1.0" encoding="UTF-8"?>
<rule xmlns="http://purl.oclc.org/dsdl/schematron" id="p1r1"
      context="*[hl7:participant[hl7:templateId[@root = '2.16.756.5.30.1.1.10.2.81'] and hl7:templateId[@root = '2.16.756.5.30.1.1.10.2.43'] and hl7:templateId[@root = '1.3.6.1.4.1.19376.1.5.3.1.2.4']]]/hl7:participant[hl7:templateId[@root = '2.16.756.5.30.1.1.10.2.81'] and hl7:templateId[@root = '2.16.756.5.30.1.1.10.2.43'] and hl7:templateId[@root = '1.3.6.1.4.1.19376.1.5.3.1.2.4']]">
    <assert role="error"
            test="string(@typeCode) = ('IND')">(cdachsmcp_header_PatientContact): The value for typeCode SHALL be 'IND'. Found: "<value-of select="@typeCode"/>"</assert>
</rule>
