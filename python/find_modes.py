#!/usr/bin/env python

import matplotlib.pyplot as pp
import os
import sys
from collections import OrderedDict
from autohist import *
from sqlighter import *
from mc3kit import *
import json

def getVecs(db, pNames, burnin, thin):
	pVecs = OrderedDict()
	for pName in pNames:
		pVecs[pName] = getParameter(db, pName, burnin, thin)
	return pVecs

def findModes(db, pNames, pVecs, pDict):
	pModes = OrderedDict()
	bestHists = OrderedDict()
	for pName in pNames:
		pInfo = pDict[pName]
		if 'range' in pInfo:
			xRange = pInfo['range']
		else:
			xRange = None

		bestHist, hists = autohist(pVecs[pName], xRange=xRange)
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
	paramFilename = sys.argv[2]
	burnin = int(sys.argv[3])
	thin = int(sys.argv[4])

	db = connectToDatabase(dbFilename)
	createIndexes(db)

	pDict = json.load(open(paramFilename), object_pairs_hook=OrderedDict)

	pNames = pDict.keys()
	pVecs = getVecs(db, pNames, burnin, thin)
	pModes, bestHists = findModes(db, pNames, pVecs, pDict)

	json.dump(pModes, open(dbFilename + '.modes.json', 'w'), indent=2)

	plotHists(pNames, bestHists)
	pp.savefig(dbFilename + '.histograms.png')

	plotPairs(db, pNames, pVecs, pModes)
	pp.savefig(dbFilename + '.bivariate_plots.png')

