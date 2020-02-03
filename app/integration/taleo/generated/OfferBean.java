
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for OfferBean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OfferBean">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:TBEWebAPI}EntityBean">
 *       &lt;sequence>
 *         &lt;element name="candidateId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="creator" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="employmentType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="expirationDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="manager" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="offerFileName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="payRate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="requisitionId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="startDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="stockOptions" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OfferBean", propOrder = {
    "candidateId",
    "creator",
    "employmentType",
    "expirationDate",
    "manager",
    "offerFileName",
    "payRate",
    "requisitionId",
    "startDate",
    "stockOptions",
    "title"
})
public class OfferBean
    extends EntityBean
{

    protected long candidateId;
    @XmlElement(required = true, nillable = true)
    protected String creator;
    @XmlElement(required = true, nillable = true)
    protected String employmentType;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar expirationDate;
    @XmlElement(required = true, nillable = true)
    protected String manager;
    @XmlElement(required = true, nillable = true)
    protected String offerFileName;
    @XmlElement(required = true, nillable = true)
    protected String payRate;
    protected long requisitionId;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startDate;
    @XmlElement(required = true, nillable = true)
    protected String stockOptions;
    @XmlElement(required = true, nillable = true)
    protected String title;

    /**
     * Gets the value of the candidateId property.
     * 
     */
    public long getCandidateId() {
        return candidateId;
    }

    /**
     * Sets the value of the candidateId property.
     * 
     */
    public void setCandidateId(long value) {
        this.candidateId = value;
    }

    /**
     * Gets the value of the creator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Sets the value of the creator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreator(String value) {
        this.creator = value;
    }

    /**
     * Gets the value of the employmentType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmploymentType() {
        return employmentType;
    }

    /**
     * Sets the value of the employmentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmploymentType(String value) {
        this.employmentType = value;
    }

    /**
     * Gets the value of the expirationDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the value of the expirationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setExpirationDate(XMLGregorianCalendar value) {
        this.expirationDate = value;
    }

    /**
     * Gets the value of the manager property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManager() {
        return manager;
    }

    /**
     * Sets the value of the manager property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManager(String value) {
        this.manager = value;
    }

    /**
     * Gets the value of the offerFileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOfferFileName() {
        return offerFileName;
    }

    /**
     * Sets the value of the offerFileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOfferFileName(String value) {
        this.offerFileName = value;
    }

    /**
     * Gets the value of the payRate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPayRate() {
        return payRate;
    }

    /**
     * Sets the value of the payRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPayRate(String value) {
        this.payRate = value;
    }

    /**
     * Gets the value of the requisitionId property.
     * 
     */
    public long getRequisitionId() {
        return requisitionId;
    }

    /**
     * Sets the value of the requisitionId property.
     * 
     */
    public void setRequisitionId(long value) {
        this.requisitionId = value;
    }

    /**
     * Gets the value of the startDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartDate() {
        return startDate;
    }

    /**
     * Sets the value of the startDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartDate(XMLGregorianCalendar value) {
        this.startDate = value;
    }

    /**
     * Gets the value of the stockOptions property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStockOptions() {
        return stockOptions;
    }

    /**
     * Sets the value of the stockOptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStockOptions(String value) {
        this.stockOptions = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

}
