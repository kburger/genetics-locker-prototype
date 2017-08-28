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
import nl.dtls.fairdatapoint.service.MyconsentServiceException;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
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
    
    public boolean getRequestStatus(String url) throws MyconsentServiceException, 
            IllegalArgumentException {
        boolean status = false;
        try {
            HttpResponse<String> response = Unirest.get(url)
                    .header("accept", "application/json")
                    .header("Authorization", ("Bearer " + researcherToken))
                    .asString();
            if(response.getStatus() != 200) {
                throw (new IllegalArgumentException("Not valid request url"));
            }
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
