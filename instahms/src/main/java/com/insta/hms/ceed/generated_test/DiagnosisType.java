
package com.insta.hms.ceed.generated_test;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DiagnosisType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DiagnosisType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Principal"/>
 *     &lt;enumeration value="Secondary"/>
 *     &lt;enumeration value="Admitting"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DiagnosisType")
@XmlEnum
public enum DiagnosisType {

    @XmlEnumValue("Principal")
    PRINCIPAL("Principal"),
    @XmlEnumValue("Secondary")
    SECONDARY("Secondary"),
    @XmlEnumValue("Admitting")
    ADMITTING("Admitting");
    private final String value;

    DiagnosisType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DiagnosisType fromValue(String v) {
        for (DiagnosisType c: DiagnosisType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
