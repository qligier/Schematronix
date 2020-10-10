<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    <title>Schematronix sample file 1</title>

    <pattern id="pattern1">
        <rule context="//book" id="rule1">
            <assert test="not(@isbn)">Found ISBN with value '<value-of select="@isbn"/>' for tag '<name/>'</assert>
        </rule>
    </pattern>
</schema>
