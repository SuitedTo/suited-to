
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for FlexFieldBean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FlexFieldBean">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fieldName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="valueBool" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="valueDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="valueDbl" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="valueInt" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="valueLong" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="valueStr" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FlexFieldBean", propOrder = {
    "fieldName",
    "valueBool",
    "valueDate",
    "valueDbl",
    "valueInt",
    "valueLong",
    "valueStr"
})
public class FlexFieldBean {

    @XmlElement(required = true, nillable = true)
    protected String fieldName;
    @XmlElement(required = true, type = Boolean.class, nillable = true)
    protected Boolean valueBool;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar valueDate;
    @XmlElement(required = true, type = Double.class, nillable = true)
    protected Double valueDbl;
    @XmlElement(required = true, type = Integer.class, nillable = true)
    protected Integer valueInt;
    @XmlElement(required = true, type = Long.class, nillable = true)
    protected Long valueLong;
    @XmlElement(required = true, nillable = true)
    protected String valueStr;

    /**
     * Gets the value of the fieldName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the value of the fieldName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFieldName(String value) {
        this.fieldName = value;
    }

    /**
     * Gets the value of the valueBool property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isValueBool() {
        return valueBool;
    }

    /**
     * Sets the value of the valueBool property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setValueBool(Boolean value) {
        this.valueBool = value;
    }

    /**
     * Gets the value of the valueDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getValueDate() {
        return valueDate;
    }

    /**
     * Sets the value of the valueDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setValueDate(XMLGregorianCalendar value) {
        this.valueDate = value;
    }

    /**
     * Gets the value of the valueDbl property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getValueDbl() {
        return valueDbl;
    }

    /**
     * Sets the value of the valueDbl property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setValueDbl(Double value) {
        this.valueDbl = value;
    }

    /**
     * Gets the value of the valueInt property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getValueInt() {
        return valueInt;
    }

    /**
     * Sets the value of the valueInt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setValueInt(Integer value) {
        this.valueInt = value;
    }

    /**
     * Gets the value of the valueLong property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getValueLong() {
        return valueLong;
    }

    /**
     * Sets the value of the valueLong property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setValueLong(Long value) {
        this.valueLong = value;
    }

    /**
     * Gets the value of the valueStr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValueStr() {
        return valueStr;
    }

    /**
     * Sets the value of the valueStr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueStr(String value) {
        this.valueStr = value;
    }

}
