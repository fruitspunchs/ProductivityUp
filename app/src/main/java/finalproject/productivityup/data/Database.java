package finalproject.productivityup.data;

import net.simonvt.schematic.annotation.Table;

/**
 * Created by User on 12/12/2015.
 */
@net.simonvt.schematic.annotation.Database(version = Database.VERSION)
public class Database {
    public static final int VERSION = 1;

    @Table(DeadlinesColumns.class)
    public static final String DEADLINES = "deadlines";
}
