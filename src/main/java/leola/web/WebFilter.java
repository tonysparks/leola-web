/*
 * see license.txt
 */
package leola.web;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leola.vm.types.LeoMap;
import leola.vm.types.LeoObject;

/**
 * Filters
 * 
 * @author Tony
 *
 */
public class WebFilter implements Filter {

    private WebApp webapp;
    
    private String pathSpec;
    private LeoObject function;
    
    
    
    /**
     * @param webapp
     * @param config
     * @param function
     */
    public WebFilter(WebApp webapp, LeoMap config, LeoObject function) {
        this.webapp = webapp;
        
        this.pathSpec = config.getString("path");
        this.function = function;
    }
    
    /**
     * @return the filterMap
     */
    public String getPathSpec() {
        return pathSpec;
    }
    
    /**
     * @return the function
     */
    public LeoObject getFunction() {
        return function;
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;
        
        final Boolean allow = webapp.getRoute(httpRequest).map( route -> {            
            LeoObject context = webapp.buildContext(Optional.of(route), httpRequest, httpResponse);
            Optional<WebResponse> webResponse = Optional.empty();
            
            boolean allowRequest = true;
            
            try {
                
                LeoObject result = function.call(context);
                if(result.isError()) {
                    webResponse = Optional.ofNullable(webapp.handleException(context, result));
                }
                else if(result.isBoolean()) {
                    allowRequest = result.isTrue();
                }
                else if(result.isClass()) {
                    webResponse = Optional.ofNullable((WebResponse)result.getValue(WebResponse.class));
                }
                else {
                    /* Otherwise block it */
                    allowRequest = false;
                }
                  
            }
            catch(Exception e) {
                webResponse = Optional.ofNullable(webapp.handleException(context, e));
            }
            
            if(webResponse.isPresent()) {
                try {
                    webResponse.get().packageResponse(webapp, httpResponse);
                }
                catch (Exception e) {
                    // TODO: How else should we handle this?
                    e.printStackTrace();
                }
                
                /* We do not want to continue on, since we are responding back
                 * with a request object
                 */
                allowRequest = false;
            }
            
            return allowRequest;
        })
        .orElse(Boolean.TRUE);
        
        
        
        /* If we don't want to filter this request, lets continue on in the chain,
         * otherwise we stop
         */
        if(allow && chain != null) {
            chain.doFilter(request, response);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {        
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {        
    }

}
