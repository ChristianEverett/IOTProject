#!/usr/bin/python
__author__ = 'Christian Everett'

import bluetooth
import sys, MySQLdb, time, os
import RPi.GPIO as GPIO
import signal, socket
import threading, thread
#import pdb; pdb.set_trace()

GPIO.setmode(GPIO.BCM)
GPIO.setup(6, GPIO.IN)

GPIO.setup(27, GPIO.OUT) #red led
GPIO.setup(17, GPIO.OUT) #green led

def main():

        print ("Opening Socket connection")

        socketConnection.bind(("10.0.0.5", 8082))
        socketConnection.listen(1)

        socketThread = threading.Thread(target=acceptConnections)
        socketThread.start()

        print ("Started Bluetooth ping")
        while(True):
            pingDevices()

        socketConnection.close()

def acceptConnections():
    while (GPIO.input(6) != True):
        connection, address = socketConnection.accept()
        socketConnectionList.append(SocketHandler(connection, address))

def pingDevices():

    i = 0
    dbHandle.setPingStatus(1)
    enablePollingIndicator(True)
    dbHandle.readTables()
    if (len(dbHandle.MACs) < 1):
        dbHandle.cursor.execute ("UPDATE BluetoothPingStatus SET action=0")
        dbHandle.db.commit()
        idleScript()


    while (webServerComm.runPing and GPIO.input(6) != True):

        #print(dbHandle.MACs[i])
        #print(dbHandle.statuses[i])

        deviceIsPresent = bluetooth.lookup_name(str(dbHandle.MACs[i]))

        if(deviceIsPresent is not None and dbHandle.statuses[i] == False):
            pushToClients(dbHandle.deviceNames[i] + "^" + dbHandle.MACs[i] + "^" + "True\n", socketConnectionList)
            webServerComm.updateDatabase(i, 1)
            if(logging):
                os.system("./Log.py '" + dbHandle.deviceNames[i] + "' '" + dbHandle.MACs[i] + "' 'Entered Room'")

        elif(deviceIsPresent is None and dbHandle.statuses[i] == True):
            pushToClients(dbHandle.deviceNames[i] + "^" + dbHandle.MACs[i] + "^" + "False\n", socketConnectionList)
            webServerComm.updateDatabase(i, 0)
            if(logging):
                os.system("./Log.py '" + dbHandle.deviceNames[i] + "' '" + dbHandle.MACs[i] + "' 'Left Room'")

        else:
            webServerComm.updateDatabase(-1, False)

        i = (i + 1) % len(dbHandle.MACs)

    idleScript()

def pushToClients(data, socketConnectionList):
    for index in range(len(socketConnectionList)):
        socketConnectionList[index].pushToClient(data)

    for index in range(len(removeElements)):
        socketConnectionList.remove(removeElements[index])

    del removeElements[:]

def enablePollingIndicator(enable):
    GPIO.output(17, enable)
    GPIO.output(27, not enable)

def idleScript():
    dbHandle.setPingStatus(0)
    enablePollingIndicator(False)
    while(not webServerComm.checkPingAction()):
        print "Sleeping..."
        time.sleep(5)

#Classes-----------------
class DatabaseHandler:

    MACs = []
    deviceNames = []
    statuses = []

    def __init__(self):
        self.db = MySQLdb.connect('127.0.0.1','root','rootpassword')
        self.cursor = self.db.cursor()
        self.cursor.execute("USE BluetoothLog")

    def readTables(self):
        self.cursor.execute("SELECT MAC FROM RoomStatus")
        self.MACs = self.__tupleToString(self.cursor.fetchall())

        self.cursor.execute("SELECT device_name FROM RoomStatus")
        self.deviceNames = self.__tupleToString(self.cursor.fetchall())

        self.cursor.execute("SELECT device_present FROM RoomStatus")
        self.statuses = self.__tupleToString(self.cursor.fetchall())

    def devicePresenceChanged(self, index, presence):
        self.statuses[index] = presence
        self.cursor.execute ("UPDATE RoomStatus SET device_present=%s WHERE MAC = %s", (str(presence) ,self.MACs[index]))
        self.db.commit()

    def getPingAction(self):
        self.cursor.execute("SELECT * FROM BluetoothPingStatus")
        return self.cursor.fetchall()

    def setPingStatus(self, status):
        self.cursor.execute ("UPDATE BluetoothPingStatus SET status=%s", status)
        self.db.commit()


    def __tupleToString(self, array):

        newArray = []

        for x in range(0, len(array)):
            newArray.append(array[x][0])

        return newArray

class SocketHandler:

    __connection = 0
    __address = 0

    def __init__(self, connection, address):
        print ("Connected to: " + str(address))
        self.__connection = connection
        self.__address = address

    def pushToClient(self, data):
        try:
            self.__connection.send(data)
        except socket.error, exc:
            removeElements.append(self)
            self.__connection.close()
            print "send error"
        except IOError, e:
            removeElements.append(self)
            self.__connection.close()
            print "socket error: %s" % e


class WebServerCommunication:

    runPing = 1
    index = -1
    presence = False

    def checkPingAction(self):
        dbHandle.db.commit()
        pingStatusTable = dbHandle.getPingAction()
        self.runPing = pingStatusTable[0][0] #action False stops bluetooth ping
        print self.runPing
        return self.runPing

    def updateDatabase(self, index, presence):
        self.checkPingAction()
        if(index != -1):
            dbHandle.devicePresenceChanged(index, presence)
        #threadRunning = False
        #exit

def signal_handler(current_signal, frame):
    print("cleaning up from: " + str(signal))
    socketConnection.close()
    GPIO.cleanup()
    dbHandle.db.commit()
    sys.exit(0)

signal.signal(signal.SIGINT, signal_handler)
signal.signal(signal.SIGTERM, signal_handler)
signal.signal(signal.SIGUSR1, signal_handler)
signal.signal(signal.SIGUSR2, signal_handler)

if __name__ == "__main__":

    logging = True

    if(len(sys.argv) > 2):
        logging = False

    socketConnection = socket.socket()
    socketConnection.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    dbHandle = DatabaseHandler()
    webServerComm = WebServerCommunication()
    socketConnectionList = []
    removeElements = []
    main()
