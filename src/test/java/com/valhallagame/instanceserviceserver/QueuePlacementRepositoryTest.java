package com.valhallagame.instanceserviceserver;

import com.valhallagame.instanceserviceserver.model.QueuePlacement;
import com.valhallagame.instanceserviceserver.repository.QueuePlacementRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.Optional;

@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class QueuePlacementRepositoryTest {

    @Autowired
    private QueuePlacementRepository queuePlacementRepository;

    @Test
    public void deleteQueuePlacementByQueuer() {
        QueuePlacement q = new QueuePlacement("id", "test", "status", "mapname", "version", Instant.now());
        queuePlacementRepository.save(q);
        Optional<QueuePlacement> test = queuePlacementRepository.findQueuePlacementByQueuerUsername("test");
        Assert.assertTrue(test.isPresent());

        queuePlacementRepository.deleteQueuePlacementByQueuerUsername("test");

        test = queuePlacementRepository.findQueuePlacementByQueuerUsername("test");
        Assert.assertFalse(test.isPresent());
    }
}
