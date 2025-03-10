// you can think of this as an array of ints with in-memory compression
//
// "radix packing"
// esoteric compression, it's essentially bit-packing with number bases other than 2
// the only other base used in this project is 6
//
// the goal is to make integer arrays TINY
// we do this by only storing a single integer
// and packing all the digits into the integer
// since we can be sure the provided digits are within a ceterain range (say, 0 to 5)
// we use 6 as our base
//
// we don't want to store the radix
// as that defeats the point of representing data as a single integer
// thus, every operation requires the radix
// this radix should be consistent
// but can be a power higher or lower for more advanced usage
// (such as chunk interation)
//
// TODO: make these static!
public class RadixPacked {
	public int Packed;

	public RadixPacked() {
		Packed = 0;
	}

	public RadixPacked(int radix, int digits, int value) {
		Packed = 0;

		fill(radix, digits, value);
	}

	//resets to 0
	public void clear() {
		Packed = 0;
	}

	public void copy(int radix, int digitSrc, int digitDst, RadixPacked source) {
		setDigit(radix, digitDst, source.getDigit(radix, digitSrc));
	}

	//sets the first `digits` amount of digits to `value`
	public void fill(int radix, int digits, int value) {
		//clear the space into which we are adding these digits
		zeroLittle(radix, digits);

		for (int digit = 0; digit < digits; digit++) {
			Packed += value * (int) Math.pow(radix, digit);
		}

		//5 * 36 + 5 * 6 + 5
		//+ 5 * 1
		//+ 5 * 6
		//+ 5 * 36
	}

	//shifts the higher-index digits down truncating the lower-index digits
	public int getBig(int radix, int digit) {
		return Math.floorDiv(Packed, (int) Math.pow(radix, digit));
	}

	//get the lower-index end
	public int getLittle(int radix, int digits) {
		return Packed % (int) Math.pow(radix, digits + 1);
	}

	//will always return an int [0, radix)
	public int getDigit(int radix, int digit) {
		return getBig(radix, digit) % radix;
	}

	//`value` should always be [0, radix)
	public void setDigit(int radix, int digit, int value) {
		//get everything below out digit
		//we need this since we are doing math below that destroys
		//everything below this digit
		//
		//we will add this value back later to recover the lost digits
		int little = getLittle(radix, digit - 1);

		//we get the left part
		//but without our value
		//
		//shift it once to the left
		//so we have the same amount of digits needed when recombining
		//
		//then we add the value of this digit
		//which essentially makes it take the place of the digit we "destroyed"
		int big = getBig(radix, digit + 1) * radix + value;

		Packed = big * ((int) Math.pow(radix, digit)) + little;
	}

	//zeroes the n digits on the lower-index end
	public void zeroLittle(int radix, int digits) {
		Packed = getBig(radix, digits) * ((int) Math.pow(radix, digits));
	}

	@Override
	public String toString() {
		return "<" + Packed + '>';
	}
}
