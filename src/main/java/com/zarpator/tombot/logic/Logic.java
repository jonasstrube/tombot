package com.zarpator.tombot.logic;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import com.zarpator.tombot.datalayer.DataAccessObject;
import com.zarpator.tombot.datalayer.DbHousehold;
import com.zarpator.tombot.datalayer.DbRoomToUser;
import com.zarpator.tombot.datalayer.DbRoomToUser.Task;
import com.zarpator.tombot.datalayer.DbUser;
import com.zarpator.tombot.logic.event.Action;
import com.zarpator.tombot.logic.event.EventHandler;
import com.zarpator.tombot.utils.Logger;

public class Logic {
	DataAccessObject myDAO;
	EventHandler myEH;
	Logger logger;
	
	public Logic (EventHandler eventHandler) {
		this.myDAO = new DataAccessObject();
		this.myEH = eventHandler;
		this.logger = new Logger();
	}
	
	// TODO remove lastdayofperiod
	public void addRoomSwitchingJob(int householdId, DayOfWeek lastDayOfPeriod) {
		// start automatic room forwarding job
		addNextRoomSwitchingEvent(householdId);
	}
	
	private void addNextRoomSwitchingEvent(int householdId) {
		DbHousehold household = myDAO.getHouseholdById(householdId);
		DayOfWeek lastDayOfPeriod = household.getLastDayOfPeriod();
		LocalDateTime nextRoomSwitchingInterval = calculateTimeUntilNextSwitching(lastDayOfPeriod);
		
		myEH.addScheduledAction(nextRoomSwitchingInterval, new SwitchRoomsAction(householdId/* params needed?*/));
	}
	
	private void switchRooms(int householdId){		
		List<DbUser> userList = myDAO.getAllUsersOfHousehold(householdId);
		
		for (DbUser user : userList) {
			//TODO list still has to be sorted by room.sequencePosition (when not, a random room will be asigned
			List<DbRoomToUser> roomToUserList = myDAO.getRoomsToUser(user.getId());
			
			boolean responsibleRoomFound = false;
			for (DbRoomToUser roomToUser : roomToUserList) {

				if (responsibleRoomFound) {
					roomToUser.setTask(Task.RESPONSIBLE);
					break;
				}
				
				if (roomToUser.getTask() == Task.FINISHED) {
					responsibleRoomFound = true;
					roomToUser.setTask(Task.NOTRESPONSIBLE);
				} else if (roomToUser.getTask() == Task.RESPONSIBLE) {
					// give the user the next room (but keep this room with him, he didnt clean it yet)
					responsibleRoomFound = true;
				}
			}
		}
	}
	
	private LocalDateTime calculateTimeUntilNextSwitching(DayOfWeek lastDayOfPeriod) {
		LocalDateTime dateTime = LocalDateTime.now();
		LocalDateTime notificationTimestamp = dateTime.with(TemporalAdjusters.next(lastDayOfPeriod));
		return notificationTimestamp;
	}
	
	private class SwitchRoomsAction implements Action {
		int householdId;

		private SwitchRoomsAction(int householdId /* TODO params needed?*/) {
			this.householdId = householdId;
		}
		
		@Override
		public void execute() {
			switchRooms(householdId);
			addNextRoomSwitchingEvent(householdId);
		}
	}
}
