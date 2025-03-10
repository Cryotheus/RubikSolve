//created this, but bit-packing made more sense for what I was making it for
//I wanted packing for 10x base-12 digits
//
//an int can store ~8.6 base-12 digits without negative bit utilization
//round down to 8 per int
//
//though about it
//and 1x base-12 digit can fit in 4 bits
//an int has 32 bits
//so one int could store

//1x base 12 digit can be stored in
public class RadixPackedArray {
	public int[] Array;

	//how many digits of the radix provided we can fit in an int
	public int IntDigitCapacity;

	//ALWAYS USE THE SAME RADIX!
	//OR IT WILL VIOLENTLY BREAK!
	public RadixPackedArray(int radix, int digits) {
		IntDigitCapacity = intDigitCapacity(radix);
		Array = new int[IntDigitCapacity];
	}

	//counts how many 32-bit signed integers are needed
	//for storing the `digits` of the given `radix`
	public int intCount(int digits) {
		return Math.ceilDiv(digits, IntDigitCapacity);
	}

	//how many digits of base `radix` can fit in an int
	public static int intDigitCapacity(int radix) {
		//I miss Rust's integer types :(
		//I shouldn't need floats/doubles for logarithm
		//I should be able to specify the base
		//this is terrible
		return (int) Math.floor(Math.log(2_147_483_647) / Math.log(radix));
	}

	public int getInt(int digit) {
		return Array[getIntIndex(digit)];
	}

	//get the index of the int that holds the given digit
	public int getIntIndex(int digit) {
		return Math.floorDiv(digit, IntDigitCapacity);
	}

	//shifts the higher-index digits down truncating the lower-index digits
	public int getBig(int radix, int digit) {
		return Math.floorDiv(getInt(digit), (int) Math.pow(radix, digit % IntDigitCapacity));
	}

	//get the lower-index end
	public int getLittle(int radix, int digits) {
		int digitsMod = (digits % IntDigitCapacity);

		if (digitsMod == 0)
			return getInt(digits);
		else
			return getInt(digits) % (int) Math.pow(radix, digitsMod + 1);
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

		Array[getIntIndex(digit)] = big * ((int) Math.pow(radix, digit % IntDigitCapacity)) + little;
	}
}
