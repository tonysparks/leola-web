/*
 * see license.txt
 */
package leola.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

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
     * Gives header value from the request
     * 
     * @param header the header to retrieve
     * @return the header value 
     */
    public String header(String header) {
        return this.request.getHeader(header);
    }
    
    /**
     * Get the header value from the request as an integer
     * 
     * @param header the header to retrieve
     * @return the header value
     */
    public int headerAsInt(String header) {
        return this.request.getIntHeader(header);
    }
    
    
    /**
     * Get the header values from the request
     * @param header the header to retrieve
     * @return a list of values associated to the header
     */
    public LeoArray headers(String header) {
        LeoArray headers = new LeoArray();
        Enumeration<String> e = this.request.getHeaders(header);
        while(e.hasMoreElements()) {
            headers.add(LeoString.valueOf(e.nextElement()));
        }
        return headers;
    }
    
    /**
     * Retrieve all of the headers
     * 
     * @return all of the headers
     */
    public LeoArray headerNames() {
        LeoArray headers = new LeoArray();
        Enumeration<String> e = this.request.getHeaderNames();
        while(e.hasMoreElements()) {
            headers.add(LeoString.valueOf(e.nextElement()));
        }
        return headers;
    }
    
    
    /**
     * Get a cookie value for the supplied cookie name 
     * 
     * @param name the cookie name
     * @return the cookie value, or null if none is bound to the name
     */
    public String cookieValue(String name) {
        for(Cookie c : this.request.getCookies()) {
            if(c.getName().equals(name)) {
                return c.getValue(); 
            }
        }
        return null;
    }
    
    /**
     * Get the {@link Cookie} for the supplied cookie name 
     * @param name the cookie name
     * @return the cookie, or null if none is bound to the name
     */
    public Cookie cookie(String name) {
        for(Cookie c : this.request.getCookies()) {
            if(c.getName().equals(name)) {
                return c; 
            }
        }
        return null;
    }
    
    /**
     * @return all of the cookies stored with this request
     */
    public Cookie[] cookies() {
        return this.request.getCookies();
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
     * Determines if the request is an AJAX call or not -- most javascript API's use this
     * header to denote an AJAX call.
     * 
     * <p>
     * There is no sure way to determine this, so this really relies upon the client to
     * correctly set the <code>x-requested-with</code> header with a value of <code>XMLHttpRequest</code>.
     * 
     * @return true if we can determine this is an ajax call.
     */
    public boolean isAjax() {
        String requestedWith = this.request.getHeader("x-requested-with"); 
        return requestedWith != null && requestedWith.equalsIgnoreCase("XMLHttpRequest");
    }
    
    /**
     * Attempt to retrieve the <code>User-Agent</code> header
     * @return the User-Agent header value, may be null if not defined in the request.
     */
    public String userAgent() {
        return header("User-Agent");
    }
    
    
    /**
     * The content length (i.e., the number of bytes this request body contains).
     * 
     * @return the number of bytes this request body contains.
     */
    public int contentLength() {
        return this.request.getContentLength();
    }
    
    /**
     * The request query string.
     * 
     * @see HttpServletRequest#getQueryString()
     * @return the query string of the request.
     */
    public String queryString() {
        return this.request.getQueryString();
    }
    
    
    /**
     * The request path information.
     * 
     * @see HttpServletRequest#getPathInfo()
     * @return the request path information
     */
    public String pathInfo() {
        return this.request.getPathInfo();
    }
    
    /**
     * @return the request character encoding format.  If none was defined, it will assume <code>UTF-8</code>
     */
    public String encoding() {
        String charset = this.request.getCharacterEncoding();
        return charset!=null&&charset.isEmpty() ? charset : "UTF-8";
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
     * @param name the name of the {@link Part}
     * @return the {@link Part} if this is a <code>multipart/form-data</code> request; otherwise null.
     */
    public Part part(String name) {
        try {
            return this.request.getPart(name);
        }
        catch(Exception e) {
            return null;
        }
    }
    
    /**
     * @return attempts to return all of the {@link Part} data if and only if this is a 
     * <code>multipart/form-data</code> request.
     */
    public LeoArray parts() {
        LeoArray results = new LeoArray();
        try {
            for(Part part :this.request.getParts()) {
                results.add(LeoObject.valueOf(part));
            }
        }
        catch(Exception ignore) {            
        }
        
        return results;
    }
    
    /**
     * Attempts to save any {@link Part}'s to the specified directory.
     * 
     * @param directory the directory in which to save the files to
     * @param function an optional callback function that should return a filename or null if the file 
     * should not be stored.
     * @return the array of {@link File}'s that were saved
     * @throws IOException
     */
    public LeoArray save(String directory, LeoObject function) throws IOException {
        LeoArray result = new LeoArray();
        try {
            File parentFolder = new File(directory);
            if(!parentFolder.exists()) {
                if(!parentFolder.mkdirs()) {
                    throw new IOException("Unable to create directory structure: " + directory);
                }
            }
            
            for(Part part :this.request.getParts()) {
                String filename = Util.getFileName(part);
                if(function != null) {
                    LeoObject callbackResult = function.call(LeoObject.valueOf(part), LeoObject.valueOf(filename));
                    if(LeoObject.isNull(callbackResult)) {
                        continue;
                    }
                    
                    filename = callbackResult.toString();
                }
                
                File file = new File(parentFolder, filename);                                
                Util.writeFile(file, part.getInputStream());

                result.add(LeoObject.valueOf(file));
            }
        }
        catch (ServletException ignore) {
            // not a multipart/form-data request
        }
        
        return result;        
    }
    
    
    /**
     * Copies the request {@link InputStream} into a {@link ByteArrayOutputStream}
     * 
     * @return the {@link ByteArrayOutputStream}
     * @throws IOException
     */
    private ByteArrayOutputStream asStream() throws IOException {
        int knownLength = this.request.getContentLength();
        if(knownLength < 1) {
            knownLength = 1024 * 2;
        }
        
        ByteArrayOutputStream oStream = new ByteArrayOutputStream(knownLength);
        ServletInputStream iStream = this.request.getInputStream();
        Util.copy(iStream, oStream);
        
        return oStream;
    }
    
    /**
     * Get the request body as raw bytes.
     * 
     * @return the request body as raw bytes
     * @throws IOException
     */
    public ByteBuffer body() throws IOException {
        ByteArrayOutputStream oStream = asStream();        
        return ByteBuffer.wrap(oStream.toByteArray());
    }
    
    /**
     * Get the request body as text.  This will use the {@link RequestContext#encoding()} from the request type to
     * encode the {@link String} returned.
     * 
     * @return the String representing this request body
     * @throws IOException
     */
    public String text() throws IOException {
        ByteArrayOutputStream oStream = asStream();
        return oStream.toString(encoding());
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
        return param(name, null);
    }
    
    /**
     * Get a request parameter by name
     * 
     * @param name the name of the request parameter
     * @param defaultValue the default value to use, if the supplied parameter doesn't exist
     * @return the request parameter value
     */
    public String param(String name, String defaultValue) {
        String value = request().getParameter(name);
        if(value==null) {
            return defaultValue;
        }
        return value;
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
