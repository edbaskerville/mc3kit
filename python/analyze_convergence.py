#!/usr/bin/env python

import numpy as np
import sys
import json
from collections import OrderedDict
from autohist import *
from sqlighter import *
from mc3kit import *
import pymc
import matplotlib.pyplot as pp

def analyzeConvergence(dbFilename, db, pn, burnin):
	print 'ANALYZING {0}'.format(pn)

	vals = np.array(getParameter(db, pn, burnin=burnin))
	
	convDict = OrderedDict()
	gw = pymc.geweke(vals)
	gwDict = OrderedDict()
	gwDict['scores'] = gw

	frac2SD = len([x for x in gw if x[1] > -2 and x[1] < 2]) / float(len(gw))
	gwDict['frac_2sd'] = frac2SD
	convDict['geweke'] = gwDict

	rl = pymc.raftery_lewis(vals, 0.025, r=0.01)
	rlDict = OrderedDict()
	rlDict['iter_req_acc'] = rl[0]
	rlDict['thin_first_ord'] = rl[1]
	rlDict['burnin'] = rl[2]
	rlDict['iter_total'] = rl[3]
	rlDict['thin_ind'] = rl[4]

	convDict['raftery_lewis'] = rlDict

	print ''

	return convDict

if __name__ == '__main__':
	dbFilename = sys.argv[1]
	burnin = int(sys.argv[2])

	db = connectToDatabase(dbFilename)
	createIndexes(db)

	paramNames = getFloatParameterNames(db)

	convDict = OrderedDict()
	for i, pn in enumerate(paramNames):
		convDict[pn] = analyzeConvergence(dbFilename, db, pn, burnin)

	print('SUMMARY:')

	allFrac2SD = [x['geweke']['frac_2sd'] for x in convDict.values()]
	meanFrac2SD = np.mean(allFrac2SD)
	minFrac2SD = np.min(allFrac2SD)
	print 'fraction of geweke Z-scores within 2 SD: mean {0}, min {1}'.format(meanFrac2SD, minFrac2SD)

	allThins = [x['raftery_lewis']['thin_ind'] for x in convDict.values()]
	meanThin = np.mean(allThins)
	maxThin = np.max(allThins)
	print 'thin needed: mean {0}, max {1}'.format(meanThin, maxThin)

	allIters = [x['raftery_lewis']['iter_total'] for x in convDict.values()]
	meanIter = np.mean(allIters)
	maxIter = np.max(allIters)
	print 'iterations needed: mean {0}, max {1}'.format(meanIter, maxIter)

	jsonFile = open(dbFilename + '.convergence.json', 'w')
	json.dump(convDict, jsonFile, indent=2)
	jsonFile.write('\n')
