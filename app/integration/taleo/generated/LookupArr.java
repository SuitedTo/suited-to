
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LookupArr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LookupArr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="array" type="{urn:TBEWebAPI}ArrayOfLookupBean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LookupArr", propOrder = {
    "array"
})
public class LookupArr {

    @XmlElement(required = true, nillable = true)
    protected ArrayOfLookupBean array;

    /**
     * Gets the value of the array property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfLookupBean }
     *     
     */
    public ArrayOfLookupBean getArray() {
        return array;
    }

    /**
     * Sets the value of the array property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfLookupBean }
     *     
     */
    public void setArray(ArrayOfLookupBean value) {
        this.array = value;
    }

}
