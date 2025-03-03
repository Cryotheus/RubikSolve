// "radix packing"
// esoteric compression, it's essentially bit-packing with bases other than 2
//
// in this case, the base (aka radix)
// must be specified each time the operation is performed
//
// we don't want to store the radix
// as that defeats the point of representing data as a single integer
//
//
//
//
//
//
public class RadixPacked {
	public int Packed;

	public RadixPacked() {
		Packed = 0;
	}

	public void fill(int radix, int digits, int value) {
		Packed = getBig(radix, digits) * radix ^ digits;

		for (int digit = 0; digit < digits; digit++) {
			Packed += value * (radix ^ digit);
		}
	}

	public int getBig(int radix, int digit) {
		return Math.floorDiv(Packed, radix ^ digit);
	}

	public int getLittle(int radix, int digit) {
		return Packed % (radix ^ digit);
	}

	public int getDigit(int radix, int digit) {
		return getBig(radix, digit) % radix;
	}

	public void setDigit(int radix, int digit, int value) {
		int little;
		int big;
	}

	public void truncateLittle(int radix, int digits) {
		Packed = getBig(radix, digits) * radix ^ digits;
	}
}
