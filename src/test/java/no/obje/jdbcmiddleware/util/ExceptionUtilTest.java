package no.obje.jdbcmiddleware.util;

import org.junit.Assert;
import org.junit.Test;

public class ExceptionUtilTest {

    @Test(expected = RuntimeException.class)
    public void soften_should_throw_runtime_exception_when_a_checked_exception_is_thrown() {
        ExceptionUtil.soften(() -> {
            throw new Exception();
        });
    }

    @Test
    public void soften_should_rethrow_runtime_exception() {
        final RuntimeException runtimeException = new RuntimeException("message");
        try {
            ExceptionUtil.soften(() -> {
                throw runtimeException;
            });
        }
        catch(RuntimeException e) {
            Assert.assertEquals(e, runtimeException);
        }
    }

    @Test
    public void soften_should_return_expected_value_if_no_exception_was_thrown() {
        final String result = ExceptionUtil.soften(() -> "someValue");
        Assert.assertEquals("someValue", result);
    }
}