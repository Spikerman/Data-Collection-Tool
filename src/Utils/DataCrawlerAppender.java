package Utils;

import org.apache.log4j.net.SMTPAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Created by chenhao on 3/8/16.
 */
public class DataCrawlerAppender extends SMTPAppender {
    public DataCrawlerAppender() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (cb.length() > 0) {
                    //sendBuffer();
                }
            }
        });
    }

    @Override
    public void append(LoggingEvent event) {
        if (!checkEntryConditions()) {
            return;
        }
        event.getThreadName();
        event.getNDC();
        event.getMDCCopy();
        if (this.getLocationInfo()) {
            event.getLocationInformation();
        }
        cb.add(event);
        if (evaluator.isTriggeringEvent(event)) {
            if (cb.length() > this.getBufferSize() / 2) {
                //sendBuffer();
            }
        }
    }
}
