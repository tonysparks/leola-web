/*
 * see license.txt
 */
package leola.web;

import java.security.SecureRandom;
import java.util.Optional;

import leola.vm.types.LeoInteger;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

/**
 * A server web-socket that delicates to the user configuration which will contain methods bound in Leola.
 * 
 * <p>
 * In the Leola script:
 * <pre>
 *   app.webSocket

    ({
        route -> "/socket",
        
        onOpen -> def(session) {
        },
        
        onClose -> def(session, reason, statusCode) {
        },
        
        onMessage -> def(session, message) {
        },
        
        onError -> def(session, exception) {
        }        
    })
 * </pre>
 * 
 * @author Tony
 */
public class LeolaWebSocket extends WebSocketAdapter {

    public static final String ON_OPEN_KEY = "onOpen";
    public static final String ON_CLOSE_KEY = "onClose";
    public static final String ON_MESSAGE_KEY = "onMessage";
    public static final String ON_ERROR_KEY = "onError";
    
    private Optional<LeoObject> onOpen;
    private Optional<LeoObject> onClose;
    private Optional<LeoObject> onMessage;
    private Optional<LeoObject> onError;
    
    private Optional<WebSocketSession> socketSession;
    private String id;
    
    /**
     * @param config
     */
    public LeolaWebSocket(LeoMap config) {
        this.onOpen = Optional.ofNullable(config.getByString(ON_OPEN_KEY));
        this.onClose = Optional.ofNullable(config.getByString(ON_CLOSE_KEY));
        this.onMessage = Optional.ofNullable(config.getByString(ON_MESSAGE_KEY));
        this.onError = Optional.ofNullable(config.getByString(ON_ERROR_KEY));
        
        this.socketSession = Optional.empty();
        this.id = Long.toHexString(new SecureRandom().nextLong());
    }

    /**
     * @return the socketSession
     */
    public Optional<WebSocketSession> getSocketSession() {
        return socketSession;
    }
    
    @Override
    public void onWebSocketConnect(Session sess) {     
        super.onWebSocketConnect(sess);
        
        this.socketSession = Optional.ofNullable(new WebSocketSession(this.id, sess));
        
        this.onOpen.ifPresent( function -> {
            LeoObject result = function.call(LeoObject.valueOf(this.socketSession.get()));
            if(result.isError()) {                
                throw new RuntimeException(result.toString());
            }
        });
    }
    
    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        this.onClose.ifPresent( function -> {
            LeoObject result = function.call(LeoObject.valueOf(this.socketSession.get()), LeoString.valueOf(reason), LeoInteger.valueOf(statusCode));
            if(result.isError()) {                
                throw new RuntimeException(result.toString());
            }
        });
    }
    
    @Override
    public void onWebSocketText(String message) {
        this.onMessage.ifPresent( function -> {
            LeoObject result = function.call(LeoObject.valueOf(this.socketSession.get()), LeoString.valueOf(message));
            if(result.isError()) {                
                throw new RuntimeException(result.toString());
            }
        });
    }
    
    @Override
    public void onWebSocketError(Throwable cause) {
        this.onError.ifPresent( function -> {
            LeoObject result = function.call(LeoObject.valueOf(this.socketSession.get()), LeoObject.valueOf(cause));
            if(result.isError()) {                
                throw new RuntimeException(result.toString());
            }
        });
    }
    
}
