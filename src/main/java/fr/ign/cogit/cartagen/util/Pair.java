package fr.ign.cogit.cartagen.util;

public class Pair<First, Second> {

	private First first;
	private Second second;

	public Pair(First first, Second second) {
		this.first = first;
		this.second = second;
	}

	public void set1(First first) {
		this.first = first;
	}

	public void set2(Second second) {
		this.second = second;
	}

	public First first() {
		return first;
	}

	public Second second() {
		return second;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Pair<?, ?> pair = (Pair<?, ?>) o;

		if (first != null ? !first.equals(pair.first) : pair.first != null)
			return false;
		if (second != null ? !second.equals(pair.second) : pair.second != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = first != null ? first.hashCode() : 0;
		result = 31 * result + (second != null ? second.hashCode() : 0);
		return result;
	}
}