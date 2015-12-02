#!/usr/bin/python

__author__ = 'Christian Everett'

#Script args
#device name, MAC, action

import sys, MySQLdb, time
#import pdb; pdb.set_trace()

def main():
    currentTime = time.strftime("%I:%M:%S")
    currentDate = time.strftime("%d/%m/%Y")

    db = MySQLdb.connect('127.0.0.1','root','rootpassword')
    cursor = db.cursor();
    cursor.execute("USE BluetoothLog")

    cursor.execute("INSERT INTO RoomLog (action, device_name, MAC, date, time) \
		VALUES( %s, %s, %s, %s, %s)", ( sys.argv[3], sys.argv[1], sys.argv[2], currentDate, currentTime))

    db.commit()
    print "Entry Added"


if __name__ == "__main__":
    if(len(sys.argv) < 4):
        print "Not enough args"
        sys.exit()

    main()