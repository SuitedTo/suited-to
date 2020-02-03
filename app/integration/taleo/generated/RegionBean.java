
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for RegionBean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegionBean">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="additionalProperties" type="{http://xml.apache.org/xml-soap}Map"/>
 *         &lt;element name="creationDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="lastUpdated" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="regionId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="regionName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="associatedLocations" type="{urn:TBEWebAPI}ArrayOf_xsd_string"/>
 *         &lt;element name="associatedUsers" type="{urn:TBEWebAPI}ArrayOf_xsd_long"/>
 *         &lt;element name="defaultApprovers" type="{urn:TBEWebAPI}ArrayOf_xsd_long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegionBean", propOrder = {
    "additionalProperties",
    "creationDate",
    "lastUpdated",
    "regionId",
    "regionName",
    "associatedLocations",
    "associatedUsers",
    "defaultApprovers"
})
public class RegionBean {

    @XmlElement(required = true, nillable = true)
    protected Map additionalProperties;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar creationDate;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastUpdated;
    protected long regionId;
    @XmlElement(required = true, nillable = true)
    protected String regionName;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfXsdString associatedLocations;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfXsdLong associatedUsers;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfXsdLong defaultApprovers;

    /**
     * Gets the value of the additionalProperties property.
     * 
     * @return
     *     possible object is
     *     {@link Map }
     *     
     */
    public Map getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Sets the value of the additionalProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link Map }
     *     
     */
    public void setAdditionalProperties(Map value) {
        this.additionalProperties = value;
    }

    /**
     * Gets the value of the creationDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the value of the creationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreationDate(XMLGregorianCalendar value) {
        this.creationDate = value;
    }

    /**
     * Gets the value of the lastUpdated property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Sets the value of the lastUpdated property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastUpdated(XMLGregorianCalendar value) {
        this.lastUpdated = value;
    }

    /**
     * Gets the value of the regionId property.
     * 
     */
    public long getRegionId() {
        return regionId;
    }

    /**
     * Sets the value of the regionId property.
     * 
     */
    public void setRegionId(long value) {
        this.regionId = value;
    }

    /**
     * Gets the value of the regionName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegionName() {
        return regionName;
    }

    /**
     * Sets the value of the regionName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegionName(String value) {
        this.regionName = value;
    }

    /**
     * Gets the value of the associatedLocations property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfXsdString }
     *     
     */
    public ArrayOfXsdString getAssociatedLocations() {
        return associatedLocations;
    }

    /**
     * Sets the value of the associatedLocations property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfXsdString }
     *     
     */
    public void setAssociatedLocations(ArrayOfXsdString value) {
        this.associatedLocations = value;
    }

    /**
     * Gets the value of the associatedUsers property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfXsdLong }
     *     
     */
    public ArrayOfXsdLong getAssociatedUsers() {
        return associatedUsers;
    }

    /**
     * Sets the value of the associatedUsers property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfXsdLong }
     *     
     */
    public void setAssociatedUsers(ArrayOfXsdLong value) {
        this.associatedUsers = value;
    }

    /**
     * Gets the value of the defaultApprovers property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfXsdLong }
     *     
     */
    public ArrayOfXsdLong getDefaultApprovers() {
        return defaultApprovers;
    }

    /**
     * Sets the value of the defaultApprovers property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfXsdLong }
     *     
     */
    public void setDefaultApprovers(ArrayOfXsdLong value) {
        this.defaultApprovers = value;
    }

}
