@echo off
:: Validates the XML file with the Schematron definition and the reference implementation

:: The path to the transform utility
set "transform=C:\Program Files\Saxonica\SaxonHE9.9N\bin\Transform.exe"

:: The XML file to validate and the Schematron definition
set schema_file="schematron2"

set schematron_path="../../schematron/official"

:: Step 1: compile the Schematron file
"%transform%" -xsl:%schematron_path%/iso_dsdl_include.xsl -o:tmp1.sch %schema_file%.sch
"%transform%" -xsl:%schematron_path%/iso_abstract_expand.xsl -o:tmp2.sch tmp1.sch
"%transform%" -xsl:%schematron_path%/iso_svrl_for_xslt2.xsl -o:%schema_file%.xsl tmp2.sch


:: Step 3: cleaning
del tmp1.sch
del tmp2.sch