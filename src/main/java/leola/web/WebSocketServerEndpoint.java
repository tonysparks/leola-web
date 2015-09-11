/*
 * see license.txt
 */
package leola.web;

import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;

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
        
        onClose -> def(session, reason) {
        },
        
        onMessage -> def(session, message) {
        },
        
        onError -> def(session, exception) {
        }        
    })
 * </pre>
 * 
 * @author Tony
 *
 */
@ServerEndpoint("/socket")
public class WebSocketServerEndpoint {

    public static final String ON_OPEN_KEY = "onOpen";
    public static final String ON_CLOSE_KEY = "onClose";
    public static final String ON_MESSAGE_KEY = "onMessage";
    public static final String ON_ERROR_KEY = "onError";
    
    @OnMessage
    public void onMessage(String message, Session session) {
        Object obj = session.getUserProperties().get(ON_MESSAGE_KEY);
        if(obj instanceof LeoObject) {
            LeoObject function = (LeoObject)obj;
            LeoObject result = function.call(LeoObject.valueOf(new WebSocketSession(session)), LeoString.valueOf(message));
            if(result.isError()) {                
                throw new RuntimeException(result.toString());
            }
        }
    }
    
    @OnError
    public void onError(Throwable cause, Session session) {
        Object obj = session.getUserProperties().get(ON_ERROR_KEY);
        if(obj instanceof LeoObject) {
            LeoObject function = (LeoObject)obj;
            LeoObject result = function.call(LeoObject.valueOf(new WebSocketSession(session)), LeoObject.valueOf(cause));
            if(result.isError()) {                
                throw new RuntimeException(result.toString());
            }
        }
    }
    
    @OnOpen
    public void onOpen(Session session) throws IOException {
        Object obj = session.getUserProperties().get(ON_OPEN_KEY);
        if(obj instanceof LeoObject) {
            LeoObject function = (LeoObject)obj;
            LeoObject result = function.call(LeoObject.valueOf(new WebSocketSession(session)));
            if(result.isError()) {                
                throw new RuntimeException(result.toString());
            }
        }
    }
    
    @OnClose
    public void onClose(Session session) {
        Object obj = session.getUserProperties().get(ON_CLOSE_KEY);
        if(obj instanceof LeoObject) {
            LeoObject function = (LeoObject)obj;
            LeoObject result = function.call(LeoObject.valueOf(new WebSocketSession(session)));
            if(result.isError()) {                
                throw new RuntimeException(result.toString());
            }
        }
    } 
   
}
