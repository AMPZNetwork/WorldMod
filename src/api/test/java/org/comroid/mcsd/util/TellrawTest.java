package org.comroid.mcsd.util;

import org.comroid.api.text.minecraft.Tellraw;

import static org.comroid.api.text.minecraft.McFormatCode.*;

public class TellrawTest {
    public static void main(String[] args) {
        System.out.println(
                Tellraw.Command.builder()
                        .selector(Tellraw.Selector.Base.ALL_PLAYERS)
                        .component(Gray.text("<").build())
                        .component(Dark_Aqua.text("kaleidox").build())
                        .component(Gray.text(">").build())
                        .component(Reset.text(" hello world").build())
                        .build()
                        .toString()
        );
    }
}
