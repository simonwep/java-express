package express.middleware;

import express.http.HttpRequestHandler;

/**
 * @author Simon R.
 * <p>
 * Options-Class for FileProvider
 */
public class FileProviderOptions {

    private String[] extensions;
    private HttpRequestHandler handler;
    private boolean fallBackSearching;
    private boolean lastModified;
    private long maxAge;
    private DotFiles dotFiles;

    {
        // Initalize some values.
        this.extensions = null;
        this.handler = null;
        this.fallBackSearching = false;
        this.lastModified = true;
        this.maxAge = 0;
        this.dotFiles = DotFiles.IGNORE;
    }

    public FileProviderOptions() {
    }

    /**
     * @return The current allowed extensions.
     */
    public String[] getExtensions() {
        return extensions;
    }

    /**
     * Set the extension from the file which can be accessed outside.
     * By default all extensions are allowed.
     *
     * @param extensions The extensions.
     * @return This instance.
     */
    public FileProviderOptions setExtensions(String... extensions) {
        this.extensions = extensions;
        return this;
    }

    /**
     * @return If the fallback-search is activated.
     */
    public boolean isFallBackSearching() {
        return fallBackSearching;
    }

    /**
     * Activate the fallback-search.
     * E.g. if an request to <code>/js/code.js</code> was made but the
     * requested resource cannot be found. It will be looked for an file called <code>code</code>
     * and return it.
     * <p>
     * Default is false.
     *
     * @param fallBackSearching If you want to activate the file-fallback-search.
     * @return This instance.
     */
    public FileProviderOptions setFallBackSearching(boolean fallBackSearching) {
        this.fallBackSearching = fallBackSearching;
        return this;
    }

    /**
     * @return If the last modified date of the file on the OS will be send.
     */
    public boolean isLastModified() {
        return lastModified;
    }

    /**
     * Set the Last-Modified header to the last modified date of the file on the OS.
     * Default is true.
     *
     * @param lastModified If you want to send the last modified date of the file on the OS.
     * @return This instance.
     */
    public FileProviderOptions setLastModified(boolean lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * @return The current maxAge of of the Cache-Control header.
     */
    public long getMaxAge() {
        return maxAge;
    }

    /**
     * Set the max-age property of the Cache-Control header in milliseconds or a string in ms format.
     * <p>
     * Default is 0.
     *
     * @param maxAge The new maxAge value.
     * @return This instance.
     */
    public FileProviderOptions setMaxAge(long maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    /**
     * @return The current corresponding handler.
     */
    HttpRequestHandler getHandler() {
        return handler;
    }

    /**
     * Add an alternate request-handler which will be fired before the file will be send.
     *
     * @param handler The HttpRequest handler.
     * @return This instance.
     */
    public FileProviderOptions setHandler(HttpRequestHandler handler) {
        this.handler = handler;
        return this;
    }

    /**
     * @return How Dot-files will be currently handled.
     */
    public DotFiles getDotFiles() {
        return dotFiles;
    }

    /**
     * Set how ".foo" file will be handled.
     * Ignore: Act if these files don't exists, response with 404
     * Deny: Deny the file, response with 303 and do nothing.
     * Allow: No special treatment for this files.
     *
     * @param dotFiles The handling type.
     */
    public void setDotFiles(DotFiles dotFiles) {
        this.dotFiles = dotFiles;
    }
}
