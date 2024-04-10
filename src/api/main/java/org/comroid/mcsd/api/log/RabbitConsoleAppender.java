package org.comroid.mcsd.api.log;

import lombok.Value;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.filter.LevelRangeFilter;
import org.comroid.api.net.Rabbit;
import org.comroid.mcsd.api.dto.comm.ConsoleData;

import java.util.Calendar;

import static org.apache.logging.log4j.Level.*;
import static org.apache.logging.log4j.core.Filter.Result.*;
import static org.comroid.mcsd.api.dto.comm.ConsoleData.output;

@Value
@Plugin(name = "MCSD Console Relay", category = "Core", elementType = "appender", printObject = true)
public class RabbitConsoleAppender extends AbstractAppender {
    Rabbit.Exchange.Route<ConsoleData> route;

    public RabbitConsoleAppender(Rabbit.Exchange.Route<ConsoleData> route, Level level) {
        super("MCSD Console Appender",
                LevelRangeFilter.createFilter(OFF, level, ACCEPT, DENY),
                null, false, null);
        this.route = route;

        ((Logger) LogManager.getRootLogger()).addAppender(this);
    }

    @Override
    public void append(LogEvent event) {
        try {
            var calendar = Calendar.getInstance();
            calendar.setTimeInMillis(event.getTimeMillis());
            var str = "[%tT %s] [%s]: %s\n".formatted(
                    calendar,
                    event.getLevel().name(),
                    event.getLoggerName(),
                    event.getMessage().getFormattedMessage());
            route.send(output(str));
        } catch (Throwable t) {
            System.err.println("Error in RabbitAppender");
            t.printStackTrace(System.err);
        }
    }
}
