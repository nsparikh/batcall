package com.neenaparikh.locationsender.model;

import java.util.Comparator;

/**
 * Comparator for comparing Person objects based on last contact time,
 * rather than the default comparator which compares Person names.
 * 
 * Sorts in reverse order, so the most recently contacted Person will
 * be the first in the list.
 * 
 * @author neenaparikh
 *
 */
public class PersonTimeComparator implements Comparator<Person> {

	@Override
	public int compare(Person p1, Person p2) {
		if (p1.getLastContacted() < p2.getLastContacted()) return 1;
		else if (p1.getLastContacted() == p2.getLastContacted()) return 0;
		else return -1;
	}

}
