package pw.server.logreporter.api.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MetaAccountFromToken {
  public boolean required() default true;
}

