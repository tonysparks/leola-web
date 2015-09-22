/*
 * see license.txt
 */
package leola.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import leola.vm.lib.LeolaIgnore;
import leola.vm.types.LeoObject;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * An HTTP response to be sent back to the client.  A response will contain:
 * 
 * <ul>
 *  <li>Headers</li>
 *  <li>Cookies</li>
 *  <li>Content Type</li>
 *  <li>Content Length</li>
 *  <li>Character Encoding</li>
 *  <li>HTTP Status</li>
 *  <li>Payload</li>
 * </ul>
 * 
 * There are various convenience methods supplied to the {@link WebResponse} that take care of most of the response values.  For example:
 * 
 * <pre>
 *   // in leola code
 *   web:ok().json({
 *      message -> "Everything is well!"
 *   })
 * </pre>
 * 
 * The above will create a {@link WebResponse} with an HTTP status code of <code>200</code> and set the content type to <code>text/json</code> and appropriately set 
 * the content length and payload.
 * 
 * @author Tony
 *
 */
public class WebResponse {

    private MultivaluedMap<String, String> headers;
    private List<Cookie> cookies;
    private String contentType;
    private int contentLength;
    private String characterEncoding;
    private int status;
    
    private Optional<Object> result;
    private Optional<String> templatePath;
    private Optional<String> redirectUrl;
    
    /**
     * @param result the payload result
     * @param status the http status
     */
    public WebResponse(LeoObject result, int status) {
        this.result = Optional.ofNullable(result);
        this.templatePath = Optional.empty();
        this.redirectUrl = Optional.empty();
        
        this.status = status;
        this.headers = new MultivaluedMapImpl();
        this.cookies = new ArrayList<Cookie>();
        this.characterEncoding = "UTF-8";        
    }

    public WebResponse(int status) {
        this(null, status);
    }
    
    public WebResponse(Status status) {
        this(null, status.getStatusCode());
    }
    
    public WebResponse(LeoObject result, Status status) {
        this(result, status.getStatusCode());
    }
    
    private WebResponse obj(LeoObject obj) {
        String json = obj.toString();
        result = Optional.ofNullable(json);
        contentLength = json.getBytes().length;
        return this;
    }
    
    
    /**
     * Sets a header value
     * 
     * @param name the name of the header
     * @param value the value of the header
     * @return this {@link WebResponse} instance for method chaining
     */
    public WebResponse header(String name, String value) {
        headers.add(name, value);
        return this;
    }
    
    
    /**
     * Sets a cookie value
     * 
     * @param name the name of the cookie
     * @param value the value of the cookie
     * @return this {@link WebResponse} instance for method chaining
     */
    public WebResponse cookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);        
        cookies.add(cookie);
        return this;
    }
    
    /**
     * Add's a new {@link Cookie}, which is a more robust way of adding 
     * a cookie to the HTTP response (you can set domain, maxAge, etc.).
     * 
     * @param cookie
     * @return this {@link WebResponse} instance for method chaining
     */
    public WebResponse addCookie(Cookie cookie) {
        cookies.add(cookie);
        return this;
    }
    
    
    /**
     * Set the HTTP response character encoding.  As a default <code>UTF-8</code>
     * is used.
     * 
     * @param encoding
     * @return this {@link WebResponse} instance for method chaining
     */
    public WebResponse characterEncoding(String encoding) {
        characterEncoding = encoding;
        return this;
    }
    
    
    /**
     * Set the "Accept" content type header.
     * 
     * @param contentType
     * @return this {@link WebResponse} instance for method chaining
     */
    public WebResponse contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
    
    /**
     * Interpret the supplied object as a JSON payload and set the content
     * type to <code>text/json</code>
     * 
     * 
     * @param obj the json payload
     * @return this {@link WebResponse} instance for method chaining
     */
    public WebResponse json(LeoObject obj) {
        contentType = "text/json";
        return obj(obj);
    }

    public WebResponse html(LeoObject obj) {
        contentType = "text/html";
        return obj(obj);
    }
    
    public WebResponse text(LeoObject obj) {
        contentType = "text/txt";
        return obj(obj);
    }
    
    
    /**
     * Issues a 301 redirect.
     * 
     * @param path - the redirect URL
     * @return this {@link WebResponse} instance for method chaining
     */
    public WebResponse redirect(String path) {
        this.redirectUrl = Optional.ofNullable(path);        
        return this;
    }
    
    
    /**
     * This will return a templated file (for example a mustache enabled html file) that will
     * use the supplied 'templateValues' to populate the template.  The leola-web framework will 
     * return the resulting templated file as the HTTP response.
     * 
     * @param templateValues
     * @param templateFile
     * @return this {@link WebResponse} instance for method chaining
     */
    public WebResponse template(String templateFile, Object templateValues) {
        result = Optional.ofNullable(templateValues);
        templatePath = Optional.ofNullable(templateFile);
        contentType = "text/html";        
        return this;
    }
    
    /**
     * @return the templatePath
     */    
    public String getTemplatePath() {
        return templatePath.orElse("");
    }
    
    /**
     * @return the redirectUrl
     */
    public String getRedirectUrl() {
        return redirectUrl.orElse("");
    }
    
    /**
     * @return true if there is a result body attached to this {@link WebResponse}
     */
    public boolean hasResult() {
        return this.result.isPresent();
    }
    
    /**
     * @return the result
     */    
    public Object getResult() {
        return result.orElse(null);
    }
    
    /**
     * @return the isTemplate
     */    
    public boolean hasTemplate() {
        return templatePath.isPresent();
    }
    
    /**
     * @return if this is a redirect
     */
    public boolean isRedirect() {
        return this.redirectUrl.isPresent();
    }
    
    /**
     * Transfers the state of this {@link WebResponse} into the appropriate settings for the
     * {@link HttpServletResponse}
     * 
     * @param resp
     * @throws IOException
     */
    @LeolaIgnore
    public void packageResponse(final WebApp webapp, final HttpServletResponse resp) throws IOException {
        
        /*
         * If we have a template, let's render the template and return that 
         * as the response
         */
        if(hasTemplate()) {
            /* TODO - Move this logic out of here, delegate to a Template engine
             * interface, so that Mustache isn't hard-coded.
             */
            MustacheFactory mf = new DefaultMustacheFactory(webapp.getRootDirectory());
            
            Mustache mustache = mf.compile(getTemplatePath());            
            Object result = getResult();
            mustache.execute(resp.getWriter(), result);
        }
        
        
        headers.forEach((key, values) -> {
            values.forEach(value -> resp.addHeader(key, value) );            
        });
        
        cookies.forEach(cookie -> resp.addCookie(cookie));
        resp.setContentType(contentType);
        if(contentLength > 0) {
            resp.setContentLength(contentLength);
        }
        resp.setCharacterEncoding(characterEncoding);
        resp.setStatus(status);
        

        if(isRedirect()) {
            resp.sendRedirect(resp.encodeRedirectURL(getRedirectUrl()));
        }
        else {
            /* Do not write to the outputstream if we do not have a 
             * result OR we have a Template
             */
            PrintWriter writer = resp.getWriter();
            if(result.isPresent() && !hasTemplate()) {            
                writer.println(result.get());            
            }
            writer.flush();
        }
    }
}
