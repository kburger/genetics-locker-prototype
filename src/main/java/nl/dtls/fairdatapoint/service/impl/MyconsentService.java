/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.dtls.fairdatapoint.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.util.HashMap;
import java.util.Map;
import nl.dtls.fairdatapoint.service.MyconsentServiceException;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Service layer for communicating with myconsent api
 *
 * @author Rajaram Kaliyaperumal <rr.kaliyaperumal@gmail.com>
 * @author Kees Burger <kees.burger@dtls.nl>
 * @since 2017-08-28
 * @version 0.1
 */
@Service("myconsentService")
public class MyconsentService {
    
    private final static org.apache.logging.log4j.Logger LOGGER
            = LogManager.getLogger(MyconsentService.class);
    
    @Autowired
    @Qualifier("myconsentApiUrl")
    private String apiUrl;     
    @Autowired
    @Qualifier("myconsentResearcherToken")
    private String researcherToken; 
    
    public boolean getRequestStatus(String url) throws MyconsentServiceException {
        boolean status = false;
        try {
            HttpResponse<String> response = Unirest.get(url)
                    .header("accept", "application/json")
                    .header("Authorization", ("Bearer " + researcherToken))
                    .asString();
            JsonObject jsonObject = new Gson().fromJson(response.getBody(), JsonObject.class);
            status = jsonObject.get("request").getAsJsonObject().get("status").getAsBoolean();
        } catch (UnirestException ex) {
            String msg = "Error querying myconsent API. " + ex.getMessage();
            LOGGER.error(msg);
            throw (new MyconsentServiceException(msg));
        }
        return status;
    }   
    
}
