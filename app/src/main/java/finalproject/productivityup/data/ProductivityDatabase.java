package finalproject.productivityup.data;

import net.simonvt.schematic.annotation.Table;

/**
 * Created by User on 12/12/2015.
 */
@net.simonvt.schematic.annotation.Database(version = ProductivityDatabase.VERSION)
public class ProductivityDatabase {
    public static final int VERSION = 1;

    @Table(DeadlineTasksColumns.class)
    public static final String DEADLINE_TASKS = "deadline_tasks";

    @Table(DeadlineDaysColumns.class)
    public static final String DEADLINE_DAYS = "deadline_days";
}
