/*
 * see license.txt
 */
package leola.web;

import java.util.Date;

import javax.servlet.http.HttpSession;

import leola.vm.lib.LeolaMethod;
import leola.vm.types.LeoObject;

/**
 * A wrapper around a {@link HttpSession}
 * 
 * @author Tony
 *
 */
public class WebSession {

    private HttpSession session;
    
    /**
     * @param session
     */
    public WebSession(HttpSession session) {
        this.session = session;
    }
    
    
    /**
     * Retrieve an attribute by name.
     * 
     * <pre>
     *   var x = session["x"]
     * </pre>
     *
     * @see WebSession#getAttribute(String)
     * @param name the attribute name
     * @return the value bound to the attribute
     */
    @LeolaMethod(alias="$index")
    public LeoObject get(String reference) {
        return LeoObject.valueOf(getAttribute(reference));
    }
    
    
    /**
     * Set a session attribute.
     *
     * <pre>
     *   session["x"] = "timmah"
     * </pre>
     * 
     * @see WebSession#setAttribute(String, Object)
     * @param name the attribute name
     * @param value the value of the attribute
     */
    @LeolaMethod(alias="$sindex")
    public void set(String name, LeoObject value) {
        setAttribute(name, value);
    }
    
    
    /**
     * Retrieve the attribute bounded at the supplied name.
     * 
     * @param name the attribute name
     * @return the attribute bounded at the supplied name
     */
    public Object getAttribute(String name) {
        return this.session.getAttribute(name);
    }
    
    
    /**
     * Sets the attribute with the supplied name to the supplied
     *  value.
     *  
     * @param name the attribute name
     * @param value the attribute value
     */
    public void setAttribute(String name, Object value) {
        this.session.setAttribute(name, value);
    }
    
    /**
     * Invalidate this session.
     * @see HttpSession#invalidate()
     */
    public void invalidate() {
        this.session.invalidate();
    }
    
    /**
     * @see HttpSession#isNew()
     * @return true if this session was just created
     */
    public boolean isNew() {
        return this.session.isNew();
    }
    
    /**
     * @see HttpSession#getId()
     * @return the session ID
     */
    public String id() {
        return this.session.getId();
    }

    /**
     * The time this session was created in UTC.
     * 
     * @see HttpSession#getCreationTime()
     * @return the time this session was created
     */
    public Date creationTime() {
        return new Date(this.session.getCreationTime());
    }
    
    /**
     * @return The time this session was last accessed. 
     */
    public Date lastAccessedTime() {
        return new Date(this.session.getLastAccessedTime());
    }
    
    /**
     * Sets the max inactive time (in seconds).  If the session has not been used (i.e., a request has
     * come in that is bound to this session) in this amount of time, it will expire.
     * 
     * @param maxIdleTimeout
     */
    public void setMaxInactiveSec(int maxIdleTimeout) {
        this.session.setMaxInactiveInterval(maxIdleTimeout);
    }
    
    
    /**
     * @return the amount of time (in seconds) before this session will expire with no activity.
     */
    public int getMaxInactiveSec() {
        return this.session.getMaxInactiveInterval();
    }
}
