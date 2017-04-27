/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.time.Instant;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author julien.baumgart
 */
@Entity
@Table(name = "resolution")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Resolution.findAll", query = "SELECT r FROM Resolution r"),
    @NamedQuery(name = "Resolution.findByIdResolution", query = "SELECT r FROM Resolution r WHERE r.idResolution = :idResolution"),
    @NamedQuery(name = "Resolution.findByDescription", query = "SELECT r FROM Resolution r WHERE r.description = :description"),
    @NamedQuery(name = "Resolution.findByYear", query = "SELECT r FROM Resolution r WHERE r.year = :year")})
public class Resolution implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idResolution")
    private Integer idResolution;
    @Size(max = 45)
    @Column(name = "Description")
    private String description;
    @Column(name = "Year")
    private Integer year;
    @JoinColumn(name = "Tag_idTag", referencedColumnName = "idTag")
    @ManyToOne(optional = false)
    private Tag tagidTag;
    @JoinColumn(name = "User_idUser", referencedColumnName = "idUser")
    @ManyToOne(optional = false)
    private User useridUser;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "resolutionidResolution")
    private Collection<Validation> validationCollection;
    public Resolution() {
    }

    public Resolution(Integer idResolution) {
        this.idResolution = idResolution;
    }

    public Integer getIdResolution() {
        return idResolution;
    }

    public void setIdResolution(Integer idResolution) {
        this.idResolution = idResolution;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Tag getTagidTag() {
        return tagidTag;
    }

    public void setTagidTag(Tag tagidTag) {
        this.tagidTag = tagidTag;
    }

    public User getUseridUser() {
        return useridUser;
    }

    public void setUseridUser(User useridUser) {
        this.useridUser = useridUser;
    }

    @XmlTransient
    public Collection<Validation> getValidationCollection() {
        return validationCollection;
    }

    public void setValidationCollection(Collection<Validation> validationCollection) {
        this.validationCollection = validationCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idResolution != null ? idResolution.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Resolution)) {
            return false;
        }
        Resolution other = (Resolution) object;
        if ((this.idResolution == null && other.idResolution != null) || (this.idResolution != null && !this.idResolution.equals(other.idResolution))) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "entities.Resolution[ idResolution=" + idResolution + " ]";
    }
    
    public boolean isResolutionValid(){
        
        Calendar cal = Calendar.getInstance();
        cal.set(year, 0, 1, 0, 0, 0);
        while(cal.before(Calendar.getInstance())){
            if(!isValidAt(cal.getTime())){
                return false;
            }
            cal.add(Calendar.DATE, 1);
        }
        return true;
    }
    
    private boolean isValidAt(Date date){
        for(Validation val : validationCollection){
            if(val.getDay().getYear() == date.getYear() &&
               val.getDay().getMonth()== date.getMonth() &&
               val.getDay().getDay()== date.getDay())
                return true;
        }
        return false;
    }
}
