
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LocationBean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LocationBean">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:TBEWebAPI}AddressEntityBean">
 *       &lt;sequence>
 *         &lt;element name="additionalProperties" type="{http://xml.apache.org/xml-soap}Map"/>
 *         &lt;element name="directions" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="locationId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="locationName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="regionId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="timeZone" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="interviewRooms" type="{urn:TBEWebAPI}ArrayOf_xsd_string"/>
 *         &lt;element name="defaultApprovers" type="{urn:TBEWebAPI}ArrayOf_xsd_long"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LocationBean", propOrder = {
    "additionalProperties",
    "directions",
    "locationId",
    "locationName",
    "regionId",
    "timeZone",
    "interviewRooms",
    "defaultApprovers"
})
public class LocationBean
    extends AddressEntityBean
{

    @XmlElement(required = true, nillable = true)
    protected Map additionalProperties;
    @XmlElement(required = true, nillable = true)
    protected String directions;
    protected long locationId;
    @XmlElement(required = true, nillable = true)
    protected String locationName;
    protected long regionId;
    @XmlElement(required = true, nillable = true)
    protected String timeZone;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfXsdString interviewRooms;
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
     * Gets the value of the directions property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDirections() {
        return directions;
    }

    /**
     * Sets the value of the directions property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDirections(String value) {
        this.directions = value;
    }

    /**
     * Gets the value of the locationId property.
     * 
     */
    public long getLocationId() {
        return locationId;
    }

    /**
     * Sets the value of the locationId property.
     * 
     */
    public void setLocationId(long value) {
        this.locationId = value;
    }

    /**
     * Gets the value of the locationName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocationName() {
        return locationName;
    }

    /**
     * Sets the value of the locationName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocationName(String value) {
        this.locationName = value;
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
     * Gets the value of the timeZone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the value of the timeZone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeZone(String value) {
        this.timeZone = value;
    }

    /**
     * Gets the value of the interviewRooms property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfXsdString }
     *     
     */
    public ArrayOfXsdString getInterviewRooms() {
        return interviewRooms;
    }

    /**
     * Sets the value of the interviewRooms property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfXsdString }
     *     
     */
    public void setInterviewRooms(ArrayOfXsdString value) {
        this.interviewRooms = value;
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
