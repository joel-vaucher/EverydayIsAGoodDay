/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import controllers.exceptions.IllegalOrphanException;
import controllers.exceptions.NonexistentEntityException;
import controllers.exceptions.RollbackFailureException;
import entities.Role;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
public class RoleJpaController implements Serializable {

    public RoleJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Role role) throws RollbackFailureException, Exception {
        if (role.getUserCollection() == null) {
            role.setUserCollection(new ArrayList<User>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<User> attachedUserCollection = new ArrayList<User>();
            for (User userCollectionUserToAttach : role.getUserCollection()) {
                userCollectionUserToAttach = em.getReference(userCollectionUserToAttach.getClass(), userCollectionUserToAttach.getIdUser());
                attachedUserCollection.add(userCollectionUserToAttach);
            }
            role.setUserCollection(attachedUserCollection);
            em.persist(role);
            for (User userCollectionUser : role.getUserCollection()) {
                Role oldRoleidRoleOfUserCollectionUser = userCollectionUser.getRoleidRole();
                userCollectionUser.setRoleidRole(role);
                userCollectionUser = em.merge(userCollectionUser);
                if (oldRoleidRoleOfUserCollectionUser != null) {
                    oldRoleidRoleOfUserCollectionUser.getUserCollection().remove(userCollectionUser);
                    oldRoleidRoleOfUserCollectionUser = em.merge(oldRoleidRoleOfUserCollectionUser);
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

    public void edit(Role role) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Role persistentRole = em.find(Role.class, role.getIdRole());
            Collection<User> userCollectionOld = persistentRole.getUserCollection();
            Collection<User> userCollectionNew = role.getUserCollection();
            List<String> illegalOrphanMessages = null;
            for (User userCollectionOldUser : userCollectionOld) {
                if (!userCollectionNew.contains(userCollectionOldUser)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain User " + userCollectionOldUser + " since its roleidRole field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<User> attachedUserCollectionNew = new ArrayList<User>();
            for (User userCollectionNewUserToAttach : userCollectionNew) {
                userCollectionNewUserToAttach = em.getReference(userCollectionNewUserToAttach.getClass(), userCollectionNewUserToAttach.getIdUser());
                attachedUserCollectionNew.add(userCollectionNewUserToAttach);
            }
            userCollectionNew = attachedUserCollectionNew;
            role.setUserCollection(userCollectionNew);
            role = em.merge(role);
            for (User userCollectionNewUser : userCollectionNew) {
                if (!userCollectionOld.contains(userCollectionNewUser)) {
                    Role oldRoleidRoleOfUserCollectionNewUser = userCollectionNewUser.getRoleidRole();
                    userCollectionNewUser.setRoleidRole(role);
                    userCollectionNewUser = em.merge(userCollectionNewUser);
                    if (oldRoleidRoleOfUserCollectionNewUser != null && !oldRoleidRoleOfUserCollectionNewUser.equals(role)) {
                        oldRoleidRoleOfUserCollectionNewUser.getUserCollection().remove(userCollectionNewUser);
                        oldRoleidRoleOfUserCollectionNewUser = em.merge(oldRoleidRoleOfUserCollectionNewUser);
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
                Integer id = role.getIdRole();
                if (findRole(id) == null) {
                    throw new NonexistentEntityException("The role with id " + id + " no longer exists.");
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
            Role role;
            try {
                role = em.getReference(Role.class, id);
                role.getIdRole();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The role with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<User> userCollectionOrphanCheck = role.getUserCollection();
            for (User userCollectionOrphanCheckUser : userCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Role (" + role + ") cannot be destroyed since the User " + userCollectionOrphanCheckUser + " in its userCollection field has a non-nullable roleidRole field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(role);
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

    public List<Role> findRoleEntities() {
        return findRoleEntities(true, -1, -1);
    }

    public List<Role> findRoleEntities(int maxResults, int firstResult) {
        return findRoleEntities(false, maxResults, firstResult);
    }

    private List<Role> findRoleEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Role.class));
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

    public Role findRole(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Role.class, id);
        } finally {
            em.close();
        }
    }

    public int getRoleCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Role> rt = cq.from(Role.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
