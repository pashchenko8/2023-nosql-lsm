package ru.vk.itmo.test;

import ru.vk.itmo.BaseEntry;
import ru.vk.itmo.Config;
import ru.vk.itmo.Dao;
import ru.vk.itmo.Entry;

import java.io.IOException;
import java.util.Iterator;

class TestDao<DATA, E extends Entry<DATA>> implements Dao<String, Entry<String>> {

    Dao<DATA, E> delegate;

    final DaoFactory.Factory<DATA, E> factory;
    final Config config;
    final String name;

    TestDao(DaoFactory.Factory<DATA, E> factory, Config config) {
        this.factory = factory;
        this.config = config;
        delegate = factory.createDao(config);

        Class<?> delegateClass = delegate.getClass();
        String packageName = delegateClass.getPackageName();
        String lastPackagePart = packageName.substring(packageName.lastIndexOf('.') + 1);

        name = "TestDao<" + lastPackagePart + "." + delegateClass.getSimpleName() + ">";
    }

    public Dao<String, Entry<String>> reopen() {
        return new TestDao<>(factory, config);
    }

    @Override
    public Entry<String> get(String key) {
        E result = delegate.get(factory.fromString(key));
        if (result == null) {
            return null;
        }
        return new BaseEntry<>(
                factory.toString(result.key()),
                factory.toString(result.value())
        );
    }

    @Override
    public Iterator<Entry<String>> get(String from, String to) {
        Iterator<E> iterator = delegate.get(
                factory.fromString(from),
                factory.fromString(to)
        );
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Entry<String> next() {
                E next = iterator.next();
                String key = factory.toString(next.key());
                String value = factory.toString(next.value());
                return new BaseEntry<>(key, value);
            }
        };
    }

    @Override
    public void upsert(Entry<String> entry) {
        BaseEntry<DATA> e = new BaseEntry<>(
                factory.fromString(entry.key()),
                factory.fromString(entry.value())
        );
        delegate.upsert(factory.fromBaseEntry(e));
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        if (delegate != null) {
            delegate.close();
            delegate = null;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
