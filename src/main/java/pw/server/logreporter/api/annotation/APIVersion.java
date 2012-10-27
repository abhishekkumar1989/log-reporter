package pw.server.logreporter.api.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface APIVersion {
    public float value() default 0.0f;
}

