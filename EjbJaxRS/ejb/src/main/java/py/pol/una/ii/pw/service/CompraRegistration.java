/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package py.pol.una.ii.pw.service;

import py.pol.una.ii.pw.model.Compra;
import py.pol.una.ii.pw.model.CompraDetalle;
import py.pol.una.ii.pw.model.Provider;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class CompraRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Inject
    private Event<Compra> CompraEventSrc;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void register(Compra newcompra, List<CompraDetalle> detalles, Long proveedorId) {
    	try{
    		
	        Date dNow = new Date();
	        SimpleDateFormat ft = new SimpleDateFormat("MM-dd-yyyy");
	        newcompra.setFecha(ft.format(dNow));
	        newcompra.setProveedor(em.find(Provider.class, proveedorId));
	    	for (CompraDetalle dc : detalles){
	    		dc.setCompra(newcompra);
	    		newcompra.setDetalleCompra(dc);
	    	}
			log.info("Registering " + newcompra.getId());
	        em.persist(newcompra);
	        CompraEventSrc.fire(newcompra);
    	}catch (Exception e){
    		e.printStackTrace();
    	}
        
    }
    
    public void delete(Long id){
    	Compra Compra = new Compra();
    	Compra = em.find(Compra.class, id);
    	log.info("Deleting " + Compra.getId());
    	em.remove(Compra);
    	CompraEventSrc.fire(Compra);
    }
    
    public void update(Compra newCompra) throws Exception{
    	
    	try{
    		log.info("Updating " + newCompra.getId());
    		Compra oldCompra = getCompra(newCompra.getId());
    		em.merge(oldCompra);
    		oldCompra.setFecha(newCompra.getFecha());
    		oldCompra.setMonto(newCompra.getMonto());
    		oldCompra.setProveedor(newCompra.getProveedor());
    		CompraEventSrc.fire(oldCompra);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    }
    
    public List<Compra> search(String name){
    	log.info("Searching " + name);
    	List<Compra> result = (List<Compra>)em.createQuery("SELECT p FROM Compra p WHERE p.name LIKE :pname")
    		.setParameter("pname", "%"+name+"%")
    		.setMaxResults(10)
    		.getResultList();
    	if (result.isEmpty())
    		log.info("Esta vacio nio");
    	return result;
    }
    public Compra getCompra(Long id){
    	log.info("getCompra llega");
    	Compra compra = new Compra();
    	compra = em.find(Compra.class, id);
    	return compra;
    }
    
}
