# leola-web
An embedded web server and web framework for the Leola Programming Language.

Why should you use this?
You probably shouldn't.  There are more robust and feature complete web frameworks out there (Flask, Rails, etc.).  If you still feel like using leola-web, please read on.  I believe there are some cool features that make developing web sites and/or REST API's relatively painless and dare I say fun.

Creating the Application
====
Create a WebApp that allows you to bind HTTP request URL's (i.e., Routes) to functions.

````javascript
require("lib/web", "web" )  // path to the leola-web.jar

var app = web:newWebApp({

    /* The directory in which this application is installed, more percisely, where the
    application should look for the CSS, JS and HTML files */
    resourceBase -> "./",
    
    /* The application context of the REST calls, i.e., http://localhost/[context]/ */
    context -> "api",
    
    /* The port that this server will use */
    port -> 8121,
})
````

Binding Routes
====
All leola-web really does is start an embedded web server (Jetty) and allows you to bind a Leola function to HTTP requests (i.e, a Route).  There are two things to consider when binding a Route, 1) the URL path 2) the method type it accepts.

````javascript
app.route

    ({path -> "/api/echo", methods -> ["GET"]},
    def(context) {            
        // this Route expects a parameter of 'message'
        var message = context.request().getParameter("message")                
        return web:ok().json({
            echo -> message
        })
    })
    
app.route

   ({path -> "/api/{team}/roster", methods -> ["GET"]},
   def(context) {
     // use the path parameters from the request, as an example: 
     //   GET http://localhost:8121/api/GreenBayPackers/roster
     var team = context.pathParams("team")
     if team == "GreenBayPackers" {
        return web:ok().json({
           roster -> [
              { name -> "Brett Favre", position -> "QB" },
              { name -> "Donald Driver", position -> "WR" }
           ]
        })
     }
     else {
        return web:notAcceptable().json({
           message -> "No other team matters"
        })
     }
   })
````    
    
When things go wrong
====
When an exception occurs, you can bind a Leola function and handle it by returning a custom response back to the client.

````javascript
app.errorHandler

    (def(context, error) {
      return web:serverError().json({
            "error" -> toString(error),
            "message" -> "Sorry, there was an internal error"
      })  
    })
````    

Web Sockets
====
Web Sockets are pretty hip, leola-web has support for those too.  Create a set of callback functions when a WebSocket is initiated.

````javascript    
app.webSocket

    ({
        // the path to the socket (ws://localhost:8121/api/testSocket)
        route -> "/testSocket",
        
        onOpen -> def(session) {
        },
        
        onClose -> def(session, reason) {
        },
        
        onMessage -> def(session, message) {            
        },
        
        onError -> def(session, exception) {
        }        
    })
````

Starting the Web Server
====
Finally, start the web server.  This will block, so it should be the last statement of your script.

````javascript
app.start()    
````
