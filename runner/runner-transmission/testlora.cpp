/*  
 *  LoRa 868 / 915MHz SX1272 LoRa module
 *  
 *  Copyright (C) Libelium Comunicaciones Distribuidas S.L. 
 *  http://www.libelium.com 
 *  
 *  This program is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU General Public License as published by 
 *  the Free Software Foundation, either version 3 of the License, or 
 *  (at your option) any later version. 
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License 
 *  along with this program.  If not, see http://www.gnu.org/licenses/. 
 *  
 *  Version:           1.2
 *  Design:            David Gascón 
 *  Implementation:    Covadonga Albiñana, Victor Boria, Ruben Martin
 */
 
#include <Python.h>
#include <iostream>
#include <fstream>
#include <string>
#include "arduPiLoRa.h"

using namespace std;

#define DATANUM 20
char buffer[256];
string str[DATANUM]={""};
int e;

int UsingPython(){
    Py_Initialize();
    if (!Py_IsInitialized())
    {
        return -1;
    }
    PyRun_SimpleString("execfile('./pulse.py')");
    Py_Finalize();
    return 0;
}

int ReadFile(){
    int i=0;
    ifstream data("datafile.txt");
    if(!data.is_open()){
        cout << "ERROR";
        return -1;
    }
    for (i=0;i<DATANUM;i++){
        while(!data.eof()){
            data.getline(buffer,100);
            if(strcmp(buffer,"")!=0){
		e = sx1272.sendPacketTimeout(1, buffer);
                printf("Packet sent, state %d\n",e); 
	        printf("data is: %s\n",buffer);   
	    }       
            delay(2000);
        }      
    }
    data.close();
    return 0;
}

void setup()
{
  // Print a start message
  printf("SX1272 module and Raspberry Pi: send packets without ACK\n");
  
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
  e |= sx1272.setNodeAddress(3);
  printf("Setting Node address: state %d\n", e);
  
  // Print a success message
  if (e == 0)
    printf("SX1272 successfully configured\n");
  else
    printf("SX1272 initialization failed\n");

  delay(1000);
}

int main (){
	setup();
	int flag = 0;
	while(!flag){
		flag = UsingPython();
		if(!flag){
			flag = ReadFile();
		}
	}
	return (0);
}
