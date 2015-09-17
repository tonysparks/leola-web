/*
 * see license.txt
 */
package leola.web;

import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;

import leola.vm.Leola;
import leola.vm.types.LeoObject;

/**
 * A thin wrapper around the {@link Session}.  This just makes integration with {@link Leola} more convenient.
 * 
 * @author Tony
 *
 */
public class WebSocketSession {

    private Session session;
    
    /**
     * @param session
     */
    public WebSocketSession(Session session) {
        this.session = session;        
    }

    public void setMaxIdleTimeoutMSec(long timeout) {
        this.session.setMaxIdleTimeout(timeout);
    }
    
    public Session getSession() {
        return this.session;
    }
    
    public boolean isOpen() {
        return this.session.isOpen();
    }
    
    public String id() {
        return this.session.getId();
    }
    
    public WebSocketSession close() throws Exception {
        this.session.close();
        return this;
    }
    
    public WebSocketSession send(String message) throws Exception {
        Basic client = this.session.getBasicRemote();
        client.sendText(message);
        return this;
    }
    
    public WebSocketSession asyncSend(String message, final LeoObject callback) throws Exception {
        this.session.getAsyncRemote().sendText(message, new SendHandler() {
            
            @Override
            public void onResult(SendResult result) {
                if(callback != null) {
                    LeoObject res = callback.call(LeoObject.valueOf(result.isOK() ? null : result.getException()));
                    if(res.isError()) {                
                        throw new RuntimeException(res.toString());
                    }
                }
            }
        });
        
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((session == null) ? 0 : session.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof WebSocketSession))
            return false;
        WebSocketSession other = (WebSocketSession) obj;
        if (session == null) {
            if (other.session != null)
                return false;
        }
        else if (!session.equals(other.session))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return id();
    }
    
    
}
