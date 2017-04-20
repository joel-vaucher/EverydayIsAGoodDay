/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.exceptions.IllegalOrphanException;
import controllers.exceptions.NonexistentEntityException;
import controllers.exceptions.PreexistingEntityException;
import controllers.exceptions.RollbackFailureException;
import entities.Resolution;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import entities.User;
import entities.Tag;
import java.util.ArrayList;
import java.util.Collection;
import entities.Validation;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author julien.baumgart
 */
public class ResolutionJpaController implements Serializable {

    public ResolutionJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Resolution resolution) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (resolution.getTagCollection() == null) {
            resolution.setTagCollection(new ArrayList<Tag>());
        }
        if (resolution.getValidationCollection() == null) {
            resolution.setValidationCollection(new ArrayList<Validation>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            User useridUser = resolution.getUseridUser();
            if (useridUser != null) {
                useridUser = em.getReference(useridUser.getClass(), useridUser.getIdUser());
                resolution.setUseridUser(useridUser);
            }
            Collection<Tag> attachedTagCollection = new ArrayList<Tag>();
            for (Tag tagCollectionTagToAttach : resolution.getTagCollection()) {
                tagCollectionTagToAttach = em.getReference(tagCollectionTagToAttach.getClass(), tagCollectionTagToAttach.getIdTag());
                attachedTagCollection.add(tagCollectionTagToAttach);
            }
            resolution.setTagCollection(attachedTagCollection);
            Collection<Validation> attachedValidationCollection = new ArrayList<Validation>();
            for (Validation validationCollectionValidationToAttach : resolution.getValidationCollection()) {
                validationCollectionValidationToAttach = em.getReference(validationCollectionValidationToAttach.getClass(), validationCollectionValidationToAttach.getValidationPK());
                attachedValidationCollection.add(validationCollectionValidationToAttach);
            }
            resolution.setValidationCollection(attachedValidationCollection);
            em.persist(resolution);
            if (useridUser != null) {
                useridUser.getResolutionCollection().add(resolution);
                useridUser = em.merge(useridUser);
            }
            for (Tag tagCollectionTag : resolution.getTagCollection()) {
                tagCollectionTag.getResolutionCollection().add(resolution);
                tagCollectionTag = em.merge(tagCollectionTag);
            }
            for (Validation validationCollectionValidation : resolution.getValidationCollection()) {
                Resolution oldResolutionOfValidationCollectionValidation = validationCollectionValidation.getResolution();
                validationCollectionValidation.setResolution(resolution);
                validationCollectionValidation = em.merge(validationCollectionValidation);
                if (oldResolutionOfValidationCollectionValidation != null) {
                    oldResolutionOfValidationCollectionValidation.getValidationCollection().remove(validationCollectionValidation);
                    oldResolutionOfValidationCollectionValidation = em.merge(oldResolutionOfValidationCollectionValidation);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findResolution(resolution.getIdResolution()) != null) {
                throw new PreexistingEntityException("Resolution " + resolution + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Resolution resolution) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Resolution persistentResolution = em.find(Resolution.class, resolution.getIdResolution());
            User useridUserOld = persistentResolution.getUseridUser();
            User useridUserNew = resolution.getUseridUser();
            Collection<Tag> tagCollectionOld = persistentResolution.getTagCollection();
            Collection<Tag> tagCollectionNew = resolution.getTagCollection();
            Collection<Validation> validationCollectionOld = persistentResolution.getValidationCollection();
            Collection<Validation> validationCollectionNew = resolution.getValidationCollection();
            List<String> illegalOrphanMessages = null;
            for (Validation validationCollectionOldValidation : validationCollectionOld) {
                if (!validationCollectionNew.contains(validationCollectionOldValidation)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Validation " + validationCollectionOldValidation + " since its resolution field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (useridUserNew != null) {
                useridUserNew = em.getReference(useridUserNew.getClass(), useridUserNew.getIdUser());
                resolution.setUseridUser(useridUserNew);
            }
            Collection<Tag> attachedTagCollectionNew = new ArrayList<Tag>();
            for (Tag tagCollectionNewTagToAttach : tagCollectionNew) {
                tagCollectionNewTagToAttach = em.getReference(tagCollectionNewTagToAttach.getClass(), tagCollectionNewTagToAttach.getIdTag());
                attachedTagCollectionNew.add(tagCollectionNewTagToAttach);
            }
            tagCollectionNew = attachedTagCollectionNew;
            resolution.setTagCollection(tagCollectionNew);
            Collection<Validation> attachedValidationCollectionNew = new ArrayList<Validation>();
            for (Validation validationCollectionNewValidationToAttach : validationCollectionNew) {
                validationCollectionNewValidationToAttach = em.getReference(validationCollectionNewValidationToAttach.getClass(), validationCollectionNewValidationToAttach.getValidationPK());
                attachedValidationCollectionNew.add(validationCollectionNewValidationToAttach);
            }
            validationCollectionNew = attachedValidationCollectionNew;
            resolution.setValidationCollection(validationCollectionNew);
            resolution = em.merge(resolution);
            if (useridUserOld != null && !useridUserOld.equals(useridUserNew)) {
                useridUserOld.getResolutionCollection().remove(resolution);
                useridUserOld = em.merge(useridUserOld);
            }
            if (useridUserNew != null && !useridUserNew.equals(useridUserOld)) {
                useridUserNew.getResolutionCollection().add(resolution);
                useridUserNew = em.merge(useridUserNew);
            }
            for (Tag tagCollectionOldTag : tagCollectionOld) {
                if (!tagCollectionNew.contains(tagCollectionOldTag)) {
                    tagCollectionOldTag.getResolutionCollection().remove(resolution);
                    tagCollectionOldTag = em.merge(tagCollectionOldTag);
                }
            }
            for (Tag tagCollectionNewTag : tagCollectionNew) {
                if (!tagCollectionOld.contains(tagCollectionNewTag)) {
                    tagCollectionNewTag.getResolutionCollection().add(resolution);
                    tagCollectionNewTag = em.merge(tagCollectionNewTag);
                }
            }
            for (Validation validationCollectionNewValidation : validationCollectionNew) {
                if (!validationCollectionOld.contains(validationCollectionNewValidation)) {
                    Resolution oldResolutionOfValidationCollectionNewValidation = validationCollectionNewValidation.getResolution();
                    validationCollectionNewValidation.setResolution(resolution);
                    validationCollectionNewValidation = em.merge(validationCollectionNewValidation);
                    if (oldResolutionOfValidationCollectionNewValidation != null && !oldResolutionOfValidationCollectionNewValidation.equals(resolution)) {
                        oldResolutionOfValidationCollectionNewValidation.getValidationCollection().remove(validationCollectionNewValidation);
                        oldResolutionOfValidationCollectionNewValidation = em.merge(oldResolutionOfValidationCollectionNewValidation);
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
                Integer id = resolution.getIdResolution();
                if (findResolution(id) == null) {
                    throw new NonexistentEntityException("The resolution with id " + id + " no longer exists.");
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
            Resolution resolution;
            try {
                resolution = em.getReference(Resolution.class, id);
                resolution.getIdResolution();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The resolution with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Validation> validationCollectionOrphanCheck = resolution.getValidationCollection();
            for (Validation validationCollectionOrphanCheckValidation : validationCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Resolution (" + resolution + ") cannot be destroyed since the Validation " + validationCollectionOrphanCheckValidation + " in its validationCollection field has a non-nullable resolution field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            User useridUser = resolution.getUseridUser();
            if (useridUser != null) {
                useridUser.getResolutionCollection().remove(resolution);
                useridUser = em.merge(useridUser);
            }
            Collection<Tag> tagCollection = resolution.getTagCollection();
            for (Tag tagCollectionTag : tagCollection) {
                tagCollectionTag.getResolutionCollection().remove(resolution);
                tagCollectionTag = em.merge(tagCollectionTag);
            }
            em.remove(resolution);
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

    public List<Resolution> findResolutionEntities() {
        return findResolutionEntities(true, -1, -1);
    }

    public List<Resolution> findResolutionEntities(int maxResults, int firstResult) {
        return findResolutionEntities(false, maxResults, firstResult);
    }

    private List<Resolution> findResolutionEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Resolution.class));
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

    public Resolution findResolution(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Resolution.class, id);
        } finally {
            em.close();
        }
    }

    public int getResolutionCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Resolution> rt = cq.from(Resolution.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
