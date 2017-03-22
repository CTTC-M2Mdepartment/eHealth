#include <python2.7/Python.h>
#include <iostream>
#include <string>
#include <fstream>
#include <malloc.h>
#include "arduPiLoRa.h"

#define BUFNUM 60
#define WAIT 5
using namespace std;

int usePython (char* query) {
    printf("*****************\n");
    //使用python之前，要调用Py_Initialize();这个函数进行初始化
    Py_Initialize();

    PyRun_SimpleString("import sys");
    PyRun_SimpleString("sys.path.append('./')");

    PyObject * pModule = NULL;
    PyObject * pFunc = NULL;
    PyObject * pArgs = NULL;

    //这里是要调用的文件名
    pModule = PyImport_ImportModule("writedbs");
    pFunc = PyObject_GetAttrString(pModule, "trydb");
    pArgs = PyTuple_New(1);
    PyTuple_SetItem(pArgs,0,Py_BuildValue("s",query));
    PyObject_CallObject(pFunc, pArgs);
    Py_DECREF(pFunc);

    Py_Finalize();
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
  e |= sx1272.setPower('L');
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
    cout<<"data is:"<<lora_buffer[read_flag]<<endl;
    cout<<"flag="<<read_flag<<endl;
    read_flag++;
    if(read_flag >= BUFNUM){
	read_flag = 0;
    }
    if(counting >=WAIT){
	write_flag = write_flag +WAIT;
	cout<<"send to database"<<endl;
	char* send;
	int len = lora_buffer[write_flag].length();
	send = (char*)malloc((len+1)*sizeof(char));
	lora_buffer[write_flag].copy(send,len,0);
	usePython(send);
	free(send);
	cout<<"counting="<<counting<<endl;
	//counting = 0;
    }
    counting++;
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
