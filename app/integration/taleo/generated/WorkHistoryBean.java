
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for WorkHistoryBean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WorkHistoryBean">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:TBEWebAPI}EntityBean">
 *       &lt;sequence>
 *         &lt;element name="entityId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="seqNo" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="jobTitle" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="companyName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="dateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="jobDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="companyStreetAddress" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="companyCityState" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="phone" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="directSupervisor" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="supervisorTitle" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="explanation" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="finalRateOfPay" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="okToContact" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="reasonForLeaving" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WorkHistoryBean", propOrder = {
    "entityId",
    "seqNo",
    "jobTitle",
    "companyName",
    "dateFrom",
    "dateTo",
    "jobDescription",
    "companyStreetAddress",
    "companyCityState",
    "phone",
    "directSupervisor",
    "supervisorTitle",
    "explanation",
    "finalRateOfPay",
    "okToContact",
    "reasonForLeaving"
})
public class WorkHistoryBean
    extends EntityBean
{

    protected long entityId;
    protected int seqNo;
    @XmlElement(required = true, nillable = true)
    protected String jobTitle;
    @XmlElement(required = true, nillable = true)
    protected String companyName;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateFrom;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateTo;
    @XmlElement(required = true, nillable = true)
    protected String jobDescription;
    @XmlElement(required = true, nillable = true)
    protected String companyStreetAddress;
    @XmlElement(required = true, nillable = true)
    protected String companyCityState;
    @XmlElement(required = true, nillable = true)
    protected String phone;
    @XmlElement(required = true, nillable = true)
    protected String directSupervisor;
    @XmlElement(required = true, nillable = true)
    protected String supervisorTitle;
    @XmlElement(required = true, nillable = true)
    protected String explanation;
    @XmlElement(required = true, nillable = true)
    protected String finalRateOfPay;
    protected boolean okToContact;
    @XmlElement(required = true, nillable = true)
    protected String reasonForLeaving;

    /**
     * Gets the value of the entityId property.
     * 
     */
    public long getEntityId() {
        return entityId;
    }

    /**
     * Sets the value of the entityId property.
     * 
     */
    public void setEntityId(long value) {
        this.entityId = value;
    }

    /**
     * Gets the value of the seqNo property.
     * 
     */
    public int getSeqNo() {
        return seqNo;
    }

    /**
     * Sets the value of the seqNo property.
     * 
     */
    public void setSeqNo(int value) {
        this.seqNo = value;
    }

    /**
     * Gets the value of the jobTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJobTitle() {
        return jobTitle;
    }

    /**
     * Sets the value of the jobTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJobTitle(String value) {
        this.jobTitle = value;
    }

    /**
     * Gets the value of the companyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Sets the value of the companyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompanyName(String value) {
        this.companyName = value;
    }

    /**
     * Gets the value of the dateFrom property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateFrom() {
        return dateFrom;
    }

    /**
     * Sets the value of the dateFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateFrom(XMLGregorianCalendar value) {
        this.dateFrom = value;
    }

    /**
     * Gets the value of the dateTo property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateTo() {
        return dateTo;
    }

    /**
     * Sets the value of the dateTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateTo(XMLGregorianCalendar value) {
        this.dateTo = value;
    }

    /**
     * Gets the value of the jobDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJobDescription() {
        return jobDescription;
    }

    /**
     * Sets the value of the jobDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJobDescription(String value) {
        this.jobDescription = value;
    }

    /**
     * Gets the value of the companyStreetAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompanyStreetAddress() {
        return companyStreetAddress;
    }

    /**
     * Sets the value of the companyStreetAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompanyStreetAddress(String value) {
        this.companyStreetAddress = value;
    }

    /**
     * Gets the value of the companyCityState property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompanyCityState() {
        return companyCityState;
    }

    /**
     * Sets the value of the companyCityState property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompanyCityState(String value) {
        this.companyCityState = value;
    }

    /**
     * Gets the value of the phone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the value of the phone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhone(String value) {
        this.phone = value;
    }

    /**
     * Gets the value of the directSupervisor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDirectSupervisor() {
        return directSupervisor;
    }

    /**
     * Sets the value of the directSupervisor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDirectSupervisor(String value) {
        this.directSupervisor = value;
    }

    /**
     * Gets the value of the supervisorTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupervisorTitle() {
        return supervisorTitle;
    }

    /**
     * Sets the value of the supervisorTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupervisorTitle(String value) {
        this.supervisorTitle = value;
    }

    /**
     * Gets the value of the explanation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Sets the value of the explanation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExplanation(String value) {
        this.explanation = value;
    }

    /**
     * Gets the value of the finalRateOfPay property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinalRateOfPay() {
        return finalRateOfPay;
    }

    /**
     * Sets the value of the finalRateOfPay property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinalRateOfPay(String value) {
        this.finalRateOfPay = value;
    }

    /**
     * Gets the value of the okToContact property.
     * 
     */
    public boolean isOkToContact() {
        return okToContact;
    }

    /**
     * Sets the value of the okToContact property.
     * 
     */
    public void setOkToContact(boolean value) {
        this.okToContact = value;
    }

    /**
     * Gets the value of the reasonForLeaving property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReasonForLeaving() {
        return reasonForLeaving;
    }

    /**
     * Sets the value of the reasonForLeaving property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReasonForLeaving(String value) {
        this.reasonForLeaving = value;
    }

}
