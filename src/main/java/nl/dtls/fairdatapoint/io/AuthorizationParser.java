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
package nl.dtls.fairdatapoint.io;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import nl.dtl.fairmetadata4j.utils.vocabulary.WebAccessControl;
import nl.dtls.fairdatapoint.model.Authorization;
import org.apache.logging.log4j.LogManager;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

/**
 * Parser for authorization object
 *
 * @author Rajaram Kaliyaperumal <rr.kaliyaperumal@gmail.com>
 * @author Kees Burger <kees.burger@dtls.nl>
 * @since 2017-02-22
 * @version 0.1
 */
public class AuthorizationParser {

    private static final org.apache.logging.log4j.Logger LOGGER
            = LogManager.getLogger(AuthorizationParser.class);

    public static Authorization parse(@Nonnull List<Statement> statements,
            @Nonnull IRI metadata) {
        Preconditions.checkNotNull(metadata, "Metadata URI must not be null.");
        Preconditions.checkNotNull(statements, "Authorization statements must not be null.");
        Preconditions.checkArgument(!statements.isEmpty(), "Authorization statements must not be "
                + "empty.");
        LOGGER.info("Parsing Authorization");
        
        IRI authorizationUri = null;
        for (Statement st : statements) {
            
            Resource subject = st.getSubject();
            IRI predicate = st.getPredicate();
            Value object = st.getObject();
            if (predicate.equals(WebAccessControl.ACCESS_TO) && object.equals(metadata)) {
                authorizationUri = (IRI) subject;
                break;
            }        
        }
        Authorization authorization = new Authorization();
        authorization.setUri(authorizationUri);
        authorization.setMetadata(metadata);
        for (Statement st : statements) {
            Resource subject = st.getSubject();
            IRI predicate = st.getPredicate();
            Value object = st.getObject();
            if (subject.equals(authorizationUri)) {
                if (predicate.equals(WebAccessControl.ACCESS_AGENT)) {
                    authorization.setAgent((IRI) object);
                } else if (predicate.equals(RDFS.SEEALSO)) {
                    authorization.setRequest((IRI) object);
                }
            }
        }
        return authorization;
    }
}
