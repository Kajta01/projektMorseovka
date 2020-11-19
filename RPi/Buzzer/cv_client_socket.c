
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <sys/socket.h>
#include <sys/types.h>

#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>


int main(int argc, char *argv[]) {
	int dataInt=0;
	int socketInt;
	int n=0;
	char recvBuff[1024];
	char sendBuff[1024];
	struct sockaddr_in serv_addr;

	memset(sendBuff, '0', sizeof(sendBuff));
	memset(recvBuff, '0', sizeof(recvBuff));
	memset(&serv_addr, '0', sizeof(serv_addr));
	if(argc==3)
	{
		dataInt=atoi(argv[2]);
	}

	puts("Socket_Client"); /* prints Socket_Client */

	socketInt = socket(AF_INET, SOCK_STREAM, 0); //vytvoření socketu

	serv_addr.sin_family = AF_INET;
	serv_addr.sin_port = htons(5000);	//nastavení portu
	inet_pton(AF_INET, argv[1],&serv_addr.sin_addr); //nastavení IP

	if(connect(socketInt, (struct sockaddr *)&serv_addr, sizeof(serv_addr))<0)
	{
		printf("Nelze se pripojit! \n");
		return 1;
	}

	//odeslání dat pro server
	snprintf(sendBuff,sizeof(sendBuff), "%d", dataInt);
	if(write(socketInt,sendBuff,strlen(sendBuff))<1)
	{
		printf("chyba zapisu na server");
		return 1;
	}
	//sleep(1);

	//ctení odpovědi serveru
	while((n=read(socketInt, recvBuff, sizeof(recvBuff)-1))>0)
	{
		recvBuff[n]=0;
		printf("Prijato: %s\n",recvBuff);
	}

	return EXIT_SUCCESS;
}
