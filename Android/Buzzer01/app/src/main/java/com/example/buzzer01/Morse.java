package com.example.buzzer01;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Morse {
    static private StringBuilder MorseChar = new StringBuilder();
    static private StringBuilder MorseSymbol = new StringBuilder();
    static private StringBuilder Solving = new StringBuilder();
    static private Map<String,Character> map = new HashMap<>();

    static int COUNT_DOT = 1;
    static int COUNT_DASH = 3;
    static int COUNT_SYMBOL_SPACE = 1;
    static int COUNT_LETTER_SPACE = 2;
    static int COUNT_WORD_SPACE = 5;
    /////////////////////////////////////
    static private boolean pause = false;

    public static void initMorse()
    {
        map.put(".-",'A');
        map.put("-...",'B');
        map.put("-.-.",'C');
        map.put("-..",'D');
        map.put(".",'E');
        map.put("..-.",'F');
        map.put("--.",'G');
        map.put("....",'H');
        map.put("..",'I');
        map.put(".---",'J');
        map.put("-.-",'K');
        map.put(".-..",'L');
        map.put("--",'M');
        map.put("-.",'N');
        map.put("---",'O');
        map.put(".--.",'P');
        map.put("--.-",'Q');
        map.put(".-.",'R');
        map.put("...",'S');
        map.put("-",'T');
        map.put("..-",'U');
        map.put("...-",'V');
        map.put(".--",'W');
        map.put("-..-",'X');
        map.put("-.--",'Y');
        map.put("--..",'Z');
    }


    public static void SolveSymbol(String morseValue, int size ){
        try {
            int value = Integer.parseInt(morseValue);
            char symbol;

            if(value == 0)
            {
                symbol = '/';
            }
            else if(value <= 110)
            {
                symbol = '.';
            }

            else{
                symbol = '-';//'?';
            }
            MorseChar.append(symbol);
            MorseSymbol.append(symbol);
            if(MorseSymbol.length() > 1 && symbol == '/') {
                MorseChar.append(",");
                SolveChar(MorseSymbol.toString(), MorseSymbol.length());
                MorseSymbol.delete(0,MorseSymbol.length());
            }

        }
        catch (Exception e)
        {
            System.out.println("\n !!!!Mooc jednicek!!!\n"+e.toString());
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void SolveChar(String morseValue, int size ){

        if(morseValue.equals("//")) {
            if(pause){
                Solving.append(" ");
            }
            else{
                pause = true;
            }
            return;
        }

        String input = morseValue.replace("/", "");

        Character fetchedChar = map.get(input);
        // System.out.println(e.getKey() + " " + e.getValue());

        if (fetchedChar != null) {
            Solving.append(fetchedChar);
        } else {
            Solving.append("?");
        }
        pause = false;


    }
    public static String getMorseChar(){
        return MorseChar.toString();
    }
    public static String getSolving(){
        return Solving.toString();
    }
    public static void clearMorseChar(){
        MorseChar.delete(0,MorseChar.length());
    }
    public static void clearSolving(){
        Solving.delete(0,Solving.length());
    }

}
