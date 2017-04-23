import serial
import time
import types
port = "/dev/ttyACM0"
TIMESLOT = 5
USER = 'user1'
serialFromArduino = serial.Serial(port,115200)
serialFromArduino.flushInput()
data_list=[]
try:
    i = 0
    time.sleep(1)
    while (i<TIMESLOT):
        if(serialFromArduino.inWaiting()>0):
            input = serialFromArduino.readline()
            #fliter
            raw_data = input.rstrip("\n")
            data = raw_data.split(" ")
            print data
            pulse = int(data[0])
            oxygen = int(data[1])
            if(pulse >= 50):
                print pulse
                print oxygen
                data_list.append(pulse)
                data_list.append(oxygen)
            else:
                print "wrong data"
        i = i + 1
        print(i)
        time.sleep(2)
except:
    print("no such file")
finally:
    datafile = open("datafile.txt","w")
    total_pulse = 0;
    total_oxygen = 0;
    packet = ''
    for i in range(len(data_list)/2):
        total_pulse = total_pulse + data_list[2*i]
        total_oxygen = total_oxygen + data_list[2*i+1]
    avg_pulse = total_pulse/(len(data_list)/2)
    avg_oxygen = total_oxygen/(len(data_list)/2)
    if(avg_pulse >0):
        packet = USER+" "+str(avg_pulse)+" "+str(avg_oxygen)
    datafile.write(packet)
    datafile.close()
