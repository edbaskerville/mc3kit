import sqlite3
import numpy as np
from sqlighter import *
import json
import os
from collections import OrderedDict

def getChainHeats(chainCount, heatPower):
    chainIds = np.array(range(chainCount), dtype=float)
    return (1.0 - chainIds  / (chainCount - 1.0))**heatPower

def getMultiChainLogLikelihoodTable(dbDir):
    chainCount = max(
        [int(x.split('.')[0]) for x in os.listdir(dbDir) if x.endswith('.sqlite')]
    ) + 1
    iters = None
    iterCount = None
    llTable = None
    for i in range(chainCount):
        filename = os.path.join(dbDir, '{0}.sqlite'.format(i))
        db = sqlite3.connect(filename)
        c = db.cursor()

        if iters is None:
            iters = np.array([x[0] for x in c.execute('SELECT iteration FROM likelihood')])
            iterCount = len(iters)
            llTable = np.zeros((iterCount, chainCount))

        llTable[:,i] = np.array([x[0] for x in c.execute('SELECT logLikelihood FROM likelihood')])

        db.close()
    return iters, llTable

def integrateTrapezoidal(x, y):
    dx = x[1:] - x[:-1]
    ymeans = (y[1:] + y[:-1]) / 2.0
    return np.sum(dx * ymeans)

def integrateLikelihoodsTrapezoid(logLikeTable, heatPower):
    iterCount, chainCount = logLikeTable.shape
    chainIds = np.array(range(chainCount), dtype=float)
    heats = (1.0 - chainIds  / (chainCount - 1.0))**3.0

    integratedLikelihoods = np.zeros(logLikeTable.shape[0], dtype=float)
    for i in range(iterCount):
        integratedLikelihoods[i] = integrateTrapezoidal(
            heats[::-1],
            logLikeTable[i,:][::-1]
        )
    return integratedLikelihoods

def getFloatParameterNames(db):
    firstSamp = getFirstSample(db)
    floatParamNames = list()
    for k, v in firstSamp.items():
        if type(v) is float:
            floatParamNames.append(k)
    return floatParamNames

def getParameterNames(db):
    c = db.cursor()
    paramNames = [x['pname'] for x in c.execute('SELECT * FROM parameters')]
    return paramNames

def getMinIteration(db):
    c = db.cursor()
    x = c.execute('SELECT MIN(iteration) FROM likelihood')
    return x.next()['MIN(iteration)']

def getMaxIteration(db):
    c = db.cursor()
    x = c.execute('SELECT MAX(iteration) FROM likelihood')
    return x.next()['MAX(iteration)']

def getSampleCount(db):
    c = db.cursor()
    x = c.execute('SELECT COUNT(iteration) FROM likelihood')
    return x.next()['COUNT(iteration)']

def getPidNameMap(db):
    c = db.cursor()
    return OrderedDict([(x['pid'], x['pname']) for x in c.execute('SELECT * FROM parameters')])

def getPidForParameter(db, pname):
    c = db.cursor()
    return c.execute('SELECT pid FROM parameters WHERE pname = "{0}"'.format(pname)).next()['pid']

def getFirstSample(db):
    c = db.cursor()
    x = c.execute('SELECT * FROM samples WHERE iteration = {0}'.format(getMinIteration(db)))
    pidNameMap = getPidNameMap(db)
    return OrderedDict([(pidNameMap[y['pid']], y['value']) for y in x])

def getSample(db, iteration):
    c = db.cursor()
    x = c.execute('SELECT * FROM samples WHERE iteration = {0}'.format(iteration))
    pidNameMap = getPidNameMap(db)
    return OrderedDict([(pidNameMap[y['pid']], y['value']) for y in x])

def getMaxPosteriorSample(db, burnin):
    c = db.cursor()
    x = c.execute('SELECT iteration, MAX(logLikelihood + logPrior) FROM likelihood WHERE ROWID >= {0}'.format(burnin))
    sampIter = x.next()['iteration']
    return sampIter, getSample(db, sampIter)

def makeHierarchicalSample(s):
    hs = OrderedDict()
    for kFlat, v in s.items():
        cur = hs
        ks = kFlat.split('.')
        for i, k in enumerate(ks):
            if i == len(ks) - 1:
                if type(v) is str or type(v) is unicode:
                    v = json.loads(v, object_pairs_hook=OrderedDict)
                cur[k] = v
            else:
                if not k in cur:
                    cur[k] = OrderedDict()
                cur = cur[k]
    return hs

def getLogPrior(db, burnin=0, thin=1):
    c = db.cursor()
    c.execute('SELECT logPrior FROM likelihood')
    if burnin > 0:
        c.fetchmany(size=burnin)

    return [y['logPrior'] for i, y in enumerate(c.fetchall()) if i % thin == 0]

def getLogLikelihood(db, burnin=0, thin=1):
    c = db.cursor()
    c.execute('SELECT logLikelihood FROM likelihood')
    if burnin > 0:
        c.fetchmany(size=burnin)

    return [y['logLikelihood'] for i, y in enumerate(c.fetchall()) if i % thin == 0]

def getParameter(db, paramName, burnin=0, thin=1):
    if paramName == 'logLikelihood':
        return getLogLikelihood(db, burnin, thin)
    elif paramName == 'logPrior':
        return getLogPrior(db, burnin, thin)

    c = db.cursor()
    pid = c.execute('SELECT pid FROM parameters WHERE pname = "{0}"'.format(paramName)).next()['pid']
    #print pid
    query = 'SELECT value FROM samples WHERE pid = {0}'.format(pid)
    c.execute(query)
    if burnin > 0:
        c.fetchmany(size=burnin)

    return [y['value'] for i, y in enumerate(c.fetchall()) if i % thin == 0]

def iterateSamples(db, burnin=None, thin=1):
    if burnin is None:
        sampCount = getSampleCount(db)
        burnin = sampCount / 2

    burnIter = db.execute(
        'SELECT iteration FROM likelihood WHERE rowid = {0}'.format(burnin)
    ).next()['iteration']

    c = db.cursor()
    pidNameMap = getPidNameMap(db)
    query = 'SELECT * FROM samples WHERE iteration > {0}'.format(burnIter)

    i = 0
    lastIteration = None
    sampDict = OrderedDict()
    for rowNum, x in enumerate(c.execute(query)):
        if (rowNum / len(pidNameMap)) % thin == 0:
            sampDict[pidNameMap[x['pid']]] = x['value']

            if (rowNum + 1) % len(pidNameMap) == 0:
                assert len(sampDict) == len(pidNameMap)
                yield sampDict
                sampDict = OrderedDict()


def getFloatParameter(db, paramName, iterRange=None, thin=1):
    return [float(x) for x in getParameter(db, paramName, iterRange, thin)]

def getJsonParameter(db, paramName, iterRange=None, thin=1):
    return [json.loads(x) for x in getParameter(db, paramName, iterRange, thin)]

def getSortIndexes(x):
    return sorted([i for i in range(len(x))], key=lambda i: x[i])

def findMax(x):
    return 

def createIndexes(db):
    c = db.cursor()
    createIndex(c, 'samples', 'iteration')
    createIndex(c, 'samples', 'pid')
