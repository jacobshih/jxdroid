################################################################################
import codecs
import hashlib
import inspect
import json
import locale
import os
import sys
import time
import urllib
import urllib2
from urlparse import urlparse, parse_qs

from bs4 import BeautifulSoup

if __name__ == "__main__":
    # prevent python from generating compiled byte code (.pyc).
    sys.dont_write_bytecode = True

################################################################################

__all__ = []


def export(obj):
    __all__.append(obj.__name__)
    return obj


################################################################################
# global constants
k_urlbase_county = "http://www.319.com.tw/county/show"
k_urlbase_village = "http://www.319.com.tw/village/show"
k_urlbase_store = "http://www.319.com.tw/store/show"
k_urlbase_storecode = "http://www.319.com.tw/store/gotcode"
k_urlbase_checkin = "http://www.319.com.tw/cwApp/qrcode/checkin"
k_path_user = "./user"
k_path_data = "./data"
k_path_counties = "%s/%s" % (k_path_data, "counties")
k_path_villages = "%s/%s" % (k_path_data, "villages")
k_path_stores = "%s/%s" % (k_path_data, "stores")
k_file_json = ".json"
k_file_stores = "stores" + k_file_json
k_file_token = "%s/%s" % (k_path_user, "token.json")
k_step_interval = 5

twall = [1, 2, 3, 4, 5, 7, 8, 9, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25]


################################################################################
# deco_print() is a decorator function that helps print message in the format
# that decorated funciton defines.
#
def deco_print(f):
    # noinspection PyDecorator
    @classmethod
    def d_f(cls, *args):
        msg = ""
        if Log.level != LogLevel.SILENT:
            if len(args) > 0:
                msg += f(cls)
                for i in args:
                    msg += str(i) + " "
                pass
            print msg
            pass
        pass

    return d_f


################################################################################
class LogLevel:
    (SILENT, DEBUG, INFO, WARN, ERROR, FATAL) = \
        (0, 1, 2, 3, 4, 5)

    def __init__(self):
        pass

    pass


################################################################################
# class Log
#
@export
class Log:
    level = LogLevel.DEBUG

    def __init__(self):
        pass

    ################################################################################
    # _api_log() prints message in the format:
    #       filename(line number): message
    # e.g.:
    #       http.py(112): >>>>>  GET http://2.10.86.188/users/verify.cgi
    #
    @deco_print
    def _api_log(self):
        separator = "\\" if os.name is "nt" else "/"
        line = str(inspect.stack()[3][2])
        fname = inspect.stack()[3][1].split(separator)[-1]
        return fname + "(" + line + "): "

    @classmethod
    def debug(cls, *args):
        if cls.level <= LogLevel.DEBUG:
            cls._api_log(*args)

    @classmethod
    def info(cls, *args):
        if cls.level <= LogLevel.INFO:
            cls._api_log(*args)

    @classmethod
    def warn(cls, *args):
        if cls.level <= LogLevel.WARN:
            cls._api_log(*args)

    @classmethod
    def error(cls, *args):
        if cls.level <= LogLevel.ERROR:
            cls._api_log(*args)

    ################################################################################
    # beautify_json() converts the json string in a beautiful format. it returns
    # the orginal string if it is not a valid json.
    #
    @classmethod
    def beautify_json(cls, jsondict, beautify=True):
        encoding = locale.getdefaultlocale()[1] or "utf8"
        jsonstr = ""
        try:
            if beautify:
                jsonstr = json.dumps(jsondict, indent=2, sort_keys=True, ensure_ascii=False).encode(encoding)
            else:
                jsonstr = json.dumps(jsondict, ensure_ascii=False).encode(encoding)
        except ValueError, e:
            del e
            pass
        return jsonstr

    ################################################################################
    # beautify_jsonstr() converts the json string in a beautiful format. it returns
    # the orginal string if it is not a valid json.
    #
    @classmethod
    def beautify_jsonstr(cls, jsonstr, beautify=True):
        try:
            jsondict = json.loads(jsonstr)
            jsonstr = cls.beautify_json(jsondict, beautify)
        except ValueError, e:
            del e
            pass
        return jsonstr

    ################################################################################
    # whoami() return the name of the caller function.
    #
    @classmethod
    def whoami(cls):
        caller_name = inspect.stack()[1][3]
        return caller_name


