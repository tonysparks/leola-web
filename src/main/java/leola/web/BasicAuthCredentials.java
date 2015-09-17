/*
 * see license.txt
 */
package leola.web;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Optional;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import leola.vm.lib.LeolaMethod;

/**
 * HTTP Basic Authentication credentials.
 * 
 * @author Tony
 *
 */
public class BasicAuthCredentials {

    private String username;
    private String password;
    
    /**
     * @param username
     * @param password
     */
    public BasicAuthCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    /**
     * @return the Basic Auth Username
     */
    @LeolaMethod(alias="username")
    public String getUsername() {
        return this.username;
    }
    
    /**
     * @return the Basic Auth Password
     */
    @LeolaMethod(alias="password")
    public String getPassword() {
        return this.password;
    }

    /**
     * Attempts to construct a {@link BasicAuthCredentials} credentials
     * @param request
     * @return
     */
    public static Optional<BasicAuthCredentials> fromRequest(HttpServletRequest request) {
        Optional<BasicAuthCredentials> authResult = Optional.empty();
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();

                if (basic.equalsIgnoreCase("Basic")) {
                    try {
                        String credentials = new String(Base64.getDecoder().decode(st.nextToken()), "UTF-8");

                        int p = credentials.indexOf(":");
                        if (p != -1) {
                            String _username = credentials.substring(0, p).trim();
                            String _password = credentials.substring(p + 1).trim();

                            authResult = Optional.of(new BasicAuthCredentials(_username, _password));
                        }                        
                    }
                    catch (UnsupportedEncodingException e) {
                        throw new Error("Couldn't retrieve authentication", e);
                    }
                }
            }
        }
        
        return authResult;
    }
}
