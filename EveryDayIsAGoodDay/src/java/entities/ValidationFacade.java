/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author julien.baumgart
 */
@Stateless
public class ValidationFacade extends AbstractFacade<Validation> {

    @PersistenceContext(unitName = "EverydayIsAGoodDayPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ValidationFacade() {
        super(Validation.class);
    }
    
}
