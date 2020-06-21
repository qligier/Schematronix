<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    <ns uri="urn:hl7-org:v3" prefix="hl7"/>
    <ns uri="urn:hl7-org:v3" prefix="cda"/>
    <ns uri="http://www.w3.org/2001/XMLSchema-instance" prefix="xsi"/>
    <ns uri="http://www.w3.org/2001/XMLSchema" prefix="xs"/>

    <title>Schematron sample file 1</title>

    <pattern id="pattern1">
        <rule context="*[substance[template[@root = '1']]]/substance[template[@root = '1]]/template[@root='1']" id="rule1">
            <assert role="error" test="@attr1">pattern1 rule1 attr1</assert>
            <assert role="error" test="@attr2">pattern1 rule1 attr2</assert>
            <assert role="error" test="@attr3">pattern1 rule1 attr3</assert>
        </rule>
        <rule context="*[@id]" id="rule2">
            <assert role="error" test="@attr1">pattern1 rule2 attr1</assert>
            <assert role="warning" test="@attr2">pattern1 rule2 attr2</assert>
            <assert role="error" test="@attr3">pattern1 rule2 attr3</assert>
        </rule>
        <rule abstract="true" id="ruleAbstract">
            <assert role="error" test="something">abstract rule</assert>
        </rule>
    </pattern>
    <pattern id="pattern2">
        <rule context="//*[@id]" id="rule4">
            <assert role="fatal" test="@attr1">pattern2 rule4 attr1</assert>
            <assert role="error" test="@attr2">pattern2 rule4 attr2</assert>
            <assert role="error" test="@attr3">pattern2 rule4 attr3</assert>
        </rule>
        <rule context="//book" id="rule3">
            <assert role="error" test="@attr1">pattern2 rule3 attr1</assert>
            <assert role="error" test="@attr2">pattern2 rule3 attr2</assert>
            <assert role="error" test="@attr3">pattern2 rule3 attr3</assert>
        </rule>
    </pattern>
</schema>
