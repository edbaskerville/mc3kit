#!/usr/bin/env python

import sys
import csv
from mc3kit import *

if __name__ == '__main__':
	dbDir = sys.argv[1]
	iters, llTable = getMultiChainLogLikelihoodTable(dbDir)

	chainCount = llTable.shape[1]
	cw = csv.writer(sys.stdout)
	cw.writerow(["iteration"] + ['chain_{0}'.format(cid) for cid in range(chainCount)])

	for ind, i in enumerate(iters):
		cw.writerow([i] + llTable[ind,:].tolist())
