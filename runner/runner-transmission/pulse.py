# -*- coding: utf-8 -*-
import serial
import time
import types
port = "/dev/ttyACM0"
READNUM = 20
EID = '001'

serialFromArduino = serial.Serial(port,115200)
serialFromArduino.flushInput()
try:
    datafile = open("datafile.txt","w")
    i=0
    time.sleep(1)
    while (i<READNUM):
        if(serialFromArduino.inWaiting()>0):
            input = serialFromArduino.readline()
            #fliter
            data = input.rstrip("\n")
            data = data.split(" ")
            print data
            pulse=int(data[0])
            oxygen=int(data[1])
            if(pulse>=60 and oxygen >=94):
                print pulse
                print oxygen
                packet = EID+' '+timestamp+' '+str(input)
                datafile.write(packet)
            else:
                print "wrong data"
            timestamp = time.strftime("%y-%m-%d %H:%M:%S",time.localtime(time.time()))
            print timestamp
        i=i+1
        print(i)
        time.sleep(2)
except:
    print("no such file")
finally:
    datafile.close()
