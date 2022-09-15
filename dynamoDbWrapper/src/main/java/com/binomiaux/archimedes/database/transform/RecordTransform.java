package com.binomiaux.archimedes.database.transform;

public interface RecordTransform<E, M> {
    M transform(E entity);
    E untransform(M model);
}
