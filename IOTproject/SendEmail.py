#!/usr/bin/python

__author__ = 'Christian Everett'

#script args
#subject, body

#gmail login(ceverettPI, 01433128)

import sys, smtplib, MySQLdb
import email, mimetypes, email.mime.application
#import pdb; pdb.set_trace()

def tupleToString(array):

    newArray = []

    for x in range(0, len(array)):
        newArray.append(array[x][0])

    return newArray

def main():

    db = MySQLdb.connect('127.0.0.1','root','rootpassword')

    with db:
        cursor = db.cursor()
        cursor.execute("USE BluetoothLog")

        cursor.execute("SELECT email FROM login WHERE email_enable = True")

        emailEnabledUsers = tupleToString(cursor.fetchall())

        if (emailEnabledUsers is None):
            return

        msg = email.mime.Multipart.MIMEMultipart()
        msg['Subject'] = sys.argv[1]
        msg['From'] = 'Bluetooth Server'
        msg['To'] = 'RegisteredUsers'

        body = email.mime.Text.MIMEText(sys.argv[2])
        msg.attach(body)

        s = smtplib.SMTP('smtp.gmail.com:587')
        s.starttls()
        s.login('ceverettPI@gmail.com', '01433128')

        s.sendmail( 'ceverettPI@gmail.com' , emailEnabledUsers , msg.as_string())
        s.quit()

if __name__ == "__main__":
    if(len(sys.argv) < 2):
        print "Not enough args"
    main()
    print "Emails Sent"