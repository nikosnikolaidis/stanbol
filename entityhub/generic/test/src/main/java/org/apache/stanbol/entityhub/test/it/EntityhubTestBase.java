package org.apache.stanbol.entityhub.test.it;

import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Collections;

import org.apache.stanbol.commons.testing.http.RequestExecutor;
import org.apache.stanbol.commons.testing.http.RetryLoop;
import org.apache.stanbol.commons.testing.stanbol.StanbolTestBase;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Base Class intended to be used for all integration tests of the Entityhub
 * @author Rupert Westenthaler
 *
 */
public abstract class EntityhubTestBase extends StanbolTestBase{
    
    protected final Logger log;
    
    private final Collection<String> referencedSites;
    
    public EntityhubTestBase(Collection<String> referencedSites,Logger log) {
        if(log != null){
            this.log = log;
        } else {
            this.log = LoggerFactory.getLogger(getClass());
        }
        if(referencedSites == null){
            this.referencedSites = Collections.emptyList();
        } else {
            this.referencedSites = referencedSites;
        }
    }

    // TODO configurable via system properties??
    public static final int ENTITYHUB_TIMEOUT_SECONDS = 60;
    public static final int WAIT_BETWEEN_TRIES_MSEC = 1000;
    
    static boolean entityhubReady;
    static boolean timedOut;
    
    @Before
    public void checkEntityhubReady() throws Exception {
    
        // Check only once per test run
        if(entityhubReady) {
            return;
        }
        
        // If we timed out previously, don't waste time checking again
        if(timedOut) {
            fail("Timeout in previous check of the entityhub, cannot run tests");
        }
        
        // We'll retry the check for all engines to be ready
        // for up to ENGINES_TIMEOUT_SECONDS 
        final RetryLoop.Condition c = new RetryLoop.Condition() {
            
            @Override
            public boolean isTrue() throws Exception {
                /*  Check the entityhub and the referenced site dbpedia
                 */
                executor.execute(
                        builder.buildGetRequest("/entityhub")
                        .withHeader("Accept", "text/html")
                )
                .assertStatus(200)
                .assertContentType("text/html");                
                /*  List of expected referencedSites could also be made 
                 *  configurable via system properties, but we don't expect it 
                 *  to change often. 
                 */
                RequestExecutor re = executor.execute(
                        builder.buildGetRequest("/entityhub/sites/referenced")
                        .withHeader("Accept", "application/json"));
                re.assertStatus(200);
                re.assertContentType("application/json");
                //check if all the required referenced sites are available
                for(String referencedSite : referencedSites){
                    if(referencedSite != null && !referencedSite.isEmpty()){
                        re.assertContentRegexp(String.format(
                            "http:\\\\/\\\\/.*\\\\/entityhub\\\\/site\\\\/%s\\\\/",
                            referencedSite));
                    }
                }
                log.info("Enhancement engines checked, all present");
                return true;
            }
            
            @Override
            public String getDescription() {
                return "Checking that teh Entityhub and the dbpedia ReferencedSite are ready";
            }
        };
        
        new RetryLoop(c, ENTITYHUB_TIMEOUT_SECONDS, WAIT_BETWEEN_TRIES_MSEC) {
            @Override
            protected void reportException(Throwable t) {
                log.info("Exception in RetryLoop, will retry for up to " 
                        + getRemainingTimeSeconds() + " seconds: " + t);
            }
            
            protected void onTimeout() {
                timedOut = true;
            }
        };
        
        entityhubReady = true;
    }
}
