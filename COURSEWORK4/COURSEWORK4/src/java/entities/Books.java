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
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author feder
 */

//selecting the table from which the following class functions will retrieve information from the database 
@Entity
@Table(name = "BOOKS")
@XmlRootElement
//defining the functions used to retrieve information from the database
@NamedQueries({
    @NamedQuery(name = "Books.findAll", query = "SELECT b FROM Books b"),
    @NamedQuery(name = "Books.findByBookname", query = "SELECT b FROM Books b WHERE b.bookname = :bookname"),
    @NamedQuery(name = "Books.findByBookdescription", query = "SELECT b FROM Books b WHERE b.bookdescription = :bookdescription"),
    @NamedQuery(name = "Books.findByBookurl", query = "SELECT b FROM Books b WHERE b.bookurl = :bookurl")})
public class Books implements Serializable {

    //the attributes of the books class
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    //selecting the book name from the database
    @Column(name = "BOOKNAME")
    private String bookname;
    @Size(max = 10000)
    //selecting the book description  from the database
    @Column(name = "BOOKDESCRIPTION")
    private String bookdescription;
    @Size(max = 255)
    //selecting the book url from the database
    @Column(name = "BOOKURL")
    private String bookurl;
    //setting the type of relationship with the greek gods table which will be mapped by the book name attribute
    @OneToMany(mappedBy = "bookname")
    private Collection<Greekgods> greekgodsCollection = new ArrayList<>();
    

    public Books() {
    }

    //getters and setters
    public Books(String bookname) {
        this.bookname = bookname;
    }

    public String getBookname() {
        return bookname;
    }

    public void setBookname(String bookname) {
        this.bookname = bookname;
    }

    public String getBookdescription() {
        return bookdescription;
    }

    public void setBookdescription(String bookdescription) {
        this.bookdescription = bookdescription;
    }

    public String getBookurl() {
        return bookurl;
    }

    public void setBookurl(String bookurl) {
        this.bookurl = bookurl;
    }
    
    @XmlTransient
    public Collection<Greekgods> getGreekgodsCollection() {
        return greekgodsCollection;
    }

     public void setGreekgodsCollection(Collection<Greekgods> greekgodsCollection) {
    this.greekgodsCollection = (greekgodsCollection != null) ? greekgodsCollection : new ArrayList<>();
}

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (bookname != null ? bookname.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Books)) {
            return false;
        }
        Books other = (Books) object;
        if ((this.bookname == null && other.bookname != null) || (this.bookname != null && !this.bookname.equals(other.bookname))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Books[ bookname=" + bookname + " ]";
    }
    
}
