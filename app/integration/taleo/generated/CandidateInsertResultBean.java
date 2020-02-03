
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CandidateInsertResultBean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CandidateInsertResultBean">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="candidateId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="dup" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CandidateInsertResultBean", propOrder = {
    "candidateId",
    "dup"
})
public class CandidateInsertResultBean {

    protected long candidateId;
    protected long dup;

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
     * Gets the value of the dup property.
     * 
     */
    public long getDup() {
        return dup;
    }

    /**
     * Sets the value of the dup property.
     * 
     */
    public void setDup(long value) {
        this.dup = value;
    }

}
