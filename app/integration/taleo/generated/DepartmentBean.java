
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for DepartmentBean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DepartmentBean">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="additionalProperties" type="{http://xml.apache.org/xml-soap}Map"/>
 *         &lt;element name="creationDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="departmentId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="departmentName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="lastUpdated" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
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
@XmlType(name = "DepartmentBean", propOrder = {
    "additionalProperties",
    "creationDate",
    "departmentId",
    "departmentName",
    "lastUpdated",
    "associatedUsers",
    "defaultApprovers"
})
public class DepartmentBean {

    @XmlElement(required = true, nillable = true)
    protected Map additionalProperties;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar creationDate;
    protected long departmentId;
    @XmlElement(required = true, nillable = true)
    protected String departmentName;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastUpdated;
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
     * Gets the value of the departmentId property.
     * 
     */
    public long getDepartmentId() {
        return departmentId;
    }

    /**
     * Sets the value of the departmentId property.
     * 
     */
    public void setDepartmentId(long value) {
        this.departmentId = value;
    }

    /**
     * Gets the value of the departmentName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDepartmentName() {
        return departmentName;
    }

    /**
     * Sets the value of the departmentName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDepartmentName(String value) {
        this.departmentName = value;
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
