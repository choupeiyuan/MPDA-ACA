/*
 * ArrayColumnComparator for String
 */

package mixedmodeaca;

import java.util.Comparator;

/**
 *
 * @author Administrator
 */
class ArrayColumnComparator1 implements Comparator {

	private int columnToSortOn = 0;

	// Constructor takes & stores the column to use for sorting
	ArrayColumnComparator1(int columnToSortOn) {
	this.columnToSortOn = columnToSortOn;
	}

	// Return the result of comparing the two row arrays
	public int compare(Object o1, Object o2) {

	// cast the object args back to string arrays
	String[] row1 = (String[])o1;
	String[] row2 = (String[])o2;


	// compare the desired column values & return result
	return row1[columnToSortOn].compareTo(row2[columnToSortOn]);
	//return Double.compare(row1[columnToSortOn],row2[columnToSortOn]);
	}
}
