#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <sys/socket.h>
#include <sys/types.h>

#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

int getData(int sockfd) {
	char buffer[32];
	int n;

	if ((n = read(sockfd, buffer, 31)) < 0)
		printf("ERROR reading from socket");
	buffer[n] = '\0';
	return atoi(buffer);
}

int main(int argc, char *argv[]) {

	int socketId, newsockfd;
	int data = 0;

	char recvBuff[1024];
	struct sockaddr_in serv_addr;

	memset(recvBuff, '0', sizeof(recvBuff));
	memset(&serv_addr, '0', sizeof(serv_addr));

	puts("Server");

	socketId = socket(AF_INET, SOCK_STREAM, 0);
	if (socketId < 0)
		return 1;

	serv_addr.sin_family = AF_INET;
	serv_addr.sin_port = htons(5000);
	serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);

	bind(socketId, (struct sockaddr*) &serv_addr, sizeof(serv_addr));
	listen(socketId, 10);

	while (1) {
		printf("waiting for new client...\n");
		if ((newsockfd = accept(socketId, (struct sockaddr*) NULL, NULL)) < 0)

			printf("opened new communication with client\n");
		while (1) {
			//---- wait for a number from client ---
			data = getData(newsockfd);
			printf("got %d\n", data);
			if (data < 0)
				break;
		}
	}

	return EXIT_SUCCESS;
}
