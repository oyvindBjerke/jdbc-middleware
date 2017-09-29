package util;

import java.util.concurrent.Callable;

public final class ExceptionUtil {

    private ExceptionUtil() {}

    public static <T> T soften(Callable<T> callable) {
        try {
            return callable.call();
        }
        catch(RuntimeException e) {
            throw e;
        }
        catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
