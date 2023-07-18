package com.ionos.go.plugin.notifier.template;

import javax.el.ELContext;
import javax.el.PropertyNotWritableException;
import javax.el.ValueExpression;

class SimpleValueExpression<T> extends ValueExpression {

    /** Auto generated serial version UID. */
    private static final long serialVersionUID = -2489604594178988600L;

    /** The value stored. */
    private final T value;

    /**
     * Constructor.
     *
     * @param obj
     *            the value that should be stored
     */
    SimpleValueExpression(final T obj) {
        value = obj;
    }

    @Override
    public Class<?> getExpectedType() {
        return value.getClass();
    }

    @Override
    public Class<?> getType(final ELContext ctx) {
        return value.getClass();
    }

    @Override
    public Object getValue(final ELContext ctx) {
        return value;
    }

    @Override
    public boolean isReadOnly(final ELContext ctx) {
        return true;
    }

    @Override
    public void setValue(final ELContext ctx, final Object val) {
        throw new PropertyNotWritableException();
    }

    @Override
    public boolean equals(final Object o) {
        return value.equals(o);
    }

    @Override
    public String getExpressionString() {
        return value.toString();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean isLiteralText() {
        return false;
    }
}
