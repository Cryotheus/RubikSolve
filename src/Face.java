// for the packed representation
// the radix is 6, and we have 9 digits
// resulting in 6^9 (10,077,696) possible representations
//
public class Face {
	// we use 6, as there are 6 possible colors a tile on a Rubik's Cube can be
	// the colors will be enumerated 0 to 5 (which is 6 enums)
	static int RADIX = 6;

	static int[] CORNER_INDICES = {0, 2, 6, 8};

	//the packed representation of the 9 tiles
	//
	// Index layout
	//  0 1 2
	//  3 4 5
	//  6 7 8
	public RadixPacked Tiles;

	public Face(int tile) {
		Tiles = new RadixPacked(RADIX, 9, tile);
	}

	public String display() {
		StringBuilder builder = new StringBuilder(21);

		for (int i = 0; i < 9; i++) {
			int tile = Tiles.getDigit(RADIX, i);
			builder.append(' ');
			builder.append(Tile.CHARS[tile]);

			if (i % 3 == 2)
				builder.append('\n');
		}

		return builder.toString();
	}

	public String displaySimple() {
		StringBuilder builder = new StringBuilder(12);

		for (int i = 0; i < 9; i++) {
			builder.append(Tile.CHARS[Tiles.getDigit(RADIX, i)]);

			if (i % 3 == 2)
				builder.append('\n');
		}

		return builder.toString();
	}

	public String displayRow(int rowIndex) {
		StringBuilder builder = new StringBuilder(6);
		int[] row = getRow(rowIndex);

		for (int i = 0; i < 3; i++) {
			builder.append(' ');
			builder.append(Tile.CHARS[row[i]]);
		}

		return builder.toString();
	}

	// always 3 items
	public int[] getColumn(int columnIndex) {
		return getTiles(getColumnIndices(columnIndex));
	}

	// always 3 items
	public int[] getColumnRev(int columnIndex) {
		return getTiles(getColumnIndicesRev(columnIndex));
	}

	// always 3 items
	public int[] getRow(int rowIndex) {
		return getTiles(getRowIndices(rowIndex));
	}

	// always 3 items
	public int[] getRowRev(int rowIndex) {
		return getTiles(getRowIndicesRev(rowIndex));
	}

	// always 3 items
	public int[] getColumnIndices(int columnIndex) {
		return new int[]{
			columnIndex,
			columnIndex + 3,
			columnIndex + 6,
		};
	}

	// always 3 items
	public int[] getColumnIndicesRev(int columnIndex) {
		return new int[]{
			columnIndex + 6,
			columnIndex + 3,
			columnIndex,
		};
	}

	// always 3 items
	public int[] getRowIndices(int rowIndex) {
		final int offsetIndex = rowIndex * 3;

		return new int[]{
			offsetIndex,
			offsetIndex + 1,
			offsetIndex + 2
		};
	}

	// always 3 items
	public int[] getRowIndicesRev(int rowIndex) {
		final int offsetIndex = rowIndex * 3;

		return new int[]{
			offsetIndex + 2,
			offsetIndex + 1,
			offsetIndex,
		};
	}

	// always 3 items
	public int[] getStrip(int strip) {
		return getTiles(getStripIndices(strip));
	}

	// always 3 items
	public int[] getStripIndices(int strip) {
		int endCorner = strip & 0b11;
		int startCorner = strip >> 2;

		//same start and end - useless
		//case 0b00_00:
		//case 0b01_01:
		//case 0b10_10:
		//case 0b11_11:
		if (endCorner == startCorner)
			throw new RuntimeException("getStripIndices given point");

		//diagonal
		//case 0b00_11:
		//case 0b01_10:
		//case 0b10_01:
		//case 0b11_00:
		if (endCorner == (startCorner ^ 0b11))
			throw new RuntimeException("getStripIndices given diagonal");

		return switch (strip) {
			// Index layout
			//  0 1 2       0 - 1       00 -- 01
			//  3 4 5       - - -       -- -- --
			//  6 7 8       2 - 3       10 -- 11
			case 0b00_01 -> getRowIndices(0);
			case 0b00_10 -> getColumnIndices(0);

			case 0b01_00 -> getRowIndicesRev(0);
			case 0b01_11 -> getColumnIndices(2);

			case 0b10_00 -> getColumnIndicesRev(0);
			case 0b10_11 -> getRowIndices(2);

			case 0b11_01 -> getColumnIndicesRev(2);
			case 0b11_10 -> getRowIndicesRev(2);

			default -> throw new RuntimeException("getStripIndices given invalid strip");
		};
	}

	public int getTile(int index) {
		return Tiles.getDigit(RADIX, index);
	}

	// maps tile index to their current tile enums
	public int[] getTiles(int[] indices) {
		int[] mapped = new int[indices.length];

		for (int i = 0; i < indices.length; i++) {
			mapped[i] = Tiles.getDigit(RADIX, indices[i]);
		}

		return mapped;
	}

	public void setStrip(int strip, int[] values) {
		if (values.length != 3)
			throw new RuntimeException("setStrip must be given 3 values");

		int[] indices = getStripIndices(strip);

		for (int i = 0; i < indices.length; i++) {
			Tiles.setDigit(RADIX, indices[i], values[i]);
		}
	}

	public void setTile(int index, int tile) {
		Tiles.setDigit(RADIX, index, tile);
	}

	public void rotate(boolean clockwise) {
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
		int[] indexMap = {2, 5, 8, 1, 4, 7, 0, 3, 6};
		RadixPacked rotated = new RadixPacked();

		if (clockwise)
			for (int i = 0; i < 9; i++) {
				rotated.copy(RADIX, i, indexMap[i], Tiles);
			}
		else
			for (int i = 0; i < 9; i++) {
				rotated.copy(RADIX, indexMap[i], i, Tiles);
			}

		Tiles = rotated;
	}

	public boolean solved() {
		//solved faces have only one tile index in all spots
		//to check, create a solved face from any tile on the face
		//then check if they're the same integer representation
		int firstTile = getTile(0);
		RadixPacked target = new RadixPacked(RADIX, 9, firstTile);

		return Tiles.Packed == target.Packed;
	}
}
