package dev.khanh.plugin.kplugin.task;

import com.tcoded.folialib.wrapper.task.WrappedTask;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Adapter implementation of {@link ScheduledTask} backed by FoliaLib's {@link WrappedTask}.
 * Bridges between the library's internal task types and the public-facing API,
 * isolating all FoliaLib task dependencies within this class.
 */
public class ScheduledTaskImpl implements ScheduledTask {

    private final WrappedTask delegate;

    /**
     * Creates a new FoliaScheduledTask wrapping the given delegate.
     *
     * @param delegate the underlying FoliaLib task
     */
    public ScheduledTaskImpl(WrappedTask delegate) {
        this.delegate = delegate;
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }

    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    /**
     * Returns the underlying FoliaLib task for internal scheduler operations
     * that require the native task type.
     *
     * @return the wrapped task delegate
     */
    public WrappedTask unwrap() {
        return delegate;
    }

    /**
     * Adapts a {@link Consumer} of {@link ScheduledTask} to a {@link Consumer} of {@link WrappedTask}.
     * Used internally to bridge user-facing consumers with FoliaLib's scheduler API.
     *
     * @param consumer the consumer accepting ScheduledTask
     * @return a consumer accepting WrappedTask that wraps and delegates to the given consumer
     */
    public static Consumer<WrappedTask> adaptConsumer(Consumer<ScheduledTask> consumer) {
        return wrappedTask -> consumer.accept(new ScheduledTaskImpl(wrappedTask));
    }

    /**
     * Wraps a list of {@link WrappedTask} instances into a list of {@link ScheduledTask}.
     *
     * @param tasks the list of FoliaLib tasks
     * @return a list of ScheduledTask wrappers
     */
    public static List<ScheduledTask> wrapList(List<WrappedTask> tasks) {
        return tasks.stream()
                .map(ScheduledTaskImpl::new)
                .collect(Collectors.toList());
    }
}
