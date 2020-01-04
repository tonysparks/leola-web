/*
 * see license.txt
 */
package leola.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;

import leola.vm.Leola;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.lib.LeolaMethod;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoClass;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Library for creating web applications
 * 
 * @author Tony
 *
 */
public class WebLeolaLibrary implements LeolaLibrary {
    private static Gson gson = new GsonBuilder().create();
    private Leola runtime;
    
    
    /* (non-Javadoc)
     * @see leola.vm.lib.LeolaLibrary#init(leola.vm.Leola, leola.vm.types.LeoNamespace)
     */
    @Override
    public void init(Leola leola, LeoNamespace namespace) throws LeolaRuntimeException {
        this.runtime = leola;
        leola.putIntoNamespace(this, namespace);
        
    }

    
    /**
     * constructs a new {@link WebApp} that contains an embedded web server that allows
     * routes to be bound to {@link LeoObject} functions.
     * 
     * @return the {@link WebApp}
     */
    public WebApp newWebApp(LeoMap config) {
        WebApp app = new WebApp(this.runtime, config);
        return app;
    }
    
    
    public WebResponse ok() {
        return new WebResponse(HttpStatus.OK);
    }
    public WebResponse noContent() {
        return new WebResponse(HttpStatus.NO_CONTENT);
    }
    public WebResponse created() {
        return new WebResponse(HttpStatus.CREATED);
    }
    public WebResponse accepted() {
        return new WebResponse(HttpStatus.ACCEPTED);
    }
    
    public WebResponse notModified() {
        return new WebResponse(HttpStatus.NOT_MODIFIED);
    }
    public WebResponse notFound() {
        return new WebResponse(HttpStatus.NOT_FOUND);
    }
    public WebResponse notAcceptable() {
        return new WebResponse(HttpStatus.NOT_ACCEPTABLE);
    }
    public WebResponse unauthorized() {
        return new WebResponse(HttpStatus.UNAUTHORIZED);
    }
    public WebResponse badRequest() {
        return new WebResponse(HttpStatus.BAD_REQUEST);
    }
    public WebResponse forbidden() {
        return new WebResponse(HttpStatus.FORBIDDEN);
    }    
    
    
    public WebResponse serverError() {
        return new WebResponse(HttpStatus.INTERNAL_SERVER_ERROR);
    }
 
    /**
     * Issue a redirect
     * 
     * @param url
     * @return the {@link WebResponse} configured for a redirect
     */
    public WebResponse redirect(String url) {
        return new WebResponse(HttpStatus.MOVED_PERMANENTLY).redirect(url);
    }
    
    /**
     * Constructs a {@link WebResponse} with the supplied status HTTP code.
     * 
     * @param status the HTTP status code
     * @return the {@link WebResponse}
     */
    public WebResponse status(int status) {
        return new WebResponse(status);
    }
    
    /**
     * Converts the supplied {@link LeoObject} into a JSON {@link String}
     * 
     * @param obj
     * @return the JSON string
     */
    public static String toJson(LeoObject obj) {           
        return gson.toJson(toJsonElement(obj));    
    }
    
    /**
     * Converts the supplied JSON {@link String} into a {@link LeoObject}
     * 
     * @param message
     * @return the {@link LeoObject}
     */
    @LeolaMethod(alias="fromJson")
    public static LeoObject fromJson(String message) {
        JsonElement element = gson.fromJson(message, JsonElement.class);
        return toLeoObject(element);
    }
    
    @LeolaIgnore
    public static LeoObject fromJson(InputStream iStream) throws IOException {
        JsonElement element = gson.fromJson(new InputStreamReader(iStream), JsonElement.class);
        return toLeoObject(element);
    }
    
    
    private static JsonElement toJsonElement(LeoObject obj) {
        if(LeoObject.isNull(obj)) {
            return JsonNull.INSTANCE;
        }
        
        if(obj.isArray()) {
            JsonArray array = new JsonArray();
            LeoArray leoArray = obj.as();
            for(int i = 0; i < leoArray.size(); i++) {
                array.add(toJsonElement(leoArray.get(i)));
            }
            
            return array;
        }
        
        if(obj.isMap()) {
            JsonObject object = new JsonObject();
            LeoMap leoMap = obj.as();
            for(LeoObject key : leoMap.keySet()) {
                object.add(key.toString(), toJsonElement(leoMap.get(key)));
            }
            
            return object;
        }
        
        if(obj.isClass()) {
            JsonObject object = new JsonObject();
            LeoClass leoClass = obj.as();
            for(LeoObject key : leoClass.getPropertyNames()) {
                String name = key.toString();
                if(!name.equals("this")) {
                    object.add(key.toString(), toJsonElement(leoClass.getProperty(key))); 
                }
            }
            
            return object;
        }
        
        if(obj.isString()) {
            return new JsonPrimitive(obj.toString());
        }
        
        if(obj.isNumber()) {
            return new JsonPrimitive((Number)obj.getValue());
        }
        
        if(obj.isBoolean()) {
            return new JsonPrimitive(obj.isTrue());
        }
        
        return gson.toJsonTree(obj);
    }
    
    /**
     * Converts the {@link JsonElement} into the equivalent {@link LeoObject}
     * 
     * @param element
     * @return the {@link LeoObject}
     */
    private static LeoObject toLeoObject(JsonElement element) {
        if(element==null||element.isJsonNull()) {
            return LeoObject.NULL;
        }
        
        if(element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            LeoArray leoArray = new LeoArray(array.size());
            array.forEach(e -> leoArray.add(toLeoObject(e)));
            return leoArray;
        }
        
        if(element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            LeoMap leoMap = new LeoMap();
            object.entrySet().forEach( entry -> {
                leoMap.putByString(entry.getKey(), toLeoObject(entry.getValue()));
            });
            
            return leoMap;
        }
        
        if(element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if(primitive.isBoolean()) {
                return LeoObject.valueOf(primitive.getAsBoolean());
            }
            
            if(primitive.isNumber()) {
                return LeoObject.valueOf(primitive.getAsDouble());
            }
            
            if(primitive.isString()) {
                return LeoString.valueOf(primitive.getAsString());
            }
        }
        
        return LeoObject.NULL;
    }
    
    /**
     * @return the host name of the server this application is running on
     */
    public String hostname() {
        try { 
            return InetAddress.getLocalHost().getHostName(); 
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}



