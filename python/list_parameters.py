#!/usr/bin/env python

import sys
from collections import OrderedDict
from autohist import *
from sqlighter import *
from mc3kit import *

if __name__ == '__main__':
	dbFilename = sys.argv[1]
	db = connectToDatabase(dbFilename)

	paramNames = getParameterNames(db)
	for pn in paramNames:
		print pn
