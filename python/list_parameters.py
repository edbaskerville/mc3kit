#!/usr/bin/env python

import sys
from collections import OrderedDict
from autohist import *
from sqlighter import *


def getParameterNames(db):
	c = db.cursor()
	paramNames = [x['pname'] for x in c.execute('SELECT * FROM parameters')]
	return paramNames

if __name__ == '__main__':
	dbFilename = sys.argv[1]
	db = connectToDatabase(dbFilename)

	paramNames = getParameterNames(db)
	for pn in paramNames:
		print pn
