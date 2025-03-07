/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.commons.initializers.metadata;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.scheduler.wsEvents.SbiWsEvent;
import it.eng.spagobi.tools.scheduler.wsEvents.dao.SbiWsEventsDao;

/**
 * @author Marco Libanori
 */
public class WsEventCleanJobInitializer extends SpagoBIInitializer {

	private static final Logger LOGGER = Logger.getLogger(WsEventCleanJobInitializer.class);

	private static final String JOB_GROUP = "common";
	private static final String JOB_NAME = "WsEventCleaner";

	public static class WsEventCleaner implements Job {

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {

			String timePeriodAsString = SingletonConfig.getInstance().getConfigValue("KNOWAGE.WSEVENTCLEANJOB.PERIOD");

			int timePeriod = Optional.ofNullable(timePeriodAsString)
					.map(Integer::valueOf)
					.orElse(31536000);

			SbiWsEventsDao dao = DAOFactory.getWsEventsDao();

			List<SbiWsEvent> wsEventList = dao.getWsEventList();

			for (SbiWsEvent sbiWsEvent : wsEventList) {

				Date takeChargeDate = sbiWsEvent.getTakeChargeDate();

				if (takeChargeDate != null) {

					Instant takeChargeDateAsInstant = takeChargeDate.toInstant();

					if (takeChargeDateAsInstant.isBefore(Instant.now().minusSeconds(timePeriod))) {

						dao.deleteEvent(sbiWsEvent);

					}
				}
			}
		}

	}

	@Override
	public void init(SourceBean config, Session hibernateSession) {

		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

			JobDetail job = scheduler.getJobDetail(JOB_NAME, JOB_GROUP);

			if (job == null) {

				job = new JobDetail(JOB_NAME, JOB_GROUP, WsEventCleaner.class);
				job.setDurability(false);

				scheduler.addJob(job, true);
				scheduler.triggerJob(JOB_NAME, JOB_GROUP);

			}
		} catch (SchedulerException e) {
			LOGGER.error("Error creating WsEvent Clean job", e);
		}

	}

}
