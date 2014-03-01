package ch.epfl.cmiapp.core;

import org.joda.time.LocalDateTime;

public class Slot
{
	public enum BookingStatus { AVAILABLE, REQUEST, BOOKED, BOOKED_SELF, RESTRICTED, MAINTENANCE, NOT_BOOKABLE, DUMMY, INCOMPATIBLE }
	public enum BookingAction { NONE, BOOK, UNBOOK, REQUEST }

	public LocalDateTime getStartTime()
	{
		// TODO Auto-generated method stub
		return null;
	}

	
}
