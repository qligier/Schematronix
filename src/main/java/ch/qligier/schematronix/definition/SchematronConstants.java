package ch.qligier.schematronix.definition;

import lombok.experimental.UtilityClass;

/**
 * Some Schematron constants.
 *
 * @author Quentin Ligier
 */
@UtilityClass
@SuppressWarnings("WeakerAccess")
public class SchematronConstants {

    /**
     * Definition of the Schematron tag names.
     */
    public static final String ROOT_TAG_NAME = "schema";
    public static final String PATTERN_TAG_NAME = "pattern";
    public static final String RULE_TAG_NAME = "rule";
    public static final String ASSERT_TAG_NAME = "assert";
    public static final String REPORT_TAG_NAME = "report";
    public static final String LET_TAG_NAME = "let";
    public static final String EXTENDS_TAG_NAME = "extends";
    public static final String ACTIVE_TAG_NAME = "active";
    public static final String PHASE_TAG_NAME = "phase";
    public static final String TITLE_TAG_NAME = "title";
    public static final String NAMESPACE_TAG_NAME = "ns";
    public static final String INCLUDE_TAG_NAME = "include";

    public static final String SCHEMATRON_NAMESPACE = "http://purl.oclc.org/dsdl/schematron";
}
