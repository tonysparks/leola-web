/*
 * see license.txt
 */
package leola.web;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leola.vm.lib.LeolaMethod;
import leola.vm.types.LeoArray;
import leola.vm.types.LeoMap;
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
 * The Request context contains all the information from a http request/response cycle.
 * 
 * @author Tony
 *
 */
public class RequestContext {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private WebApp webapp;
    
    private LeoMap contents;
    private LeoMap pathParams;

    /**
     * @param request
     * @param response
     * @param webapp
     */
    public RequestContext(HttpServletRequest request, HttpServletResponse response, WebApp webapp, LeoMap pathParams) {
        this.request = request;
        this.response = response;
        this.webapp = webapp;
        this.pathParams = pathParams;
        
        this.contents = new LeoMap();
    }

    @LeolaMethod(alias="$index")
    public LeoObject get(String reference) {
        return this.contents.getByString(reference);
    }
    
    @LeolaMethod(alias="$sindex")
    public void set(String reference, LeoObject value) {
        this.contents.put(LeoString.valueOf(reference), value);
    }
    
    public HttpServletRequest request() {
        return this.request;
    }
    
    public HttpServletResponse response() {
        return this.response;
    }
    
    public LeoObject json() throws IOException {
        Gson gson = new GsonBuilder().create();        
        JsonElement element = gson.fromJson(new InputStreamReader(this.request.getInputStream()), JsonElement.class);
        return toLeoObject(element);
    }
    
    private LeoObject toLeoObject(JsonElement element) {
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
    
    public LeoObject pathParam(String name) {
        return this.pathParams.getByString(name);
    }
    
    public LeoMap pathParams() {
        return this.pathParams;
    }
        
    public LeoObject content(String name) {
        return this.contents.getByString(name);
    }
    
    public LeoMap contents() {
        return this.contents;
    }
    
    public WebApp webapp() {
        return this.webapp;
    }
}
