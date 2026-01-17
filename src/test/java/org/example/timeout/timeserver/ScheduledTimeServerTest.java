package org.example.timeout.timeserver;

import org.example.timeout.alarm.PassiveAlarm;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScheduledTimeServerTest {

    @Test
    void notifiesObserversOnEveryTick() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(5);
        try (ScheduledTimeServer server = new ScheduledTimeServer(Duration.ofMillis(5))) {
            server.addListener(snapshot -> latch.countDown());
            server.start();
            assertTrue(latch.await(500, TimeUnit.MILLISECONDS), "Listeners should receive ticks");
        }
    }

    @Test
    void passiveAlarmFiresAtTargetTick() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try (ScheduledTimeServer server = new ScheduledTimeServer(Duration.ofMillis(5));
             PassiveAlarm alarm = new PassiveAlarm("passive", server, 3, notification -> latch.countDown())) {
            server.start();
            alarm.arm();
            assertTrue(latch.await(500, TimeUnit.MILLISECONDS), "Passive alarm should be invoked");
        }
    }

    @Test
    void resetTicksResetsCounter() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try (ScheduledTimeServer server = new ScheduledTimeServer(Duration.ofMillis(5))) {
            server.addListener(snapshot -> {
                if (snapshot.tickCount() >= 3) {
                    latch.countDown();
                }
            });
            server.start();
            assertTrue(latch.await(500, TimeUnit.MILLISECONDS), "Server should emit several ticks");
            server.resetTicks();
            assertEquals(0, server.getCurrentTick(), "Tick counter should be cleared");
        }
    }

}
