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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.net.ssl.HttpsURLConnection;
import nl.dtls.fairdatapoint.service.OrcidServiceException;
import org.apache.logging.log4j.LogManager;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Service layer for communicating with ORCID api
 *
 * @author Rajaram Kaliyaperumal <rr.kaliyaperumal@gmail.com>
 * @author Kees Burger <kees.burger@dtls.nl>
 * @since 2017-02-28
 * @version 0.1
 */
@Service("orcidService")
public class OrcidService {

    private final static org.apache.logging.log4j.Logger LOGGER
            = LogManager.getLogger(OrcidService.class);
    @Autowired
    @Qualifier("orcidTokenUrl")
    private String orcidTokenUrl;
    @Autowired
    @Qualifier("orcidAuthorizeUrl")
    private String orcidAuthorizeUrl;
    @Autowired
    @Qualifier("orcidClientId")
    private String clientId;
    @Autowired
    @Qualifier("orcidClientSecret")
    private String clientSecret;
    @Autowired
    @Qualifier("orcidGrantType")
    private String grantType;
    @Autowired
    @Qualifier("orcidRedirectUrl")
    private String redirectUri;
    
    public String getAuthorizeUrl(){
        
        String url = orcidAuthorizeUrl + "?client_id=" + clientId + 
                "&response_type=code&scope=/authenticate&redirect_uri="+ 
                redirectUri;
        return url;           
    }

    public IRI getOrcidUri(@Nonnull String code) throws OrcidServiceException {
        IRI orcidUri = null;
        try {
            HashMap<String, Object> params = new HashMap<>();
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);
            params.put("grant_type", grantType);
            params.put("redirect_uri", redirectUri);
            params.put("code", code);
            //This REST call is bit hacky. The call returns 400 error if the content type is not set
            HttpResponse<String> response = Unirest.post(orcidTokenUrl).queryString(params).
                    header("Content-Type", "application/x-www-form-urlencoded")
                    .asString();
            if (response.getStatus() != 200) {
                String msg = "Error getting orcid url. ORCID api returns " + response.getStatus()
                        + " response status";
                LOGGER.error(code);
                throw (new OrcidServiceException(msg));
            }
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.getBody(), JsonObject.class);
            String orcid = jsonObject.get("orcid").getAsString();
            String orcidUriPrefix = "http://orcid.org/";
            ValueFactory valueFactory = SimpleValueFactory.getInstance();
            orcidUri = valueFactory.createIRI(orcidUriPrefix + orcid);
        } catch (UnirestException ex) {
            String msg = "Error getting orcid url." + ex.getMessage();
            LOGGER.error(code);
            throw (new OrcidServiceException(msg));
        }
        return orcidUri;
    }

}
