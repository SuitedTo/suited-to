
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MetadataArr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MetadataArr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="array" type="{urn:TBEWebAPI}ArrayOfMetadataBean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MetadataArr", propOrder = {
    "array"
})
public class MetadataArr {

    @XmlElement(required = true, nillable = true)
    protected ArrayOfMetadataBean array;

    /**
     * Gets the value of the array property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMetadataBean }
     *     
     */
    public ArrayOfMetadataBean getArray() {
        return array;
    }

    /**
     * Sets the value of the array property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMetadataBean }
     *     
     */
    public void setArray(ArrayOfMetadataBean value) {
        this.array = value;
    }

}
