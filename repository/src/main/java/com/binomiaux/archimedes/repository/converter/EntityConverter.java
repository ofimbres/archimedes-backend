package com.binomiaux.archimedes.repository.converter;

public interface EntityConverter<E, M> {
    M transform(E entity);
    E untransform(M model);
}
