package com.owl.owlyhome.light.util;

import java.util.ArrayList;
import java.util.List;

public class OperationListUtils {

    public static OperationList colourMigrate(Integer from, Integer to) {
        List<Operation> ops = new ArrayList<>();
        ops.add(new SendPacketOperation(Commands.RGBW_COLOR_LED_ALL_ON.toByteArray()));
        ops.add(new SleepOperation(100));

        int current = from;
        while (current != to) {
            if (current > 255) {
                current = 0;
            }

            ops.add(new SendPacketOperation(Colours.toByteArray(current)));
            ops.add(new SleepOperation(55));

            current++;
        }

        return new OperationList().withOps(ops);
    }

    public static OperationList brightMigrate(Integer from, Integer to) {
        List<Operation> ops = new ArrayList<>();
        ops.add(new SendPacketOperation(Commands.RGBW_COLOR_LED_ALL_ON.toByteArray()));
        ops.add(new SleepOperation(100));

        int current = from;
        while (current != to) {
            if (current > 27) {
                current = 0;
            }

            ops.add(new SendPacketOperation(Brightness.toByteArray(current)));
            ops.add(new SleepOperation(55));

            current++;
        }

        return new OperationList().withOps(ops);
    }
}
