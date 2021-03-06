# leola-web
An embedded web server and web framework for the Leola Programming Language.

Why should you use this?
You probably shouldn't.  There are more robust and feature complete web frameworks out there (Flask, Rails, etc.).  If you still feel like using leola-web, please read on.  I believe there are some cool features that make developing web sites and/or REST API's relatively painless and dare I say fun.

Hello World
====
A simple Hello World application.

````javascript
require("lib/web", "web")

var app = web:newWebApp()

app.route

    ({path -> "/hello"},
    def(context) {            
        return web:ok().text("Hello World")
    })

app.start()
````

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
    
    /* Listen for file edits, and reload the application (should only be enabled for development) */
    autoReload -> true,
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
        var message = context.param("message")                
        return web:ok().json({
            echo -> message
        })
    })
    
app.route

   ({path -> "/api/{team}/roster", methods -> ["GET"]},
   def(context) {
     // use the path parameters from the request, as an example: 
     //   GET http://localhost:8121/api/GreenBayPackers/roster
     var team = context.pathParam("team")
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
    
Responses
====
There are a number of ways to make an HTTP response.

````javascript
// There are a number of factory methods from the 'web' namespace that map to common used HTTP response 
// types
web:ok() // 200
web:created()
web:noContent()
web:serverError() // 500
web:status(status) // custom status

// These factory methods return a WebResponse which then allows you to construct different payloads:
var response = web:ok().json( {} ) // accepts map which gets converted to a Json string
response.cookie("name", "value" )
        .header("name", " value" )
        .characterEncoding("UTF-8")

var templateObj = {
 // add values here to be used by the template engine (mustache)
}        
response.template( "/ui/index.html", templateObj ) // return back the html file (uses mustache as a template engine)
// 

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
