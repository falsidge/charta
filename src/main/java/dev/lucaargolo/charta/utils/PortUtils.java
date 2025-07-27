package dev.lucaargolo.charta.utils;

import net.minecraft.util.FastColor;

public class PortUtils {
    public static int fromArrayColors(float[] colors){
        return FastColor.ARGB32.color(255, (int) colors[0]*255, (int) colors[1]*255,(int) colors[2]*255);
    }


}
