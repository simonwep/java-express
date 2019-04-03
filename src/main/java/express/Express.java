package express;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import express.filter.FilterImpl;
import express.filter.FilterLayerHandler;
import express.filter.FilterTask;
import express.filter.FilterWorker;
import express.http.HttpRequestHandler;
import express.http.request.Request;
import express.http.response.Response;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Simon Reinisch
 * Core class of java-express
 */
public class Express implements Router {

    private final ConcurrentHashMap<String, HttpRequestHandler> parameterListener;
    private final ConcurrentHashMap<Object, Object> locals;

    private final ArrayList<FilterWorker> worker;
    private final FilterLayerHandler handler;

    private Executor executor;
    private String hostname;
    private HttpServer httpServer;
    private HttpsConfigurator httpsConfigurator;

    {
        // Initialize
        parameterListener = new ConcurrentHashMap<>();
        locals = new ConcurrentHashMap<>();

        worker = new ArrayList<>();
        handler = new FilterLayerHandler(2);

        executor = Executors.newCachedThreadPool();
    }

    /**
     * Create an express instance and bind the server to an hostname.
     * Default is "Localhost"
     *
     * @param hostname The host name
     */
    public Express(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Default, will bind the server to "localhost"
     *
     * @param httpsConfigurator The HttpsConfigurator for https
     */
    public Express(HttpsConfigurator httpsConfigurator) {
        this.httpsConfigurator = httpsConfigurator;
    }

    /**
     * Create an express instance and bind the server to an hostname.
     * Default is "Localhost"
     *
     * @param hostname          The host name
     * @param httpsConfigurator The HttpsConfigurator for https
     */
    public Express(String hostname, HttpsConfigurator httpsConfigurator) {
        this.hostname = hostname;
        this.httpsConfigurator = httpsConfigurator;
    }

    /**
     * Default, will bind the server to "localhost"
     */
    public Express() {
    }

    /**
     * @return True if the server uses https.
     */
    public boolean isSecure() {
        return httpsConfigurator != null;
    }

    /**
     * Add a listener which will be called when an url with this parameter is called.
     *
     * @param param   The parameter name.
     * @param request An request handler.
     * @return Express this express instance
     */
    public Express onParam(String param, HttpRequestHandler request) {
        parameterListener.put(param, request);
        return this;
    }

    public ConcurrentHashMap<String, HttpRequestHandler> getParameterListener() {
        return parameterListener;
    }

    /**
     * Add an key-val pair to the express app, can be used
     * to store data. Uses ConcurrentHashMap so it's thread save.
     *
     * @param key The key
     * @param val The value
     * @return The last value which was attached by this key, can be null.
     */
    public Object set(String key, String val) {
        return locals.put(key, val);
    }

    /**
     * Returns the value which was allocated by this key.
     *
     * @param key The key.
     * @return The value.
     */
    public Object get(String key) {
        return locals.get(key);
    }

    /**
     * Set an executor service. Default is CachedThreadPool
     * Can only changed if the server isn't already stardet.
     *
     * @param executor The new executor.
     * @throws IOException If the server is currently running
     */
    public void setExecutor(Executor executor) throws IOException {
        if (httpServer != null) {
            throw new IOException("Cannot set executor after the server has stardet!");
        } else {
            this.executor = executor;
        }
    }

    /**
     * Add an routing object.
     *
     * @param router The router.
     * @return Express this express instance
     */
    public Express use(ExpressRouter router) {
        this.handler.combine(router.getHandler());
        this.worker.addAll(router.getWorker());
        return this;
    }

    /**
     * Add an routing object with an specific root root.
     *
     * @param root   The root path for all request to this router.
     * @param router The router.
     * @return Express this express instance
     */
    @SuppressWarnings("unchecked")
    public Express use(String root, ExpressRouter router) {

        router.getHandler().forEach(fl -> fl.getFilter().forEach(layer -> {
            ((FilterImpl) layer).setRoot(root);
        }));

        this.handler.combine(router.getHandler());
        this.worker.addAll(router.getWorker());

        return this;
    }

    public Express use(HttpRequestHandler middleware) {
        addMiddleware("*", "*", middleware);
        return this;
    }

    public Express use(String context, HttpRequestHandler middleware) {
        addMiddleware("*", context, middleware);
        return this;
    }

    public Express use(String context, String requestMethod, HttpRequestHandler middleware) {
        addMiddleware(requestMethod.toUpperCase(), context, middleware);
        return this;
    }

    // Internal service to handle middleware
    private void addMiddleware(String requestMethod, String context, HttpRequestHandler middleware) {
        if (middleware instanceof FilterTask) {
            worker.add(new FilterWorker((FilterTask) middleware));
        }

        handler.add(0, new FilterImpl(requestMethod, context, middleware));
    }

    public Express all(HttpRequestHandler request) {
        handler.add(1, new FilterImpl("*", "*", request));
        return this;
    }

    public Express all(String context, HttpRequestHandler request) {
        handler.add(1, new FilterImpl("*", context, request));
        return this;
    }

    public Express all(String context, String requestMethod, HttpRequestHandler request) {
        handler.add(1, new FilterImpl(requestMethod, context, request));
        return this;
    }

    public Express get(String context, HttpRequestHandler request) {
        handler.add(1, new FilterImpl("GET", context, request));
        return this;
    }

    public Express post(String context, HttpRequestHandler request) {
        handler.add(1, new FilterImpl("POST", context, request));
        return this;
    }

    public Express put(String context, HttpRequestHandler request) {
        handler.add(1, new FilterImpl("PUT", context, request));
        return this;
    }

    public Express delete(String context, HttpRequestHandler request) {
        handler.add(1, new FilterImpl("DELETE", context, request));
        return this;
    }

    public Express patch(String context, HttpRequestHandler request) {
        handler.add(1, new FilterImpl("PATCH", context, request));
        return this;
    }

    /**
     * Binds a or multiple objects with request-handler methods on this express instance.
     * With the use of the DynExpress Annotation handler can be declared with a annotation
     * on a compatible method which has as parameter a Request and Response object.
     *
     * @param objects Object with proper request handler methods.
     * @return This express instance.
     */
    public Express bind(Object... objects) {
        for (Object o : objects) {

            // Skip null objects
            if (o == null) {
                continue;
            }

            // Find methods wich has the DynExpress annotation
            Method[] methods = o.getClass().getDeclaredMethods();
            for (Method method : methods) {

                // Skip if annotation is not present
                if (!method.isAnnotationPresent(DynExpress.class)) {
                    continue;
                }

                // Make private method accessible
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                // Validate parameter types
                Class<?>[] params = method.getParameterTypes();
                if (params.length < 1 || params[0] != Request.class || params[1] != Response.class) {
                    StringBuilder sb = new StringBuilder();
                    for (Class<?> c : params) {
                        sb.append(c.getSimpleName());
                        sb.append(", ");
                    }

                    String paramString = sb.toString();
                    if (paramString.length() > 2) {
                        paramString = paramString.substring(0, paramString.length() - 2);
                    }

                    System.err.println("Skipped method with invalid parameter types found in " +
                            method.getName() + "(" + paramString + ") in " + o.getClass().getName() +
                            ". Expected Request and Response.");
                    continue;
                }

                DynExpress[] annotations = method.getAnnotationsByType(DynExpress.class);
                for (DynExpress dex : annotations) {
                    String context = dex.context();
                    String requestMethod = dex.method().getMethod();

                    // Bind to instance
                    handler.add(1, new FilterImpl(requestMethod, context, (req, res) -> {
                        try {
                            method.invoke(o, req, res);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }));
                }
            }
        }

        return this;
    }

    /**
     * Start the HTTP-Server on port 80.
     * This method is asynchronous so be sure to add an listener or keep it in mind!
     */
    public void listen() {
        listen(null, 80);
    }

    /**
     * Start the HTTP-Server on a specific port
     * This method is asynchronous so be sure to add an listener or keep it in mind!
     *
     * @param port The port.
     */
    public void listen(int port) {
        listen(null, port);
    }

    /**
     * Start the HTTP-Server on port 80.
     * This method is asynchronous so be sure to add an listener or keep it in mind!
     *
     * @param onStart An listener which will be fired after the server is stardet.
     */
    public void listen(ExpressListener onStart) {
        listen(onStart, 80);
    }

    /**
     * Start the HTTP-Server on a specific port.
     * This method is asynchronous so be sure to add an listener or keep it in mind.
     *
     * @param onStart An listener which will be fired after the server is stardet.
     * @param port    The port.
     */
    public void listen(ExpressListener onStart, int port) {
        new Thread(() -> {
            try {

                // Fire worker threads
                worker.forEach(FilterWorker::start);

                InetSocketAddress socketAddress = this.hostname == null ? new InetSocketAddress(port) : new InetSocketAddress(this.hostname, port);
                if (httpsConfigurator != null) {

                    // Create https server
                    httpServer = HttpsServer.create(socketAddress, 0);
                    ((HttpsServer) httpServer).setHttpsConfigurator(httpsConfigurator);
                } else {

                    // Create http server
                    httpServer = HttpServer.create(socketAddress, 0);
                }

                // Set thread executor
                httpServer.setExecutor(executor);

                // Create handler for all contexts
                httpServer.createContext("/", exchange -> handler.handle(exchange, this));

                // Start server
                httpServer.start();

                // Fire listener
                if (onStart != null) {
                    onStart.action();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Stop express
     */
    public void stop() {
        if (httpServer != null) {

            // Stop http-server
            httpServer.stop(0);

            // Stop worker threads
            worker.forEach(FilterWorker::stop);
        }
    }
}
