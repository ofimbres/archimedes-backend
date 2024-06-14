package com.binomiaux.archimedes.repository.converter;

public interface EntityTransform<E, M> {
    M transform(E entity);
    E untransform(M model);
}
