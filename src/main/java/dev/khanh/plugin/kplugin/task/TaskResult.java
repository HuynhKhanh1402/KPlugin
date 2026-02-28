package dev.khanh.plugin.kplugin.task;

import com.tcoded.folialib.enums.EntityTaskResult;

/**
 * Represents the result of an entity-bound task execution.
 * Mirrors FoliaLib's {@link EntityTaskResult} to decouple consumer code from the library's API.
 */
public enum TaskResult {

    /**
     * The task was successfully executed.
     */
    SUCCESS,

    /**
     * The entity was retired (removed/invalid) before the task could execute.
     */
    ENTITY_RETIRED,

    /**
     * The scheduler was retired before the task could execute.
     */
    SCHEDULER_RETIRED;

    /**
     * Converts a FoliaLib {@link EntityTaskResult} to the corresponding {@link TaskResult}.
     *
     * @param result the FoliaLib entity task result
     * @return the corresponding TaskResult
     * @throws IllegalArgumentException if the result is not recognized
     */
    public static TaskResult from(EntityTaskResult result) {
        switch (result) {
            case SUCCESS:
                return SUCCESS;
            case ENTITY_RETIRED:
                return ENTITY_RETIRED;
            case SCHEDULER_RETIRED:
                return SCHEDULER_RETIRED;
            default:
                throw new IllegalArgumentException("Unknown EntityTaskResult: " + result);
        }
    }
}
