import os
import sqlite3
from flask import Flask, render_template, request, Response, g, send_from_directory
from urllib.request import Request, urlopen
import urllib
import json

app = Flask(__name__)

@app.errorhandler(404)
def page_not_found(e):
    return render_template('404.html'), 200

@app.errorhandler(500)
def internal_server_error(e):
    return render_template('500.html'), 200

@app.route("/")
def _index():
    return render_template("map.html");

@app.route("/logo", methods=['GET'])
def logo():
    with open("logo.svg", "rb") as logo:
        contents = logo.read()
    return Response(contents, mimetype='image/svg+xml')

@app.route('/static/<path:path>')
def send_static(path):
    return send_from_directory('static', path, cache_timeout=0)

if __name__ == "__main__":
    app.run(host='127.0.0.1', port=8000, debug=True)
