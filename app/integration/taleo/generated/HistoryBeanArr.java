
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HistoryBeanArr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HistoryBeanArr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="array" type="{urn:TBEWebAPI}ArrayOfHistoryBean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HistoryBeanArr", propOrder = {
    "array"
})
public class HistoryBeanArr {

    @XmlElement(required = true, nillable = true)
    protected ArrayOfHistoryBean array;

    /**
     * Gets the value of the array property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfHistoryBean }
     *     
     */
    public ArrayOfHistoryBean getArray() {
        return array;
    }

    /**
     * Sets the value of the array property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfHistoryBean }
     *     
     */
    public void setArray(ArrayOfHistoryBean value) {
        this.array = value;
    }

}
