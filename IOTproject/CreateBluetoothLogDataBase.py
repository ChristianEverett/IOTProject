#!/usr/bin/python
__author__ = 'Christian Everett'

import MySQLdb as mdb
import md5
import socket
import os
import time

import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BCM)
GPIO.setup(6, GPIO.IN)

cnx = mdb.connect('127.0.0.1','root','rootpassword')

if (GPIO.input(6) == True or True):
    with cnx:
        print("Creating DataBase...")
        cur = cnx.cursor()
        cur.execute("CREATE DATABASE IF NOT EXISTS BluetoothLog")
        cur.execute("USE BluetoothLog")
        cur.execute("DROP TABLE IF EXISTS login")
        cur.execute("CREATE TABLE login (username CHAR(32), password CHAR(64), role CHAR(1), email CHAR(35), email_enable boolean)")

        m = md5.new()
        m.update("admin") #password
        epw= m.hexdigest()
        cur.execute("INSERT INTO login (username, password, email, role, email_enable) \
		VALUES( 'admin',%s,'ceverett@cs.uml.edu','a', True)", epw)

        cur.execute("DROP TABLE IF EXISTS RoomStatus")
        cur.execute("CREATE TABLE IF NOT EXISTS RoomStatus (device_name char(25), MAC char(25), device_present boolean)")
        cur.execute("CREATE TABLE IF NOT EXISTS RoomLog (action char(20), device_name char(25), MAC char(17), date char(11), time char(12))")
        cur.execute("INSERT INTO RoomLog (action, device_name, MAC, date, time) \
		VALUES( 'Initialized' ,%s, '00:15:83:E5:0F:66' , %s, %s)", (socket.gethostname() + "(pi)", time.strftime("%d/%m/%Y"), time.strftime("%I:%M:%S")))
        cur.execute("CREATE TABLE IF NOT EXISTS BluetoothPingStatus (action boolean, status boolean, bluetoothLock boolean)")
        cur.execute("INSERT INTO BluetoothPingStatus (action, status, bluetoothLock) VALUES(1, 1, 0)")
        print("DataBase Created")
else:
    print "\nNo action was taken\n"