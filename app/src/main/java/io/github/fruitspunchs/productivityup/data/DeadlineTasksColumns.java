/*
 * Copyright (c) 2016. Bel Jones Echavez
 */

package io.github.fruitspunchs.productivityup.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by User on 12/10/2015.
 */
public class DeadlineTasksColumns {
    @DataType(INTEGER)
    @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";

    @DataType(INTEGER)
    @NotNull
    public static final String DATE = "date";

    @DataType(INTEGER)
    @NotNull
    public static final String TIME = "time";

    @DataType(TEXT)
    @NotNull
    public static final String TASK = "task";
}
