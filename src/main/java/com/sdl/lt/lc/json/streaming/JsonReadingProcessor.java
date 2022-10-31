package com.sdl.lt.lc.json.streaming;

import com.fasterxml.jackson.core.JsonToken;
import com.sdl.lt.lc.json.streaming.matchers.PathMatcher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Iterator;

/**
 * @author anegruti
 * @since 5/25/2022
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class JsonReadingProcessor implements AutoCloseable {

    private final JsonPathProcessor processor;

    /**
     * Returns an iterator over objects of specified class at
     *
     * @param pathMatcher the {@link PathMatcher} to stop at (needs to stop at start of an array)
     * @param clazz hints towards the type of objects we will find inside the array
     * @return {@link Iterator} iterator over objects of type {@link T}
     * @throws UnsupportedOperationException when next token read after finding the path is not START_OBJECT
     *
     * @implNote Before starting to read an iterator the position of the cursor has to be on START_OBJECT
     * After getting an iterator we update the path by closing the START_OBJECT we added before getting the iterator
     * and we also close the START_ARRAY where we opened the array.
     *
     * <p>
     *     <b>CAUTION:</b>
     *     <p>The returned iterator must be used as follows:</p>
     *     <pre>{@code
     *         while (iterator.hasNext())
     *             iterator.next();
     *     }</pre>
     *
     *     <b>In other words ensure that .hasNext() is called one more time after all elements have been iterated over</b>
     * </p>
     */
    public <T> Iterator<T> readValuesAs(PathMatcher pathMatcher, Class<T> clazz) {
        processor.skipUntilPathOrEnd(pathMatcher);

        if (!hasNext()) {
            return Collections.emptyIterator();
        }
        processor.ensureCurrentTokenIsOfTypeStartArray();

        if (JsonToken.END_ARRAY.equals(processor.next())) {
            return Collections.emptyIterator();
        }

        Iterator<T> tIterator = processor.readValuesAs(clazz);
        processor.updatePath(JsonToken.END_OBJECT);
        processor.updatePath(JsonToken.END_ARRAY);
        return tIterator;
    }

    public boolean hasNext() {
        return processor.hasNext();
    }

    @Override
    public void close() throws Exception {
        processor.close();
    }

}
