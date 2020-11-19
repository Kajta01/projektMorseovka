#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <sys/socket.h>
#include <sys/types.h>

#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>


int main(void) {
	puts("Socket_Server"); /* prints Socket_Server */
	int conRq = 0;
	int dataInt = 0;
	int n = 0;
	int socketInt;
	char sendBuff[1024];
	char recvBuff[1024];
	struct sockaddr_in serv_addr;

	memset(sendBuff, '0', sizeof(sendBuff));
	memset(recvBuff, '0', sizeof(recvBuff));
	memset(&serv_addr, '0', sizeof(serv_addr));

	socketInt = socket(AF_INET, SOCK_STREAM, 0); //vytvoření socketu


	serv_addr.sin_family = AF_INET;
	serv_addr.sin_port = htons(5000);	//nastavení portu
	serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);

	bind(socketInt, (struct sockaddr*)&serv_addr, sizeof(serv_addr));
	listen(socketInt,10);


	while(1)
	{
		conRq = accept(socketInt,(struct sockaddr*)NULL,NULL);

		if((n = recv(conRq, recvBuff, sizeof(recvBuff)-1,0))>0)
		{
			recvBuff[n]=0;
			printf("Prijato: %s\n",recvBuff);
			dataInt=atoi(recvBuff)+1;
		}else{printf("zadna data neprijata");}

		//formátování pro výstup
		snprintf(sendBuff,sizeof(sendBuff), "%d",dataInt);
		//odeslání na clienta
		printf("Odesláno: %s\n",sendBuff);
		write(conRq,sendBuff, strlen(sendBuff));

		close(conRq);
	}

	return EXIT_SUCCESS;
}
