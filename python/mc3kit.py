from sqlighter import *
import json
from collections import OrderedDict

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
    x = c.execute('SELECT MAX(iteration) FROM likelihood')
    return x.next()['MAX(iteration)']

def getPidNameMap(db):
    c = db.cursor()
    return OrderedDict([(x['pid'], x['pname']) for x in c.execute('SELECT * FROM parameters')])

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

def getLogPrior(db, iterRange=None):
    c = db.cursor()
    query = 'SELECT logPrior FROM likelihood'
    if iterRange is not None:
        query += ' WHERE iteration BETWEEN {0} AND {1}'.format(iterRange[0], iterRange[1])
    return [y['logPrior'] for y in c.execute(query)]

def getLogLikelihood(db, iterRange=None):
    c = db.cursor()
    query = 'SELECT logLikelihood FROM likelihood'
    if iterRange is not None:
        query += ' WHERE iteration BETWEEN {0} AND {1}'.format(iterRange[0], iterRange[1])
    return [y['logLikelihood'] for y in c.execute(query)]

def getParameter(db, paramName, burnin=0, thin=1):
    c = db.cursor()
    pid = c.execute('SELECT pid FROM parameters WHERE pname = "{0}"'.format(paramName)).next()['pid']
    #print pid
    query = 'SELECT value FROM samples WHERE pid = {0}'.format(pid)
    c.execute(query)
    if burnin > 0:
        c.fetchmany(size=burnin)

    return [y['value'] for i, y in enumerate(c.fetchall()) if i % thin == 0]

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