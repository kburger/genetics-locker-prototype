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
package nl.dtls.fairdatapoint.api.config;

import java.io.IOException;
import java.util.List;
import nl.dtl.fairmetadata4j.model.Agent;
import nl.dtls.fairdatapoint.api.converter.AbstractMetadataMessageConverter;
import nl.dtls.fairdatapoint.repository.StoreManager;
import nl.dtls.fairdatapoint.repository.StoreManagerException;
import nl.dtls.fairdatapoint.repository.impl.StoreManagerImpl;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Spring test context file. 
 * 
 * @author Rajaram Kaliyaperumal <rr.kaliyaperumal@gmail.com>
 * @author Kees Burger <kees.burger@dtls.nl>
 * @since 2016-02-11
 * @version 0.1
 */
@EnableWebMvc
@Configuration
@ComponentScan(basePackages = "nl.dtls.fairdatapoint.*")
public class RestApiTestContext extends WebMvcConfigurerAdapter  {
    
    @Autowired
    private List<AbstractMetadataMessageConverter<?>> metadataConverters;    
    
    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> 
            converters) {
        converters.addAll(metadataConverters);
    }
    
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer 
            configurer) {
        for (AbstractMetadataMessageConverter<?> converter : 
                metadataConverters) {
            converter.configureContentNegotiation(configurer);
        }
    }
    
    @Bean(name="repository", initMethod = "initialize",
            destroyMethod = "shutDown")
    public Repository repository(final Environment env)
            throws RepositoryException, IOException, RDFParseException {
        // For tets we use only in memory
        Sail store = new MemoryStore();
        return new SailRepository(store);
    }
    
    @Bean(name = "storeManager")
    @DependsOn({"repository"})
    public StoreManager storeManager() throws RepositoryException,
            StoreManagerException {
        return new StoreManagerImpl();
    }
        
    @Bean(name = "publisher")
    public Agent publisher() {
        Agent publisher = new Agent();
        publisher.setUri(valueFactory.createIRI("https://www.dtls.nl"));
        publisher.setName(valueFactory.createLiteral("DTLS"));
        return publisher;
    } 
    
    @Bean(name = "language")
    public IRI language() {
        IRI language = valueFactory.createIRI(
                "http://id.loc.gov/vocabulary/iso639-1/en");
        return language;
    }
    
    @Bean(name = "license")
    public IRI license() {
        IRI license =  valueFactory.createIRI(
                "http://rdflicense.appspot.com/rdflicense/cc-by-nc-nd3.0");
        return license;
    } 
    
    @Bean(name = "myconsentApiUrl")    
    public String myconsentApiUrl() {
        String url = "https://myconsent.nl/api/";
        return url;
    }
    
    @Bean(name = "myconsentResearcherToken")    
    public String myconsentResearcherToken() {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjY1ZmJiNTExMTlkMzliZjljOGQ3ZmYyN2MyNTBjYTAyN2EwMTBhYWRlYTQ1MjY5NjFjN2FkMjQ1YjhjNjk5MWJjNWZlMDFmOWIwYzBiMzYwIn0.eyJhdWQiOiIxIiwianRpIjoiNjVmYmI1MTExOWQzOWJmOWM4ZDdmZjI3YzI1MGNhMDI3YTAxMGFhZGVhNDUyNjk2MWM3YWQyNDViOGM2OTkxYmM1ZmUwMWY5YjBjMGIzNjAiLCJpYXQiOjE1MDQwOTc2MTEsIm5iZiI6MTUwNDA5NzYxMSwiZXhwIjoxNTM1NjMzNjExLCJzdWIiOiIzIiwic2NvcGVzIjpbXX0.KkgRPvpop5U40W2AeL9i_2a_9LUqQnYR5qrkNkua3zsxBdHL-P8KTarjiadfIN4TkgzqVw0PIIpJf9_jVFfzxRzNk7a0EPfbGnsgl9yRjDSvKPLGBUK6YdrgCsMIIRap7n5dvG-9uy1sQkKJMXPbVAz0AMMxH5IScZkvnxWv_9wzlA83-hO_QR0hh4MvariLcsQmVsbjaNcgG67n53Jt_j_6G9xcq5EkgOC0E9Apa5TQcATmSQOeAro1QmCSdDVuQ740c6_DkxMBNf0Kd5odmYTAtmoHfcr1ES7LRtgaQXgNdvmsv0T07yMwkfk9Tre7V2Hfm7hyO_jH03kMvPGuoHCfSib1qkfw20fWoLig1YVlgxfFQOZiJgEBJokXPIT5TovQK-dCMCOQAb_qaujJ9EufZ7NQW5qccDnlcyBWCMyBA9AxSpIuzPNtwSbztVKnWgNWaLcoz0pMy-wxKXG64cI6XbJrdkL9FRfYSkObesbkw67ZNXAiNQSlensqdiWEScV7TzDdvQUDhc7iURmkMiuVHdjvsB-uu8IxA-DSc5k6BDWfFOc6mG7jhWmAPyPc6NIlCFHVaLrTPhwEIw2HKV588IGSroSftUkxl0ITl5jpWBt2JguVNGb5933BUuxaDrTR9PyCGRg5Jlln6o_606N5pqlvqi70J3zRiAJyRpE";
        return token;
    }
    
    @Bean(name = "myconsentResearcherStudyId")    
    public String myconsentResearcherStudyId() {
        String studyId = "3";
        return studyId;
    }
}
