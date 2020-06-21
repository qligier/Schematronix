# Schematronix

A Schematron validator inspired by the pure version of [ph-schematron](https://github.com/phax/ph-schematron), improved to efficiently
 validate CDA-CH-EMED documents.

The Schematronix library is made of the following parts:

- A Schematron definition parser, transformer and writer that can output optimized Schematronix definitions.
- A Schematronix validator that takes both the Schematronix definition and the file to validate (both are XML files).

The validator uses several tricks to optimize the validation duration and the memory footprint:

- The expected Schematron file (Schematronix file) is optimized in a way that it is possible to execute it while reading it in a single
pass, with a streaming API (StAX);
- There are no SVRL (Schematron Validation Report Language) document generated as a validation output;
- The validation can stop to the first encountered error, returning the failure status as early as possible.

## Development

Schematronix was developed with the JDK11. Saxon is used as XQuery processor. 

## Quick start

```java
// Convert the Schematron definition file to an optimized version
final File definitionFile = new File("cdachemed-MTP.sch");
final DefinitionParser parser = new DefinitionParser();
final SchematronDefinition definition = parser.parse(definitionFile);

final File schematronixFile = new File("cdachemed-MTP-schematronix.sch");
final SchematronixWriter schematronixWriter = new SchematronixWriter();
schematronixWriter.writeSchematronix(definition, schematronixFile);

// Do it as many time as you want
final File cceFile = new File("cce-MTP.xml");
final SchematronixValidator validator = new SchematronixValidator(new StreamSource(cceFile), schematronixFile);
final boolean failFast = false; // True for a fast validation, false for a complete validation
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
 valid Schematron file) is made of the following
 optimizations:

- It does not depend on includes. This one helps a lot when reading the Schematron file, as the original CDA-CH-EMED Schematron files
 contain a lot of these (`cdachemed-MTP.sch` alone includes 204 other files).
- It does not contain rules that extend other rules. All rules directly contain all their tests, no need for lookups. The resulting file
 size increases, but storage is cheap and reading it with a streaming API (e.g. StAX) is not harder.
- Only the main phase is kept, as it contains all rules in the CDA-CH-EMED project.
- The only assert role that is kept is _error_, other asserts and all reports are dismissed. The CDA-CH-EMED project only uses _error_ and
 _warning_ roles.
