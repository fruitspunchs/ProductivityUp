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
        String DEADLINES = "deadlines";
        String DEADLINE_DAYS = "deadline_days";
    }

    @TableEndpoint(table = ProductivityDatabase.DEADLINES)
    public static class Deadlines {
        @ContentUri(path = Path.DEADLINES, type = "vnd.android.cursor.dir/deadlines", defaultSort = DeadlinesColumns.DATE + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.DEADLINES);
    }

    @TableEndpoint(table = ProductivityDatabase.DEADLINE_DAYS)
    public static class DeadlineDays {
        @ContentUri(path = Path.DEADLINE_DAYS, type = "vnd.android.cursor.dir/deadline_days", defaultSort = DeadlineDaysColumns.DATE + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.DEADLINE_DAYS);
    }


}
