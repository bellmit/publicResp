
package com.insta.hms.ceed.generated_test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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
 *         &lt;element name="Claim" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="Person" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="BirthDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="Gender" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}Gender" minOccurs="0"/>
 *                             &lt;element name="Weight" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *                             &lt;element name="BreastFeeding" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}BreastFeeding" minOccurs="0"/>
 *                             &lt;element name="WeeksOfAmenorrhoea" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *                             &lt;element name="CreatinClearance" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="Encounter" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="Type" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}EncounterType"/>
 *                             &lt;element name="Start" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="End" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="StartType" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}EncounterStartType" minOccurs="0"/>
 *                             &lt;element name="EndType" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}EncounterEndType" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="Diagnosis" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="CodeTerm" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}DiagnosisCodeTerm"/>
 *                             &lt;element name="Type" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}DiagnosisType"/>
 *                             &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="Activity" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="CodeTerm" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}ActivityCodeTerm"/>
 *                             &lt;element name="Start" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                             &lt;element name="Clinician" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="Observation" maxOccurs="unbounded" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                                       &lt;element name="Type" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}ObservationType"/>
 *                                       &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                                       &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                                       &lt;element name="ValueType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    "claim"
})
@XmlRootElement(name = "request", namespace = "")
public class Request {

    @XmlElement(name = "Claim", namespace = "")
    protected List<Request.Claim> claim;

