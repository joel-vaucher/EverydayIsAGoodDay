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
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 *
 * @author julien.baumgart
 */
@Embeddable
public class ValidationPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "Day")
    @Temporal(TemporalType.DATE)
    private Date day;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Resolution_idResolution")
    private int resolutionidResolution;

    public ValidationPK() {
    }

    public ValidationPK(Date day, int resolutionidResolution) {
        this.day = day;
        this.resolutionidResolution = resolutionidResolution;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public int getResolutionidResolution() {
        return resolutionidResolution;
    }

    public void setResolutionidResolution(int resolutionidResolution) {
        this.resolutionidResolution = resolutionidResolution;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (day != null ? day.hashCode() : 0);
        hash += (int) resolutionidResolution;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ValidationPK)) {
            return false;
        }
        ValidationPK other = (ValidationPK) object;
        if ((this.day == null && other.day != null) || (this.day != null && !this.day.equals(other.day))) {
            return false;
        }
        if (this.resolutionidResolution != other.resolutionidResolution) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.ValidationPK[ day=" + day + ", resolutionidResolution=" + resolutionidResolution + " ]";
    }
    
}
