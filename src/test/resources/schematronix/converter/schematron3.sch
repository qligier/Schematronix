<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    <title>Simple Schematron definition</title>
    <ns uri="http://www.w3.org/2001/XMLSchema-instance" prefix="ns1"/>
    <ns uri="http://www.w3.org/2001/XMLSchema" prefix="ns2"/>

    <include href="schematron3/rule1.sch"/>
    <include href="schematron3/rule2.sch"/>

    <pattern id="pattern1">
        <title>Pattern 1</title>
        <rule id="rule3" context="/">
            <extends rule="rule2" />
            <assert role="error" test="test3.1" see="http">Message 3.1</assert>
        </rule>
    </pattern>

    <rule id="rule4" context="/root"></rule>
</schema>
