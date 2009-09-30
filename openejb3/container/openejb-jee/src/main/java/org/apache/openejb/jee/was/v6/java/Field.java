
package org.apache.openejb.jee.was.v6.java;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.openejb.jee.was.v6.ecore.ETypedElement;


/**
 * <p>Java class for Field complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Field">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}ETypedElement">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="initializer" type="{java.xmi}Block"/>
 *       &lt;/choice>
 *       &lt;attribute name="final" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="javaVisibility" type="{java.xmi}JavaVisibilityKind" />
 *       &lt;attribute name="static" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="transient" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="volatile" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Field", propOrder = {
    "initializers"
})
public class Field
    extends ETypedElement
{

    @XmlElement(name = "initializer")
    protected List<Block> initializers;
    @XmlAttribute(name = "final")
    protected Boolean isFinal;
    @XmlAttribute
    protected JavaVisibilityKind javaVisibility;
    @XmlAttribute(name = "static")
    protected Boolean isStatic;
    @XmlAttribute(name = "transient")
    protected Boolean isTransient;
    @XmlAttribute(name = "volatile")
    protected Boolean isVolatile;

    /**
     * Gets the value of the initializers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the initializers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInitializers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Block }
     * 
     * 
     */
    public List<Block> getInitializers() {
        if (initializers == null) {
            initializers = new ArrayList<Block>();
        }
        return this.initializers;
    }

    /**
     * Gets the value of the isFinal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsFinal() {
        return isFinal;
    }

    /**
     * Sets the value of the isFinal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsFinal(Boolean value) {
        this.isFinal = value;
    }

    /**
     * Gets the value of the javaVisibility property.
     * 
     * @return
     *     possible object is
     *     {@link JavaVisibilityKind }
     *     
     */
    public JavaVisibilityKind getJavaVisibility() {
        return javaVisibility;
    }

    /**
     * Sets the value of the javaVisibility property.
     * 
     * @param value
     *     allowed object is
     *     {@link JavaVisibilityKind }
     *     
     */
    public void setJavaVisibility(JavaVisibilityKind value) {
        this.javaVisibility = value;
    }

    /**
     * Gets the value of the isStatic property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsStatic() {
        return isStatic;
    }

    /**
     * Sets the value of the isStatic property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsStatic(Boolean value) {
        this.isStatic = value;
    }

    /**
     * Gets the value of the isTransient property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsTransient() {
        return isTransient;
    }

    /**
     * Sets the value of the isTransient property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsTransient(Boolean value) {
        this.isTransient = value;
    }

    /**
     * Gets the value of the isVolatile property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsVolatile() {
        return isVolatile;
    }

    /**
     * Sets the value of the isVolatile property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsVolatile(Boolean value) {
        this.isVolatile = value;
    }

}
