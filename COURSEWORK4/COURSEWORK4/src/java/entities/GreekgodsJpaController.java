/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

////imports used for the books controller
import entities.exceptions.NonexistentEntityException;
import entities.exceptions.PreexistingEntityException;
import entities.exceptions.RollbackFailureException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.io.Serializable;
import jakarta.persistence.Query;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author feder
 */
public class GreekgodsJpaController implements Serializable {

    public GreekgodsJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    //retrieving the entity manager
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    //Finding the god description given the name of the god
    public String findGodDescriptionByName(String godName) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("Greekgods.findByGodname");
            query.setParameter("godname", godName);
            List<Greekgods> gods = query.getResultList();
            if (gods.isEmpty()) {
                return "An invalid god name has been entered:  " + godName;
            } else {
                // Assuming the query returns a single result, get the first result
                Greekgods god = gods.get(0);
                return god.getGoddescription();
            }
        } finally {
            em.close();
        }
    }

    public Greekgods findGreekgodsByName(String name) {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Greekgods> cq = cb.createQuery(Greekgods.class);
            Root<Greekgods> root = cq.from(Greekgods.class);
            cq.where(cb.equal(root.get("godname"), name));
            TypedQuery<Greekgods> query = em.createQuery(cq);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
    //creating new gods
    public void create(Greekgods greekgods) throws PreexistingEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Books bookname = greekgods.getBookname();
            if (bookname != null) {
                bookname = em.getReference(bookname.getClass(), bookname.getBookname());
                greekgods.setBookname(bookname);
            }
            em.persist(greekgods);
            if (bookname != null) {
                bookname.getGreekgodsCollection().add(greekgods);
                bookname = em.merge(bookname);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findGreekgods(greekgods.getGodid()) != null) {
                throw new PreexistingEntityException("Greekgods " + greekgods + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    //editing the gods
    public void edit(Greekgods greekgods) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Greekgods persistentGreekgods = em.find(Greekgods.class, greekgods.getGodid());
            Books booknameOld = persistentGreekgods.getBookname();
            Books booknameNew = greekgods.getBookname();
            if (booknameNew != null) {
                booknameNew = em.getReference(booknameNew.getClass(), booknameNew.getBookname());
                greekgods.setBookname(booknameNew);
            }
            greekgods = em.merge(greekgods);
            if (booknameOld != null && !booknameOld.equals(booknameNew)) {
                booknameOld.getGreekgodsCollection().remove(greekgods);
                booknameOld = em.merge(booknameOld);
            }
            if (booknameNew != null && !booknameNew.equals(booknameOld)) {
                if (booknameNew.getGreekgodsCollection() == null) {
                    booknameNew.setGreekgodsCollection(new ArrayList<Greekgods>());
                }
                booknameNew.getGreekgodsCollection().add(greekgods);
                booknameNew = em.merge(booknameNew);
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
                Integer id = greekgods.getGodid();
                if (findGreekgods(id) == null) {
                    throw new NonexistentEntityException("The greekgods with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    //destroying the gods
    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Greekgods greekgods;
            try {
                greekgods = em.getReference(Greekgods.class, id);
                greekgods.getGodid();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The greekgods with id " + id + " no longer exists.", enfe);
            }
            Books bookname = greekgods.getBookname();
            if (bookname != null) {
                bookname.getGreekgodsCollection().remove(greekgods);
                bookname = em.merge(bookname);
            }
            em.remove(greekgods);
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

    public List<Greekgods> findGreekgodsEntities() {
        return findGreekgodsEntities(true, -1, -1);
    }

    public List<Greekgods> findGreekgodsEntities(int maxResults, int firstResult) {
        return findGreekgodsEntities(false, maxResults, firstResult);
    }

    private List<Greekgods> findGreekgodsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Greekgods.class));
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

    public Greekgods findGreekgods(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Greekgods.class, id);
        } finally {
            em.close();
        }
    }

    public int getGreekgodsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Greekgods> rt = cq.from(Greekgods.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
