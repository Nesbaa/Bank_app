package util;

import exceptions.ValidationException;

@FunctionalInterface
public interface Validation<T> {
    void Validate(T value) throws ValidationException;

}
