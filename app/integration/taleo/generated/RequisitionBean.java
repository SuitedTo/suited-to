
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for RequisitionBean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RequisitionBean">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:TBEWebAPI}EntityBean">
 *       &lt;sequence>
 *         &lt;element name="city" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="duration" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="filledDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="jobCategory" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="jobCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="location" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="numOpen" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="openedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="payRange" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="state" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
@XmlType(name = "RequisitionBean", propOrder = {
    "city",
    "description",
    "duration",
    "filledDate",
    "jobCategory",
    "jobCode",
    "location",
    "numOpen",
    "openedDate",
    "payRange",
    "state",
    "title"
})
public class RequisitionBean
    extends EntityBean
{

    @XmlElement(required = true, nillable = true)
    protected String city;
    @XmlElement(required = true, nillable = true)
    protected String description;
    @XmlElement(required = true, nillable = true)
    protected String duration;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar filledDate;
    @XmlElement(required = true, nillable = true)
    protected String jobCategory;
    @XmlElement(required = true, nillable = true)
    protected String jobCode;
    @XmlElement(required = true, nillable = true)
    protected String location;
    protected int numOpen;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar openedDate;
    @XmlElement(required = true, nillable = true)
    protected String payRange;
    @XmlElement(required = true, nillable = true)
    protected String state;
    @XmlElement(required = true, nillable = true)
    protected String title;

    /**
     * Gets the value of the city property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the value of the city property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCity(String value) {
        this.city = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the duration property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDuration(String value) {
        this.duration = value;
    }

    /**
     * Gets the value of the filledDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getFilledDate() {
        return filledDate;
    }

    /**
     * Sets the value of the filledDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFilledDate(XMLGregorianCalendar value) {
        this.filledDate = value;
    }

    /**
     * Gets the value of the jobCategory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJobCategory() {
        return jobCategory;
    }

    /**
     * Sets the value of the jobCategory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJobCategory(String value) {
        this.jobCategory = value;
    }

    /**
     * Gets the value of the jobCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJobCode() {
        return jobCode;
    }

    /**
     * Sets the value of the jobCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJobCode(String value) {
        this.jobCode = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocation(String value) {
        this.location = value;
    }

    /**
     * Gets the value of the numOpen property.
     * 
     */
    public int getNumOpen() {
        return numOpen;
    }

    /**
     * Sets the value of the numOpen property.
     * 
     */
    public void setNumOpen(int value) {
        this.numOpen = value;
    }

    /**
     * Gets the value of the openedDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getOpenedDate() {
        return openedDate;
    }

    /**
     * Sets the value of the openedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setOpenedDate(XMLGregorianCalendar value) {
        this.openedDate = value;
    }

    /**
     * Gets the value of the payRange property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPayRange() {
        return payRange;
    }

    /**
     * Sets the value of the payRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPayRange(String value) {
        this.payRange = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setState(String value) {
        this.state = value;
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
