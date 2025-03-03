// for the packed representation
// the radix is 6, and we have 9 digits
// resulting in 6^9 (10,077,696) possible representations
//
public class Face {
	// we use 6, as there are 6 possible colors a tile on a Rubik's Cube can be
	// the colors will be enumerated 0 to 5 (which is 6 enums)
	static int RADIX = 6;

	//the packed representation of the 9 tiles
	//
	// Index layout
	//  0 1 2
	//  3 4 5
	//  6 7 8
	public RadixPacked Packed;

	public Face(int fill) {
		Packed = new RadixPacked();

		//set all tiles to `fill`
		Packed.fill(RADIX, 9, fill);
	}

	public int getTile(int index) {
		return Packed.getDigit(RADIX, index);
	}

	public void setTile(int index, int tile) {
		Packed.setDigit(RADIX, index, tile);
	}

	// Index layout
	//  0 1 2
	//  3 4 5
	//  6 7 8
	//
	// clockwise
	//  6 3 0
	//  7 4 1
	//  8 5 2
	//
	// ccw
	//  2 5 8
	//  1 4 7
	//  0 3 6
	public void rotate(boolean clockwise) {
		int[] indexMapCw = {};
		int[] indexMapCcw = {};
		RadixPacked newFace = new RadixPacked();


	}
}
