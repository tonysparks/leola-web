/*
 * see license.txt
 */
package leola.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.core.Response.Status;

import leola.vm.Leola;
import leola.vm.lib.LeolaIgnore;
import leola.vm.lib.LeolaLibrary;
import leola.vm.lib.LeolaMethod;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoMap;
import leola.vm.types.LeoNamespace;
import leola.vm.types.LeoNull;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoString;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Library for creating web applications
 * 
 * @author Tony
 *
 */
public class WebLeolaLibrary implements LeolaLibrary {
    
    private Leola runtime;
    
    /* (non-Javadoc)
     * @see leola.vm.lib.LeolaLibrary#init(leola.vm.Leola, leola.vm.types.LeoNamespace)
     */
    @Override
    public void init(Leola leola, LeoNamespace namespace) throws Exception {
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
        return new WebResponse(Status.OK);
    }
    public WebResponse noContent() {
        return new WebResponse(Status.NO_CONTENT);
    }
    public WebResponse created() {
        return new WebResponse(Status.CREATED);
    }
    public WebResponse accepted() {
        return new WebResponse(Status.ACCEPTED);
    }
    
    public WebResponse notModified() {
        return new WebResponse(Status.NOT_MODIFIED);
    }
    public WebResponse notFound() {
        return new WebResponse(Status.NOT_FOUND);
    }
    public WebResponse notAcceptable() {
        return new WebResponse(Status.NOT_ACCEPTABLE);
    }
    public WebResponse unauthorized() {
        return new WebResponse(Status.UNAUTHORIZED);
    }
    public WebResponse badRequest() {
        return new WebResponse(Status.BAD_REQUEST);
    }
    public WebResponse forbidden() {
        return new WebResponse(Status.FORBIDDEN);
    }    
    
    
    public WebResponse serverError() {
        return new WebResponse(Status.INTERNAL_SERVER_ERROR);
    }
 
    /**
     * Issue a redirect
     * 
     * @param url
     * @return the {@link WebResponse} configured for a redirect
     */
    public WebResponse redirect(String url) {
        return new WebResponse(Status.MOVED_PERMANENTLY).redirect(url);
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
        Gson gson = new GsonBuilder().create();        
        return gson.toJson(obj);        
    }
    
    /**
     * Converts the supplied JSON {@link String} into a {@link LeoObject}
     * 
     * @param message
     * @return the {@link LeoObject}
     */
    @LeolaMethod(alias="fromJson")
    public static LeoObject fromJson(String message) {
        Gson gson = new GsonBuilder().create();        
        JsonElement element = gson.fromJson(message, JsonElement.class);
        return toLeoObject(element);
    }
    
    @LeolaIgnore
    public static LeoObject fromJson(InputStream iStream) throws IOException {
        Gson gson = new GsonBuilder().create();        
        JsonElement element = gson.fromJson(new InputStreamReader(iStream), JsonElement.class);
        return toLeoObject(element);
    }
    
    private static LeoObject toLeoObject(JsonElement element) {
        if(element==null||element.isJsonNull()) {
            return LeoNull.LEONULL;
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
        
        return LeoNull.LEONULL;
    }
}



