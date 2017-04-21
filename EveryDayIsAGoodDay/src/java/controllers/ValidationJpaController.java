/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.exceptions.NonexistentEntityException;
import controllers.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import entities.Resolution;
import entities.Validation;
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

    public void create(Validation validation) throws RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Resolution resolutionidResolution = validation.getResolutionidResolution();
            if (resolutionidResolution != null) {
                resolutionidResolution = em.getReference(resolutionidResolution.getClass(), resolutionidResolution.getIdResolution());
                validation.setResolutionidResolution(resolutionidResolution);
            }
            em.persist(validation);
            if (resolutionidResolution != null) {
                resolutionidResolution.getValidationCollection().add(validation);
                resolutionidResolution = em.merge(resolutionidResolution);
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

    public void edit(Validation validation) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Validation persistentValidation = em.find(Validation.class, validation.getIdValidation());
            Resolution resolutionidResolutionOld = persistentValidation.getResolutionidResolution();
            Resolution resolutionidResolutionNew = validation.getResolutionidResolution();
            if (resolutionidResolutionNew != null) {
                resolutionidResolutionNew = em.getReference(resolutionidResolutionNew.getClass(), resolutionidResolutionNew.getIdResolution());
                validation.setResolutionidResolution(resolutionidResolutionNew);
            }
            validation = em.merge(validation);
            if (resolutionidResolutionOld != null && !resolutionidResolutionOld.equals(resolutionidResolutionNew)) {
                resolutionidResolutionOld.getValidationCollection().remove(validation);
                resolutionidResolutionOld = em.merge(resolutionidResolutionOld);
            }
            if (resolutionidResolutionNew != null && !resolutionidResolutionNew.equals(resolutionidResolutionOld)) {
                resolutionidResolutionNew.getValidationCollection().add(validation);
                resolutionidResolutionNew = em.merge(resolutionidResolutionNew);
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
                Integer id = validation.getIdValidation();
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

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Validation validation;
            try {
                validation = em.getReference(Validation.class, id);
                validation.getIdValidation();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The validation with id " + id + " no longer exists.", enfe);
            }
            Resolution resolutionidResolution = validation.getResolutionidResolution();
            if (resolutionidResolution != null) {
                resolutionidResolution.getValidationCollection().remove(validation);
                resolutionidResolution = em.merge(resolutionidResolution);
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

    public Validation findValidation(Integer id) {
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
