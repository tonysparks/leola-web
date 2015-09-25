/*
 * see license.txt
 */
package leola.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tony
 *
 */
public class BasicAuthCredentialsTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testNormal() {
        String username = "brett";
        String password = "favre";
        BasicAuthCredentials basic = new BasicAuthCredentials(username, password);
        assertEquals(username, basic.getUsername());
        assertEquals(password, basic.getPassword());
    }

    @Test
    public void testFromRequest() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic YnJldHQ6ZmF2cmU=");
        
        String username = "brett";
        String password = "favre";
        Optional<BasicAuthCredentials> oBasic = BasicAuthCredentials.fromRequest(request);
        assertTrue(oBasic.isPresent());
        
        BasicAuthCredentials basic = oBasic.get();
        assertEquals(username, basic.getUsername());
        assertEquals(password, basic.getPassword());
    }
}
