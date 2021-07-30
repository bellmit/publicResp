
package com.insta.hms.ceed.generated_test;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DiagnosisCodeTerm.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DiagnosisCodeTerm">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ICD10"/>
 *     &lt;enumeration value="ICD9"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DiagnosisCodeTerm")
@XmlEnum
public enum DiagnosisCodeTerm {

    @XmlEnumValue("ICD10")
    ICD_10("ICD10"),
    @XmlEnumValue("ICD9")
    ICD_9("ICD9");
    private final String value;

    DiagnosisCodeTerm(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DiagnosisCodeTerm fromValue(String v) {
        for (DiagnosisCodeTerm c: DiagnosisCodeTerm.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
