package edu.uc.rphash.decoders;

import java.util.HashMap;
import java.util.Random;

import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.util.VectorUtil;

public class TestLSHCollisionProbabilities {
	public static void main(String[] args) {
		Random r = new Random();
		int d = 24;

//		E8 e8 = new E8(1f);
//		MultiDecoder sp = new MultiDecoder( d, e8);
//		PsdLSH sp = new PsdLSH(2,24);
//		Leech sp = new Leech(1f);
		Spherical sp = new Spherical(24, 6, 1);
		MurmurHash hash = new MurmurHash(Integer.MAX_VALUE);
		float testResolution = 10000f;

		HashMap<Long, Integer> ctmap = new HashMap<Long, Integer>();

		for (int i = 0; i < 400; i++) {
			int ct = 0;
			float distavg = 0.0f;
			for (int j = 0; j < testResolution; j++) {
				float p1[] = new float[d];
				float p2[] = new float[d];

				// generate a vector
				for (int k = 0; k < d; k++) {
					p1[k] = r.nextFloat() * 2 - 1f;
					p2[k] = (float) (p1[k] + r.nextGaussian()
							* ((float) i / 1000f));
				}
				float dist = VectorUtil.distance(p1, p2);
				distavg += dist;
				long[] l1 = sp.decode(p1);
				long[] l2 = sp.decode(p2);

				ctmap.put(l1[0],
						ctmap.containsKey(l1[0]) ? 1 + ctmap.get(l1[0]) : 1);

				long hp1 = hash.hash(l1);
				long hp2 = hash.hash(l2);

				// ctmap.put(hp1,ctmap.containsKey(hp1)?1+ctmap.get(hp1):1);

				ct += (hp2 == hp1) ? 1 : 0;

			}

			System.out.println(distavg / testResolution + "\t" + (float) ct
			/ testResolution);
		}
	}
}
