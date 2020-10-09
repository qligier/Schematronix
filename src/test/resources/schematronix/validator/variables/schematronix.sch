<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    <title>Schematronix sample file for variables</title>

    <pattern id="pattern1">
        <rule context="//times/time" id="rule1">
        	<let name="hours1" value="number(substring(.,1,1))"/>
        	<let name="hours2" value="number(substring(.,1,2))"/>
        	<let name="hours" value="if (string-length(.)=7) then $hours1 else $hours2"/> <!-- Variable in variable -->
        	<let name="minutes" value="if (string-length(.)=7) then number(substring(.,3,2)) else number(substring(.,4,2))"/>
        	<let name="seconds" value="if (string-length(.)=7) then number(substring(.,6,2)) else number(substring(.,7,2))"/>
            <assert test="$hours >= 0 and $hours &lt;= 23">Hours shall be between 0 and 23, found '<value-of select="$hours"/>'</assert> <!-- Variable in test and value-of -->
		    <assert test="$minutes >= 0 and $minutes &lt;= 59">Minutes shall be between 0 and 59, found '<value-of select="$minutes"/>'</assert>
		    <assert test="$seconds >= 0 and $seconds &lt;= 59">Seconds shall be between 0 and 59, found '<value-of select="$seconds"/>'</assert>
        </rule>
    </pattern>
</schema>
