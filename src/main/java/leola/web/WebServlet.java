/*
 * see license.txt
 */
package leola.web;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leola.vm.types.LeoObject;


/**
 * A {@link HttpServlet} that handles the Web to Java to Leola relationships.
 * 
 * @author Tony
 *
 */
public class WebServlet extends HttpServlet {   
    
    /**
     * SUID
     */
    private static final long serialVersionUID = 334080569766757765L;
    
    
    private WebApp webapp;
    
    /**
     * @param webapp
     */
    public WebServlet(WebApp webapp) {
        this.webapp = webapp;
    }

    /**
     * @return this servlets {@link MultipartConfig} 
     */
    public MultipartConfig getMultipartConfig() {
        return new MultipartConfig() {
            
            @Override
            public Class<? extends Annotation> annotationType() {
                return MultipartConfig.class;
            }
            
            @Override
            public long maxRequestSize() {
                return webapp.config().getByString("multiPart").getObject("maxRequestSize").asLong();
            }
            
            @Override
            public long maxFileSize() {                
                return webapp.config().getByString("multiPart").getObject("maxFileSize").asLong();
            }
            
            @Override
            public String location() {                
                return "";
            }
            
            @Override
            public int fileSizeThreshold() {
                return 0;
            }
        };
    }

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
        .orElse(new WebResponse(HttpStatus.NOT_FOUND.getStatusCode()));
        
        webResponse.packageResponse(this.webapp, resp);        
    }

}
