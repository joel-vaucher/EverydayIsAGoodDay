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
import entities.Role;
import entities.Resolution;
import entities.User;
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
public class UserJpaController implements Serializable {

    public UserJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(User user) throws RollbackFailureException, Exception {
        if (user.getResolutionCollection() == null) {
            user.setResolutionCollection(new ArrayList<Resolution>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Role roleidRole = user.getRoleidRole();
            if (roleidRole != null) {
                roleidRole = em.getReference(roleidRole.getClass(), roleidRole.getIdRole());
                user.setRoleidRole(roleidRole);
            }
            Collection<Resolution> attachedResolutionCollection = new ArrayList<Resolution>();
            for (Resolution resolutionCollectionResolutionToAttach : user.getResolutionCollection()) {
                resolutionCollectionResolutionToAttach = em.getReference(resolutionCollectionResolutionToAttach.getClass(), resolutionCollectionResolutionToAttach.getIdResolution());
                attachedResolutionCollection.add(resolutionCollectionResolutionToAttach);
            }
            user.setResolutionCollection(attachedResolutionCollection);
            em.persist(user);
            if (roleidRole != null) {
                roleidRole.getUserCollection().add(user);
                roleidRole = em.merge(roleidRole);
            }
            for (Resolution resolutionCollectionResolution : user.getResolutionCollection()) {
                User oldUseridUserOfResolutionCollectionResolution = resolutionCollectionResolution.getUseridUser();
                resolutionCollectionResolution.setUseridUser(user);
                resolutionCollectionResolution = em.merge(resolutionCollectionResolution);
                if (oldUseridUserOfResolutionCollectionResolution != null) {
                    oldUseridUserOfResolutionCollectionResolution.getResolutionCollection().remove(resolutionCollectionResolution);
                    oldUseridUserOfResolutionCollectionResolution = em.merge(oldUseridUserOfResolutionCollectionResolution);
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

    public void edit(User user) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            User persistentUser = em.find(User.class, user.getIdUser());
            Role roleidRoleOld = persistentUser.getRoleidRole();
            Role roleidRoleNew = user.getRoleidRole();
            Collection<Resolution> resolutionCollectionOld = persistentUser.getResolutionCollection();
            Collection<Resolution> resolutionCollectionNew = user.getResolutionCollection();
            List<String> illegalOrphanMessages = null;
            for (Resolution resolutionCollectionOldResolution : resolutionCollectionOld) {
                if (!resolutionCollectionNew.contains(resolutionCollectionOldResolution)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Resolution " + resolutionCollectionOldResolution + " since its useridUser field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (roleidRoleNew != null) {
                roleidRoleNew = em.getReference(roleidRoleNew.getClass(), roleidRoleNew.getIdRole());
                user.setRoleidRole(roleidRoleNew);
            }
            Collection<Resolution> attachedResolutionCollectionNew = new ArrayList<Resolution>();
            for (Resolution resolutionCollectionNewResolutionToAttach : resolutionCollectionNew) {
                resolutionCollectionNewResolutionToAttach = em.getReference(resolutionCollectionNewResolutionToAttach.getClass(), resolutionCollectionNewResolutionToAttach.getIdResolution());
                attachedResolutionCollectionNew.add(resolutionCollectionNewResolutionToAttach);
            }
            resolutionCollectionNew = attachedResolutionCollectionNew;
            user.setResolutionCollection(resolutionCollectionNew);
            user = em.merge(user);
            if (roleidRoleOld != null && !roleidRoleOld.equals(roleidRoleNew)) {
                roleidRoleOld.getUserCollection().remove(user);
                roleidRoleOld = em.merge(roleidRoleOld);
            }
            if (roleidRoleNew != null && !roleidRoleNew.equals(roleidRoleOld)) {
                roleidRoleNew.getUserCollection().add(user);
                roleidRoleNew = em.merge(roleidRoleNew);
            }
            for (Resolution resolutionCollectionNewResolution : resolutionCollectionNew) {
                if (!resolutionCollectionOld.contains(resolutionCollectionNewResolution)) {
                    User oldUseridUserOfResolutionCollectionNewResolution = resolutionCollectionNewResolution.getUseridUser();
                    resolutionCollectionNewResolution.setUseridUser(user);
                    resolutionCollectionNewResolution = em.merge(resolutionCollectionNewResolution);
                    if (oldUseridUserOfResolutionCollectionNewResolution != null && !oldUseridUserOfResolutionCollectionNewResolution.equals(user)) {
                        oldUseridUserOfResolutionCollectionNewResolution.getResolutionCollection().remove(resolutionCollectionNewResolution);
                        oldUseridUserOfResolutionCollectionNewResolution = em.merge(oldUseridUserOfResolutionCollectionNewResolution);
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
                Integer id = user.getIdUser();
                if (findUser(id) == null) {
                    throw new NonexistentEntityException("The user with id " + id + " no longer exists.");
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
            User user;
            try {
                user = em.getReference(User.class, id);
                user.getIdUser();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The user with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Resolution> resolutionCollectionOrphanCheck = user.getResolutionCollection();
            for (Resolution resolutionCollectionOrphanCheckResolution : resolutionCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This User (" + user + ") cannot be destroyed since the Resolution " + resolutionCollectionOrphanCheckResolution + " in its resolutionCollection field has a non-nullable useridUser field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Role roleidRole = user.getRoleidRole();
            if (roleidRole != null) {
                roleidRole.getUserCollection().remove(user);
                roleidRole = em.merge(roleidRole);
            }
            em.remove(user);
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

    public List<User> findUserEntities() {
        return findUserEntities(true, -1, -1);
    }

    public List<User> findUserEntities(int maxResults, int firstResult) {
        return findUserEntities(false, maxResults, firstResult);
    }

    private List<User> findUserEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(User.class));
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

    public User findUser(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }

    public int getUserCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<User> rt = cq.from(User.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
