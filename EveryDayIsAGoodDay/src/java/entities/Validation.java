/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
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
    @NamedQuery(name = "Validation.findByIdValidation", query = "SELECT v FROM Validation v WHERE v.idValidation = :idValidation"),
    @NamedQuery(name = "Validation.findByDay", query = "SELECT v FROM Validation v WHERE v.day = :day")})
public class Validation implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idValidation")
    private Integer idValidation;
    @Basic(optional = false)
    @NotNull
    @Column(name = "day")
    @Temporal(TemporalType.DATE)
    private Date day;
    @JoinColumn(name = "Resolution_idResolution", referencedColumnName = "idResolution")
    @ManyToOne(optional = false)
    private Resolution resolutionidResolution;

    public Validation() {
    }

    public Validation(Integer idValidation) {
        this.idValidation = idValidation;
    }

    public Validation(Integer idValidation, Date day) {
        this.idValidation = idValidation;
        this.day = day;
    }

    public Integer getIdValidation() {
        return idValidation;
    }

    public void setIdValidation(Integer idValidation) {
        this.idValidation = idValidation;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public Resolution getResolutionidResolution() {
        return resolutionidResolution;
    }

    public void setResolutionidResolution(Resolution resolutionidResolution) {
        this.resolutionidResolution = resolutionidResolution;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idValidation != null ? idValidation.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Validation)) {
            return false;
        }
        Validation other = (Validation) object;
        if ((this.idValidation == null && other.idValidation != null) || (this.idValidation != null && !this.idValidation.equals(other.idValidation))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Validation[ idValidation=" + idValidation + " ]";
    }
    
}
