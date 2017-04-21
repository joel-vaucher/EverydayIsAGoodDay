/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.exceptions.IllegalOrphanException;
import controllers.exceptions.NonexistentEntityException;
import controllers.exceptions.RollbackFailureException;
import entities.Resolution;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import entities.Tag;
import entities.User;
import entities.Validation;
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

    public void create(Resolution resolution) throws RollbackFailureException, Exception {
        if (resolution.getValidationCollection() == null) {
            resolution.setValidationCollection(new ArrayList<Validation>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Tag tagidTag = resolution.getTagidTag();
            if (tagidTag != null) {
                tagidTag = em.getReference(tagidTag.getClass(), tagidTag.getIdTag());
                resolution.setTagidTag(tagidTag);
            }
            User useridUser = resolution.getUseridUser();
            if (useridUser != null) {
                useridUser = em.getReference(useridUser.getClass(), useridUser.getIdUser());
                resolution.setUseridUser(useridUser);
            }
            Collection<Validation> attachedValidationCollection = new ArrayList<Validation>();
            for (Validation validationCollectionValidationToAttach : resolution.getValidationCollection()) {
                validationCollectionValidationToAttach = em.getReference(validationCollectionValidationToAttach.getClass(), validationCollectionValidationToAttach.getIdValidation());
                attachedValidationCollection.add(validationCollectionValidationToAttach);
            }
            resolution.setValidationCollection(attachedValidationCollection);
            em.persist(resolution);
            if (tagidTag != null) {
                tagidTag.getResolutionCollection().add(resolution);
                tagidTag = em.merge(tagidTag);
            }
            if (useridUser != null) {
                useridUser.getResolutionCollection().add(resolution);
                useridUser = em.merge(useridUser);
            }
            for (Validation validationCollectionValidation : resolution.getValidationCollection()) {
                Resolution oldResolutionidResolutionOfValidationCollectionValidation = validationCollectionValidation.getResolutionidResolution();
                validationCollectionValidation.setResolutionidResolution(resolution);
                validationCollectionValidation = em.merge(validationCollectionValidation);
                if (oldResolutionidResolutionOfValidationCollectionValidation != null) {
                    oldResolutionidResolutionOfValidationCollectionValidation.getValidationCollection().remove(validationCollectionValidation);
                    oldResolutionidResolutionOfValidationCollectionValidation = em.merge(oldResolutionidResolutionOfValidationCollectionValidation);
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

    public void edit(Resolution resolution) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Resolution persistentResolution = em.find(Resolution.class, resolution.getIdResolution());
            Tag tagidTagOld = persistentResolution.getTagidTag();
            Tag tagidTagNew = resolution.getTagidTag();
            User useridUserOld = persistentResolution.getUseridUser();
            User useridUserNew = resolution.getUseridUser();
            Collection<Validation> validationCollectionOld = persistentResolution.getValidationCollection();
            Collection<Validation> validationCollectionNew = resolution.getValidationCollection();
            List<String> illegalOrphanMessages = null;
            for (Validation validationCollectionOldValidation : validationCollectionOld) {
                if (!validationCollectionNew.contains(validationCollectionOldValidation)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Validation " + validationCollectionOldValidation + " since its resolutionidResolution field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (tagidTagNew != null) {
                tagidTagNew = em.getReference(tagidTagNew.getClass(), tagidTagNew.getIdTag());
                resolution.setTagidTag(tagidTagNew);
            }
            if (useridUserNew != null) {
                useridUserNew = em.getReference(useridUserNew.getClass(), useridUserNew.getIdUser());
                resolution.setUseridUser(useridUserNew);
            }
            Collection<Validation> attachedValidationCollectionNew = new ArrayList<Validation>();
            for (Validation validationCollectionNewValidationToAttach : validationCollectionNew) {
                validationCollectionNewValidationToAttach = em.getReference(validationCollectionNewValidationToAttach.getClass(), validationCollectionNewValidationToAttach.getIdValidation());
                attachedValidationCollectionNew.add(validationCollectionNewValidationToAttach);
            }
            validationCollectionNew = attachedValidationCollectionNew;
            resolution.setValidationCollection(validationCollectionNew);
            resolution = em.merge(resolution);
            if (tagidTagOld != null && !tagidTagOld.equals(tagidTagNew)) {
                tagidTagOld.getResolutionCollection().remove(resolution);
                tagidTagOld = em.merge(tagidTagOld);
            }
            if (tagidTagNew != null && !tagidTagNew.equals(tagidTagOld)) {
                tagidTagNew.getResolutionCollection().add(resolution);
                tagidTagNew = em.merge(tagidTagNew);
            }
            if (useridUserOld != null && !useridUserOld.equals(useridUserNew)) {
                useridUserOld.getResolutionCollection().remove(resolution);
                useridUserOld = em.merge(useridUserOld);
            }
            if (useridUserNew != null && !useridUserNew.equals(useridUserOld)) {
                useridUserNew.getResolutionCollection().add(resolution);
                useridUserNew = em.merge(useridUserNew);
            }
            for (Validation validationCollectionNewValidation : validationCollectionNew) {
                if (!validationCollectionOld.contains(validationCollectionNewValidation)) {
                    Resolution oldResolutionidResolutionOfValidationCollectionNewValidation = validationCollectionNewValidation.getResolutionidResolution();
                    validationCollectionNewValidation.setResolutionidResolution(resolution);
                    validationCollectionNewValidation = em.merge(validationCollectionNewValidation);
                    if (oldResolutionidResolutionOfValidationCollectionNewValidation != null && !oldResolutionidResolutionOfValidationCollectionNewValidation.equals(resolution)) {
                        oldResolutionidResolutionOfValidationCollectionNewValidation.getValidationCollection().remove(validationCollectionNewValidation);
                        oldResolutionidResolutionOfValidationCollectionNewValidation = em.merge(oldResolutionidResolutionOfValidationCollectionNewValidation);
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
                illegalOrphanMessages.add("This Resolution (" + resolution + ") cannot be destroyed since the Validation " + validationCollectionOrphanCheckValidation + " in its validationCollection field has a non-nullable resolutionidResolution field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Tag tagidTag = resolution.getTagidTag();
            if (tagidTag != null) {
                tagidTag.getResolutionCollection().remove(resolution);
                tagidTag = em.merge(tagidTag);
            }
            User useridUser = resolution.getUseridUser();
            if (useridUser != null) {
                useridUser.getResolutionCollection().remove(resolution);
                useridUser = em.merge(useridUser);
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
