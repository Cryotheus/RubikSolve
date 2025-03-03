import java.util.Arrays;

public class Rubik {
	Face[] Faces;

	public Rubik() {
		Faces = new Face[6];

		for (int i = 0; i < 6; i++) {
			Faces[i] = new Face(i);
		}
	}

	@Override
	public String toString() {
		return "Rubik{" +
			"Faces=" + Arrays.toString(Faces) +
			'}';
	}
}
