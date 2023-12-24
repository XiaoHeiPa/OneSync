package io.github.hello09x.onesync.api.handler;

import org.apache.commons.lang3.mutable.MutableObject;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public abstract class CachedSnapshotHandler<T extends SnapshotComponent> implements SnapshotHandler<T> {

    private final MutableObject<T> theLast = new MutableObject<>();

    @Override
    public void save(@NotNull Long snapshotId, @NotNull Player player) {
        var snapshot = this.save0(snapshotId, player);
        theLast.setValue(snapshot);
    }

    @Override
    public @Nullable T getOne(@NotNull Long snapshotId) {
        return Optional.ofNullable(theLast.getValue())
                .filter(snapshot -> snapshot.snapshotId().equals(snapshotId))
                .orElseGet(() -> this.getOne0(snapshotId));
    }


    @Override
    public void remove(@NotNull List<Long> snapshotIds) {
        this.remove0(snapshotIds);
        theLast.setValue(null);
    }

    protected abstract @Nullable T save0(@NotNull Long snapshotId, @NotNull Player player);

    protected abstract @Nullable T getOne0(@NotNull Long snapshotId);

    protected abstract void remove0(@NotNull List<Long> snapshotIds);


}
