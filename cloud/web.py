import os

import MySQLdb
import webapp2
import types
import json
import datetime


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


class Gateway(webapp2.RequestHandler):
    def post(self):
        """Simple request handler that shows all of the MySQL variables."""
        self.response.headers['Content-Type'] = 'text/plain'
        content = self.request.POST.items()
        data = content[0][0]
        unpack = json.loads(data)
        username = unpack['username']
        typeofsensor = unpack['sensor']
        data = (int)(unpack['data'])
        runnumber = unpack['runnumber']
        db = connect_to_cloudsql()
        cursor = db.cursor()
        cursor.execute('USE testing')
        cursor.execute('insert into senddata(username,typeofsensor,sdata,raceid) values (%s,%s,%s,%s)',(username,typeofsensor,data,runnumber))
        db.commit()
        db.close()

class Register(webapp2.RequestHandler):
    def post(self):
        """
        register new user
        parameter:username,password
        """
        response = ""
        self.response.headers['Content-Type'] = 'text/plain'
        content = self.request.POST.items()
        data = content[0][0]
        unpack = json.loads(data)
        username = unpack['username']
        password = unpack['password']
        db = connect_to_cloudsql()
        cursor = db.cursor()
        cursor.execute('USE testing')
        cursor.execute('select username from usertable where username=%s',(username,))
        check = cursor.fetchall()
        response = len(check)
        if(len(check)==0):
            cursor.execute('INSERT INTO usertable (username,upassword) VALUES(%s,%s)',(username,password))
            response = "success"
        else:
            response = "used"
        db.commit()
        db.close()
        self.response.write(response)
        

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
        if(len(read_password)==0):
            verify = ""
        else:
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

class Current(webapp2.RequestHandler):
    def post(self):
        self.response.headers['Content-Type'] = 'text/plain'
        content = self.request.POST.items()
        packet = content[0][0]
        unpack = json.loads(packet)
        username = unpack['userlogin']
        date = str(unpack['date'])
        format_date = '%'+date+'%'
        db = connect_to_cloudsql()
        cursor = db.cursor()
        cursor.execute('USE testing')
        cursor.execute('select distinct raceid from senddata where username=%s and ts like %s order by ts desc limit 0,1',(username,format_date))
        query = cursor.fetchall()
        for q in query:
            runnumber = str(q[0])
        db.commit()
        db.close()
        jsondata = json.dumps(runnumber)
        self.response.write(jsondata)

class Update(webapp2.RequestHandler):
    def post(self):
        datalist = []
        self.response.headers['Content-Type'] = 'text/plain'
        content = self.request.POST.items()
        packet = content[0][0]
        unpack = json.loads(packet)
        runnumber = unpack['runnumber']
        change = runnumber.strip('["')
        raceid = change.strip('"]')
        db = connect_to_cloudsql()
        cursor = db.cursor()
        cursor.execute('USE testing')
        cursor.execute('select sdata from senddata where raceid=%s and typeofsensor like %s order by ts desc limit 0,10',(raceid,'pulse'))
        query = cursor.fetchall()
        for q in query:
            data = str(q[0])
            datalist.append(data)
        db.commit()
        db.close()
        jsondata = json.dumps(datalist)
        self.response.write(jsondata)
        
class Change(webapp2.RequestHandler):
    def post(self):
        self.response.headers['Content-Type'] = 'text/plain'
        content = self.request.POST.items()
        packet = content[0][0]
        unpack = json.loads(packet)
        username = unpack["username"]
        gender = unpack["gender"]
        birthday = unpack["birthday"]
        rate = int(unpack["rate"])
        db = connect_to_cloudsql()
        cursor = db.cursor()
        cursor.execute('USE testing')
        cursor.execute('update userinformation set gender=%s,birthday=%s,restRate=%s where username=%s',(gender,birthday,rate,username))
        cursor.execute('select gender,birthday,restRate from userinformation where username=%s',(username,))
        query = cursor.fetchall()
        change_gender = ''.join(query[0][0])
        change_birthday = ''.join(query[0][1])
        change_rate = int(query[0][2])
        if(cmp(gender,change_gender)==0 and cmp(birthday,change_birthday)==0 and rate==change_rate):
            response = "ok"
        else:
            response = "error"
        db.commit()
        db.close()
        jsondata = json.dumps(response)
        self.response.write(response)

class Warn(webapp2.RequestHandler):
    def post(self):
        datalist = []
        self.response.headers['Content-Type'] = 'text/plain'
        content = self.request.POST.items()
        packet = content[0][0]
        unpack = json.loads(packet)
        username = unpack["username"]
        db = connect_to_cloudsql()
        cursor = db.cursor()
        cursor.execute('USE testing')
        cursor.execute('select gender,birthday,restRate from userinformation where username=%s',(username,))
        query = cursor.fetchall()
        gender = str(query[0][0])
        birthday = str(query[0][1])
        rate = int(query[0][2])
        db.commit()
        db.close()
        now_time = datetime.datetime.now().strftime('%Y-%m-%d')
        list1 = birthday.split("-")
        list2 = now_time.split("-")
        age = int(list2[0])-int(list1[0])
        month = int(list2[1])-int(list1[1])
        if month < 0:
            age = age - 1
        if month == 0:
            day = int(list2[2])-int(list1[2])
            if day < 0:
                age = age-1
        heart_rate_max = 0;
        if(cmp(gender,"Male")==0):
            heart_rate_max = 220 - age
        else:
            heart_rate_max = 226 - age
        heart_rate_reserve = heart_rate_max - rate
        minpulse = int(heart_rate_reserve*0.6)+rate
        maxpulse = int(heart_rate_reserve*0.7)+rate
        datalist.append(minpulse)
        datalist.append(maxpulse)
        jsondata = json.dumps(datalist)
        self.response.write(datalist)
        

app = webapp2.WSGIApplication([
    ('/', MainPage),
    ('/register',Register),
    ('/login',Login),
    ('/previous',Previous),
    ('/gateway',Gateway),
    ('/current',Current),
    ('/update',Update),
    ('/changeSetting',Change),
    ('/warn',Warn),
], debug=True)
