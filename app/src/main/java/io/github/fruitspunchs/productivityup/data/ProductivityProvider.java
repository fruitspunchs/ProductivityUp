package io.github.fruitspunchs.productivityup.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by User on 12/12/2015.
 */
@ContentProvider(authority = ProductivityProvider.AUTHORITY, database = ProductivityDatabase.class)
public final class ProductivityProvider {
    public static final String AUTHORITY = "io.github.fruitspunchs.productivityup.data.ProductivityProvider";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    interface Path {
        String DEADLINE_DAYS = ProductivityDatabase.DEADLINE_DAYS;
        String DEADLINE_TASKS = ProductivityDatabase.DEADLINE_TASKS;
        String AGENDA_DAYS = ProductivityDatabase.AGENDA_DAYS;
        String AGENDA_TASKS = ProductivityDatabase.AGENDA_TASKS;
        String ACCOUNTABILITY_CHART_DAYS = ProductivityDatabase.ACCOUNTABILITY_CHART_DAYS;
        String ACCOUNTABILITY_CHART_TASKS = ProductivityDatabase.ACCOUNTABILITY_CHART_TASKS;
    }
    @TableEndpoint(table = ProductivityDatabase.DEADLINE_DAYS)
    public static class DeadlineDays {
        @ContentUri(path = Path.DEADLINE_DAYS, type = "vnd.android.cursor.dir/" + Path.DEADLINE_DAYS, defaultSort = DeadlineDaysColumns.DATE + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.DEADLINE_DAYS);
    }

    @TableEndpoint(table = ProductivityDatabase.DEADLINE_TASKS)
    public static class DeadlineTasks {
        @ContentUri(path = Path.DEADLINE_TASKS, type = "vnd.android.cursor.dir/" + Path.DEADLINE_TASKS, defaultSort = DeadlineTasksColumns.DATE + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.DEADLINE_TASKS);
    }

    @TableEndpoint(table = ProductivityDatabase.AGENDA_DAYS)
    public static class AgendaDays {
        @ContentUri(path = Path.AGENDA_DAYS, type = "vnd.android.cursor.dir/" + Path.AGENDA_DAYS, defaultSort = AgendaDaysColumns.DATE + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.AGENDA_DAYS);
    }

    @TableEndpoint(table = ProductivityDatabase.AGENDA_TASKS)
    public static class AgendaTasks {
        @ContentUri(path = Path.AGENDA_TASKS, type = "vnd.android.cursor.dir/" + Path.AGENDA_TASKS, defaultSort = AgendaTasksColumns.DATE + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.AGENDA_TASKS);
    }

    @TableEndpoint(table = ProductivityDatabase.ACCOUNTABILITY_CHART_DAYS)
    public static class AccountabilityChartDays {
        @ContentUri(path = Path.ACCOUNTABILITY_CHART_DAYS, type = "vnd.android.cursor.dir/" + Path.ACCOUNTABILITY_CHART_DAYS, defaultSort = AccountabilityDaysColumns.DATE + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.ACCOUNTABILITY_CHART_DAYS);
    }

    @TableEndpoint(table = ProductivityDatabase.ACCOUNTABILITY_CHART_TASKS)
    public static class AccountabilityChartTasks {
        @ContentUri(path = Path.ACCOUNTABILITY_CHART_TASKS, type = "vnd.android.cursor.dir/" + Path.ACCOUNTABILITY_CHART_TASKS, defaultSort = AccountabilityTasksColumns.DATE + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.ACCOUNTABILITY_CHART_TASKS);
    }
}
