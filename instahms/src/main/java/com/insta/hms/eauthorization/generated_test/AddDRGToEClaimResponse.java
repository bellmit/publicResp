
package com.insta.hms.eauthorization.generated_test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AddDRGToEClaimResult" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="drgFileContent" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="drgFileName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="auditFileContent" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="reportFileContent" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="errorMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="errorReport" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "addDRGToEClaimResult",
    "drgFileContent",
    "drgFileName",
    "auditFileContent",
    "reportFileContent",
    "errorMessage",
    "errorReport"
})
@XmlRootElement(name = "AddDRGToEClaimResponse")
public class AddDRGToEClaimResponse {

    @XmlElement(name = "AddDRGToEClaimResult")
    protected int addDRGToEClaimResult;
    protected byte[] drgFileContent;
    protected String drgFileName;
    protected byte[] auditFileContent;
    protected byte[] reportFileContent;
    protected String errorMessage;
    protected byte[] errorReport;

    /**
     * Gets the value of the addDRGToEClaimResult property.
     * 
     */
    public int getAddDRGToEClaimResult() {
        return addDRGToEClaimResult;
    }

    /**
     * Sets the value of the addDRGToEClaimResult property.
     * 
     */
    public void setAddDRGToEClaimResult(int value) {
        this.addDRGToEClaimResult = value;
    }

    /**
     * Gets the value of the drgFileContent property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getDrgFileContent() {
        return drgFileContent;
    }

    /**
     * Sets the value of the drgFileContent property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setDrgFileContent(byte[] value) {
        this.drgFileContent = ((byte[]) value);
    }

    /**
     * Gets the value of the drgFileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDrgFileName() {
        return drgFileName;
    }

    /**
     * Sets the value of the drgFileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDrgFileName(String value) {
        this.drgFileName = value;
    }

    /**
     * Gets the value of the auditFileContent property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getAuditFileContent() {
        return auditFileContent;
    }

    /**
     * Sets the value of the auditFileContent property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setAuditFileContent(byte[] value) {
        this.auditFileContent = ((byte[]) value);
    }

    /**
     * Gets the value of the reportFileContent property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getReportFileContent() {
        return reportFileContent;
    }

    /**
     * Sets the value of the reportFileContent property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setReportFileContent(byte[] value) {
        this.reportFileContent = ((byte[]) value);
    }

    /**
     * Gets the value of the errorMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the value of the errorMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }

    /**
     * Gets the value of the errorReport property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getErrorReport() {
        return errorReport;
    }

    /**
     * Sets the value of the errorReport property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setErrorReport(byte[] value) {
        this.errorReport = ((byte[]) value);
    }

}
