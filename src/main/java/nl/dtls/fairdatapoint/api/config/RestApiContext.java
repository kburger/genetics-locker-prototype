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
package nl.dtls.fairdatapoint.api.config;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver;
import java.io.File;
import nl.dtl.fairmetadata4j.model.Agent;

import nl.dtls.fairdatapoint.api.converter.AbstractMetadataMessageConverter;
import nl.dtls.fairdatapoint.repository.StoreManager;
import nl.dtls.fairdatapoint.repository.StoreManagerException;
import nl.dtls.fairdatapoint.repository.impl.StoreManagerImpl;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.springframework.beans.factory.annotation.Value;

/**
 * Spring context file.
 *
 * @author Rajaram Kaliyaperumal <rr.kaliyaperumal@gmail.com>
 * @author Kees Burger <kees.burger@dtls.nl>
 * @since 2015-11-19
 * @version 0.2
 */
@EnableWebMvc
@Configuration
@Import(ApplicationSwaggerConfig.class)
@ComponentScan(basePackages = "nl.dtls.fairdatapoint.*")
public class RestApiContext extends WebMvcConfigurerAdapter {

    private final static Logger LOGGER
            = LogManager.getLogger(RestApiContext.class);

    @Autowired
    private List<AbstractMetadataMessageConverter<?>> metadataConverters;
    
    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.addAll(metadataConverters);
    }
    
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        for (AbstractMetadataMessageConverter<?> converter : metadataConverters) {
            converter.configureContentNegotiation(configurer);
        }
    }
    
    @Bean(name = "publisher")
    public Agent publisher(@Value("${metadataProperties.publisherURI:nil}") 
            String publisherURI, 
            @Value("${metadataProperties.publisherName:nil}") 
                    String publishername) {
        Agent publisher = null;
        if (!publisherURI.contentEquals("nil") && 
                !publishername.contentEquals("nil")) {
            publisher = new Agent();
            publisher.setUri(valueFactory.createIRI(publisherURI));
            publisher.setName(valueFactory.createLiteral(publishername));            
        }
        return publisher;
    } 
    
    @Bean(name = "language")
    public IRI language(@Value("${metadataProperties.language:nil}") 
            String languageURI) {
        IRI language = null;
        if (!languageURI.contentEquals("nil")) {
            language = valueFactory.createIRI(languageURI);           
        }
        return language;
    }
    
    @Bean(name = "license")
    public IRI license(@Value("${metadataProperties.license:nil}") 
            String licenseURI) {
        IRI license = null;
        if (!licenseURI.contentEquals("nil")) {
            license = valueFactory.createIRI(licenseURI);           
        }
        return license;
    }
    

    @Bean(name = "repository", initMethod = "initialize",
            destroyMethod = "shutDown")
    public Repository repository(@Value("${store.type:1}") int storeType,
            @Value("${store.url}") String storeUrl, 
            @Value("${store.username:nil}") String storeUsername,
            @Value("${store.password:nil}") String storeUserPassword,
            @Value("${store.dir:}") String storeDir)
            throws RepositoryException {
        Repository repository;
        if (storeType == 1 && !storeUsername.isEmpty() && 
                !storeUsername.contains("nil")) { // HTTP endpoint
            SPARQLRepository sRepository = new SPARQLRepository(storeUrl);
            LOGGER.info("Initializing HTTP triple store ");
            sRepository.setUsernameAndPassword(storeUsername, 
                    storeUserPassword); 
            return sRepository;
        } else if (storeType == 2 && !storeDir.isEmpty()){
            File dataDir = new File(storeDir);
            LOGGER.info("Initializing native store");
            repository = new SailRepository(new NativeStore(dataDir));
        }
        else { // In memory is the default store
            Sail store = new MemoryStore();
            repository = new SailRepository(store);
            LOGGER.info("Initializing inmemory store");
        }
        return repository;
    }

    @Bean(name = "storeManager")
    @DependsOn({"repository"})
    public StoreManager storeManager() throws RepositoryException,
            StoreManagerException {
        return new StoreManagerImpl();
    }
    
    @Bean(name = "orcidTokenUrl")    
    public String orcidTokenUrl(@Value("${orcid.tokenUrl:nil}") 
            String url) {
        String orcidTokenUrl = null;
        if (!url.contentEquals("nil")) {
            orcidTokenUrl = url;           
        }
        return orcidTokenUrl;
    }
    
    
    @Bean(name = "orcidAuthorizeUrl")    
    public String orcidAuthorizeUrl(@Value("${orcid.authorizeUrl:nil}") 
            String url) {
        String authorizeUrl = null;
        if (!url.contentEquals("nil")) {
            authorizeUrl = url;           
        }
        return authorizeUrl;
    }
    
   
    @Bean(name = "orcidClientId")    
    public String orcidClientId(@Value("${orcid.clientId:nil}") 
            String id) {
        String clientId = null;
        if (!id.contentEquals("nil")) {
            clientId = id;           
        }
        return clientId;
    }    
    
    @Bean(name = "orcidClientSecret")    
    public String orcidClientSecret(@Value("${orcid.clientSecret:nil}") 
            String secret) {
        String clientSecret = null;
        if (!secret.contentEquals("nil")) {
            clientSecret = secret;           
        }
        return clientSecret;
    }
    
    @Bean(name = "orcidGrantType")    
    public String orcidGrantType(@Value("${orcid.grantType:nil}") 
            String type) {
        String grantType = null;
        if (!type.contentEquals("nil")) {
            grantType = type;           
        }
        return grantType;
    }
    
    @Bean(name = "orcidRedirectUrl")    
    public String orcidRedirectUrl(@Value("${orcid.redirectUrl:nil}") 
            String url) {
        if (!url.contentEquals("nil")) {
            return url;           
        }
        return null;
    }
    
    @Bean(name = "myconsentApiUrl")    
    public String myconsentApiUrl(@Value("${myconsent.apiUrl:nil}") 
            String url) {
        if (!url.contentEquals("nil")) {
            return url;         
        }
        return null;
    }
    
    @Bean(name = "myconsentResearcherToken")    
    public String myconsentResearcherToken(@Value("${myconsent.researcherToken:nil}") 
            String token) {
        if (!token.contentEquals("nil")) {
            return token;           
        }
        return null;
    }
    
    @Bean(name = "myconsentResearcherStudyId")    
    public String myconsentResearcherStudyId(@Value("${myconsent.researcherStudyId:nil}") 
            String id) {
        if (!id.contentEquals("nil")) {
            return id;           
        }
        return null;
    }
    
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.setOrder(Integer.MIN_VALUE + 1).
                addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.setOrder(Integer.MIN_VALUE + 2).
                addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void configureDefaultServletHandling(
            final DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
    
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(handlebars());
    }
    
    @Bean
    public ViewResolver handlebars() {
        HandlebarsViewResolver viewResolver = new HandlebarsViewResolver();
        
        // add handlebars helper to get a label's literal without datatype
        viewResolver.registerHelper("literal", new Helper<Literal>() {
            @Override
            public Object apply(Literal literal, Options options) throws IOException {
                return literal.getLabel();
            }
        });
        
        viewResolver.setPrefix("/WEB-INF/templates/");
        viewResolver.setSuffix(".hbs");
        viewResolver.setFailOnMissingFile(false);
        
        return viewResolver;
    }
}
