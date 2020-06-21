<?xml version="1.0" encoding="UTF-8"?>
<rule xmlns="http://purl.oclc.org/dsdl/schematron" abstract="true" id="rule2">
    <extends rule="rule1"/>
    <assert role="error" test="test2.1">Message 2.1</assert>
    <let name="var2.2" value="'Variable 2.2'"/>
    <assert role="error" test="test2.3">Message 2.3</assert>
    <report role="warning" test="test2.4" see="http">Message 2.4</report>
</rule>
