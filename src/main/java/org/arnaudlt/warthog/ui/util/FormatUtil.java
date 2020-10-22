package org.arnaudlt.warthog.ui.util;

import java.text.DecimalFormat;

public class FormatUtil {


    private static final DecimalFormat formatter = new DecimalFormat("#.##");


    private FormatUtil() {}


    public static String format(double value) {

        return formatter.format(value);
    }

}
