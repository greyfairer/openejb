/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee.bval;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for classType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="classType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="group-sequence" type="{http://jboss.org/xml/ns/javax/validation/mapping}groupSequenceType" minOccurs="0"/>
 *         &lt;element name="constraint" type="{http://jboss.org/xml/ns/javax/validation/mapping}constraintType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ignore-annotations" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "classType", propOrder = {
    "groupSequence",
    "constraint"
})
public class ClassType {

    @XmlElement(name = "group-sequence")
    protected GroupSequenceType groupSequence;
    protected List<ConstraintType> constraint;
    @XmlAttribute(name = "ignore-annotations")
    protected Boolean ignoreAnnotations;

    /**
     * Gets the value of the groupSequence property.
     *
     * @return
     *     possible object is
     *     {@link GroupSequenceType }
     *
     */
    public GroupSequenceType getGroupSequence() {
        return groupSequence;
    }

    /**
     * Sets the value of the groupSequence property.
     *
     * @param value
     *     allowed object is
     *     {@link GroupSequenceType }
     *
     */
    public void setGroupSequence(GroupSequenceType value) {
        this.groupSequence = value;
    }

    /**
     * Gets the value of the constraint property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the constraint property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConstraint().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConstraintType }
     *
     *
     */
    public List<ConstraintType> getConstraint() {
        if (constraint == null) {
            constraint = new ArrayList<ConstraintType>();
        }
        return this.constraint;
    }

    /**
     * Gets the value of the ignoreAnnotations property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isIgnoreAnnotations() {
        return ignoreAnnotations;
    }

    /**
     * Sets the value of the ignoreAnnotations property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setIgnoreAnnotations(Boolean value) {
        this.ignoreAnnotations = value;
    }

}
