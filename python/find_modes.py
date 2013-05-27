#!/usr/bin/env python

import matplotlib.pyplot as pp
import os
import sys
from collections import OrderedDict
from autohist import *
from sqlighter import *
from mc3kit import *
import json

def getParameterNames(db):
	c = db.cursor()
	paramNames = [x['pname'] for x in c.execute('SELECT * FROM parameters')]
	return paramNames

def getVecs(db, pNames):
	sampCount = getSampleCount(db)
	iterRange = (sampCount - 999, sampCount)

	pVecs = OrderedDict()
	for pName in pNames:
		pVecs[pName] = getFloatParameter(db, pName, iterRange)
	return pVecs

def findModes(db, pNames, pVecs, pRanges):
	pModes = OrderedDict()
	bestHists = OrderedDict()
	for pName in pNames:
		bestHist, hists = autohist(pVecs[pName], xRange=(pRanges[pName] if pName in pRanges else None))
		pModes[pName] = average_modes(hists)
		bestHists[pName] = bestHist
	return pModes, bestHists

def plotHists(pNames, bestHists):
	nVec = len(pNames)
	fig = pp.figure(figsize=(8, 5*nVec))

	for i, pName in enumerate(pNames):
		pp.subplot(nVec, 1, i+1)
		pp.title(pName)
		bestHists[pName].plot()

def plotPairs(db, pNames, pVecs, pModes):
	nVec = len(pVecs)
	fig = pp.figure(figsize=(6*nVec, 4*nVec))

	for i1, pName1 in enumerate(pNames):
		for i2, pName2 in enumerate(pNames):
			print pName1, pName2, len(pVecs[pName1]), len(pVecs[pName2])

			pp.subplot(nVec, nVec, nVec * i1 + i2 + 1)
			pp.scatter(pVecs[pName1], pVecs[pName2])
			pp.xlabel(pName1)
			pp.ylabel(pName2)
			pp.axvline(pModes[pName1])
			pp.axhline(pModes[pName2])

if __name__ == '__main__':
	dbFilename = sys.argv[1]
	db = connectToDatabase(dbFilename)

	createIndexes(db)

	if os.path.exists(dbFilename + '.ranges.json'):
		pRanges = json.load(open(dbFilename + '.ranges.json'))
	else:
		pRanges = {}

	try:
		allPNames = getParameterNames(db)
		pNames = eval('allPNames[{0}]'.format(sys.argv[2]))
	except Exception as e:
		pNames = sys.argv[2:]

	if len(pNames) == 0:
		print 'Usage: find_modes <db-filename> <parameter-names>'
		sys.exit(1)

	pVecs = getVecs(db, pNames)
	pModes, bestHists = findModes(db, pNames, pVecs, pRanges)

	json.dump(pModes, open(dbFilename + '.modes.json', 'w'), indent=2)

	plotHists(pNames, bestHists)
	pp.savefig(dbFilename + '.histograms.png')

	plotPairs(db, pNames, pVecs, pModes)
	pp.savefig(dbFilename + '.bivariate_plots.png')

