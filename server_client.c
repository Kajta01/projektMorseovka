//192.168.0.109

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <sys/socket.h>
#include <sys/types.h>

#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

int main(int argc, char *argv[]){

    int socketId;
    int n = 0;
    char recvBuff[1024];
    struct sockaddr_in serv_addr;

    memset(&recvBuff, '0', sizeof(recvBuff) );
    memset(serv_addr, '0', sizeof(serv_addr) );

    puts("Client");

    socketId = socket(AF_INET,SOCK_STREAM,0);
    if(socketId < 0) return 1;

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_port = htons(5000);
    inet_pton(AF_INET,"192.168.0.109" , &serv_addr.sin_addr); //argv[1]

    if(connect(socketId, (struct sockaddr *) &serv_addr, sizeof(serv_addr))<0)
    {
        printf("Nelze se pripojit!");
        return 1;
    }
    while(n = read(socketId, recvBuff, sizeof(recvBuff-1))>0)
    {
        recvBuff[n]=0;
        printf("Přijato %s\n",recvBuff);
    }
    
    return EXIT_SUCCESS;
}