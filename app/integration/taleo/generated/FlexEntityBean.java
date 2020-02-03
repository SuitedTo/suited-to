
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FlexEntityBean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FlexEntityBean">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:TBEWebAPI}EntityBean">
 *       &lt;sequence>
 *         &lt;element name="attributes" type="{urn:TBEWebAPI}ArrayOfFlexFieldBean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FlexEntityBean", propOrder = {
    "attributes"
})
@XmlSeeAlso({
    FlexRollingEntityBean.class
})
public class FlexEntityBean
    extends EntityBean
{

    @XmlElement(required = true, nillable = true)
    protected ArrayOfFlexFieldBean attributes;

    /**
     * Gets the value of the attributes property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfFlexFieldBean }
     *     
     */
    public ArrayOfFlexFieldBean getAttributes() {
        return attributes;
    }

    /**
     * Sets the value of the attributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfFlexFieldBean }
     *     
     */
    public void setAttributes(ArrayOfFlexFieldBean value) {
        this.attributes = value;
    }

}
