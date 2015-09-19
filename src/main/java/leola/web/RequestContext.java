/*
 * see license.txt
 */
package leola.web;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaMethod;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;

/**
 * The Request context contains all the information from a http request/response cycle.
 * 
 * @author Tony
 *
 */
public class RequestContext {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private WebApp webapp;
    
    private LeoMap contents;
    private LeoMap pathParams;

    private WebSession session;
    
    /**
     * @param request
     * @param response
     * @param webapp
     * @param pathParams
     */
    public RequestContext(HttpServletRequest request, HttpServletResponse response, WebApp webapp, LeoMap pathParams) {
        this.request = request;
        this.response = response;
        this.webapp = webapp;
        this.pathParams = pathParams;
        
        this.contents = new LeoMap();
    }

    
    /**
     * Retrieves the {@link WebSession} for this request.
     * 
     * @param create
     * @return the {@link WebSession}
     */
    public WebSession session(Boolean create) {
        if(this.session == null) {
            if(create==null) {
                create = true;
            }
            
            this.session = new WebSession(this.request.getSession(create));
        }
        
        return (this.session);
    }
    
    /**
     * Allows to index into this object for a content in leola code
     * 
     * <pre>
     *   var x = context["x"]
     * </pre>
     *
     * @see RequestContext#content(String)
     * @param reference the key in which the contents is bound (key/value)
     * @return the contents bounded at the supplied reference
     */
    @LeolaMethod(alias="$index")
    public LeoObject get(String reference) {
        return content(reference);
    }
    
    
    /**
     * Allows to set a content by reference in leola code
     *
     * <pre>
     *   context["x"] = "timmah"
     * </pre>
     * 
     * @see RequestContext#setContent(String,LeoObject)
     * @param reference the key in which the contents is bound (key/value)
     * @param value the value to set the reference to
     */
    @LeolaMethod(alias="$sindex")
    public void set(String reference, LeoObject value) {
        this.contents.put(LeoString.valueOf(reference), value);
    }
    
    
    /**
     * @return the {@link HttpServletRequest}
     */
    public HttpServletRequest request() {
        return this.request;
    }
    
    
    /**
     * @return the {@link HttpServletResponse}
     */
    public HttpServletResponse response() {
        return this.response;
    }
    
    /**
     * Attempts to parse the 'Authorization' header for Basic Authorization.
     * 
     * @return the {@link BasicAuthCredentials} if present
     */
    @LeolaIgnore
    public Optional<BasicAuthCredentials> parseBasicAuth() {
        return BasicAuthCredentials.fromRequest(this.request);
    }
    
    /**
     * Attempts to parse the 'Authorization' header for Basic Authorization.
     * 
     * @return a {@link LeoMap} with 'username' and 'password' properties
     */
    public LeoMap auth() {
        Optional<BasicAuthCredentials> auth = parseBasicAuth();
        return auth.map(b -> {
            LeoMap result = new LeoMap();
            result.putByString("username", LeoString.valueOf(b.getUsername()));
            result.putByString("password", LeoString.valueOf(b.getPassword()));
            return result;
        }).orElseGet( () -> {
        	LeoMap result = new LeoMap();
            result.putByString("username", LeoString.valueOf(""));
            result.putByString("password", LeoString.valueOf(""));
            return result;
        });
    }
    
    /**
     * Gets the body of the request as a JSON payload
     * 
     * @return the body of the request as a JSON payload, represented as a {@link LeoObject}
     * @throws IOException
     */
    public LeoObject json() throws IOException {        
        return WebLeolaLibrary.fromJson(this.request.getInputStream());
    }
    
    /**
     * Get a request parameter by name
     * 
     * @param name the name of the request parameter
     * @return the request parameter value
     */
    public String param(String name) {
        return request().getParameter(name);
    }
    
    /**
     * If a request parameter has multiple values, this will return all of the values in
     * an array.
     * 
     * @param name the request parameter name
     * @return the request parameter values
     */
    public LeoArray params(String name) {
        String[] params = request().getParameterValues(name);
        LeoArray result = new LeoArray();
        if(params!=null) {
            for(int i = 0; i < params.length; i++) {
                result.add(LeoString.valueOf(params[i]));
            }
        }
        return result;
    }
    
    
    /**
     * @return all of the request parameter names
     */
    public LeoArray paramNames() {
        Enumeration<String> e = request().getParameterNames();
        LeoArray params = new LeoArray();
        while(e.hasMoreElements()) {
            params.add(LeoString.valueOf(e.nextElement()));
        }
        return params;
    }
    
    
    /**
     * The path parameter at the supplied name.
     * 
     * <pre>
     *   path = "/users/{userid}"
     *   requestPath = "/users/tonys"
     *   
     *   var name = request.pathParam("userid")
     *   println(name) // 'tonys'
     * </pre>
     * 
     * @param name
     * @return
     */
    public LeoObject pathParam(String name) {
        return this.pathParams.getByString(name);
    }
    
    
    /**
     * @return all of the path parameters in the format of: pathVariableName -> Value
     */
    public LeoMap pathParams() {
        return this.pathParams;
    }
        
    /**
     * Retrieve the content referenced by the supplied name
     * 
     * @see RequestContext#get(String)
     * @param reference
     * @return the content value stored by the referenced name
     */
    public LeoObject content(String reference) {
        return this.contents.getByString(reference);
    }
    
    /**
     * Binds the supplied reference content to the supplied value
     * 
     * @param reference the name of the content
     * @param value the value to bind to the reference
     */
    public void setContent(String reference, LeoObject value) {
        this.contents.putByString(reference, value);
    }
    
    
    /**
     * @see WebApp#buildContext(leola.web.RoutingTable.Route, HttpServletRequest, HttpServletResponse)
     * @return the contents {@link LeoMap} which binds references to values.  This can be used
     * to store custom data elements in a {@link RequestContext}.
     */
    public LeoMap contents() {
        return this.contents;
    }
    
    
    /**
     * @return the {@link WebApp} that this {@link RequestContext} was constructed from.
     */
    public WebApp webapp() {
        return this.webapp;
    }
    
    @Override
    public String toString() {
        LeoMap obj = new LeoMap();
        obj.putByString("contents", this.contents);
        obj.putByString("pathParams", this.pathParams);
        obj.putByString("params", LeoObject.valueOf(this.request.getParameterMap()));
        obj.putByString("method", LeoString.valueOf(this.request.getMethod()));
        obj.putByString("requestUri", LeoString.valueOf(this.request.getRequestURI()));
        
        return obj.toString();
    }
}
