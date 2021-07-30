
package com.insta.hms.ceed.generated_test;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ClaimEditRank.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ClaimEditRank">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="A"/>
 *     &lt;enumeration value="R"/>
 *     &lt;enumeration value="I"/>
 *     &lt;enumeration value="NA"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ClaimEditRank")
@XmlEnum
public enum ClaimEditRank {

    A,
    R,
    I,
    NA;

    public String value() {
        return name();
    }

    public static ClaimEditRank fromValue(String v) {
        return valueOf(v);
    }

}
