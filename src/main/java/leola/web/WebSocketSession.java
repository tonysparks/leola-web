/*
 * see license.txt
 */
package leola.web;



import leola.vm.Leola;
import leola.vm.types.LeoObject;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;

/**
 * A thin wrapper around the {@link Session}.  This just makes integration with {@link Leola} more convenient.
 * 
 * @author Tony
 *
 */
public class WebSocketSession {

    private Session session;
    private String id;
    
    /**
     * @param session
     */
    public WebSocketSession(String id, Session session) {
        this.id = id;
        this.session = session;        
    }

    public void setMaxIdleTimeoutMSec(long timeout) {
        this.session.setIdleTimeout(timeout);
    }
    
    public Session getSession() {
        return this.session;
    }
    
    public boolean isOpen() {
        return this.session.isOpen();
    }
    
    public String id() {
        return this.id;
    }
    
    public WebSocketSession close() throws Exception {
        this.session.close();
        return this;
    }
    
    public WebSocketSession send(String message) throws Exception {
        RemoteEndpoint client = this.session.getRemote();
        client.sendString(message);
        return this;
    }
    
    public WebSocketSession asyncSend(String message, final LeoObject callback) throws Exception {        
        RemoteEndpoint client = this.session.getRemote();
        client.sendString(message, new WriteCallback() {
            
            @Override
            public void writeSuccess() {
                if(callback != null) {
                    LeoObject res = callback.call(LeoObject.NULL);
                    if(res.isError()) {                
                        throw new RuntimeException(res.toString());
                    }
                }
            }
            
            @Override
            public void writeFailed(Throwable x) {
                if(callback != null) {
                    LeoObject res = callback.call(LeoObject.valueOf(x));
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
