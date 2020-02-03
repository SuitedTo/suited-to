
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for InterviewBean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InterviewBean">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:TBEWebAPI}EntityBean">
 *       &lt;sequence>
 *         &lt;element name="candidateId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="comments" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="creator" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="interviewRoom" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="interviewType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="participants" type="{urn:TBEWebAPI}ArrayOfParticipantBean"/>
 *         &lt;element name="requisitionId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="startDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InterviewBean", propOrder = {
    "candidateId",
    "comments",
    "creator",
    "interviewRoom",
    "interviewType",
    "participants",
    "requisitionId",
    "startDate"
})
public class InterviewBean
    extends EntityBean
{

    protected long candidateId;
    @XmlElement(required = true, nillable = true)
    protected String comments;
    @XmlElement(required = true, nillable = true)
    protected String creator;
    @XmlElement(required = true, nillable = true)
    protected String interviewRoom;
    @XmlElement(required = true, nillable = true)
    protected String interviewType;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfParticipantBean participants;
    protected long requisitionId;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startDate;

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
     * Gets the value of the interviewRoom property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInterviewRoom() {
        return interviewRoom;
    }

    /**
     * Sets the value of the interviewRoom property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInterviewRoom(String value) {
        this.interviewRoom = value;
    }

    /**
     * Gets the value of the interviewType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInterviewType() {
        return interviewType;
    }

    /**
     * Sets the value of the interviewType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInterviewType(String value) {
        this.interviewType = value;
    }

    /**
     * Gets the value of the participants property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfParticipantBean }
     *     
     */
    public ArrayOfParticipantBean getParticipants() {
        return participants;
    }

    /**
     * Sets the value of the participants property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfParticipantBean }
     *     
     */
    public void setParticipants(ArrayOfParticipantBean value) {
        this.participants = value;
    }

    /**
     * Gets the value of the requisitionId property.
     * 
     */
    public long getRequisitionId() {
        return requisitionId;
    }

    /**
     * Sets the value of the requisitionId property.
     * 
     */
    public void setRequisitionId(long value) {
        this.requisitionId = value;
    }

    /**
     * Gets the value of the startDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartDate() {
        return startDate;
    }

    /**
     * Sets the value of the startDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartDate(XMLGregorianCalendar value) {
        this.startDate = value;
    }

}
