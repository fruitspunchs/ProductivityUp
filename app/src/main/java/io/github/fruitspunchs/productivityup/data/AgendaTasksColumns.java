package io.github.fruitspunchs.productivityup.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by User on 1/11/2016.
 */
public class AgendaTasksColumns {
    @DataType(INTEGER)
    @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";

    @DataType(INTEGER)
    @NotNull
    public static final String DATE = "date";

    @DataType(TEXT)
    @NotNull
    public static final String TASK = "task";

    @DataType(INTEGER)
    @NotNull
    public static final String IS_CHECKED = "is_checked";
}
