/*
 * see license.txt
 */
package leola.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import leola.vm.types.LeoObject;


/**
 * Servlet that handles the Web to Java to Leola relationships.
 * 
 * @author Tony
 *
 */
public class WebServlet extends HttpServlet {

    /**
     * SUID
     */
    private static final long serialVersionUID = -8568176621876984967L;
    
    
    private WebApp webapp;
    
    /**
     * @param webapp
     */
    public WebServlet(WebApp webapp) {
        this.webapp = webapp;
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {     
        WebResponse webResponse = this.webapp.getRoute(req).map(route -> {            
            LeoObject context = webapp.buildContext(route, req, resp);
            try {
                LeoObject result = route.getFunction().call(context);
                if(result.isError()) {
                    return webapp.handleException(context, result);
                }
                
                return (WebResponse)result.getValue(WebResponse.class);
            }
            catch(Exception e) {
                return webapp.handleException(context, e);
            }
            
        })
        .orElse(new WebResponse(Response.Status.NOT_FOUND.getStatusCode()));
        
        webResponse.packageResponse(this.webapp, resp);
    }

}
