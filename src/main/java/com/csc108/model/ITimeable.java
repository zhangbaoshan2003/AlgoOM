package com.csc108.model;

import java.time.LocalDateTime;
import java.util.Comparator;

public interface ITimeable {
	LocalDateTime getDateTime();

	long getQty();

	
	public static ITimeable defaultTimeable(LocalDateTime dt_) {
		return new DefaultTimeable(dt_);
	}
	
	static class DefaultTimeable implements ITimeable{		
		private LocalDateTime dateTime;
		public DefaultTimeable(LocalDateTime dt_) {	
			this.dateTime = dt_;
		}

		@Override
		public LocalDateTime getDateTime() {
			return this.dateTime;
		}

		@Override
		public long getQty() {
			return 0;
		}
	}
	
	// used for sort
	public final static Comparator<ITimeable> TimeComparator = new Comparator<ITimeable>() {

		public int compare(ITimeable o1, ITimeable o2) {
			if (o1 == o2)
				return 0;
			if (o1 == null)
				return -1;
			if (o2 == null)
				return +1;
			return o1.getDateTime().compareTo(o2.getDateTime());
		}
	};

	// used for search from
	public final static Comparator<ITimeable> TimeComparator2 = new Comparator<ITimeable>() {

		public int compare(ITimeable o1, ITimeable o2) {
			if (o1 == o2)
				return 0;
			if (o1 == null)
				return -1;
			if (o2 == null)
				return +1;
			return o1.getDateTime().compareTo(o2.getDateTime()) >= 0 ? 1 : -1;
		}
	};

	// used for search to
	public final static Comparator<ITimeable> TimeComparator3 = new Comparator<ITimeable>() {

		public int compare(ITimeable o1, ITimeable o2) {
			if (o1 == o2)
				return 0;
			if (o1 == null)
				return -1;
			if (o2 == null)
				return +1;
			return o1.getDateTime().compareTo(o2.getDateTime()) <= 0 ? -1 : 1;
		}
	};
}
