/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author julien.baumgart
 */
@Entity
@Table(name = "validation")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Validation.findAll", query = "SELECT v FROM Validation v"),
    @NamedQuery(name = "Validation.findByDay", query = "SELECT v FROM Validation v WHERE v.validationPK.day = :day"),
    @NamedQuery(name = "Validation.findByResolutionidResolution", query = "SELECT v FROM Validation v WHERE v.validationPK.resolutionidResolution = :resolutionidResolution")})
public class Validation implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ValidationPK validationPK;
    @JoinColumn(name = "Resolution_idResolution", referencedColumnName = "idResolution", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Resolution resolution;

    public Validation() {
    }

    public Validation(ValidationPK validationPK) {
        this.validationPK = validationPK;
    }

    public Validation(Date day, int resolutionidResolution) {
        this.validationPK = new ValidationPK(day, resolutionidResolution);
    }

    public ValidationPK getValidationPK() {
        return validationPK;
    }

    public void setValidationPK(ValidationPK validationPK) {
        this.validationPK = validationPK;
    }

    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (validationPK != null ? validationPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Validation)) {
            return false;
        }
        Validation other = (Validation) object;
        if ((this.validationPK == null && other.validationPK != null) || (this.validationPK != null && !this.validationPK.equals(other.validationPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Validation[ validationPK=" + validationPK + " ]";
    }
    
}
