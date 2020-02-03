
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BackgroundCheckBean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BackgroundCheckBean">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:TBEWebAPI}EntityBean">
 *       &lt;sequence>
 *         &lt;element name="candidateId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="checkerEmail" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="checkerName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="checkerPhone" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="comments" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="creator" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BackgroundCheckBean", propOrder = {
    "candidateId",
    "checkerEmail",
    "checkerName",
    "checkerPhone",
    "comments",
    "creator"
})
public class BackgroundCheckBean
    extends EntityBean
{

    protected long candidateId;
    @XmlElement(required = true, nillable = true)
    protected String checkerEmail;
    @XmlElement(required = true, nillable = true)
    protected String checkerName;
    @XmlElement(required = true, nillable = true)
    protected String checkerPhone;
    @XmlElement(required = true, nillable = true)
    protected String comments;
    @XmlElement(required = true, nillable = true)
    protected String creator;

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
     * Gets the value of the checkerEmail property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCheckerEmail() {
        return checkerEmail;
    }

    /**
     * Sets the value of the checkerEmail property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCheckerEmail(String value) {
        this.checkerEmail = value;
    }

    /**
     * Gets the value of the checkerName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCheckerName() {
        return checkerName;
    }

    /**
     * Sets the value of the checkerName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCheckerName(String value) {
        this.checkerName = value;
    }

    /**
     * Gets the value of the checkerPhone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCheckerPhone() {
        return checkerPhone;
    }

    /**
     * Sets the value of the checkerPhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCheckerPhone(String value) {
        this.checkerPhone = value;
    }

    /**
     * Gets the value of the comments property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComments() {
        return comments;
    }

    /**
     * Sets the value of the comments property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComments(String value) {
        this.comments = value;
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

}
