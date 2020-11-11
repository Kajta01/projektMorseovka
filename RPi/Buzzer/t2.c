#include <wiringPi.h>
#include <softTone.h>
#include <stdio.h>
#include <morseAlpha.h>

#define BuzzPin 1

const int TIME_UNIT = 150;

const int DOT = TIME_UNIT;
const int DASH = 3 * TIME_UNIT;
const int SYMBOL_SPACE = TIME_UNIT;
const int LETTER_SPACE = 3 * TIME_UNIT - SYMBOL_SPACE;
const int WORD_SPACE = 7 * TIME_UNIT - LETTER_SPACE;

int main(void) {

	if (wiringPiSetup() == -1) {
		printf("WiringPi initialization failed !");
		return 1;
	}
	if (softToneCreate(BuzzPin) == -1) {
		printf("14CORE| Soft Tone Failed !");
		return 1;
	}

	while (1)
	{
		printMorse("slusi ti to");


	}
	return 0;
}
void printMorse(char* message) {
	//loop through the message
	for (int i = 0; i < strlen(message); i++) {
		printf("\nChar: %c", message[i]);
		fflush(stdout);
		const char *ch = strchr(characters, tolower(message[i]));

		if (ch != NULL) {
			int index = (int) (ch - characters);
			const char *morseSymbols = mappings[index];
			int count = strlen(morseSymbols);

			for (int i = 0; i < count; i++) {
				softToneWrite(BuzzPin, 500);

				int symbolTime;
				char symbol = morseSymbols[i];
				if (symbol == '.')
					symbolTime = DOT;
				else
					symbolTime = DASH;

				delay(symbolTime);
				softToneWrite(BuzzPin, 0);
				delay(SYMBOL_SPACE);
			}
			delay(LETTER_SPACE);
		}
		delay(WORD_SPACE);
	}
}

