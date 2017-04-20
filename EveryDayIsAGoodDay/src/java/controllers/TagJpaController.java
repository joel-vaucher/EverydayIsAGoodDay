/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.exceptions.NonexistentEntityException;
import controllers.exceptions.PreexistingEntityException;
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

    public void create(Tag tag) throws PreexistingEntityException, RollbackFailureException, Exception {
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
                resolutionCollectionResolution.getTagCollection().add(tag);
                resolutionCollectionResolution = em.merge(resolutionCollectionResolution);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findTag(tag.getIdTag()) != null) {
                throw new PreexistingEntityException("Tag " + tag + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Tag tag) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Tag persistentTag = em.find(Tag.class, tag.getIdTag());
            Collection<Resolution> resolutionCollectionOld = persistentTag.getResolutionCollection();
            Collection<Resolution> resolutionCollectionNew = tag.getResolutionCollection();
            Collection<Resolution> attachedResolutionCollectionNew = new ArrayList<Resolution>();
            for (Resolution resolutionCollectionNewResolutionToAttach : resolutionCollectionNew) {
                resolutionCollectionNewResolutionToAttach = em.getReference(resolutionCollectionNewResolutionToAttach.getClass(), resolutionCollectionNewResolutionToAttach.getIdResolution());
                attachedResolutionCollectionNew.add(resolutionCollectionNewResolutionToAttach);
            }
            resolutionCollectionNew = attachedResolutionCollectionNew;
            tag.setResolutionCollection(resolutionCollectionNew);
            tag = em.merge(tag);
            for (Resolution resolutionCollectionOldResolution : resolutionCollectionOld) {
                if (!resolutionCollectionNew.contains(resolutionCollectionOldResolution)) {
                    resolutionCollectionOldResolution.getTagCollection().remove(tag);
                    resolutionCollectionOldResolution = em.merge(resolutionCollectionOldResolution);
                }
            }
            for (Resolution resolutionCollectionNewResolution : resolutionCollectionNew) {
                if (!resolutionCollectionOld.contains(resolutionCollectionNewResolution)) {
                    resolutionCollectionNewResolution.getTagCollection().add(tag);
                    resolutionCollectionNewResolution = em.merge(resolutionCollectionNewResolution);
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

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
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
            Collection<Resolution> resolutionCollection = tag.getResolutionCollection();
            for (Resolution resolutionCollectionResolution : resolutionCollection) {
                resolutionCollectionResolution.getTagCollection().remove(tag);
                resolutionCollectionResolution = em.merge(resolutionCollectionResolution);
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
