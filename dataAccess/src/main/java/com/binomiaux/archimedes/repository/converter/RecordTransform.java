package com.binomiaux.archimedes.repository.converter;

public interface RecordTransform<E, M> {
    M transform(E entity);
    E untransform(M model);
}
