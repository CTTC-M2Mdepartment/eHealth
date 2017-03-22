#!/usr/bin/python
# Filename: script.py
import MySQLdb
import types

def trydb(word):
    conn= MySQLdb.connect(
        host='localhost',
        port = 3306,
        user='root',
        passwd='657477181',
        db ='test',
        )
    cur = conn.cursor()
    print type(word)
    cur.execute("insert into lora values('2','lora2','99',99)")
    cur.close()
    conn.commit()
    conn.close()
