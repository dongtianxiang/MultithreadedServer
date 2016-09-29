#include <sys/types.h>
#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <string.h>
#include <termios.h>
#include <unistd.h>

int main(){
	/*
		1. Open the file
	*/
	int fd = open("/dev/ttyUSB10", O_RDWR);
	if (fd == -1);  //oops

	/*
		2. Configure fd for USB
	*/
	struct termios options; // struct to hold options
	tcgetattr(fd, &options); // associate with this fd
	cfsetispeed(&options, 9600); // set input baud rate
	cfsetospeed(&options, 9600); // set output baud rate
	tcsetattr(fd, TCSANOW, &options); // set options 

	/*
		3. Read & Print
	*/
	char buf[100];
	int bytes_read = read(fd, buf, 100);
	buf[bytes_read] = '\0';
	char tem[100];
	strcpy(tem, buf);
	//bytes_read = read(fd, buf, 100);

	while(1){
		if(tem[strlen(tem) - 1] == '\n') {
		  printf("%s\n", tem);  // maybe the additional '\n' is not necessary
		  bytes_read = read(fd, buf, 100);
		  buf[bytes_read] = '\0';
		  strcpy(tem, buf);
		} else {
		  bytes_read = read(fd, buf, 100);
		  buf[bytes_read] = '\0';
		  strcat(tem, buf);
		}

	}

	/*
		4. Last step
	*/
	close(fd);
}