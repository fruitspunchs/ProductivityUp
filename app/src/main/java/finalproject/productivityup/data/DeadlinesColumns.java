package finalproject.productivityup.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by User on 12/10/2015.
 */
public class DeadlinesColumns {
    @DataType(INTEGER)
    @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";

    @DataType(INTEGER)
    public static final String DEADLINE = "deadline";

    @DataType(TEXT)
    @NotNull
    public static final String TASK = "task";
}
