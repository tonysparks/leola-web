/*
 * see license.txt
 */
package leola.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leola.vm.Leola;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;
import leola.vm.types.LeoUserFunction;
import leola.web.RoutingTable.Route;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Tony
 *
 */
public class WebAppTest {

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

    @Ignore
    private WebApp createWebApp(WebLeolaLibrary webLib) throws Exception {
        Leola runtime = new Leola();
        runtime.loadLibrary(webLib, "web");
        
        WebApp webapp = webLib.newWebApp(new LeoMap());
        webapp.errorHandler(new LeoUserFunction() {
            @Override
            public LeoObject call(LeoObject[] args) {
                fail("Failed on error handler!");
                return LeoObject.NULL;
            } 
        });
        
        return webapp;
    }
        
    @Test
    public void testHelloWorld() throws Exception {
        // 1) Create the Web App
        final WebLeolaLibrary webLib = new WebLeolaLibrary();
        WebApp webapp = createWebApp(webLib);
        
        
        // 2) Create the Route
        final LeoObject messageBody = LeoString.valueOf("Hello World");
        webapp.get("/hello", (context) -> {
            return webLib.ok().text(messageBody);
        });
                
        
        // 3) Mock up the Request/Response object
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/hello");
        
        
        // 4) Get the bounded Route for the Request
        Optional<Route> route = webapp.getRoute(request);
        assertTrue(route.isPresent());
        
        
        // 5) Mock the WebServlet, by invoking creating the RequestContext
        // and invoking the callback and inspecting the WebResponse
        route.ifPresent(r -> {
            assertEquals(r.getConfig().getString("path"), "/hello");
            LeoObject context = webapp.buildContext(route, request, response);
            try {
                WebResponse webRes = (WebResponse)r.getFunction().call(context).getValue(WebResponse.class);
                assertEquals(webRes.status(), 200);
                assertEquals(webRes.getResult(), messageBody.toString());
            }
            catch(Exception e) {
                fail("failed on route execution: " + e);
            }
        });
        
        
        // 6) Cleanup after ourselves
        webapp.shutdown();
    }

}
