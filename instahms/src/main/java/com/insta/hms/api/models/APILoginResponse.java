package com.insta.hms.api.models;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.insta.hms.api.services.CustomerService.ReturnCode;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requestHandlerKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="expiresIn" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="returnMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="returnCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@JsonInclude(Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "requestHandlerKey", "expiresIn", "returnMessage", "returnCode" })
@XmlRootElement(name = "APILoginResponse")
public class APILoginResponse {

	/**
	 * @param requestHandlerKey
	 * @param expiresIn
	 * @param returnMessage
	 * @param returnCode
	 */
	public APILoginResponse(String requestHandlerKey, BigInteger expiresIn, String returnMessage, String returnCode) {
		this.requestHandlerKey = requestHandlerKey;
		this.expiresIn = expiresIn;
		this.returnMessage = returnMessage;
		this.returnCode = returnCode;
	}

	public APILoginResponse() {

	}

	@XmlElement(name = "request_handler_key")
	protected String requestHandlerKey;
	@XmlElement(name = "expires_in")
	protected BigInteger expiresIn;
	@XmlElement(required = true, name = "return_message")
	protected String returnMessage;
	@XmlElement(required = true, name = "return_code")
	protected String returnCode;

	/**
	 * Gets the value of the requestHandlerKey property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getRequestHandlerKey() {
		return requestHandlerKey;
	}

	/**
	 * Sets the value of the requestHandlerKey property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setRequestHandlerKey(String value) {
		this.requestHandlerKey = value;
	}

	/**
	 * Gets the value of the expiresIn property.
	 * 
	 * @return possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getExpiresIn() {
		return expiresIn;
	}

	/**
	 * Sets the value of the expiresIn property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setExpiresIn(BigInteger value) {
		this.expiresIn = value;
	}

	/**
	 * Gets the value of the returnMessage property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getReturnMessage() {
		return returnMessage;
	}

	/**
	 * Sets the value of the returnMessage property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setReturnMessage(String value) {
		this.returnMessage = value;
	}

	/**
	 * Gets the value of the returnCode property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getReturnCode() {
		return returnCode;
	}

	/**
	 * Sets the value of the returnCode property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setReturnCode(String value) {
		this.returnCode = value;
	}

	public void setReturnCodeAndMessage(ReturnCode returnCode) {
		this.setReturnCode(returnCode.getReturnCode());
		this.setReturnMessage(returnCode.getReturnMessage());
	}
	
	
}
