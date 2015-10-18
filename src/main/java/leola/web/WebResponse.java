/*
 * see license.txt
 */
package leola.web;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import leola.vm.lib.LeolaIgnore;
import leola.vm.types.LeoObject;
import leola.web.templates.TemplateEngine;
import leola.web.templates.TemplateEngine.TemplateDocument;

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

    static class StreamEntry {
        InputStream stream;
        Optional<File> file;        
    }
    
    private MultivaluedMap headers;
    private List<Cookie> cookies;
    private List<String> deletedCookies;
    
    private String contentType;
    private int contentLength;
    private String characterEncoding;
    private int status;
    
    private Optional<Object> result;
    private Optional<String> templatePath;
    private Optional<String> redirectUrl;
    
    private Optional<StreamEntry> stream;
    
    
    
    /**
     * @param result the payload result
     * @param status the http status
     */
    public WebResponse(LeoObject result, int status) {
        this.result = Optional.ofNullable(result);
        this.templatePath = Optional.empty();
        this.redirectUrl = Optional.empty();
        this.stream = Optional.empty();
        
        this.status = status;
        this.headers = new MultivaluedMap();
        this.cookies = new ArrayList<Cookie>();
        this.deletedCookies = new ArrayList<String>();
        this.characterEncoding = "UTF-8";        
    }

    public WebResponse(int status) {
        this(null, status);
    }
    
    public WebResponse(HttpStatus status) {
        this(null, status.getStatusCode());
    }
    
    public WebResponse(LeoObject result, HttpStatus status) {
        this(result, status.getStatusCode());
    }
    
    private WebResponse obj(LeoObject obj) {
        String json = obj.toString();
        result = Optional.ofNullable(json);
        contentLength = json.getBytes().length;
        return this;
    }
    
    /**
     * @return the HTTP status code
     */
    public int status() {
        return this.status;
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
     * Deletes the supplied cookie from the client.
     * 
     * @param name
     * @return this {@link WebResponse} instance for method chaining
     */
    public WebResponse removeCookie(String name) {
        deletedCookies.add(name);
        return this;
    }
    
    /**
     * Removes the cookie from the response
     * 
     * @param response
     * @param name
     */
    private void removeCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
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
     * Stream back a series of bytes back as a response.
     * 
     * @param iStream the {@link InputStream}
     * @param mimeType the mime type of the stream
     * @return this {@link WebResponse} instance for method chaining
     */
    public WebResponse stream(InputStream iStream, String mimeType) {
        StreamEntry entry = new StreamEntry();
        entry.stream = iStream;
        if(mimeType!=null) {
            this.contentType = mimeType;
        }
        
        this.stream = Optional.ofNullable(entry);
        return this;
    }
    
    /**
     * Stream back a file as a response.
     * 
     * @param filePath the path to the file to stream back
     * @param mimeType the mime type of the file
     * @return this {@link WebResponse} instance for method chaining
     * @throws FileNotFoundException
     */
    public WebResponse file(String filePath, String mimeType) throws FileNotFoundException {
        Optional<File> file = Optional.ofNullable(new File(filePath));
        stream(new BufferedInputStream(new FileInputStream(file.get())), mimeType);
        this.stream.ifPresent(s -> s.file = file);
        
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
        
        /* If we have a template, let's render the template and return that 
         * as the response
         */
        if(hasTemplate()) {           
            
            TemplateEngine engine = webapp.getTemplateEngine();
            TemplateDocument template = engine.getTemplate(new File(webapp.getRootDirectory(), getTemplatePath()));
            
            Object result = getResult();
            template.apply(resp.getWriter(), result);
        }                
        
        headers.forEach((key, values) -> {
            values.forEach(value -> resp.addHeader(key, value) );            
        });
        
        cookies.forEach(cookie -> resp.addCookie(cookie));
        deletedCookies.forEach(name -> removeCookie(resp, name));
        
        resp.setContentType(contentType);
        if(contentLength > 0) {
            resp.setContentLength(contentLength + 2);
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
            
            if(result.isPresent() && !hasTemplate()) {
                PrintWriter writer = resp.getWriter();
                writer.println(result.get());
                writer.flush();
            }
            else if(stream.isPresent()) {
                StreamEntry entry = stream.get();
                if(contentType==null) {
                    resp.setContentType("application/octet-stream");
                }
                
                entry.file.ifPresent(file -> {
                    resp.setContentLengthLong(file.length());
                    String headerKey = "Content-Disposition";
                    String headerValue = String.format("attachment; filename=\"%s\"", file.getName());
                    resp.setHeader(headerKey, headerValue);
                });
             
                OutputStream oStream = resp.getOutputStream();
                
                try {
                    Util.copy(entry.stream, oStream);
                }
                finally {
                    entry.stream.close();
                }
                
            }            
            else {
                resp.getWriter().flush();
            }
            
        }
    }
}
