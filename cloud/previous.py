import os

import MySQLdb
import webapp2
import types
import json


# These environment variables are configured in app.yaml.
CLOUDSQL_CONNECTION_NAME = os.environ.get('CLOUDSQL_CONNECTION_NAME')
CLOUDSQL_USER = os.environ.get('CLOUDSQL_USER')
CLOUDSQL_PASSWORD = os.environ.get('CLOUDSQL_PASSWORD')


def connect_to_cloudsql():
    # When deployed to App Engine, the `SERVER_SOFTWARE` environment variable
    # will be set to 'Google App Engine/version'.
    if os.getenv('SERVER_SOFTWARE', '').startswith('Google App Engine/'):
        # Connect using the unix socket located at
        # /cloudsql/cloudsql-connection-name.
        cloudsql_unix_socket = os.path.join(
            '/cloudsql', CLOUDSQL_CONNECTION_NAME)

        db = MySQLdb.connect(
            unix_socket=cloudsql_unix_socket,
            user=CLOUDSQL_USER,
            passwd=CLOUDSQL_PASSWORD)

    # If the unix socket is unavailable, then try to connect using TCP. This
    # will work if you're running a local MySQL server or using the Cloud SQL
    # proxy, for example:
    #
    #   $ cloud_sql_proxy -instances=your-connection-name=tcp:3306
    #
    else:
        db = MySQLdb.connect(
            host='104.199.11.47', user=CLOUDSQL_USER, passwd=CLOUDSQL_PASSWORD)

    return db


class Register(webapp2.RequestHandler):
    def post(self):
        """
        register new user
        parameter:username,password
        """
        self.response.headers['Content-Type'] = 'text/plain'
        content = self.request.POST.items()
        data = content[0][0]
        unpack = json.loads(data)
        username = unpack['username']
        password = unpack['password']
        db = connect_to_cloudsql()
        cursor = db.cursor()
        cursor.execute('USE testing')
        cursor.execute('INSERT INTO usertable (username,upassword) VALUES(%s,%s)',(username,password))
        db.commit()
        db.close()

class Login(webapp2.RequestHandler):
    def post(self):
        """
        user login
        parameter:username,password
        return: correct/wrong
        """
        verify = "wrong"
        self.response.headers['Content-Type'] = 'text/plain'
        content = self.request.POST.items()
        data = content[0][0]
        unpack = json.loads(data)
        username = unpack['username']
        get_password = unpack['password']
        db = connect_to_cloudsql()
        cursor = db.cursor()
        cursor.execute('USE testing')
        cursor.execute("SELECT upassword FROM usertable WHERE username = %s",(username,))
        read_password = cursor.fetchall()
        password = ''.join(read_password[0])
        db.close()
        if(cmp(get_password,password)==0):
            verify = "correct"
        in_json = json.dumps(verify)
        self.response.write(verify)


class Previous(webapp2.RequestHandler):
    def post(self):
        """
        get previous data from database by using the username
        calculate max,min,average data for each run
        parameter:username
        return:runid,max,min,average
        """
        datalist = []
        self.response.headers['Content-Type'] = 'text/plain'
        content = self.request.POST.items()
        packet = content[0][0]
        unpack = json.loads(packet)
        username = unpack['userlogin']
        db = connect_to_cloudsql()
        cursor = db.cursor()
        cursor.execute('USE testing')
        cursor.execute("SELECT DISTINCT runnumber FROM sensordata WHERE username = %s",(username,))
        runnumbers = cursor.fetchall()
        for runnumber in runnumbers:
            s = runnumber[0]
            cursor.execute("SELECT max(sdata) as max_value,min(sdata) as min_value,avg(sdata) as avg_value FROM sensordata WHERE username = %s AND runnumber = %s",(username,s))
            data = cursor.fetchall()
            max_value = str(data[0][0])
            min_value = str(data[0][1])
            avg_value = str(data[0][2])
            datalist.append(s)
            datalist.append(max_value)
            datalist.append(min_value)
            datalist.append(avg_value)
        db.commit()
        db.close()
        jsondata = json.dumps(datalist)
        self.response.write(jsondata)

class MainPage(webapp2.RequestHandler):
    def get(self):
        """
        Simple request handler that shows all of the MySQL variables.
        """
        self.response.headers['Content-Type'] = 'text/plain'
        db = connect_to_cloudsql()
        cursor = db.cursor()
        cursor.execute('USE testing')
        cursor.execute('SELECT * FROM usertable')
        for r in cursor.fetchall():
            self.response.write('{}\n'.format(r))
        db.commit()
        db.close()


app = webapp2.WSGIApplication([
    ('/', MainPage),
    ('/register',Register),
    ('/login',Login),
    ('/previous',Previous)
], debug=True)
