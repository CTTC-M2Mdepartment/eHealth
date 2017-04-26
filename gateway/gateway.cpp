#include <python2.7/Python.h>
#include <iostream>
#include <string>
#include <fstream>
#include <malloc.h>
#include "arduPiLoRa.h"

#define BUFNUM 60
#define WAIT 0
using namespace std;

int usePython (char* query) {
    printf("*****************\n");

    Py_Initialize();

    PyRun_SimpleString("import sys");
    PyRun_SimpleString("sys.path.append('./')");

    PyObject * pModule = NULL;
    PyObject * pFunc = NULL;
    PyObject * pArgs = NULL;


    pModule = PyImport_ImportModule("send");
    pFunc = PyObject_GetAttrString(pModule, "gateway_post");
    pArgs = PyTuple_New(1);
    PyTuple_SetItem(pArgs,0,Py_BuildValue("s",query));
    PyObject_CallObject(pFunc, pArgs);
    Py_DECREF(pFunc);

    Py_Finalize();
    printf("********xxxxxxxxxxxxxxx*********\n");
    return 0;
}

int e;
char my_packet[100];
string lora_buffer[BUFNUM] = {""};
int read_flag = 0;
int write_flag = 0;
int counting = 0;

void setup()
{
  // Print a start message
  printf("SX1272 module and Raspberry Pi: receive packets without ACK\n");

  // Power ON the module
  e = sx1272.ON();
  printf("Setting power ON: state %d\n", e);

  // Set transmission mode
  e |= sx1272.setMode(4);
  printf("Setting Mode: state %d\n", e);

  // Set header
  e |= sx1272.setHeaderON();
  printf("Setting Header ON: state %d\n", e);

  // Select frequency channel
  e |= sx1272.setChannel(CH_13_868);
  printf("Setting Channel: state %d\n", e);

  // Set CRC
  e |= sx1272.setCRC_ON();
  printf("Setting CRC ON: state %d\n", e);

  // Select output power (Max, High or Low)
  e |= sx1272.setPower('H');
  printf("Setting Power: state %d\n", e);

  // Set the node address
  e |= sx1272.setNodeAddress(1);
  printf("Setting Node address: state %d\n", e);

  // Print a success message
  if (e == 0)
    printf("SX1272 successfully configured\n");
  else
    printf("SX1272 initialization failed\n");

  delay(1000);
}

void loop(void)
{
  // Receive message
  e = sx1272.receivePacketTimeout(5000);
  if ( e == 0 )
  {
    printf("Receive packet, state %d\n",e);

    for (unsigned int i = 0; i < sx1272.packet_received.length; i++)
    {
      my_packet[i] = (char)sx1272.packet_received.data[i];
    }
    printf("Message: %s\n", my_packet);
    lora_buffer[read_flag] = my_packet;
    char* send;
    send = my_packet;
    usePython(send);
  }
  else {
    printf("Receive packet, state %d\n",e);
  }
}

int main (){
	setup();
	while(1){
		loop();
	}
	return (0);
}
