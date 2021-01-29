# Schematronix

⚠️ The experiment has ended, as Schematronix has revealed much slower than [ph-schematron](https://github.com/phax/ph-schematron) with
precompiled XSLT files (even without using its pure version). It may be possible to further speed it up but it surely requires much more
work. Just use [ph-schematron](https://github.com/phax/ph-schematron). 🙂

![GitHub](https://img.shields.io/github/license/qligier/Schematronix)
[![Build Status](https://travis-ci.org/qligier/Schematronix.png?branch=master)](https://travis-ci.org/qligier/Schematronix)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ch.qligier/schematronix/badge.svg?style=flat)](https://search.maven.org/artifact/ch.qligier/schematronix)
[![Latest release](https://img.shields.io/github/release/qligier/Schematronix.svg)](https://github.com/qligier/Schematronix/releases/latest)
[![Known Vulnerabilities](https://snyk.io/test/github/qligier/Schematronix/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/qligier/Schematronix?targetFile=pom.xml)
[![Libraries.io dependency status for latest release](https://img.shields.io/librariesio/release/github/qligier/Schematronix)](https://libraries.io/maven/ch.qligier:schematronix)

A Schematron validator inspired by the pure version of [ph-schematron](https://github.com/phax/ph-schematron), improved to efficiently
 validate CDA-CH-EMED documents.

The Schematronix library is made of the following parts:

- A Schematron definition parser, transformer and writer that can generate optimized Schematronix definitions.
- A Schematronix validator that takes both the Schematronix definition and the file to validate (both are XML files) and performs a
 Schematron-compliant validation.

The validator uses several tricks to optimize the validation duration and the memory footprint:

- The Schematronix file is optimized in a way that makes possible to execute the validation while reading it in a single pass, with a
 streaming API (StAX);
- There are no SVRL (Schematron Validation Report Language) document generated as a validation output but a single list of messages;
- The validation can stop at the first encountered error, returning the failure status as early as possible.

## Development

Schematronix was developed with the JDK11. Saxon is used as XQuery processor. 

⚠ As per semantic versioning 2.0.0, until release of the 1.0.0 version, breaking changes can be introduced in minors or patches.

## Quick start

```java
// Convert the Schematron definition file to an optimized version
final File definitionFile = new File("cdachemed-MTP.sch");
final SchematronParser parser = new SchematronParser();
final SchematronDefinition definition = parser.parse(definitionFile);
// Update the definition if you want, adding, modifying or removing rules, asserts or reports
final File schematronixFile = new File("cdachemed-MTP-schematronix.sch");
final SchematronixWriter schematronixWriter = new SchematronixWriter();
schematronixWriter.writeSchematronix(definition, schematronixFile);

// Do it as many time as you want
final File cceFile = new File("cce-MTP.xml");
final SchematronixValidator validator = new SchematronixValidator(new StreamSource(cceFile), schematronixFile);
final boolean failFast = false; // `true` for a fast validation, `false` for a complete validation
final SchematronixValidationReport report = validator->validate(failFast);
System.out.println("CCE passed validation: " + report.isSchematronixValid());
```

## Design choices

The [ISO Schematron](http://schematron.com/) is a language for making assertions about the presence or absence of patterns in XML documents.

In the 2016 specification:

> **Query language binding for XPath 2** states:
> > The let element should not be used.

So the specification tells to use the XSLT binding if _let_ elements are used, as it is in the CDA-CH-EMED Schematron.
The drawback of this, as seen in the ph-schematron, is that the XSLT binding is much slower that the XPath one (see the ph-schematron
 documentation for details on this point).
 
Is there still a way to use an XPath binding with _let_ elements?
That's what the Schematronix tries to achieve. The optimized Schematron file (called Schematronix file internally, while it still is a
valid Schematron file) is made of the following optimizations (all these limitations are shared by the Schematronix writer and validator):

- It does not support includes. This one helps a lot when reading the Schematron file, as the original CDA-CH-EMED Schematron files
 contain a lot of these (`cdachemed-MTP.sch` alone includes 204 other files, that may also have includes).
- It does not contain rules that extend other rules. All rules directly contain all their tests, no need for lookups. The resulting file
 size increases, but storage is cheap and reading it with a streaming API (e.g. StAX) is not harder. Abstract rules and patterns are
 thus not supported.
- Only the main phase is evaluated. If multiple phases are to be supported, the way to go would be to generate one Schematronix file per
 phase.
 
Even though they are supported by the validator, it is recommended to get rid of `reports` and non-error `asserts` if a validation report
 is not expected. If a full report is expected, it is recommended to use a slower, more stable Schematron validator.
