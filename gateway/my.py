#!/usr/bin/python
# Filename: script.py
import MySQLdb
import types

import os
import httplib
import urllib
import urllib2
import json
import subprocess
import time

race_id = {}
last_time = {}
#datapacket = {'username':'','sensor':'','data',0}
datapacket = {'username':'','data':0,'sensor':'','runnumber':''}
headers = {"Content-type": "application/x-www-form-urlencoded"}
url = "m2m-ehealth.appspot.com"
res = None

def trydb(query):
    print query
    f = open("race.txt",'r')
    print 'open'
    for line in f:
        data = line.split(",")
        race_id[data[0]] = int(data[1].replace("\n",""))
        print race_id[data[0]]
    f.close()
    print 'close'
    updatefile = open("lastupdate.txt","r")
    print 'open again'
    for line in updatefile:
        data = line.split(",")
        print data
        last_time[data[0]] = str(data[1].replace("\n",""))
        print last_time[data[0]]
    updatefile.close()
    print 'close again'
    #get current time
    print 'get current'
    timeStamp = int(time.mktime(time.localtime( time.time() ) ))
    print timeStamp
    #split the query
    print 'split query'
    datalist = query.split(" ")
    print datalist
    user = datalist[0]
    print user
    pulse = int(datalist[1])
    oxygen = int(datalist[2])
    print pulse
    print oxygen
    #check if it is the current running,update the race id
    print last_time[user]
    last_t = int(time.mktime(time.strptime(last_time[user], "%Y-%m-%d %H:%M:%S")))
    print last_t
    if((timeStamp-last_t)>1800):
        race_num = race_id[user]
        race_num = race_num+1
        race_id[user] = race_num
    print race_id[user]
    #send data to cloud
    datapacket['username'] = user
    datapacket['sensor'] = 'pulse'
    datapacket['data'] = pulse
    datapacket['runnumber'] = race_id[user]
    try:
        print 'sending'
        conn = httplib.HTTPConnection(url, timeout=8)
        conn.request('POST', "/gateway", json.dumps(datapacket), headers)
        response = conn.getresponse()
        res = response.reason
    except:
        print "Error Sending Data to m2m-ehealth.appspot.com/gateway"
    conn.close()
    print "AppEngine pulse" + res
    datapacket['username'] = user
    datapacket['sensor'] = 'oxygen'
    datapacket['data'] = oxygen
    datapacket['runnumber'] = race_id[user]
    try:
        print 'sending2'
        conn = httplib.HTTPConnection(url, timeout=8)
        conn.request('POST', "/gateway", json.dumps(datapacket), headers)
        response = conn.getresponse()
        res = response.reason
    except:
        print "Error Sending Data to m2m-ehealth.appspot.com/gateway"
    conn.close()
    print "AppEngine oxygen" + res
    #update the last data time and race id
    last_time[user] = timeStamp
    f = open("race.txt",'w')
    for run in race_id:
        insert = run+","+str(race_id[run])+"\n"
        f.write(insert)
    f.close()
    updatefile = open("lastupdate.txt","w")
    for update in last_time:
        insert = update+","+str(time.strftime("%Y-%m-%d %H:%M:%S",time.localtime(timeStamp)))+"\n"
        updatefile.write(insert)
    updatefile.close()
