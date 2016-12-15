package com.csc108.model.data;

import com.csc108.utility.DateUtil;
import com.csc108.utility.FormattedTable;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.drools.marshalling.impl.ProtobufMessages;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class SessionGroup implements Comparable<SessionGroup> {
	@Getter
	public String name;
	@Getter
	public List<TradingSession> sessions;

	public int count() {
		return sessions == null ? 0 : sessions.size();
	}

	public SessionGroup(String name_, List<TradingSession> sessions_) {
		name = name_;
		sessions = ImmutableList.copyOf(sessions_);
	}

	public boolean isTradable(LocalTime dateTime_) {
		return isTradable(dateTime_, IntervalType.End, AuctionType.All);
	}

	public boolean isTradable(LocalTime dateTime_, IntervalType inveralType_, AuctionType auctionType_) {
		for (TradingSession ts : sessions) {
			if (!ts.isAuctionMatch(auctionType_))
				continue;
			if (inveralType_ == IntervalType.End && dateTime_.isAfter(ts.getStartTime()) && !dateTime_.isAfter(ts.getEndTime())) {
				return ts.isTradable();
			} else if (inveralType_ == IntervalType.Start && !dateTime_.isBefore(ts.getStartTime()) && dateTime_.isBefore(ts.getEndTime())) {
				return ts.isTradable();
			}
		}
		return false;
	}

	/**
	 * note the sessions_ is already sorted symbol_ = 600000.sh
	 */
	public LocalTime getMarketOpen() {
		return getMarketOpen(AuctionType.All);
	}

	public LocalTime getMarketOpen(AuctionType auctionType_) {
		for (TradingSession ts : sessions) {
			if (!isAMAuction(auctionType_) && ts.isAMAuction())
				continue;
			if (ts.isTradable(AuctionType.All))
				return ts.getStartTime();
		}
		return LocalTime.MAX;
	}

	public LocalTime getMarketClose() {
		return getMarketClose(AuctionType.All);
	}

	public LocalTime getMarketClose(AuctionType auctionType_) {
		LocalTime closeTime = LocalTime.MIN;
		for (TradingSession ts : sessions) {
			if (!isClsAuction(auctionType_) && ts.isClsAuction())
				continue;

			if (ts.isTradable(AuctionType.All))
				closeTime = ts.getEndTime();
		}

		return closeTime;
	}

	public boolean isAuctionSession(LocalTime dateTime_) {
		return isAuctionSession(dateTime_, IntervalType.End);
	}

	public boolean isAuctionSession(LocalTime dateTime_, IntervalType intervalType_) {
		return isAMAuctionSession(dateTime_, intervalType_) || isPMAuctionSession(dateTime_, intervalType_) || isClsAuctionSession(dateTime_, intervalType_);
	}

	public boolean isLunchBreak(LocalTime dateTime_) {
		return isLunchBreak(dateTime_, IntervalType.End);
	}

	public boolean isLunchBreak(LocalTime dateTime_, IntervalType intervalType_) {
		TradingSession ts = getSessionByTime(dateTime_, intervalType_);
		return ts.isLunchBreak();
	}

	public boolean isMarketClose(LocalTime dateTime_) {
		return isMarketClose(dateTime_, IntervalType.End);
	}

	public boolean isMarketClose(LocalTime dateTime_, IntervalType intervalType_) {
		TradingSession ts = getSessionByTime(dateTime_, intervalType_);
		return ts.getSessionType() == SessionType.MarketClose;
	}

	public boolean isContinuousSession(LocalTime dateTime_) {
		return isContinuousSession(dateTime_, IntervalType.End);
	}

	public boolean isContinuousSession(LocalTime dateTime_, IntervalType intervalType_) {
		TradingSession ts = getSessionByTime(dateTime_, intervalType_);
		return ts.getSessionType() == SessionType.AMContinuous || ts.getSessionType() == SessionType.PMContinuous;
	}

	public boolean isAMAuctionSession(LocalTime dateTime_) {
		return isAMAuctionSession(dateTime_, IntervalType.End);
	}

	public boolean isAMAuctionSession(LocalTime dateTime_, IntervalType intervalType_) {
		for (TradingSession ts : sessions) {
			if (intervalType_ == IntervalType.End && dateTime_.isAfter(ts.getStartTime()) && !dateTime_.isAfter(ts.getEndTime())) {
				return ts.isAMAuction();
			} else if (intervalType_ == IntervalType.Start && !dateTime_.isBefore(ts.getStartTime()) && dateTime_.isBefore(ts.getEndTime())) {
				return ts.isAMAuction();
			}
		}
		return false;
	}

	public boolean isPMAuctionSession(LocalTime dateTime_) {
		return isPMAuctionSession(dateTime_, IntervalType.End);
	}

	public boolean isPMAuctionSession(LocalTime dateTime_, IntervalType intervalType_) {
		for (TradingSession ts : sessions) {
			if (intervalType_ == IntervalType.End && dateTime_.isAfter(ts.getStartTime()) && !dateTime_.isAfter(ts.getEndTime())) {
				return ts.isPMAuction();
			} else if (intervalType_ == IntervalType.Start && !dateTime_.isBefore(ts.getStartTime()) && dateTime_.isBefore(ts.getEndTime())) {
				return ts.isPMAuction();
			}
		}
		return false;
	}

	public boolean isClsAuctionSession(LocalTime dateTime_) {
		return isClsAuctionSession(dateTime_, IntervalType.End);
	}

	public boolean isClsAuctionSession(LocalTime dateTime_, IntervalType intervalType_) {
		for (TradingSession ts : sessions) {
			if (intervalType_ == IntervalType.End && dateTime_.isAfter(ts.getStartTime()) && !dateTime_.isAfter(ts.getEndTime())) {
				return ts.isClsAuction();
			} else if (intervalType_ == IntervalType.Start && !dateTime_.isBefore(ts.getStartTime()) && dateTime_.isBefore(ts.getEndTime())) {
				return ts.isClsAuction();
			}
		}
		return false;
	}

	public TradingSession getSessionByType(SessionType sessionType_) {
		for (TradingSession ts : sessions) {
			if (ts.getSessionType() == sessionType_)
				return ts;
		}

		return null;
	}

	public TradingSession getSessionByTime(LocalTime dateTime_) {
		return getSessionByTime(dateTime_, IntervalType.End);
	}

	public TradingSession getSessionByTime(LocalTime dateTime_, IntervalType intervalType_) {
		for (TradingSession ts : sessions) {
			if (intervalType_ == IntervalType.End && dateTime_.isAfter(ts.getStartTime()) && !dateTime_.isAfter(ts.getEndTime())) {
				return ts;
			} else if (intervalType_ == IntervalType.Start && !dateTime_.isBefore(ts.getStartTime()) && dateTime_.isBefore(ts.getEndTime())) {
				return ts;
			}
		}
		return null;
	}

	public TradingSession getNextSession(LocalTime dateTime_) {
		return getNextSession(dateTime_, IntervalType.End);
	}

	public TradingSession getNextSession(LocalTime dateTime_, IntervalType intervalType_) {
		for (int i = 0; i < sessions.size(); i++) {
			TradingSession ts = sessions.get(i);
			if (intervalType_ == IntervalType.End && dateTime_.isAfter(ts.getStartTime()) && !dateTime_.isAfter(ts.getEndTime())) {
				if (i == sessions.size() - 1)
					break;
				return sessions.get(i + 1);
			} else if (intervalType_ == IntervalType.Start && !dateTime_.isBefore(ts.getStartTime()) && dateTime_.isBefore(ts.getEndTime())) {
				if (i == sessions.size() - 1)
					break;
				return sessions.get(i + 1);
			}
		}
		return null;
	}

	public TradingSession getNextTradableSession(LocalTime dateTime_) {
		return getNextTradableSession(dateTime_, IntervalType.End, AuctionType.All);
	}

	public TradingSession getNextTradableSession(LocalTime dateTime_, IntervalType intervalType_, AuctionType auctionType_) {
		for (int i = 0; i < sessions.size(); i++) {
			TradingSession ts = sessions.get(i);
			if (intervalType_ == IntervalType.End && dateTime_.isAfter(ts.getStartTime()) && !dateTime_.isAfter(ts.getEndTime())) {
				while (++i < sessions.size()) {
					if (sessions.get(i).isTradable() && sessions.get(i).isAuctionMatch(auctionType_))
						return sessions.get(i);
				}
				break;
			} else if (intervalType_ == IntervalType.Start && !dateTime_.isBefore(ts.getStartTime()) && dateTime_.isBefore(ts.getEndTime())) {
				while (++i < sessions.size()) {
					if (sessions.get(i).isTradable() && sessions.get(i).isAuctionMatch(auctionType_))
						return sessions.get(i);
				}
				break;
			}
		}
		return null;
	}

	public TradingSession getFirstTradableSession() {
		return getFirstTradableSession(AuctionType.All);
	}

	public TradingSession getFirstTradableSession(AuctionType auctionType_) {
		for (TradingSession ts : sessions) {
			if (!ts.isAuctionMatch(auctionType_))
				continue;

			if (ts.isTradable())
				return ts;
		}

		return null;
	}

	public TradingSession getLastTradableSession() {
		return getLastTradableSession(AuctionType.All);
	}

	public TradingSession getLastTradableSession(AuctionType auctionType_) {
		TradingSession tradingSession = null;
		for (TradingSession ts : sessions) {
			if (!ts.isAuctionMatch(auctionType_))
				continue;

			if (ts.isTradable())
				tradingSession = ts;
		}

		return tradingSession;
	}

	public TradingSession getPrevSession(LocalTime dateTime_) {
		return getPrevSession(dateTime_, IntervalType.End);
	}

	public TradingSession getPrevSession(LocalTime dateTime_, IntervalType intervalType_) {
		for (int i = 0; i < sessions.size(); i++) {
			TradingSession ts = sessions.get(i);
			if (intervalType_ == IntervalType.End && dateTime_.isAfter(ts.getStartTime()) && !dateTime_.isAfter(ts.getEndTime())) {
				if (i == 0)
					break;
				return sessions.get(i - 1);
			} else if (intervalType_ == IntervalType.Start && !dateTime_.isBefore(ts.getStartTime()) && dateTime_.isBefore(ts.getEndTime())) {
				if (i == 0)
					break;
				return sessions.get(i - 1);
			}
		}
		return null;
	}

	public TradingSession getPrevTradableSession(LocalTime dateTime_) {
		return getPrevTradableSession(dateTime_, IntervalType.End, AuctionType.All);
	}

	public TradingSession getPrevTradableSession(LocalTime dateTime_, IntervalType intervalType_, AuctionType auctionType_) {
		for (int i = 0; i < sessions.size(); i++) {
			TradingSession ts = sessions.get(i);
			if (intervalType_ == IntervalType.End && dateTime_.isAfter(ts.getStartTime()) && !dateTime_.isAfter(ts.getEndTime())) {
				while (--i > 0) {
					if (sessions.get(i).isTradable() && sessions.get(i).isAuctionMatch(auctionType_))
						return sessions.get(i);
				}
				break;
			} else if (intervalType_ == IntervalType.Start && !dateTime_.isBefore(ts.getStartTime()) && dateTime_.isBefore(ts.getEndTime())) {
				while (--i > 0) {
					if (sessions.get(i).isTradable() && sessions.get(i).isAuctionMatch(auctionType_))
						return sessions.get(i);
				}
				break;
			}
		}
		return null;
	}

	private static boolean isAMAuction(AuctionType type_) {
		return type_ == AuctionType.All || type_ == AuctionType.AMAuction;
	}

	@SuppressWarnings("unused")
	private static boolean isPMAuction(AuctionType type_) {
		return type_ == AuctionType.All || type_ == AuctionType.PMAuction;
	}

	private static boolean isClsAuction(AuctionType type_) {
		return type_ == AuctionType.All || type_ == AuctionType.CloseAuction;
	}

	@Override
	public String toString() {
		return FormattedTable.toString(this);
	}

	@Override
	public int compareTo(SessionGroup o) {
		// TODO Auto-generated method stub
		return 0;
	}
}
