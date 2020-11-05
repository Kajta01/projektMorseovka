#include <wiringPi.h>
#include <softTone.h>
#include <stdio.h>
#include <morseAlpha.h>

#define BuzzPin 1


const int TIME_UNIT = 250;

const int DOT = TIME_UNIT;
const int DASH = 3 * TIME_UNIT;
const int SYMBOL_SPACE = TIME_UNIT;
const int LETTER_SPACE = 3 * TIME_UNIT - SYMBOL_SPACE;
const int WORD_SPACE = 7 * TIME_UNIT - LETTER_SPACE;

const char* message = "ahoj\0";

int main(void){
    pinMode(13, OUTPUT);
    int size = strlen(message);

  //loop through the message
  for (int i = 0; i < size; i++)
  {
     const char* ch = strchr(characters, tolower(message[i]));  
  }


    return 0;
}
/*void loop()
{
  
    
       
    if (ch != NULL)
    {
      int index = (int)(ch - characters);    
      const char* morseSymbols = mappings[index];
      int count = strlen(morseSymbols);

      for (int i = 0; i < count; i++)
      {
        digitalWrite(13, HIGH);
        
        int symbolTime;
        char symbol = morseSymbols[i];
        if (symbol == '.')
          symbolTime = DOT;
        else
          symbolTime = DASH; 
        
        delay(symbolTime);
        digitalWrite(13, LOW);
        delay(SYMBOL_SPACE);
      }
      delay(LETTER_SPACE);        
    }
  }
  delay(WORD_SPACE);
}*/