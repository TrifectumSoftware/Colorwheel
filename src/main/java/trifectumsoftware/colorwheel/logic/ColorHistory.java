package trifectumsoftware.colorwheel.logic;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ColorHistory {

    private static final int MAX_OPERATIONS = 10;

    private static final Map<String, Deque<Op>> UNDO = new HashMap<>();
    private static final Map<String, Deque<Op>> REDO = new HashMap<>();

    private ColorHistory() {}

    public static void push(String player, int dimension, List<Change> changes) {
        Deque<Op> undo = UNDO.computeIfAbsent(player, key -> new ArrayDeque<>());
        undo.addLast(new Op(dimension, changes.toArray(new Change[0])));
        while (undo.size() > MAX_OPERATIONS) {
            undo.removeFirst();
        }
        Deque<Op> redo = REDO.get(player);
        if (redo != null) {
            redo.clear();
        }
    }

    public static Op undo(String player) {
        return move(player, UNDO, REDO);
    }

    public static Op redo(String player) {
        return move(player, REDO, UNDO);
    }

    private static Op move(String player, Map<String, Deque<Op>> from, Map<String, Deque<Op>> to) {
        Deque<Op> source = from.get(player);
        if (source == null || source.isEmpty()) {
            return null;
        }
        Op op = source.removeLast();
        Deque<Op> target = to.computeIfAbsent(player, key -> new ArrayDeque<>());
        target.addLast(op);
        while (target.size() > MAX_OPERATIONS) {
            target.removeFirst();
        }
        return op;
    }

    public static final class Op {

        public final int dimension;
        public final Change[] changes;

        private Op(int dimension, Change[] changes) {
            this.dimension = dimension;
            this.changes = changes;
        }

        public List<Change> changeList() {
            return Arrays.asList(changes);
        }
    }

    public static final class Change {

        public final int x;
        public final int y;
        public final int z;
        public final int oldColor;
        public final int newColor;
        public final boolean hasOld;
        public final boolean hasNew;

        private Change(int x, int y, int z, Integer oldColor, Integer newColor) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.hasOld = oldColor != null;
            this.oldColor = oldColor == null ? 0 : oldColor;
            this.hasNew = newColor != null;
            this.newColor = newColor == null ? 0 : newColor;
        }

        public static Change of(int x, int y, int z, Integer oldColor, Integer newColor) {
            return new Change(x, y, z, oldColor, newColor);
        }

        public boolean present(boolean useNew) {
            return useNew ? hasNew : hasOld;
        }

        public int color(boolean useNew) {
            return useNew ? newColor : oldColor;
        }
    }
}
