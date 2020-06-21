<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    <title>Schematronix sample file 1</title>

    <pattern id="pattern1">
        <rule context="//ClinicalDocument" id="rule1">
            <assert role="error" test="string(title) = ''">title not empty</assert>
        </rule>
    </pattern>
</schema>