################################################################################
# parse_json() parses an json file and returns the parsed elements in dictionary.
#
@export
def parse_json(filename):
    data = {}
    try:
        with codecs.open(filename, "r", "utf-8-sig") as data_file:
            data = json.load(data_file)
    except IOError:
        pass
    except ValueError as e:
        print e
        pass
    return data


def pause():
    if k_step_interval > 0:
        time.sleep(k_step_interval)
    return


def get_villages_in_county(filepath):
    county = parse_json(filepath)
    villages = county["items"] if "items" in county else []
    return villages


def get_stores_in_village(filepath):
    village = parse_json(filepath)
    stores = village["items"] if "items" in village else []
    return stores


def get_county(countyid):
    villages = []
    url = "%s/%s" % (k_urlbase_county, countyid)
    resp = urllib2.build_opener().open(url)
    response = resp.read()
    bs = BeautifulSoup(response, "html.parser", from_encoding="utf-8")
    tag_villages = bs.select("a[class=city]")
    for tag_village in tag_villages:
        bs_village = BeautifulSoup(str(tag_village), "html.parser", from_encoding="utf-8")
        href = bs_village.a["href"]
        index = href.rfind("/")
        villageid = 0 if index == -1 else href[(index + 1):]
        village = dict()
        village["id"] = villageid
        village["name"] = bs_village.text
        village["url"] = "%s/%s" % (k_urlbase_village, villageid)
        villages.append(village)
    return villages


def get_counties():
    for i in twall:
        countyid = str(i)
        countyfile = countyid + k_file_json
        villages = get_county(countyid)
        county = {"items": villages}
        save_to_file(k_path_counties, countyfile, county)
        Log.debug("countyid: %s" % countyid)
        pause()
    return


def merge_stores(saved_stores, stores):
    exstores = []
    for saved_store in saved_stores:
        obsoleted = True
        for store in stores:
            if saved_store["id"] == store["id"]:
                obsoleted = False
                break
        if obsoleted:
            exstores.append(saved_store)

    return stores + exstores


def get_village(villageid):
    stores = []
    url = "%s/%s" % (k_urlbase_village, villageid)
    resp = urllib2.build_opener().open(url)
    response = resp.read().decode("utf8")
    bs = BeautifulSoup(response, "html.parser", from_encoding="utf-8")
    tag_stores = bs.select("div[id=tab2] table tr")
    for tag_store in tag_stores:
        bs_store = BeautifulSoup(str(tag_store), "html.parser", from_encoding="utf-8")
        data = bs_store.select("td")

        href = data[1].a["href"]
        index = href.rfind("/")
        storeid = 0 if index == -1 else href[(index + 1):]
        store = dict()
        store["id"] = storeid
        store["url"] = "%s/%s" % (k_urlbase_storecode, storeid)
        store["category"] = data[0].img["title"]
        store["icon"] = data[0].img["src"]
        store["name"] = data[1].a.text
        store["telephone"] = data[2].text
        store["address"] = data[3].text
        stores.append(store)
    return stores


def get_villages():
    for curdir, subdirs, counties in os.walk(k_path_counties):
        for county in counties:
            filepath = "%s/%s" % (curdir, county)
            villages = get_villages_in_county(filepath)
            for village in villages:
                villageid = village["id"]
                stores = get_village(villageid)
                # +++ merge the saved stores
                villagefile = villageid + k_file_json
                villagepath = "%s/%s" % (k_path_villages, villagefile)
                saved_stores = get_stores_in_village(villagepath)
                thevillage = {"items": merge_stores(saved_stores, stores)}
                # --- merge the saved stores
                save_to_file(k_path_villages, villagefile, thevillage)
                Log.debug("villageid: %s" % villageid)
                pause()
                pass
            pass
        pass
    return


