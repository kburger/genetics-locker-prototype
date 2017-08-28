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
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjFkZTc3MTkyZmU3ZTIyZDRkN2Uw"
                + "ZGI5Y2FhMjZlMmY4OTQ1OTIxOTQxZDIyMjk3YmRhOTIxZjBlMDYzZjY3OWNkMzIyMDhkNmFkNjM4NjZj"
                + "In0.eyJhdWQiOiIxIiwianRpIjoiMWRlNzcxOTJmZTdlMjJkNGQ3ZTBkYjljYWEyNmUyZjg5NDU5Mj"
                + "E5NDFkMjIyOTdiZGE5MjFmMGUwNjNmNjc5Y2QzMjIwOGQ2YWQ2Mzg2NmMiLCJpYXQiOjE1MDI3MjA5Nz"
                + "EsIm5iZiI6MTUwMjcyMDk3MSwiZXhwIjoxNTM0MjU2OTcxLCJzdWIiOiI1Iiwic2NvcGVzIjpbXX0."
                + "OYfsgcP5KgH4IRZLfvwVObyARNNQwfxyvczM-BwrSWVUcR93w7FoXJPdIjTvKnNYls9WBgZ6w27tas"
                + "CVuzYFfomIMMKe1NH4f_U66pdMa9IkubkJEyknHrn2utFxUKDkdMcrFVK7E2XmoNsfFzmhMsygOz5e"
                + "3OGNrPjAULX1dUDzoQqEmEl4zxBDOBRnIJDJ53TtFklVVrgiA6bBUIjpqbn6eBtmUmr00PBwdhmr"
                + "TC6SW4fOtH8SjhsMlVOXSr5TGjfb5y_JE9Xc-KDE_ITUzlPobdE0iwl4j7GtzBfMEed8QFzYsdPgPG-"
                + "CEXkdIjRZeR6eIeKZc6V0M_rguORhQd6rJlD4EUssFsNR0HlTA5FCbI4ahuNv_NEADvRNCcX7--"
                + "UrJaqfILfPFB4DrFAKs0PRslPppkYtNeQxBZL6l4JotwL0jEwvjdQyDeAzT3QYisFD2JnpH_kDOizUh"
                + "QZmoA_gFHnWHGMVnkC1wTimiGdmuybwhiW8duybOXGwAAHtWH758dr8Rzue7AGrldluRA0zmPh55u2N"
                + "xkBGd2xsH_7P61mWxsprwNrdYzNSystZKjwoY3Z4BwPziPGpTT-Xrr-weID8C2ibm2tC8hO7XXK4cB4V"
                + "fhAglG9Yo2Hobticz1Lp0GTF0j8-qblXB8oJDExJTkS1JASRJWajk9kRB6s";
        return token;
    }
}
