/*
 * see license.txt
 */
package leola.web;


/**
 * HTTP status codes
 * 
 * @author Tony
 *
 */
public enum HttpStatus {

    /**
     * 200 OK, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.1">HTTP/1.1 documentation</a>}.
     */
    OK(200, "OK"),
    /**
     * 201 Created, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.2">HTTP/1.1 documentation</a>}.
     */
    CREATED(201, "Created"),
    /**
     * 202 Accepted, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.3">HTTP/1.1 documentation</a>}.
     */
    ACCEPTED(202, "Accepted"),
    /**
     * 204 No Content, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.5">HTTP/1.1 documentation</a>}.
     */
    NO_CONTENT(204, "No Content"),
    /**
     * 205 Reset Content, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.6">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    RESET_CONTENT(205, "Reset Content"),
    /**
     * 206 Reset Content, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.7">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    PARTIAL_CONTENT(206, "Partial Content"),
    /**
     * 301 Moved Permanently, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.2">HTTP/1.1 documentation</a>}.
     */
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    /**
     * 302 Found, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.3">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    FOUND(302, "Found"),
    /**
     * 303 See Other, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.4">HTTP/1.1 documentation</a>}.
     */
    SEE_OTHER(303, "See Other"),
    /**
     * 304 Not Modified, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.5">HTTP/1.1 documentation</a>}.
     */
    NOT_MODIFIED(304, "Not Modified"),
    /**
     * 305 Use Proxy, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.6">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    USE_PROXY(305, "Use Proxy"),
    /**
     * 307 Temporary Redirect, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.8">HTTP/1.1 documentation</a>}.
     */
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    /**
     * 400 Bad Request, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.1">HTTP/1.1 documentation</a>}.
     */
    BAD_REQUEST(400, "Bad Request"),
    /**
     * 401 Unauthorized, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.2">HTTP/1.1 documentation</a>}.
     */
    UNAUTHORIZED(401, "Unauthorized"),
    /**
     * 402 Payment Required, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.3">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    PAYMENT_REQUIRED(402, "Payment Required"),
    /**
     * 403 Forbidden, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.4">HTTP/1.1 documentation</a>}.
     */
    FORBIDDEN(403, "Forbidden"),
    /**
     * 404 Not Found, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.5">HTTP/1.1 documentation</a>}.
     */
    NOT_FOUND(404, "Not Found"),
    /**
     * 405 Method Not Allowed, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.6">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    /**
     * 406 Not Acceptable, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.7">HTTP/1.1 documentation</a>}.
     */
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    /**
     * 407 Proxy Authentication Required, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.8">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    /**
     * 408 Request Timeout, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.9">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    REQUEST_TIMEOUT(408, "Request Timeout"),
    /**
     * 409 Conflict, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.10">HTTP/1.1 documentation</a>}.
     */
    CONFLICT(409, "Conflict"),
    /**
     * 410 Gone, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.11">HTTP/1.1 documentation</a>}.
     */
    GONE(410, "Gone"),
    /**
     * 411 Length Required, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.12">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    LENGTH_REQUIRED(411, "Length Required"),
    /**
     * 412 Precondition Failed, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.13">HTTP/1.1 documentation</a>}.
     */
    PRECONDITION_FAILED(412, "Precondition Failed"),
    /**
     * 413 Request Entity Too Large, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.14">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    /**
     * 414 Request-URI Too Long, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.15">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
    /**
     * 415 Unsupported Media Type, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.16">HTTP/1.1 documentation</a>}.
     */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    /**
     * 416 Requested Range Not Satisfiable, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.17">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
    /**
     * 417 Expectation Failed, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.18">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    EXPECTATION_FAILED(417, "Expectation Failed"),
    /**
     * 500 Internal Server Error, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.1">HTTP/1.1 documentation</a>}.
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    /**
     * 501 Not Implemented, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.2">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    NOT_IMPLEMENTED(501, "Not Implemented"),
    /**
     * 502 Bad Gateway, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.3">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    BAD_GATEWAY(502, "Bad Gateway"),
    /**
     * 503 Service Unavailable, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.4">HTTP/1.1 documentation</a>}.
     */
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    /**
     * 504 Gateway Timeout, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.5">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    /**
     * 505 HTTP Version Not Supported, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.6">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
    
    ;
    


    /**
     * An enumeration representing the class of status code. Family is used
     * here since class is overloaded in Java.
     */
    public enum Family {

        /**
         * {@code 1xx} HTTP status codes.
         */
        INFORMATIONAL,
        /**
         * {@code 2xx} HTTP status codes.
         */
        SUCCESSFUL,
        /**
         * {@code 3xx} HTTP status codes.
         */
        REDIRECTION,
        /**
         * {@code 4xx} HTTP status codes.
         */
        CLIENT_ERROR,
        /**
         * {@code 5xx} HTTP status codes.
         */
        SERVER_ERROR,
        /**
         * Other, unrecognized HTTP status codes.
         */
        OTHER;

        /**
         * Get the response status family for the status code.
         *
         * @param statusCode response status code to get the family for.
         * @return family of the response status code.
         */
        public static Family familyOf(final int statusCode) {
            switch (statusCode / 100) {
                case 1:
                    return Family.INFORMATIONAL;
                case 2:
                    return Family.SUCCESSFUL;
                case 3:
                    return Family.REDIRECTION;
                case 4:
                    return Family.CLIENT_ERROR;
                case 5:
                    return Family.SERVER_ERROR;
                default:
                    return Family.OTHER;
            }
        }
    }

    
    private final int statusCode;
    private final String text;
    private final Family family;    

    /**
     * @param code the status code
     * @param text the display text
     */
    private HttpStatus(int code, String text) {
        this.statusCode = code;
        this.text = text;
        this.family = Family.familyOf(code);
    }
    
    /**
     * @return true if status code above 399
     */
    public boolean isError() {
        return this.statusCode > 399;
    }
    
    /**
     * @return the family
     */
    public Family getFamily() {
        return family;
    }
    
    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }
    
    /**
     * @return the text
     */
    public String getText() {
        return text;
    }
}