def get_store_coordinates(storeid):
    lat, lng = (0, 0)
    url = "%s/%s" % (k_urlbase_store, storeid)
    resp = urllib2.build_opener().open(url)
    response = resp.read()
    bs = BeautifulSoup(response, "html.parser", from_encoding="utf-8")
    tag_citymap = bs.select("img[class=cityMap]")
    if len(tag_citymap) > 0:
        src = tag_citymap[0]["src"]
        if src is not None:
            srcurl = urlparse(src)
            queries = parse_qs(srcurl.query)
            if "markers" in queries:
                lat, lng = str(queries["markers"][0]).split(",")
    return lat, lng


def get_stores():
    stores = dict()
    for curdir, subdirs, villages in os.walk(k_path_villages):
        for village in villages:
            filepath = "%s/%s" % (curdir, village)
            village_stores = get_stores_in_village(filepath)
            for store in village_stores:
                stores[store["id"]] = store
            pass
        pass
    for storeid in stores:
        store = stores[storeid]
        Log.debug("storeid: %s" % storeid)
        lat, lng = get_store_coordinates(storeid)
        store["coordinates"] = {"latitude": lat, "longitude": lng}
        save_to_file(k_path_stores, k_file_stores, stores)
        pause()
        pass
    # save_to_file(k_path_stores, k_file_stores, stores)
    return


def save_to_file(thepath, thefile, content):
    filepath = "%s/%s" % (thepath, thefile)
    data = Log.beautify_json(content, False) if type(content) is not str else content
    if not os.path.exists(thepath):
        os.makedirs(thepath)
    try:
        with codecs.open(filepath, "w", "utf-8-sig") as f:
            f.write(unicode(data, "utf-8"))
            f.write("\r\n")
    except UnicodeDecodeError as e:
        print "UnicodeDecodeError: %s" % e
    except IOError as e:
        print "IOError: %s" % e
    return


def get_token_from_file():
    user_token = parse_json(k_file_token)
    token = user_token["token"] if "token" in user_token else ""
    return token


def md5(s):
    h = hashlib.md5()
    h.update(s)
    return h.hexdigest()


def verifycode(token, storeid, lat, lng):
    gps_location = "%s,%s:319" % (lat, lng)
    return md5(token + storeid + gps_location)


def checkin(storeid, lat, lng):
    url = k_urlbase_checkin
    q = dict()
    token = get_token_from_file()
    q["token"] = token
    q["verifycode"] = verifycode(token, storeid, lat, lng)
    q["store_id"] = storeid
    q["gps_location"] = "%s,%s" % (lat, lng)
    data = urllib.urlencode(q)
    resp = urllib2.build_opener().open(url, data)
    response = resp.read()
    print Log.beautify_jsonstr(response)


def initialize():
    return


def show_usage():
    appname = os.path.basename(__file__)
    print "usage:"
    print "  python " + appname + " get { counties | villages | stores | all }"
    print "  python " + appname + " checkin { storeid } [ latitude, longitude ]"
    print "  python " + appname + " store { storeid }"
    print ""
    return


def main():
    if len(sys.argv) < 2:
        show_usage()
        return
    initialize()
    mode = sys.argv[1]
    if mode == "help":
        show_usage()
    elif mode == "store":
        if len(sys.argv) < 3:
            show_usage()
            return
        storeid = sys.argv[2]
        lat, lng = get_store_coordinates(storeid)
        print "coordinates: %s : %s" % (lat, lng)
    elif mode == "get":
        if len(sys.argv) < 3:
            show_usage()
            return
        category = sys.argv[2]
        if category == "counties":
            get_counties()
            pass
        elif category == "villages":
            get_villages()
            pass
        elif category == "stores":
            get_stores()
            pass
        elif category == "all":
            get_counties()
            get_villages()
            get_stores()
            pass
    elif mode == "checkin":
        if len(sys.argv) < 3:
            show_usage()
            return
        storeid = sys.argv[2]
        if len(sys.argv) == 5:
            lat = sys.argv[3]
            lng = sys.argv[4]
        else:
            lat, lng = get_store_coordinates(storeid)
            pass
        checkin(storeid, lat, lng)
        pass
    else:
        show_usage()
    return


################################################################################

if __name__ == "__main__":
    main()
    pass
