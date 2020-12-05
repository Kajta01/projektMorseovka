#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <sys/socket.h>
#include <sys/types.h>

#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

#include <wiringPi.h>
#include <softTone.h>
#include <stdio.h>
#include "morseAlpha.h"

#define BuzzPin 1
#define LedPin 0

const int FREQ_BUZZER = 4600;

const int TIME_UNIT = 120; //120

const int DOT = TIME_UNIT;
const int DASH = 2 * TIME_UNIT;//2
const int SYMBOL_SPACE = 1.15 * TIME_UNIT;//2
const int LETTER_SPACE = 0.5 * TIME_UNIT;
const int WORD_SPACE = 4* TIME_UNIT;

pthread_mutex_t mutexDATA;
pthread_mutex_t mutexPRINT;

void printMorse(char* message);
void *printMorseTh(char* data);

int main(void) {
	printf("Server\n"); /* prints Socket_Server */

	if (wiringPiSetup() == -1) {
		printf("WiringPi initialization failed !");
		return 1;
	}
	if (softToneCreate(BuzzPin) == -1) {
		printf("14CORE| Soft Tone Failed !");
		return 1;
	}
	////////////////////////////////////
	int rc = 0;
	int conRq = 0;
	int n = 0;
	int socketInt;
	char sendBuff[1024];
	char printBuff[1024];
	char recvBuff[1024];
	struct sockaddr_in serv_addr;
	pthread_t thread_id;

	memset(sendBuff, '0', sizeof(sendBuff));
	memset(recvBuff, '0', sizeof(recvBuff));
	memset(&serv_addr, '0', sizeof(serv_addr));
	memset(printBuff, '0', sizeof(printBuff));

	socketInt = socket(AF_INET, SOCK_STREAM, 0); //vytvoření socketu

	serv_addr.sin_family = AF_INET;
	serv_addr.sin_port = htons(5000);	//nastavení portu
	serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);

	if(bind(socketInt, (struct sockaddr*)&serv_addr, sizeof(serv_addr)))
	{
		printf("Bind socket error");
		return 1;
	}

	pinMode (LedPin, OUTPUT) ;

    pthread_mutex_init(&mutexPRINT, NULL);
	pthread_mutex_init(&mutexDATA, NULL);

	
	
	listen(socketInt,10);

	///// END INIT /////

	while(1)
	{
		printf("\nWaiting......\n");
		fflush(stdout);
		conRq = accept(socketInt,(struct sockaddr*)NULL,NULL);

		if((n = recv(conRq, recvBuff, sizeof(recvBuff)-1,0))>0)
		{
			recvBuff[n]=0;
			printf("Prijato: %s\n",recvBuff);
		}else{printf("zadna data neprijata");}

		
		/* create a new thread that will execute 'PrintMorse' */

		if(recvBuff != NULL){
			rc = pthread_create(&thread_id, NULL, printMorseTh, (char *)recvBuff);
    		printf("\n Created new thread (%u) ... \n", thread_id);
			if (rc) /* could not create thread */
			{
        		printf("\n ERROR: return code from pthread_create is %d \n", rc);
        		exit(1);
    		}
		}
    	


		close(conRq);
	}

	pthread_mutex_destroy(&mutexPRINT); //free mutex
	pthread_mutex_destroy(&mutexDATA); //free mutex
	return EXIT_SUCCESS;
}

void *printMorseTh(char *data)
{
	pthread_detach(pthread_self());
	printf("\nZacatek vysilani ...\n");

	pthread_mutex_lock(&mutexPRINT);
	printMorse(data);
	pthread_mutex_unlock(&mutexPRINT);
	printf("\nKonec vysilani ...\n");
	
	delay(1);
	fflush(stdout);
	pthread_exit(NULL); /* terminate the thread */
}

void printMorse(char* message) {
	//loop through the message
	delay(3000);
	for (int i = 0; i < strlen(message); i++) {
		printf("Char: %c\n", message[i]);
		fflush(stdout);
		const char *ch = strchr(characters, tolower(message[i]));

		if (ch != NULL) {
			int index = (int) (ch - characters);
			const char *morseSymbols = mappings[index];
			int count = strlen(morseSymbols);

			for (int i = 0; i < count; i++) {
				softToneWrite(BuzzPin, FREQ_BUZZER);
				digitalWrite (LedPin, HIGH);

				int symbolTime;
				char symbol = morseSymbols[i];
				if (symbol == '.')
					symbolTime = DOT;
				else
					symbolTime = DASH;

				delay(symbolTime);
				softToneWrite(BuzzPin, 0);
				digitalWrite (LedPin, LOW);
				delay(SYMBOL_SPACE);
			}			
			delay(LETTER_SPACE);
		}
		delay(WORD_SPACE);
	}
}
