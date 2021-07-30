
package com.insta.hms.erxprescription.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="facilityLogin" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="facilityPwd" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="clinicianLogin" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="clinicianPwd" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fileContent" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="fileName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "facilityLogin",
    "facilityPwd",
    "clinicianLogin",
    "clinicianPwd",
    "fileContent",
    "fileName"
})
@XmlRootElement(name = "UploadERxRequest")
public class UploadERxRequest {

    protected String facilityLogin;
    protected String facilityPwd;
    protected String clinicianLogin;
    protected String clinicianPwd;
    protected byte[] fileContent;
    protected String fileName;

    /**
     * Gets the value of the facilityLogin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFacilityLogin() {
        return facilityLogin;
    }

    /**
     * Sets the value of the facilityLogin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFacilityLogin(String value) {
        this.facilityLogin = value;
    }

    /**
     * Gets the value of the facilityPwd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFacilityPwd() {
        return facilityPwd;
    }

    /**
     * Sets the value of the facilityPwd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFacilityPwd(String value) {
        this.facilityPwd = value;
    }

    /**
     * Gets the value of the clinicianLogin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClinicianLogin() {
        return clinicianLogin;
    }

    /**
     * Sets the value of the clinicianLogin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClinicianLogin(String value) {
        this.clinicianLogin = value;
    }

    /**
     * Gets the value of the clinicianPwd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClinicianPwd() {
        return clinicianPwd;
    }

    /**
     * Sets the value of the clinicianPwd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClinicianPwd(String value) {
        this.clinicianPwd = value;
    }

    /**
     * Gets the value of the fileContent property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getFileContent() {
        return fileContent;
    }

    /**
     * Sets the value of the fileContent property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setFileContent(byte[] value) {
        this.fileContent = ((byte[]) value);
    }

    /**
     * Gets the value of the fileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the value of the fileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileName(String value) {
        this.fileName = value;
    }

}
