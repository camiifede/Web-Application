/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

//imports used to for the book class
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 *
 * @author feder
 */
//selecting the table from which the following class functions will retrieve information from the database 
@Entity
@Table(name = "GREEKGODS")
@XmlRootElement
//defining the functions used to retrieve information from the database
@NamedQueries({
    @NamedQuery(name = "Greekgods.findAll", query = "SELECT g FROM Greekgods g"),
    @NamedQuery(name = "Greekgods.findByGodid", query = "SELECT g FROM Greekgods g WHERE g.godid = :godid"),
    @NamedQuery(name = "Greekgods.findByGodname", query = "SELECT g FROM Greekgods g WHERE g.godname = :godname"),
    @NamedQuery(name = "Greekgods.findByGoddescription", query = "SELECT g FROM Greekgods g WHERE g.goddescription = :goddescription")})
public class Greekgods implements Serializable {
    
    //the attributes of the greek gods class
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    //selecting the god id from the database
    @Column(name = "GODID")
    private Integer godid;
    @Size(max = 255)
    //selecting the gods name from the database
    @Column(name = "GODNAME")
    private String godname;
    @Size(max = 10000)
    //selecting the gods description from the database
    @Column(name = "GODDESCRIPTION")
    private String goddescription;
    //selecting the image associated to the god from the database
    @Column(name = "IMAGEURL")
    private String imageurl;
    //selecting the book associated to the god from the database
    @JoinColumn(name = "BOOKNAME", referencedColumnName = "BOOKNAME")
    @ManyToOne
    private Books bookname;
    
    
    public Greekgods() {
    }
    
    //getters and setters
    public Greekgods(Integer godid) {
        this.godid = godid;
    }

    public Integer getGodid() {
        return godid;
    }

    public void setGodid(Integer godid) {
        this.godid = godid;
    }

    public String getGodname() {
        return godname;
    }

    public void setGodname(String godname) {
        this.godname = godname;
    }

    public String getGoddescription() {
        return goddescription;
    }

    public void setGoddescription(String goddescription) {
        this.goddescription = goddescription;
    }


    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (godid != null ? godid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Greekgods)) {
            return false;
        }
        Greekgods other = (Greekgods) object;
        if ((this.godid == null && other.godid != null) || (this.godid != null && !this.godid.equals(other.godid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Greekgods[ godid=" + godid + " ]";
    }

    public Books getBookname() {
        return bookname;
    }

    public void setBookname(Books bookname) {
        this.bookname = bookname;
    }
}

