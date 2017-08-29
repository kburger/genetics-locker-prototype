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

import nl.dtls.fairdatapoint.api.config.RestApiTestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Unit test for myconsent service layer
 *
 * @author Rajaram Kaliyaperumal <rr.kaliyaperumal@gmail.com>
 * @author Kees Burger <kees.burger@dtls.nl>
 * @since 2017-08-28
 * @version 0.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {RestApiTestContext.class})
@DirtiesContext
public class MyconsentServiceTest {    
    
    private final static Logger LOGGER
            = LogManager.getLogger(MyconsentServiceTest.class.getName());
    @Autowired
    private MyconsentService myconsentService;
   
    /**
     * Test of getRequestStatus method, of class MyconsentService.
     */
    @Test
    public void testGetRequestStatus() throws Exception {
        LOGGER.info("Test : vaild request");
        String url = "https://myconsent.nl/api/request/9";
        boolean expResult = true;
        boolean result = myconsentService.getRequestStatus(url);
        assertEquals(expResult, result);
    }
    
    
    /**
     * Test of createRequestURL method, of class MyconsentService.
     */
    @Test
    @Ignore
    public void testCreateRequestURL() throws Exception {
        LOGGER.info("Test : vaild createRequestURL");
        String dsuid = "1";
        String studyId = "1";
        String description = "Test call";
        String result = myconsentService.createRequestURL(dsuid, studyId, description);
        assertNotNull(result);
    }
    
}
