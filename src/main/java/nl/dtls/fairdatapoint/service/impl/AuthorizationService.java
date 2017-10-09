/**
 * The MIT License
 * Copyright © 2017 DTL
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.dtls.fairdatapoint.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import nl.dtl.fairmetadata4j.utils.vocabulary.WebAccessControl;
import nl.dtls.fairdatapoint.io.AuthorizationParser;
import nl.dtls.fairdatapoint.model.Authorization;
import nl.dtls.fairdatapoint.repository.StoreManager;
import nl.dtls.fairdatapoint.repository.StoreManagerException;
import nl.dtls.fairdatapoint.service.AuthorizationServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service layer for manipulating fair metadata authorization statements
 *
 * @author Rajaram Kaliyaperumal <rr.kaliyaperumal@gmail.com>
 * @since 2017-08-11
 * @version 0.1
 */
@Service("authorizationService")
public class AuthorizationService {       
        
    private final static Logger LOGGER = LogManager.getLogger(AuthorizationService.class);
    @Autowired
    private StoreManager storeManager;
    
    public Authorization getAuthorization(IRI agent, IRI metadata) throws 
            AuthorizationServiceException {
        Preconditions.checkNotNull(agent, "Agent uri not be null.");
        Preconditions.checkNotNull(metadata, "Metadata uri not be null.");
        Authorization authorization = null;
        try {            
            List<Statement> statements = storeManager.retrieveResource(agent);
            if (!statements.isEmpty()) {
                authorization = AuthorizationParser.parse(statements, metadata);
            }
        } catch (StoreManagerException ex) {
            LOGGER.error("Error retrieving authorization statements from the store");
            throw (new AuthorizationServiceException(ex.getMessage()));
        }
        return authorization;
    }
    
    public void storeAuthorization(Authorization authorization) throws
            AuthorizationServiceException {

        Model model = new LinkedHashModel();
        model.add(authorization.getUri(), RDF.TYPE, WebAccessControl.AUTHORIZATION);
        model.add(authorization.getUri(), WebAccessControl.ACCESS_AGENT, authorization.getAgent());
        model.add(authorization.getUri(), WebAccessControl.ACCESS_TO, authorization.getMetadata());
        model.add(authorization.getUri(), RDFS.SEEALSO, authorization.getRequest());
        Iterator<Statement> it = model.iterator();
        List<Statement> statements = ImmutableList.copyOf(it);
        try {
            storeManager.storeStatements(statements, authorization.getAgent());
        } catch (StoreManagerException ex) {
            LOGGER.error("Error retrieving authorization statements from the store");
            throw (new AuthorizationServiceException(ex.getMessage()));
        }
    }
    
}
