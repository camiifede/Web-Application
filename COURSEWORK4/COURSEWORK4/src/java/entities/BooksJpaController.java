/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

//all the import used for the JPA controller
import entities.exceptions.NonexistentEntityException;
import entities.exceptions.PreexistingEntityException;
import entities.exceptions.RollbackFailureException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.io.Serializable;
import jakarta.persistence.Query;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author feder
 */
public class BooksJpaController implements Serializable {

    public BooksJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    //retrieving the entity manager
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    
    //Finding the book description given the name of the book
     public String findBookDescriptionByName(String bookName) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("Books.findByBookname");
            query.setParameter("bookname", bookName);
            List<Books> books = query.getResultList();
            if (books.isEmpty()) {
                return "Description not found for book: " + bookName;
            } else {
                Books book = books.get(0);
                return book.getBookdescription();  // Assuming getDescription() method exists
            }
        } finally {
            em.close();
        }
    }

    //finding the book by selecting the name
    public Books findBooksByName(String name) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT b FROM Books b WHERE b.bookname = :name", Books.class)
                     .setParameter("name", name)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
    //creating new books
    public void create(Books books) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (books.getGreekgodsCollection() == null) {
            books.setGreekgodsCollection(new ArrayList<Greekgods>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Greekgods> attachedGreekgodsCollection = new ArrayList<Greekgods>();
            for (Greekgods greekgodsCollectionGreekgodsToAttach : books.getGreekgodsCollection()) {
                greekgodsCollectionGreekgodsToAttach = em.getReference(greekgodsCollectionGreekgodsToAttach.getClass(), greekgodsCollectionGreekgodsToAttach.getGodid());
                attachedGreekgodsCollection.add(greekgodsCollectionGreekgodsToAttach);
            }
            books.setGreekgodsCollection(attachedGreekgodsCollection);
            em.persist(books);
            for (Greekgods greekgodsCollectionGreekgods : books.getGreekgodsCollection()) {
                Books oldBooknameOfGreekgodsCollectionGreekgods = greekgodsCollectionGreekgods.getBookname();
                greekgodsCollectionGreekgods.setBookname(books);
                greekgodsCollectionGreekgods = em.merge(greekgodsCollectionGreekgods);
                if (oldBooknameOfGreekgodsCollectionGreekgods != null) {
                    oldBooknameOfGreekgodsCollectionGreekgods.getGreekgodsCollection().remove(greekgodsCollectionGreekgods);
                    oldBooknameOfGreekgodsCollectionGreekgods = em.merge(oldBooknameOfGreekgodsCollectionGreekgods);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findBooks(books.getBookname()) != null) {
                throw new PreexistingEntityException("Books " + books + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    //editing the books
    public void edit(Books books) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Books persistentBooks = em.find(Books.class, books.getBookname());
            Collection<Greekgods> greekgodsCollectionOld = persistentBooks.getGreekgodsCollection();
            Collection<Greekgods> greekgodsCollectionNew = books.getGreekgodsCollection();
            Collection<Greekgods> attachedGreekgodsCollectionNew = new ArrayList<Greekgods>();
            for (Greekgods greekgodsCollectionNewGreekgodsToAttach : greekgodsCollectionNew) {
                greekgodsCollectionNewGreekgodsToAttach = em.getReference(greekgodsCollectionNewGreekgodsToAttach.getClass(), greekgodsCollectionNewGreekgodsToAttach.getGodid());
                attachedGreekgodsCollectionNew.add(greekgodsCollectionNewGreekgodsToAttach);
            }
            greekgodsCollectionNew = attachedGreekgodsCollectionNew;
            books.setGreekgodsCollection(greekgodsCollectionNew);
            books = em.merge(books);
            for (Greekgods greekgodsCollectionOldGreekgods : greekgodsCollectionOld) {
                if (!greekgodsCollectionNew.contains(greekgodsCollectionOldGreekgods)) {
                    greekgodsCollectionOldGreekgods.setBookname(null);
                    greekgodsCollectionOldGreekgods = em.merge(greekgodsCollectionOldGreekgods);
                }
            }
            for (Greekgods greekgodsCollectionNewGreekgods : greekgodsCollectionNew) {
                if (!greekgodsCollectionOld.contains(greekgodsCollectionNewGreekgods)) {
                    Books oldBooknameOfGreekgodsCollectionNewGreekgods = greekgodsCollectionNewGreekgods.getBookname();
                    greekgodsCollectionNewGreekgods.setBookname(books);
                    greekgodsCollectionNewGreekgods = em.merge(greekgodsCollectionNewGreekgods);
                    if (oldBooknameOfGreekgodsCollectionNewGreekgods != null && !oldBooknameOfGreekgodsCollectionNewGreekgods.equals(books)) {
                        oldBooknameOfGreekgodsCollectionNewGreekgods.getGreekgodsCollection().remove(greekgodsCollectionNewGreekgods);
                        oldBooknameOfGreekgodsCollectionNewGreekgods = em.merge(oldBooknameOfGreekgodsCollectionNewGreekgods);
                    }
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = books.getBookname();
                if (findBooks(id) == null) {
                    throw new NonexistentEntityException("The books with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    //destroying the books
    public void destroy(String id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Books books;
            try {
                books = em.getReference(Books.class, id);
                books.getBookname();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The books with id " + id + " no longer exists.", enfe);
            }
            Collection<Greekgods> greekgodsCollection = books.getGreekgodsCollection();
            for (Greekgods greekgodsCollectionGreekgods : greekgodsCollection) {
                greekgodsCollectionGreekgods.setBookname(null);
                greekgodsCollectionGreekgods = em.merge(greekgodsCollectionGreekgods);
            }
            em.remove(books);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Books> findBooksEntities() {
        return findBooksEntities(true, -1, -1);
    }

    public List<Books> findBooksEntities(int maxResults, int firstResult) {
        return findBooksEntities(false, maxResults, firstResult);
    }

    private List<Books> findBooksEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Books.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Books findBooks(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Books.class, id);
        } finally {
            em.close();
        }
    }

    public int getBooksCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Books> rt = cq.from(Books.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
