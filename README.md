

![Java Express Logo](https://image.ibb.co/mCdxtm/java_express.png)


[![License MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://choosealicense.com/licenses/mit/)

# Getting Started
**This project is currently in progress, feel free to [contribute](https://github.com/Simonwep/java-express/graphs/contributors) / [report](https://github.com/Simonwep/java-express/issues) issues! :)**

```java
Express app  = new Express();

app.get("/", (req, res) -> {
   res.send("Hello World");
});

app.listen(); // Port 80 is default
```

When you create an new Express instance you can add an additional host name for example, your local network:
```java
// Will bind the server to your ip-adress
Express app  = new Express(Utils.getYourIp());
```
Default is localhost, so you can access, without setting the hostname, only from your local pc.

Docs:
* [Routing](#routing)
	* [Direct](#direct)
	* [With Router](#with-router)
* [URL Basics](#url-basics)
   * [URL Parameter](#url-parameter)
   * [URL Parameter Listener](#url-parameter-listener)
   * [URL Querys](#url-querys)
   * [Cookies](#cookies)
   * [Form Data](#form-data)
* [HTTP - Request and Response](#http---request-and-response-object)
   * [Response Object](#response-object)
   * [Request Object](#request-object)
* [Middleware](#middleware)
   * [Create own middleware](#create-own-middleware)
* [License](#license)

Every following code can be also found in [this package](https://github.com/Simonwep/java-express/tree/master/src/examples).

# Routing
## Direct
You can add routes (And middlewares) directly to the Express object to handle requests:
```java
Express app = new Express();

// Sample for home routes
app.get("/", (req, res) -> res.send("Hello index!"));
app.get("/home", (req, res) -> res.send("Homepage"));
app.get("/about", (req, res) -> res.send("About"));

// Sample for user
app.get("/user/login", (req, res) -> res.send("Please login!"));
app.get("/user/register", (req, res) -> res.send("Join now!"));

app.listen(); 
```
It also directly supports directly methods like `POST` `PATCH` `DELETE` and `PUT` others need to be created manually:
```java
Express app = new Express();

// Basic methods
app.get("/user", (req, res) -> res.send("Get an user!"));
app.patch("/user", (req, res) -> res.send("Modify an user!"));
app.delete("/user", (req, res) -> res.send("Delete an user!"));
app.put("/user", (req, res) -> res.send("Add an user!"));

// Example fot the CONNECT method
app.on("/user", "CONNECT", (req, res) -> res.send("Connect!"));

app.listen(); 
```

## With Router
But it's better to split your code, right? With the `ExpressRouter` you can create routes and add it later to the `Express` object:
```java
Express app = new Express();

// Define router for index sites
ExpressRouter indexRouter = new ExpressRouter();
indexRouter.get("/", (req, res) -> res.send("Hello World!"));
indexRouter.get("/index", (req, res) -> res.send("Index"));
indexRouter.get("/about", (req, res) -> res.send("About"));

// Define router for user pages
ExpressRouter userRouter = new ExpressRouter();
userRouter.get("/user/login", (req, res) -> res.send("User Login"));
userRouter.get("/user/register", (req, res) -> res.send("User Register"));
userRouter.get("/user/:username", (req, res) -> res.send("You want to see: " + req.getParam("username")));

// Add roter
app.use(indexRouter);
app.use(userRouter);

app.listen();
```

# URL Basics
Over the express object you can create handler for all [request-methods](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods) and contexts. Some examples:
```java
app.get("/home", (req, res) -> {
	// Will match every request which uses the 'GET' method and matches the '/home' path
});

app.post("/login", (req, res) -> {
	// Will match every request which uses the 'POST' method and matches the /login' path
});
```

## URL Parameter
Sometime you want to create dynamic URL where some parts of the URL's are not static.

Example request: `GET`  `/posts/john/all`:
```java
app.get("/posts/:user/:description", (req, res) -> {
   String user = req.getParam("user"); // Contains 'john'
   String description = req.getParam("description"); // Contains 'all'
   res.send("User: " + user + ", description: " + description); // Send: "User: john, description: all"
});
```

### URL Parameter Listener
You can also add an event listener when the user called an route which contains an certain parameter:
```java
app.get("/posts/:user/:id", (req, res) -> {
  // Code
});
```
For example, if we want to check every `id` before the associated get post etc. handler will be fired, we can use the `app.onParam([PARAM])` function:
```java
app.onParam("id", (req, res) -> {
  // Do something with the id parameter, eg. check if it's valid.
});
```
Now, this function will be called every time when an context is requested which contains the `id` parameter placeholder.

## URL Querys
If you make an request which contains querys, you can access the querys over `req.getQuery(NAME)`. 

Example request: `GET`  `/posts?page=12&from=john`:
```java
app.get("/posts", (req, res) -> {
   String page = req.getQuery("page"); // Contains '12'
   String from = req.getQuery("from"); // Contains 'John'
   res.send("Page: " + page + ", from: " + from); // Send: "Page: 12, from: John"
});
```

## Cookies
With `req.getCookie(NAME)` you can get an cookie by his name, and with `res.setCookie(NAME, VALUE)` you can easily set an cookie.

Example request: `GET`  `/setcookie`:
```java
app.get("/setcookie", (req, res) -> {
   Cookie cookie = new Cookie("username", "john");
   res.setCookie(cookie);
   res.send("Cookie has been set!");
});
```

Example request: `GET`  `/showcookie`:
```java
app.get("/showcookie", (req, res) -> {
   Cookie cookie = req.getCookie("username");
   String username = cookie.getValue();
   res.send("The username is: " + username); // Prints "The username is: john"
});
```

## Form data
Over `req.getFormQuery(NAME)` you receive the values from the input elements of an HTML-Form.
Example HTML-Form:
```html
<form action="http://localhost/register" method="post">
   <input description="text" name="email" placeholder="Your E-Mail">
   <input description="text" name="username" placeholder="Your username">
   <input description="submit">
</form>
```
**Attention: Currently, File-inputs don't work, if there is an File-input the data won't get parsed!**
Now description, for the example below, `john` in username and `john@gmail.com` in the email field.
Java code to handle the post request and access the form elements:
```java
app.post("/register", (req, res) -> {
  String email = req.getFormQuery("email");
  String username = req.getFormQuery("username");
  // Process data
   
  // Prints "E-Mail: john@gmail.com, Username: john"
  res.send("E-Mail: " + email + ", Username: " + username); 
});
```

# HTTP - Request and Response object

## Response Object
Over the response object, you have serveral possibility like setting cookies, send an file and more. Below is an short explanation what methods exists:
```java
app.get("/res", (req, res) -> {
  // res.send();                     // Send empty response
  // res.send("Hello World");        // Send an string
  // res.send("chart.pdf");          // Send an file
  // setContentType(MediaType._txt); // Set the content type, default is txt/plain
  // getContentType();               // Returns the current content type
  // res.setStatus(Status._200);     // Set the response status
  // res.getStatus();                // Returns the current response status
  // res.setCookie(new Cookie(...)); // Send an cookie
  // res.isClosed();                 // Check if already something has been send to the client
});
```
The response object calls are comments because **you can only call the .send(xy) once each request!**

## Request Object
With the request object you receive serveral data from the client which can be easily parsed by the given functions:
```java
app.get("/req/", (req, res) -> {
   // req.getURI();                        // Request URI
   // req.getHost();                       // Request host (mostly localhost)
   // req.getMethod();                     // Request method (here GET)
   // req.getContentType();                // Request content description, is here null because it's an GET request
   // req.getBody();                       // Request body inputstream
   // req.getUserAgent();                  // Request user-agent
   // req.getParam("parameter");           // Returns an url parameter
   // req.getQuery("queryname");           // Returns an url query by key
   // req.getFormQuery("formqueryname");   // Returns an form input value
   // req.getFormQuerys();                 // Returns all form querys
   // req.getCookie("user");               // Returns an cookie by name
   // req.getCookies();                    // Returns all cookies
   // req.hasAuthorization();              // Check if the request contains an authorization header
   // req.getAuthorization();              // Returns the authorization header
   // req.getMiddlewareContent("name");    // Returns data from middleware
   // req.pipe(new OutputStream() {...});  // Pipe the body to an outputstream
});
```

# Middleware
Middleware are one of the most important functions of JavaExpress, with middleware you can handle a request before it reaches any request handler. To create an own middleware you have serveral interfaces:
* `HttpRequest`  - Is **required** to handle an request.
* `ExpressFilter` - Is **required** to put data on the request listener.
* `ExpressFilterTask` - Can be used for middleware which needs an background thread.

Middlewares work, for you, exact same as request handler.
For example an middleware for all [request-methods](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods) and contexts:

```java
// Global context, matches every request.
app.use((req, res) -> {
   // Handle data
});
```
You can also filter by [request-methods](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods) and contexts:
```java
// Global context, you can also pass an context if you want
app.use("/home", "POST", (req, res) -> {
   // Handle request by context '/home' and method 'POST'
});
```
In addition to that yo can use `*` which stands for every **context** or **request method**:
```java
// Global context, you can also pass an context if you want
app.use("/home", "*", (req, res) -> {
   // Handle request which matches the context '/home' and all methods.
});
```
## Create own middleware

Now we take a look how we can create own middlewares. Here we create an simple PortParser which parse / extract the port-number for us:
```java
public class PortMiddleware implements HttpRequest, ExpressFilter {

   /**
    * From interface HttpRequest, to handle the request.
    */
   @Override
   public void handle(Request req, Response res) {
      
      // Get the port
      int port = req.getURI().getPort();
      
      // Add the port to the request middleware map
      req.addMiddlewareContent(this, port);

      /**
       * After that you can use this middleware by call:
       *   app.use(new PortMiddleware());
       *   
       * Than you can get the port with:
       *   int port = (Integer) app.getMiddlewareContent("PortParser");
       */
   }

   /**
    * Defines the middleware.
    *
    * @return The middleware name.
    */
   @Override
   public String getName() {
      return "PortParser";
   }
}
```
No we can, as we learned above, include it with:
```java
// Global context, you can also pass an context if you want
app.use(new PortMiddleware());
```
## Existing Middlewares
There are already some basic middlewares included, you can access these via static methods provided from `Express`.

#### Static File serving
If you want to allocate some files, like js-librarys or css files you can use the [static](https://github.com/Simonwep/java-express/blob/master/src/express/middleware/Static.java) middleware. But you can also provide other files like mp4 etc.
Example:
```java
 app.use(Express.statics("examplepath\\test_statics"));
```
Now you can access every files in the `test_statics` over the root adress `\`. If you want you can specify which files can be accessed:
```java
 app.use(Express.statics("examplepath\\test_statics", "html", "css", "js"));
```
#### Cookie Session
Java Express also includes an simple cookie-session middleware:
```java
// You should use an meaningless cookie name for serveral security reasons, here f3v4.
// Also you can specify the maximum age of the cookie from the creation date and the file types wich are actually allowed.
app.use(Express.cookieSession("f3v4", 9000));
```
To use a session cookie we need to get the data from the middleware which is actually an `SessionCookie`:
```java
 // Cookie session example
app.get("/session", (req, res) -> {

   /**
   * CookieSession named his data "Session Cookie" which is
   * an SessionCookie so we can Cast it.
   */
   SessionCookie sessionCookie = (SessionCookie) req.getMiddlewareContent("sessioncookie");
   int count;
   
   // Check if the data is null, we want to implement an simple counter
   if (sessionCookie.getData() == null) {
   
      // Set the default data to 1 (first request with this session cookie)
      count = (Integer) sessionCookie.setData(1);
   
   } else {
      // Now we know that the cookie has an integer as data property, increase it
      count = (Integer) sessionCookie.setData((Integer) sessionCookie.getData() + 1);
   }

   // Send an info message
   res.send("You take use of your session cookie " + count + " times.");
});
```

# License

This project is licensed under the MIT License - see the [LICENSE.md](https://choosealicense.com/licenses/mit/) file for details