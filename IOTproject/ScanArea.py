#!/usr/bin/python
__author__ = 'Christian Everett'

import bluetooth
import sys, MySQLdb, os, signal, time
#import pdb; pdb.set_trace()

dbConnection = MySQLdb.connect('127.0.0.1','root','rootpassword')

def main():

    if (len(sys.argv) > 1):
        duration = sys.argv[1]
    else:
        duration = 10

    print("Scanning Area")
    MACs = bluetooth.discover_devices(duration, True, False)
    print("Scan Complete")
    print("MAC Address found: ")
    print(MACs)

    deviceNamesArray = []
    x = 0

    for MACAddress in MACs:

        devicename = bluetooth.lookup_name(MACAddress, 20)

        if (devicename == None): #device name cant be found
            print ("Cant find device name for:" + MACAddress)
            devicename = "NameNotFound"

        deviceNamesArray.append(devicename)

    print("Devices found: ")
    print(deviceNamesArray)
    addMACAddress(deviceNamesArray, MACs)

def addMACAddress(deviceNameArray, MACs):
    with dbConnection:
        cursor = dbConnection.cursor();
        cursor.execute("USE BluetoothLog")

        for x in range(0, len(MACs)):
            cursor.execute("SELECT * FROM RoomStatus WHERE MAC = %s", MACs[x])

            if (cursor.fetchone() is None):
                print ("Adding: " + deviceNameArray[x])
                cursor.execute("INSERT INTO RoomStatus (device_name, MAC, device_present) \
                VALUES( %s, %s, True)", (deviceNameArray[x], MACs[x]))

                os.system("./Log.py '" + deviceNameArray[x] + "' '" + MACs[x] + "' Discovered")

if __name__ == "__main__":
    #pid = int(os.popen('pgrep PollingScript').readline())
    #os.kill(pid , signal.SIGUSR1)
    #time.sleep(4)
    main()
    dbConnection.commit()
    #os.kill(pid , signal.SIGUSR1)
