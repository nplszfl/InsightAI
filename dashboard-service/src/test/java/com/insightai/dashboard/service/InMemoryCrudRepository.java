package com.insightai.dashboard.service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Minimal in-memory CRUD repository used for unit tests of services.
 * Avoids mocking framework complexity; focuses the test on service behavior.
 */
public abstract class InMemoryCrudRepository<T> {
    private final Map<Long, T> store = new LinkedHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);
    private final Function<T, Long> idGetter;
    private final BiConsumer<T, Long> idSetter;

    protected InMemoryCrudRepository(Function<T, Long> idGetter, BiConsumer<T, Long> idSetter) {
        this.idGetter = idGetter;
        this.idSetter = idSetter;
    }

    public T save(T entity) {
        Long id = idGetter.apply(entity);
        if (id == null) {
            id = seq.incrementAndGet();
            idSetter.accept(entity, id);
        }
        store.put(id, entity);
        return entity;
    }

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean deleteById(Long id) {
        return store.remove(id) != null;
    }

    public List<T> findBy(java.util.function.Predicate<T> predicate) {
        return store.values().stream().filter(predicate).collect(Collectors.toList());
    }
}
