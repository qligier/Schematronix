<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    <title>Schematron sample file 2</title>

    <pattern id="p1">
        <include href="schematron2/pattern1.sch"/>
    </pattern>

    <pattern id="p2">
        <title>MedicationDispenseDocument</title>
        <rule context="/" id="p2r1">
            <assert role="error"
                    test="descendant-or-self::hl7:ClinicalDocument[hl7:templateId[@root='2.16.840.1.113883.10.12.2'] and hl7:templateId[@root='2.16.840.1.113883.10.12.1'] and hl7:templateId[@root='2.16.756.5.30.1.127.1.4'] and hl7:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.1'] and hl7:templateId[@root='1.3.6.1.4.1.19376.1.9.1.1.3'] and hl7:templateId[@root='2.16.756.5.30.1.1.10.1.5']]">Instance is expected to have the following element: descendant-or-self::hl7:ClinicalDocument[hl7:templateId[@root='2.16.840.1.113883.10.12.2'] and hl7:templateId[@root='2.16.840.1.113883.10.12.1'] and hl7:templateId[@root='2.16.756.5.30.1.127.1.4'] and hl7:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.1'] and hl7:templateId[@root='1.3.6.1.4.1.19376.1.9.1.1.3'] and hl7:templateId[@root='2.16.756.5.30.1.1.10.1.5']]</assert>
        </rule>
    </pattern>

    <include href="schematron2/pattern3.sch"/>

    <phase id="MedicationSeriesNumberObservation">
        <active pattern="p2"/>
    </phase>

    <include href="schematron2/pattern4.sch"/>

    <pattern id="p5">
        <rule id="p5r1" abstract="true">
            <assert role="error" test="not(.)">assert</assert>
        </rule>
    </pattern>

    <pattern id="p6">
        <rule context="/" id="p6r1">
            <report role="warning" test="not(.)">report</report>
        </rule>
    </pattern>
</schema>
