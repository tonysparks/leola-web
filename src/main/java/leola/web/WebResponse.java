/*
 * see license.txt
 */
package leola.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import leola.vm.types.LeoObject;

/**
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
    
    private Object result;
    private String templatePath;
    private boolean isTemplate;
    
    
    /**
     * 
     */
    public WebResponse(LeoObject result, int status) {
        this.result = result;
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
        result = json;
        contentLength = json.getBytes().length;
        return this;
    }
    
    public WebResponse header(String name, String value) {
        headers.add(name, value);
        return this;
    }
    
    public WebResponse cookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);        
        cookies.add(cookie);
        return this;
    }
    
    public WebResponse addCookie(Cookie cookie) {
        cookies.add(cookie);
        return this;
    }
    
    public WebResponse characterEncoding(String encoding) {
        characterEncoding = encoding;
        return this;
    }
    
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
    
    public WebResponse template(Object templateValues, String templateFile) {
        result = templateValues;
        contentType = "text/html";
        isTemplate = true;
        templatePath = templateFile;
        return this;
    }
    
    /**
     * @return the templatePath
     */
    public String getTemplatePath() {
        return templatePath;
    }
    
    /**
     * @return the result
     */
    public Object getResult() {
        return result;
    }
    
    /**
     * @return the isTemplate
     */
    public boolean hasTemplate() {
        return isTemplate;
    }
    
    /**
     * Transfers the state of this {@link WebResponse} into the appropriate settings for the
     * {@link HttpServletResponse}
     * 
     * @param resp
     * @throws IOException
     */
    public void packageResponse(final HttpServletResponse resp) throws IOException {
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
        
        /* Do not write to the outputstream if we do not have a 
         * result OR we have a Template
         */
        PrintWriter writer = resp.getWriter();
        if(result != null && !hasTemplate()) {            
            writer.println(result);            
        }
        writer.flush();
    }
}
