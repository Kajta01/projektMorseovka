# comments :D 
# 02 makefile_asyn
# CC - compiller
# CFLAGS - -Wall -ggdb used flags
# LDFLAGS - -l libraries
# OBJFILES - .o files
CC	= gcc
CFLAGS =
LDFLAGS	= -lcurses -lpthread -lwiringPi
OBJFILES = t4.c morseAlpha.h
TARGET	= T4app

all: $(TARGET)

$(TARGET): $(OBJFILES)
	$(CC) $(CFLAGS) -o $(TARGET) $(OBJFILES) $(LDFLAGS)
clean:
	rm -f $(OBJFILES) $(TARGET) *~
