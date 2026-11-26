package net.nicguzzo.wands.wand;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

/** Bounded by the number of changes, but evicts and replays entire actions. */
final class ActionHistory<K, T> {
    private record Action<K, T>(K world, List<T> changes) {}
    record Result(int applied, int skipped, boolean otherWorld) {}

    private final Deque<Action<K, T>> undo = new ArrayDeque<>();
    private final Deque<Action<K, T>> redo = new ArrayDeque<>();
    private final int capacity;
    private int undoSize;

    ActionHistory(int capacity) {
        this.capacity = capacity;
    }

    void add(K world, List<T> changes) {
        if (changes.isEmpty()) return;
        redo.clear();
        while (!undo.isEmpty() && undoSize + changes.size() > capacity) {
            undoSize -= undo.removeFirst().changes.size();
        }
        if (changes.size() <= capacity) {
            undo.addLast(new Action<>(world, List.copyOf(changes)));
            undoSize += changes.size();
        }
    }

    Result replay(K world, int count, boolean forward, Predicate<T> apply) {
        var source = forward ? redo : undo;
        var destination = forward ? undo : redo;
        int applied = 0;
        int skipped = 0;
        for (int i = 0; i < count && !source.isEmpty(); i++) {
            var action = source.getLast();
            if (!world.equals(action.world)) return new Result(applied, skipped, true);
            source.removeLast();
            if (!forward) undoSize -= action.changes.size();
            List<T> replayed = new ArrayList<>();
            int size = action.changes.size();
            for (int step = 0; step < size; step++) {
                T change = action.changes.get(forward ? step : size - 1 - step);
                if (apply.test(change)) replayed.add(change);
                else skipped++;
            }
            if (!replayed.isEmpty()) {
                if (!forward) java.util.Collections.reverse(replayed);
                destination.addLast(new Action<>(world, replayed));
                if (forward) undoSize += replayed.size();
                applied += replayed.size();
            }
        }
        return new Result(applied, skipped, false);
    }
}
