package express;

import express.http.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Simon Reinisch
 * Annotation to use object methods as request handler.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DynExpress {
    RequestMethod method() default RequestMethod.GET;

    String context() default "/";
}
