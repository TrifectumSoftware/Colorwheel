package trifectumsoftware.colorwheel.storage;

public final class IntIntMap {

    private static final int MIN_CAPACITY = 16;
    private static final int NO_VALUE = -1;
    private static final int LOAD_NUMERATOR = 7;
    private static final int LOAD_DENOMINATOR = 10;

    private int[] keys;
    private int[] values;
    private int size;
    private int threshold;

    public IntIntMap() {
        this(0);
    }

    public IntIntMap(int expectedSize) {
        int capacity = MIN_CAPACITY;
        while (capacity * LOAD_NUMERATOR / LOAD_DENOMINATOR < expectedSize) {
            capacity <<= 1;
        }
        this.keys = new int[capacity];
        this.values = new int[capacity];
        this.threshold = capacity * LOAD_NUMERATOR / LOAD_DENOMINATOR;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(int key) {
        int mask = keys.length - 1;
        int i = mix(key) & mask;
        while (true) {
            int stored = keys[i];
            if (stored == 0) {
                return false;
            }
            if (stored == key + 1) {
                return true;
            }
            i = (i + 1) & mask;
        }
    }

    public int get(int key) {
        int mask = keys.length - 1;
        int i = mix(key) & mask;
        while (true) {
            int stored = keys[i];
            if (stored == 0) {
                return NO_VALUE;
            }
            if (stored == key + 1) {
                return values[i];
            }
            i = (i + 1) & mask;
        }
    }

    public int put(int key, int value) {
        int mask = keys.length - 1;
        int i = mix(key) & mask;
        while (true) {
            int stored = keys[i];
            if (stored == 0) {
                keys[i] = key + 1;
                values[i] = value;
                if (++size >= threshold) {
                    resize();
                }
                return NO_VALUE;
            }
            if (stored == key + 1) {
                int previous = values[i];
                values[i] = value;
                return previous;
            }
            i = (i + 1) & mask;
        }
    }

    public int remove(int key) {
        int mask = keys.length - 1;
        int i = mix(key) & mask;
        while (true) {
            int stored = keys[i];
            if (stored == 0) {
                return NO_VALUE;
            }
            if (stored == key + 1) {
                break;
            }
            i = (i + 1) & mask;
        }
        int previous = values[i];
        keys[i] = 0;
        values[i] = 0;
        size--;

        int hole = i;
        int j = i;
        while (true) {
            j = (j + 1) & mask;
            int stored = keys[j];
            if (stored == 0) {
                break;
            }
            int home = mix(stored - 1) & mask;
            boolean between;
            if (hole < j) {
                between = home > hole && home <= j;
            } else {
                between = home > hole || home <= j;
            }
            if (!between) {
                keys[hole] = stored;
                values[hole] = values[j];
                keys[j] = 0;
                values[j] = 0;
                hole = j;
            }
        }
        return previous;
    }

    public void forEach(IntIntConsumer consumer) {
        for (int i = 0; i < keys.length; i++) {
            int stored = keys[i];
            if (stored != 0) {
                consumer.accept(stored - 1, values[i]);
            }
        }
    }

    private void resize() {
        int[] oldKeys = keys;
        int[] oldValues = values;
        int capacity = keys.length << 1;
        keys = new int[capacity];
        values = new int[capacity];
        threshold = capacity * LOAD_NUMERATOR / LOAD_DENOMINATOR;
        int mask = capacity - 1;
        for (int i = 0; i < oldKeys.length; i++) {
            int stored = oldKeys[i];
            if (stored == 0) {
                continue;
            }
            int j = mix(stored - 1) & mask;
            while (keys[j] != 0) {
                j = (j + 1) & mask;
            }
            keys[j] = stored;
            values[j] = oldValues[i];
        }
    }

    private static int mix(int key) {
        int h = key * 0x9E3779B1;
        return h ^ (h >>> 16);
    }

    public interface IntIntConsumer {

        void accept(int key, int value);
    }
}
