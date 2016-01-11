package finalproject.productivityup.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by User on 12/12/2015.
 */
@ContentProvider(authority = ProductivityProvider.AUTHORITY, database = ProductivityDatabase.class)
public final class ProductivityProvider {
    public static final String AUTHORITY = "finalproject.productivityup.data.ProductivityProvider";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    interface Path {
        String DEADLINE_TASKS = "deadline_tasks";
        String DEADLINE_DAYS = "deadline_days";
        String AGENDA_TASKS = "agenda_tasks";
        String AGENDA_DAYS = "agenda_days";
    }

    @TableEndpoint(table = ProductivityDatabase.DEADLINE_TASKS)
    public static class DeadlineTasks {
        @ContentUri(path = Path.DEADLINE_TASKS, type = "vnd.android.cursor.dir/deadline_tasks", defaultSort = DeadlineTasksColumns.DATE + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.DEADLINE_TASKS);
    }

    @TableEndpoint(table = ProductivityDatabase.DEADLINE_DAYS)
    public static class DeadlineDays {
        @ContentUri(path = Path.DEADLINE_DAYS, type = "vnd.android.cursor.dir/deadline_days", defaultSort = DeadlineDaysColumns.DATE + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.DEADLINE_DAYS);
    }

    @TableEndpoint(table = ProductivityDatabase.AGENDA_TASKS)
    public static class AgendaTasks {
        @ContentUri(path = Path.AGENDA_TASKS, type = "vnd.android.cursor.dir/agenda_tasks", defaultSort = AgendaTasksColumns.DATE + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.AGENDA_TASKS);
    }

    @TableEndpoint(table = ProductivityDatabase.AGENDA_DAYS)
    public static class AgendaDays {
        @ContentUri(path = Path.AGENDA_DAYS, type = "vnd.android.cursor.dir/agenda_tasks", defaultSort = AgendaDaysColumns.DATE + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.AGENDA_DAYS);
    }


}
