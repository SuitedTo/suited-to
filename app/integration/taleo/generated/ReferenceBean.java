
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReferenceBean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReferenceBean">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:TBEWebAPI}EntityBean">
 *       &lt;sequence>
 *         &lt;element name="candidateId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="comments" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="creator" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="employer" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="refEmail" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="refName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="refPhone" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="refTitle" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="relToCandidate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferenceBean", propOrder = {
    "candidateId",
    "comments",
    "creator",
    "employer",
    "refEmail",
    "refName",
    "refPhone",
    "refTitle",
    "relToCandidate"
})
public class ReferenceBean
    extends EntityBean
{

    protected long candidateId;
    @XmlElement(required = true, nillable = true)
    protected String comments;
    @XmlElement(required = true, nillable = true)
    protected String creator;
    @XmlElement(required = true, nillable = true)
    protected String employer;
    @XmlElement(required = true, nillable = true)
    protected String refEmail;
    @XmlElement(required = true, nillable = true)
    protected String refName;
    @XmlElement(required = true, nillable = true)
    protected String refPhone;
    @XmlElement(required = true, nillable = true)
    protected String refTitle;
    @XmlElement(required = true, nillable = true)
    protected String relToCandidate;

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

    /**
     * Gets the value of the employer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmployer() {
        return employer;
    }

    /**
     * Sets the value of the employer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmployer(String value) {
        this.employer = value;
    }

    /**
     * Gets the value of the refEmail property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefEmail() {
        return refEmail;
    }

    /**
     * Sets the value of the refEmail property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefEmail(String value) {
        this.refEmail = value;
    }

    /**
     * Gets the value of the refName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefName() {
        return refName;
    }

    /**
     * Sets the value of the refName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefName(String value) {
        this.refName = value;
    }

    /**
     * Gets the value of the refPhone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefPhone() {
        return refPhone;
    }

    /**
     * Sets the value of the refPhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefPhone(String value) {
        this.refPhone = value;
    }

    /**
     * Gets the value of the refTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefTitle() {
        return refTitle;
    }

    /**
     * Sets the value of the refTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefTitle(String value) {
        this.refTitle = value;
    }

    /**
     * Gets the value of the relToCandidate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelToCandidate() {
        return relToCandidate;
    }

    /**
     * Sets the value of the relToCandidate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelToCandidate(String value) {
        this.relToCandidate = value;
    }

}
