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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;


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
        .orElse(new WebResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
        
        /*
         * If we have a template, let's render the template and return that 
         * as the response
         */
        if(webResponse.hasTemplate()) {
            //req.getRequestDispatcher( resp.encodeURL(webResponse.getTemplatePath()) ).forward(req, resp);
            MustacheFactory mf = new DefaultMustacheFactory(webapp.getRootDirectory());
            
            Mustache mustache = mf.compile(webResponse.getTemplatePath());            
            Object result = webResponse.getResult();
            mustache.execute(resp.getWriter(), result);
        }
        

        webResponse.packageResponse(resp);
    }

}
