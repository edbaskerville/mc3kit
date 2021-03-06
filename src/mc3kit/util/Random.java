/***
  This file is part of mc3kit.
  
  Copyright (C) 2013 Edward B. Baskerville

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ***/

package mc3kit.util;

import static mc3kit.util.Math.*;

import java.math.BigInteger;

import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;

public final class Random {
	public static int nextIntFromToExcept(Uniform unif, int start, int end,
			int except) {
		int i = unif.nextIntFromTo(start, end - 1);
		if(i == except)
			i = end;
		return i;
	}
	
	public static BigInteger nextBigIntegerFromTo(RandomEngine rng, BigInteger from, BigInteger to)
	{
		if(from.compareTo(to) > 0) return null;
		
		BigInteger range = to.subtract(from).add(BigInteger.ONE);
		
		int numBits = range.bitLength() + 1;
		if(numBits % 32 != 0)
			numBits = (numBits / 32 + 1) * 32;
		assert(numBits >= range.bitLength());
		
		byte[] bytes = new byte[numBits / 8];
		for(int i = 0; i < bytes.length / 4; i++)
		{
			int val = rng.nextInt();
			bytes[i*4]     = (byte)(val >>> 24);
			bytes[i*4 + 1] = (byte)(val >>> 16);
			bytes[i*4 + 2] = (byte)(val >>>  8);
			bytes[i*4 + 3] = (byte)(val       );
		}
		bytes[0] = (byte)(bytes[0] & Byte.MAX_VALUE);
		
		return new BigInteger(bytes).remainder(range).add(from);
	}
	
	public static IterableBitSet uniformRandomSubset(Uniform unif, int n, int k) {
		IterableBitSet successes = new IterableBitSet(n);
		for(int i = 0; i < k; i++) {
			int success;
			do {
				success = unif.nextIntFromTo(0, n - 1);
			} while(successes.get(success));
			successes.set(success);
		}
		return successes;
	}
	
	public static int nextDiscreteLinearSearch(RandomEngine rng,
			double[] weights) {
		double sumWeights = sum(weights);
		double u = rng.nextDouble() * sumWeights;
		double cumSum = 0.0;
		for(int i = 0; i < weights.length; i++) {
			cumSum += weights[i];
			if(u < cumSum) {
				return i;
			}
		}
		return weights.length - 1;
	}
}
