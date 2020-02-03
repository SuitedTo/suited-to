
package integration.taleo.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for EmployeeBean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EmployeeBean">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:TBEWebAPI}AddressEntityBean">
 *       &lt;sequence>
 *         &lt;element name="employeeNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="email" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="firstName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="middleInitial" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="lastName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cellPhone" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="hiredDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="startDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="race" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="gender" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="lockedFromEws" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="reviewManagerId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="departmentId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="hierarchyPath" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="managerId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="locationId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="ewsLogin" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ewsPassword" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="jobTitle" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="jobCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="salaryGrade" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="salary" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="payFrequency" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="changePswdOnEwsLogin" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="additionalEntities" type="{http://xml.apache.org/xml-soap}Map"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EmployeeBean", propOrder = {
    "employeeNumber",
    "email",
    "firstName",
    "middleInitial",
    "lastName",
    "cellPhone",
    "title",
    "hiredDate",
    "startDate",
    "race",
    "gender",
    "lockedFromEws",
    "reviewManagerId",
    "departmentId",
    "hierarchyPath",
    "managerId",
    "locationId",
    "ewsLogin",
    "ewsPassword",
    "jobTitle",
    "jobCode",
    "salaryGrade",
    "salary",
    "payFrequency",
    "changePswdOnEwsLogin",
    "additionalEntities"
})
public class EmployeeBean
    extends AddressEntityBean
{

    @XmlElement(required = true, nillable = true)
    protected String employeeNumber;
    @XmlElement(required = true, nillable = true)
    protected String email;
    @XmlElement(required = true, nillable = true)
    protected String firstName;
    @XmlElement(required = true, nillable = true)
    protected String middleInitial;
    @XmlElement(required = true, nillable = true)
    protected String lastName;
    @XmlElement(required = true, nillable = true)
    protected String cellPhone;
    @XmlElement(required = true, nillable = true)
    protected String title;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar hiredDate;
    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startDate;
    @XmlElement(required = true, nillable = true)
    protected String race;
    @XmlElement(required = true, nillable = true)
    protected String gender;
    protected boolean lockedFromEws;
    protected long reviewManagerId;
    protected long departmentId;
    @XmlElement(required = true, nillable = true)
    protected String hierarchyPath;
    protected long managerId;
    protected long locationId;
    @XmlElement(required = true, nillable = true)
    protected String ewsLogin;
    @XmlElement(required = true, nillable = true)
    protected String ewsPassword;
    @XmlElement(required = true, nillable = true)
    protected String jobTitle;
    @XmlElement(required = true, nillable = true)
    protected String jobCode;
    @XmlElement(required = true, nillable = true)
    protected String salaryGrade;
    protected double salary;
    @XmlElement(required = true, nillable = true)
    protected String payFrequency;
    protected boolean changePswdOnEwsLogin;
    @XmlElement(required = true, nillable = true)
    protected Map additionalEntities;

    /**
     * Gets the value of the employeeNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    /**
     * Sets the value of the employeeNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmployeeNumber(String value) {
        this.employeeNumber = value;
    }

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmail(String value) {
        this.email = value;
    }

    /**
     * Gets the value of the firstName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the value of the firstName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFirstName(String value) {
        this.firstName = value;
    }

    /**
     * Gets the value of the middleInitial property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMiddleInitial() {
        return middleInitial;
    }

    /**
     * Sets the value of the middleInitial property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMiddleInitial(String value) {
        this.middleInitial = value;
    }

    /**
     * Gets the value of the lastName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the value of the lastName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastName(String value) {
        this.lastName = value;
    }

    /**
     * Gets the value of the cellPhone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCellPhone() {
        return cellPhone;
    }

    /**
     * Sets the value of the cellPhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCellPhone(String value) {
        this.cellPhone = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the hiredDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getHiredDate() {
        return hiredDate;
    }

    /**
     * Sets the value of the hiredDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setHiredDate(XMLGregorianCalendar value) {
        this.hiredDate = value;
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

    /**
     * Gets the value of the race property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRace() {
        return race;
    }

    /**
     * Sets the value of the race property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRace(String value) {
        this.race = value;
    }

    /**
     * Gets the value of the gender property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGender() {
        return gender;
    }

    /**
     * Sets the value of the gender property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGender(String value) {
        this.gender = value;
    }

    /**
     * Gets the value of the lockedFromEws property.
     * 
     */
    public boolean isLockedFromEws() {
        return lockedFromEws;
    }

    /**
     * Sets the value of the lockedFromEws property.
     * 
     */
    public void setLockedFromEws(boolean value) {
        this.lockedFromEws = value;
    }

    /**
     * Gets the value of the reviewManagerId property.
     * 
     */
    public long getReviewManagerId() {
        return reviewManagerId;
    }

    /**
     * Sets the value of the reviewManagerId property.
     * 
     */
    public void setReviewManagerId(long value) {
        this.reviewManagerId = value;
    }

    /**
     * Gets the value of the departmentId property.
     * 
     */
    public long getDepartmentId() {
        return departmentId;
    }

    /**
     * Sets the value of the departmentId property.
     * 
     */
    public void setDepartmentId(long value) {
        this.departmentId = value;
    }

    /**
     * Gets the value of the hierarchyPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHierarchyPath() {
        return hierarchyPath;
    }

    /**
     * Sets the value of the hierarchyPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHierarchyPath(String value) {
        this.hierarchyPath = value;
    }

    /**
     * Gets the value of the managerId property.
     * 
     */
    public long getManagerId() {
        return managerId;
    }

    /**
     * Sets the value of the managerId property.
     * 
     */
    public void setManagerId(long value) {
        this.managerId = value;
    }

    /**
     * Gets the value of the locationId property.
     * 
     */
    public long getLocationId() {
        return locationId;
    }

    /**
     * Sets the value of the locationId property.
     * 
     */
    public void setLocationId(long value) {
        this.locationId = value;
    }

    /**
     * Gets the value of the ewsLogin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEwsLogin() {
        return ewsLogin;
    }

    /**
     * Sets the value of the ewsLogin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEwsLogin(String value) {
        this.ewsLogin = value;
    }

    /**
     * Gets the value of the ewsPassword property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEwsPassword() {
        return ewsPassword;
    }

    /**
     * Sets the value of the ewsPassword property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEwsPassword(String value) {
        this.ewsPassword = value;
    }

    /**
     * Gets the value of the jobTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJobTitle() {
        return jobTitle;
    }

    /**
     * Sets the value of the jobTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJobTitle(String value) {
        this.jobTitle = value;
    }

    /**
     * Gets the value of the jobCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJobCode() {
        return jobCode;
    }

    /**
     * Sets the value of the jobCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJobCode(String value) {
        this.jobCode = value;
    }

    /**
     * Gets the value of the salaryGrade property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSalaryGrade() {
        return salaryGrade;
    }

    /**
     * Sets the value of the salaryGrade property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSalaryGrade(String value) {
        this.salaryGrade = value;
    }

    /**
     * Gets the value of the salary property.
     * 
     */
    public double getSalary() {
        return salary;
    }

    /**
     * Sets the value of the salary property.
     * 
     */
    public void setSalary(double value) {
        this.salary = value;
    }

    /**
     * Gets the value of the payFrequency property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPayFrequency() {
        return payFrequency;
    }

    /**
     * Sets the value of the payFrequency property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPayFrequency(String value) {
        this.payFrequency = value;
    }

    /**
     * Gets the value of the changePswdOnEwsLogin property.
     * 
     */
    public boolean isChangePswdOnEwsLogin() {
        return changePswdOnEwsLogin;
    }

    /**
     * Sets the value of the changePswdOnEwsLogin property.
     * 
     */
    public void setChangePswdOnEwsLogin(boolean value) {
        this.changePswdOnEwsLogin = value;
    }

    /**
     * Gets the value of the additionalEntities property.
     * 
     * @return
     *     possible object is
     *     {@link Map }
     *     
     */
    public Map getAdditionalEntities() {
        return additionalEntities;
    }

    /**
     * Sets the value of the additionalEntities property.
     * 
     * @param value
     *     allowed object is
     *     {@link Map }
     *     
     */
    public void setAdditionalEntities(Map value) {
        this.additionalEntities = value;
    }

}
