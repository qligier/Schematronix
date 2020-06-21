<?xml version="1.0" encoding="UTF-8"?>
<pattern xmlns="http://purl.oclc.org/dsdl/schematron" id="p3">
    <rule context="*[hl7:observation[@moodCode='EVN'][hl7:templateId[@root='2.16.840.1.113883.10.20.1.46']]]/hl7:observation[@moodCode='EVN'][hl7:templateId[@root='2.16.840.1.113883.10.20.1.46']]"
          id="p3r1">
        <assert role="error"
                test="not(@classCode) or count($theAttValue) = count($theAttCheck)">CONF-341: The value for classCode SHALL be selected from value set '2.16.840.1.113883.1.11.11529' ActClassObservation (DYNAMIC).</assert>
    </rule>
</pattern>
