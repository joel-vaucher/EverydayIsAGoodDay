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
import entities.Validation;
import entities.ValidationPK;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author julien.baumgart
 */
public class ValidationJpaController implements Serializable {

    public ValidationJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Validation validation) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (validation.getValidationPK() == null) {
            validation.setValidationPK(new ValidationPK());
        }
        validation.getValidationPK().setResolutionidResolution(validation.getResolution().getIdResolution());
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Resolution resolution = validation.getResolution();
            if (resolution != null) {
                resolution = em.getReference(resolution.getClass(), resolution.getIdResolution());
                validation.setResolution(resolution);
            }
            em.persist(validation);
            if (resolution != null) {
                resolution.getValidationCollection().add(validation);
                resolution = em.merge(resolution);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findValidation(validation.getValidationPK()) != null) {
                throw new PreexistingEntityException("Validation " + validation + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Validation validation) throws NonexistentEntityException, RollbackFailureException, Exception {
        validation.getValidationPK().setResolutionidResolution(validation.getResolution().getIdResolution());
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Validation persistentValidation = em.find(Validation.class, validation.getValidationPK());
            Resolution resolutionOld = persistentValidation.getResolution();
            Resolution resolutionNew = validation.getResolution();
            if (resolutionNew != null) {
                resolutionNew = em.getReference(resolutionNew.getClass(), resolutionNew.getIdResolution());
                validation.setResolution(resolutionNew);
            }
            validation = em.merge(validation);
            if (resolutionOld != null && !resolutionOld.equals(resolutionNew)) {
                resolutionOld.getValidationCollection().remove(validation);
                resolutionOld = em.merge(resolutionOld);
            }
            if (resolutionNew != null && !resolutionNew.equals(resolutionOld)) {
                resolutionNew.getValidationCollection().add(validation);
                resolutionNew = em.merge(resolutionNew);
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
                ValidationPK id = validation.getValidationPK();
                if (findValidation(id) == null) {
                    throw new NonexistentEntityException("The validation with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(ValidationPK id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Validation validation;
            try {
                validation = em.getReference(Validation.class, id);
                validation.getValidationPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The validation with id " + id + " no longer exists.", enfe);
            }
            Resolution resolution = validation.getResolution();
            if (resolution != null) {
                resolution.getValidationCollection().remove(validation);
                resolution = em.merge(resolution);
            }
            em.remove(validation);
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

    public List<Validation> findValidationEntities() {
        return findValidationEntities(true, -1, -1);
    }

    public List<Validation> findValidationEntities(int maxResults, int firstResult) {
        return findValidationEntities(false, maxResults, firstResult);
    }

    private List<Validation> findValidationEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Validation.class));
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

    public Validation findValidation(ValidationPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Validation.class, id);
        } finally {
            em.close();
        }
    }

    public int getValidationCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Validation> rt = cq.from(Validation.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
