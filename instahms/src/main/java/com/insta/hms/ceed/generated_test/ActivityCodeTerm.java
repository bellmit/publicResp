
package com.insta.hms.ceed.generated_test;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ActivityCodeTerm.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ActivityCodeTerm">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CPT4v2012"/>
 *     &lt;enumeration value="CPT4v2011"/>
 *     &lt;enumeration value="DSL"/>
 *     &lt;enumeration value="DDC"/>
 *     &lt;enumeration value="HCPCS"/>
 *     &lt;enumeration value="ADA"/>
 *     &lt;enumeration value="CDT"/>
 *     &lt;enumeration value="HAAD"/>
 *     &lt;enumeration value="MEDD"/>
 *     &lt;enumeration value="ASL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ActivityCodeTerm")
@XmlEnum
public enum ActivityCodeTerm {

    @XmlEnumValue("CPT4v2012")
    CPT_4_V_2012("CPT4v2012"),
    @XmlEnumValue("CPT4v2011")
    CPT_4_V_2011("CPT4v2011"),
    DSL("DSL"),
    DDC("DDC"),
    HCPCS("HCPCS"),
    ADA("ADA"),
    CDT("CDT"),
    HAAD("HAAD"),
    MEDD("MEDD"),
    ASL("ASL");
    private final String value;

    ActivityCodeTerm(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ActivityCodeTerm fromValue(String v) {
        for (ActivityCodeTerm c: ActivityCodeTerm.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
