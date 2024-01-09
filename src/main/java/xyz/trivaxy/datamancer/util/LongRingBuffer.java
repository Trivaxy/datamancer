package xyz.trivaxy.datamancer.util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

// An insert-only, non-random access ring buffer that overwrites old values
public class LongRingBuffer implements Iterable<Long> {
    private final long[] buffer;
    private int index = 0;
    private int size = 0;

    public LongRingBuffer(int size) {
        buffer = new long[size];
    }

    public void add(long value) {
        buffer[index] = value;
        index = (index + 1) % buffer.length;
        size = Math.min(size + 1, buffer.length);
    }

    public void clear() {
        index = 0;
        size = 0;
    }

    public int size() {
        return size;
    }

    @NotNull
    @Override
    public Iterator<Long> iterator() {
        return new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public Long next() {
                return buffer[i++];
            }
        };
    }
}
