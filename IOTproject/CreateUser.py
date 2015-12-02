#!/usr/bin/python
__author__ = 'Christian Everett'

#Script args
#username, password, role, email, emailEnabled

import sys, getopt, re
import MySQLdb, md5, socket, os
import smtplib
#import pdb; pdb.set_trace()

db = MySQLdb.connect('127.0.0.1','root','rootpassword')

def addUser(username, password, role, email, emailEnable):
    with db:
        cursor = db.cursor()
        cursor.execute("USE BluetoothLog")

        cursor.execute("SELECT * FROM login WHERE username = %s", username)

        if (cursor.fetchone() is not None):
            print ("User already exists.")
            sys.exit()
        if(re.match(r"[^@]+@[^@]+\.[^@]+", email) is None):
            print ("Email is invalid.")
            sys.exit()


        m = md5.new()
        m.update(password) #password
        epassword= m.hexdigest()
        cursor.execute("INSERT INTO login (username, password, role, email, email_enable) \
                VALUES( %s, %s, %s, %s, %s)", (username, epassword, role, email, emailEnable))

if (len(sys.argv) < 6):
    print("Must specify Username, password, role and email.\n")
    sys.exit()

addUser(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])

print("User: " + sys.argv[1] + " Added")
os.system("./SendEmail.py 'New User Added' 'New User Added: '" + sys.argv[1])





