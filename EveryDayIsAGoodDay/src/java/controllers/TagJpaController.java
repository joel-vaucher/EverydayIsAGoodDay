/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.exceptions.IllegalOrphanException;
import controllers.exceptions.NonexistentEntityException;
import controllers.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import entities.Resolution;
import entities.Tag;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author julien.baumgart
 */
public class TagJpaController implements Serializable {

    public TagJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Tag tag) throws RollbackFailureException, Exception {
        if (tag.getResolutionCollection() == null) {
            tag.setResolutionCollection(new ArrayList<Resolution>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Resolution> attachedResolutionCollection = new ArrayList<Resolution>();
            for (Resolution resolutionCollectionResolutionToAttach : tag.getResolutionCollection()) {
                resolutionCollectionResolutionToAttach = em.getReference(resolutionCollectionResolutionToAttach.getClass(), resolutionCollectionResolutionToAttach.getIdResolution());
                attachedResolutionCollection.add(resolutionCollectionResolutionToAttach);
            }
            tag.setResolutionCollection(attachedResolutionCollection);
            em.persist(tag);
            for (Resolution resolutionCollectionResolution : tag.getResolutionCollection()) {
                Tag oldTagidTagOfResolutionCollectionResolution = resolutionCollectionResolution.getTagidTag();
                resolutionCollectionResolution.setTagidTag(tag);
                resolutionCollectionResolution = em.merge(resolutionCollectionResolution);
                if (oldTagidTagOfResolutionCollectionResolution != null) {
                    oldTagidTagOfResolutionCollectionResolution.getResolutionCollection().remove(resolutionCollectionResolution);
                    oldTagidTagOfResolutionCollectionResolution = em.merge(oldTagidTagOfResolutionCollectionResolution);
                }
            }
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

    public void edit(Tag tag) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Tag persistentTag = em.find(Tag.class, tag.getIdTag());
            Collection<Resolution> resolutionCollectionOld = persistentTag.getResolutionCollection();
            Collection<Resolution> resolutionCollectionNew = tag.getResolutionCollection();
            List<String> illegalOrphanMessages = null;
            for (Resolution resolutionCollectionOldResolution : resolutionCollectionOld) {
                if (!resolutionCollectionNew.contains(resolutionCollectionOldResolution)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Resolution " + resolutionCollectionOldResolution + " since its tagidTag field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Resolution> attachedResolutionCollectionNew = new ArrayList<Resolution>();
            for (Resolution resolutionCollectionNewResolutionToAttach : resolutionCollectionNew) {
                resolutionCollectionNewResolutionToAttach = em.getReference(resolutionCollectionNewResolutionToAttach.getClass(), resolutionCollectionNewResolutionToAttach.getIdResolution());
                attachedResolutionCollectionNew.add(resolutionCollectionNewResolutionToAttach);
            }
            resolutionCollectionNew = attachedResolutionCollectionNew;
            tag.setResolutionCollection(resolutionCollectionNew);
            tag = em.merge(tag);
            for (Resolution resolutionCollectionNewResolution : resolutionCollectionNew) {
                if (!resolutionCollectionOld.contains(resolutionCollectionNewResolution)) {
                    Tag oldTagidTagOfResolutionCollectionNewResolution = resolutionCollectionNewResolution.getTagidTag();
                    resolutionCollectionNewResolution.setTagidTag(tag);
                    resolutionCollectionNewResolution = em.merge(resolutionCollectionNewResolution);
                    if (oldTagidTagOfResolutionCollectionNewResolution != null && !oldTagidTagOfResolutionCollectionNewResolution.equals(tag)) {
                        oldTagidTagOfResolutionCollectionNewResolution.getResolutionCollection().remove(resolutionCollectionNewResolution);
                        oldTagidTagOfResolutionCollectionNewResolution = em.merge(oldTagidTagOfResolutionCollectionNewResolution);
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
                Integer id = tag.getIdTag();
                if (findTag(id) == null) {
                    throw new NonexistentEntityException("The tag with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Tag tag;
            try {
                tag = em.getReference(Tag.class, id);
                tag.getIdTag();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tag with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Resolution> resolutionCollectionOrphanCheck = tag.getResolutionCollection();
            for (Resolution resolutionCollectionOrphanCheckResolution : resolutionCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Tag (" + tag + ") cannot be destroyed since the Resolution " + resolutionCollectionOrphanCheckResolution + " in its resolutionCollection field has a non-nullable tagidTag field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(tag);
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

    public List<Tag> findTagEntities() {
        return findTagEntities(true, -1, -1);
    }

    public List<Tag> findTagEntities(int maxResults, int firstResult) {
        return findTagEntities(false, maxResults, firstResult);
    }

    private List<Tag> findTagEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Tag.class));
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

    public Tag findTag(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Tag.class, id);
        } finally {
            em.close();
        }
    }

    public int getTagCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Tag> rt = cq.from(Tag.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
