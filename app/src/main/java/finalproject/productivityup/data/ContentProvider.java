package finalproject.productivityup.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by User on 12/12/2015.
 */
public class ContentProvider {
    public static final String AUTHORITY = "finalproject.productivityup.data.ContentProvider";
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
    }

    @TableEndpoint(table = Database.DEADLINES)
    public static class Deadlines {
        @ContentUri(path = Path.DEADLINES, type = "vnd.android.cursor.dir/deadlines", defaultSort = DeadlinesColumns.DEADLINE + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.DEADLINES);
    }
}
