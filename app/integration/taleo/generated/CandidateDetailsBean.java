
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CandidateDetailsBean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CandidateDetailsBean">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="offerBeans" type="{urn:TBEWebAPI}ArrayOfOfferBean"/>
 *         &lt;element name="historyBeans" type="{urn:TBEWebAPI}ArrayOfHistoryBean"/>
 *         &lt;element name="interviewBeans" type="{urn:TBEWebAPI}ArrayOfInterviewBean"/>
 *         &lt;element name="referenceBeans" type="{urn:TBEWebAPI}ArrayOfReferenceBean"/>
 *         &lt;element name="attachmentBeans" type="{urn:TBEWebAPI}ArrayOfAttachmentBean"/>
 *         &lt;element name="requisitionBeans" type="{urn:TBEWebAPI}ArrayOfRequisitionBean"/>
 *         &lt;element name="candidateBean" type="{urn:TBEWebAPI}CandidateBean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CandidateDetailsBean", propOrder = {
    "offerBeans",
    "historyBeans",
    "interviewBeans",
    "referenceBeans",
    "attachmentBeans",
    "requisitionBeans",
    "candidateBean"
})
public class CandidateDetailsBean {

    @XmlElement(required = true, nillable = true)
    protected ArrayOfOfferBean offerBeans;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfHistoryBean historyBeans;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfInterviewBean interviewBeans;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfReferenceBean referenceBeans;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfAttachmentBean attachmentBeans;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfRequisitionBean requisitionBeans;
    @XmlElement(required = true, nillable = true)
    protected CandidateBean candidateBean;

    /**
     * Gets the value of the offerBeans property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfOfferBean }
     *     
     */
    public ArrayOfOfferBean getOfferBeans() {
        return offerBeans;
    }

    /**
     * Sets the value of the offerBeans property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfOfferBean }
     *     
     */
    public void setOfferBeans(ArrayOfOfferBean value) {
        this.offerBeans = value;
    }

    /**
     * Gets the value of the historyBeans property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfHistoryBean }
     *     
     */
    public ArrayOfHistoryBean getHistoryBeans() {
        return historyBeans;
    }

    /**
     * Sets the value of the historyBeans property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfHistoryBean }
     *     
     */
    public void setHistoryBeans(ArrayOfHistoryBean value) {
        this.historyBeans = value;
    }

    /**
     * Gets the value of the interviewBeans property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfInterviewBean }
     *     
     */
    public ArrayOfInterviewBean getInterviewBeans() {
        return interviewBeans;
    }

    /**
     * Sets the value of the interviewBeans property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfInterviewBean }
     *     
     */
    public void setInterviewBeans(ArrayOfInterviewBean value) {
        this.interviewBeans = value;
    }

    /**
     * Gets the value of the referenceBeans property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfReferenceBean }
     *     
     */
    public ArrayOfReferenceBean getReferenceBeans() {
        return referenceBeans;
    }

    /**
     * Sets the value of the referenceBeans property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfReferenceBean }
     *     
     */
    public void setReferenceBeans(ArrayOfReferenceBean value) {
        this.referenceBeans = value;
    }

    /**
     * Gets the value of the attachmentBeans property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfAttachmentBean }
     *     
     */
    public ArrayOfAttachmentBean getAttachmentBeans() {
        return attachmentBeans;
    }

    /**
     * Sets the value of the attachmentBeans property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfAttachmentBean }
     *     
     */
    public void setAttachmentBeans(ArrayOfAttachmentBean value) {
        this.attachmentBeans = value;
    }

    /**
     * Gets the value of the requisitionBeans property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfRequisitionBean }
     *     
     */
    public ArrayOfRequisitionBean getRequisitionBeans() {
        return requisitionBeans;
    }

    /**
     * Sets the value of the requisitionBeans property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfRequisitionBean }
     *     
     */
    public void setRequisitionBeans(ArrayOfRequisitionBean value) {
        this.requisitionBeans = value;
    }

    /**
     * Gets the value of the candidateBean property.
     * 
     * @return
     *     possible object is
     *     {@link CandidateBean }
     *     
     */
    public CandidateBean getCandidateBean() {
        return candidateBean;
    }

    /**
     * Sets the value of the candidateBean property.
     * 
     * @param value
     *     allowed object is
     *     {@link CandidateBean }
     *     
     */
    public void setCandidateBean(CandidateBean value) {
        this.candidateBean = value;
    }

}