    /**
     * Gets the value of the claim property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the claim property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClaim().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Request.Claim }
     * 
     * 
     */
    public List<Request.Claim> getClaim() {
        if (claim == null) {
            claim = new ArrayList<Request.Claim>();
        }
        return this.claim;
    }


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
     *         &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="Person" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="BirthDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="Gender" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}Gender" minOccurs="0"/>
     *                   &lt;element name="Weight" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
     *                   &lt;element name="BreastFeeding" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}BreastFeeding" minOccurs="0"/>
     *                   &lt;element name="WeeksOfAmenorrhoea" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
     *                   &lt;element name="CreatinClearance" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="Encounter" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="Type" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}EncounterType"/>
     *                   &lt;element name="Start" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="End" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="StartType" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}EncounterStartType" minOccurs="0"/>
     *                   &lt;element name="EndType" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}EncounterEndType" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="Diagnosis" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="CodeTerm" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}DiagnosisCodeTerm"/>
     *                   &lt;element name="Type" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}DiagnosisType"/>
     *                   &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="Activity" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="CodeTerm" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}ActivityCodeTerm"/>
     *                   &lt;element name="Start" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}float"/>
     *                   &lt;element name="Clinician" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="Observation" maxOccurs="unbounded" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                             &lt;element name="Type" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}ObservationType"/>
     *                             &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                             &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                             &lt;element name="ValueType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
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
        "id",
        "person",
        "encounter",
        "diagnosis",
        "activity"
    })
    public static class Claim {

        @XmlElement(name = "ID", namespace = "")
        protected String id;
        @XmlElement(name = "Person", namespace = "")
        protected Request.Claim.Person person;
        @XmlElement(name = "Encounter", namespace = "")
        protected Request.Claim.Encounter encounter;
        @XmlElement(name = "Diagnosis", namespace = "")
        protected List<Request.Claim.Diagnosis> diagnosis;
        @XmlElement(name = "Activity", namespace = "")
        protected List<Request.Claim.Activity> activity;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getID() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setID(String value) {
            this.id = value;
        }

        /**
         * Gets the value of the person property.
         * 
         * @return
         *     possible object is
         *     {@link Request.Claim.Person }
         *     
         */
        public Request.Claim.Person getPerson() {
            return person;
        }

        /**
         * Sets the value of the person property.
         * 
         * @param value
         *     allowed object is
         *     {@link Request.Claim.Person }
         *     
         */
        public void setPerson(Request.Claim.Person value) {
            this.person = value;
        }

        /**
         * Gets the value of the encounter property.
         * 
         * @return
         *     possible object is
         *     {@link Request.Claim.Encounter }
         *     
         */
        public Request.Claim.Encounter getEncounter() {
            return encounter;
        }

        /**
         * Sets the value of the encounter property.
         * 
         * @param value
         *     allowed object is
         *     {@link Request.Claim.Encounter }
         *     
         */
        public void setEncounter(Request.Claim.Encounter value) {
            this.encounter = value;
        }

        /**
         * Gets the value of the diagnosis property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the diagnosis property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getDiagnosis().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Request.Claim.Diagnosis }
         * 
         * 
         */
        public List<Request.Claim.Diagnosis> getDiagnosis() {
            if (diagnosis == null) {
                diagnosis = new ArrayList<Request.Claim.Diagnosis>();
            }
            return this.diagnosis;
        }

        /**
         * Gets the value of the activity property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the activity property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getActivity().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Request.Claim.Activity }
         * 
         * 
         */
        public List<Request.Claim.Activity> getActivity() {
            if (activity == null) {
                activity = new ArrayList<Request.Claim.Activity>();
            }
            return this.activity;
        }


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
         *         &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="CodeTerm" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}ActivityCodeTerm"/>
         *         &lt;element name="Start" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="Quantity" type="{http://www.w3.org/2001/XMLSchema}float"/>
         *         &lt;element name="Clinician" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="Observation" maxOccurs="unbounded" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *                   &lt;element name="Type" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}ObservationType"/>
         *                   &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *                   &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *                   &lt;element name="ValueType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
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
            "id",
            "codeTerm",
            "start",
            "code",
            "quantity",
            "clinician",
            "observation"
        })
        public static class Activity {

            @XmlElement(name = "ID", namespace = "")
            protected String id;
            @XmlElement(name = "CodeTerm", namespace = "", required = true)
            protected ActivityCodeTerm codeTerm;
            @XmlElement(name = "Start", namespace = "")
            protected String start;
            @XmlElement(name = "Code", namespace = "")
            protected String code;
            @XmlElement(name = "Quantity", namespace = "")
            protected float quantity;
            @XmlElement(name = "Clinician", namespace = "")
            protected String clinician;
            @XmlElement(name = "Observation", namespace = "")
            protected List<Request.Claim.Activity.Observation> observation;

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getID() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setID(String value) {
                this.id = value;
            }

            /**
             * Gets the value of the codeTerm property.
             * 
             * @return
             *     possible object is
             *     {@link ActivityCodeTerm }
             *     
             */
            public ActivityCodeTerm getCodeTerm() {
                return codeTerm;
            }

            /**
             * Sets the value of the codeTerm property.
             * 
             * @param value
             *     allowed object is
             *     {@link ActivityCodeTerm }
             *     
             */
            public void setCodeTerm(ActivityCodeTerm value) {
                this.codeTerm = value;
            }

            /**
             * Gets the value of the start property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getStart() {
                return start;
            }

            /**
             * Sets the value of the start property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setStart(String value) {
                this.start = value;
            }

            /**
             * Gets the value of the code property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getCode() {
                return code;
            }

            /**
             * Sets the value of the code property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setCode(String value) {
                this.code = value;
            }

            /**
             * Gets the value of the quantity property.
             * 
             */
            public float getQuantity() {
                return quantity;
            }

            /**
             * Sets the value of the quantity property.
             * 
             */
            public void setQuantity(float value) {
                this.quantity = value;
            }

            /**
             * Gets the value of the clinician property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getClinician() {
                return clinician;
            }

            /**
             * Sets the value of the clinician property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setClinician(String value) {
                this.clinician = value;
            }

            /**
             * Gets the value of the observation property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the observation property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getObservation().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link Request.Claim.Activity.Observation }
             * 
             * 
             */
            public List<Request.Claim.Activity.Observation> getObservation() {
                if (observation == null) {
                    observation = new ArrayList<Request.Claim.Activity.Observation>();
                }
                return this.observation;
            }


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
             *         &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
             *         &lt;element name="Type" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}ObservationType"/>
             *         &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
             *         &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
             *         &lt;element name="ValueType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
                "id",
                "type",
                "code",
                "value",
                "valueType"
            })
            public static class Observation {

                @XmlElement(name = "ID", namespace = "")
                protected String id;
                @XmlElement(name = "Type", namespace = "", required = true)
                protected String type;
                @XmlElement(name = "Code", namespace = "")
                protected String code;
                @XmlElement(name = "Value", namespace = "")
                protected String value;
                @XmlElement(name = "ValueType", namespace = "")
                protected String valueType;

                /**
                 * Gets the value of the id property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getID() {
                    return id;
                }

                /**
                 * Sets the value of the id property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setID(String value) {
                    this.id = value;
                }

                /**
                 * Gets the value of the type property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getType() {
                    return type;
                }

                /**
                 * Sets the value of the type property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setType(String value) {
                    this.type = value;
                }

                /**
                 * Gets the value of the code property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getCode() {
                    return code;
                }

                /**
                 * Sets the value of the code property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setCode(String value) {
                    this.code = value;
                }

                /**
                 * Gets the value of the value property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getValue() {
                    return value;
                }

                /**
                 * Sets the value of the value property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setValue(String value) {
                    this.value = value;
                }

                /**
                 * Gets the value of the valueType property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getValueType() {
                    return valueType;
                }

                /**
                 * Sets the value of the valueType property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setValueType(String value) {
                    this.valueType = value;
                }

            }

        }


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
         *         &lt;element name="CodeTerm" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}DiagnosisCodeTerm"/>
         *         &lt;element name="Type" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}DiagnosisType"/>
         *         &lt;element name="Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
            "codeTerm",
            "type",
            "code"
        })
        public static class Diagnosis {

            @XmlElement(name = "CodeTerm", namespace = "", required = true)
            protected DiagnosisCodeTerm codeTerm;
            @XmlElement(name = "Type", namespace = "", required = true)
            protected DiagnosisType type;
            @XmlElement(name = "Code", namespace = "")
            protected String code;

            /**
             * Gets the value of the codeTerm property.
             * 
             * @return
             *     possible object is
             *     {@link DiagnosisCodeTerm }
             *     
             */
            public DiagnosisCodeTerm getCodeTerm() {
                return codeTerm;
            }

            /**
             * Sets the value of the codeTerm property.
             * 
             * @param value
             *     allowed object is
             *     {@link DiagnosisCodeTerm }
             *     
             */
            public void setCodeTerm(DiagnosisCodeTerm value) {
                this.codeTerm = value;
            }

            /**
             * Gets the value of the type property.
             * 
             * @return
             *     possible object is
             *     {@link DiagnosisType }
             *     
             */
            public DiagnosisType getType() {
                return type;
            }

            /**
             * Sets the value of the type property.
             * 
             * @param value
             *     allowed object is
             *     {@link DiagnosisType }
             *     
             */
            public void setType(DiagnosisType value) {
                this.type = value;
            }

            /**
             * Gets the value of the code property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getCode() {
                return code;
            }

            /**
             * Sets the value of the code property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setCode(String value) {
                this.code = value;
            }

        }


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
         *         &lt;element name="Type" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}EncounterType"/>
         *         &lt;element name="Start" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="End" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="StartType" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}EncounterStartType" minOccurs="0"/>
         *         &lt;element name="EndType" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}EncounterEndType" minOccurs="0"/>
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
            "type",
            "start",
            "end",
            "startType",
            "endType"
        })
        public static class Encounter {

            @XmlElement(name = "Type", namespace = "", required = true)
            protected String type;
            @XmlElement(name = "Start", namespace = "")
            protected String start;
            @XmlElement(name = "End", namespace = "")
            protected String end;
            @XmlElement(name = "StartType", namespace = "")
            protected String startType;
            @XmlElement(name = "EndType", namespace = "")
            protected String endType;

            /**
             * Gets the value of the type property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getType() {
                return type;
            }

            /**
             * Sets the value of the type property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setType(String value) {
                this.type = value;
            }

            /**
             * Gets the value of the start property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getStart() {
                return start;
            }

            /**
             * Sets the value of the start property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setStart(String value) {
                this.start = value;
            }

            /**
             * Gets the value of the end property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getEnd() {
                return end;
            }

            /**
             * Sets the value of the end property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setEnd(String value) {
                this.end = value;
            }

            /**
             * Gets the value of the startType property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getStartType() {
                return startType;
            }

            /**
             * Sets the value of the startType property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setStartType(String value) {
                this.startType = value;
            }

            /**
             * Gets the value of the endType property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getEndType() {
                return endType;
            }

            /**
             * Sets the value of the endType property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setEndType(String value) {
                this.endType = value;
            }

        }


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
         *         &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="BirthDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="Gender" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}Gender" minOccurs="0"/>
         *         &lt;element name="Weight" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
         *         &lt;element name="BreastFeeding" type="{http://www.Dimensions-healthcare.net/DHCEG/CommonTypes}BreastFeeding" minOccurs="0"/>
         *         &lt;element name="WeeksOfAmenorrhoea" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
         *         &lt;element name="CreatinClearance" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
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
            "id",
            "birthDate",
            "gender",
            "weight",
            "breastFeeding",
            "weeksOfAmenorrhoea",
            "creatinClearance"
        })
        public static class Person {

            @XmlElement(name = "ID", namespace = "")
            protected String id;
            @XmlElement(name = "BirthDate", namespace = "")
            protected String birthDate;
            @XmlElement(name = "Gender", namespace = "")
            protected Gender gender;
            @XmlElement(name = "Weight", namespace = "")
            protected BigInteger weight;
            @XmlElement(name = "BreastFeeding", namespace = "")
            protected String breastFeeding;
            @XmlElement(name = "WeeksOfAmenorrhoea", namespace = "")
            protected BigInteger weeksOfAmenorrhoea;
            @XmlElement(name = "CreatinClearance", namespace = "")
            protected BigInteger creatinClearance;

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getID() {
                return id;
            }

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setID(String value) {
                this.id = value;
            }

            /**
             * Gets the value of the birthDate property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getBirthDate() {
                return birthDate;
            }

            /**
             * Sets the value of the birthDate property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setBirthDate(String value) {
                this.birthDate = value;
            }

            /**
             * Gets the value of the gender property.
             * 
             * @return
             *     possible object is
             *     {@link Gender }
             *     
             */
            public Gender getGender() {
                return gender;
            }

            /**
             * Sets the value of the gender property.
             * 
             * @param value
             *     allowed object is
             *     {@link Gender }
             *     
             */
            public void setGender(Gender value) {
                this.gender = value;
            }

            /**
             * Gets the value of the weight property.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getWeight() {
                return weight;
            }

            /**
             * Sets the value of the weight property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setWeight(BigInteger value) {
                this.weight = value;
            }

            /**
             * Gets the value of the breastFeeding property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getBreastFeeding() {
                return breastFeeding;
            }

            /**
             * Sets the value of the breastFeeding property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setBreastFeeding(String value) {
                this.breastFeeding = value;
            }

            /**
             * Gets the value of the weeksOfAmenorrhoea property.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getWeeksOfAmenorrhoea() {
                return weeksOfAmenorrhoea;
            }

            /**
             * Sets the value of the weeksOfAmenorrhoea property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setWeeksOfAmenorrhoea(BigInteger value) {
                this.weeksOfAmenorrhoea = value;
            }

            /**
             * Gets the value of the creatinClearance property.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getCreatinClearance() {
                return creatinClearance;
            }

            /**
             * Sets the value of the creatinClearance property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setCreatinClearance(BigInteger value) {
                this.creatinClearance = value;
            }

        }

    }

}
