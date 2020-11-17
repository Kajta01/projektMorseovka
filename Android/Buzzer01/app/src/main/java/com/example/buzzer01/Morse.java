package com.example.buzzer01;

import java.util.ArrayList;

public class Morse {
    static private StringBuilder MorseChar = new StringBuilder();
    static private StringBuilder Solving = new StringBuilder();

    static int COUNT_DOT = 1;
    static int COUNT_DASH = 3;
    static int COUNT_SYMBOL_SPACE = 1;
    static int COUNT_LETTER_SPACE = 2;
    static int COUNT_WORD_SPACE = 5;
    /////////////////////////////////////




    public static void SolveSymbol(String morseValue, int size ){
        try {
            int value = Integer.parseInt(morseValue);

        if(value == 0)
        {
            MorseChar.append("/");
        }
        if(value == 10)
        {
            MorseChar.append(".");
        }
        if(value == 1110)
        {
            MorseChar.append("-");
        }
        }
        catch (Exception e)
        {
            System.out.println("\n !!!!Mooc jednicek!!!\n"+e.toString());
        }
    }
    public static void SolveChar(String morseValue, int size ){

    }
    public static String getMorseChar(){
        return MorseChar.toString();
    }
    public static String getSolving(){
        return Solving.toString();
    }

}
