package com.binomiaux.archimedes.repository.transform;

public interface RecordTransform<E, M> {
    M transform(E entity);
    E untransform(M model);
}
