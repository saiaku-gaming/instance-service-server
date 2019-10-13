package com.valhallagame.instanceserviceserver;

import com.valhallagame.instanceserviceserver.model.Dungeon;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.repository.DungeonRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class DungeonRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DungeonRepository dungeonRepository;

    @Test
    public void findDungeonByInstanceId() {
        Instance instance = new Instance();
        instance.setAddress("127.0.0.1");
        instance.setLevel("Test Level");
        instance.setMembers(Arrays.asList("Nisse", "Hult"));
        instance.setPort(8080);
        instance.setState("ACTIVE");
        instance.setVersion("9999");
        instance.setId("GAMELIFT-TEST");

        entityManager.persist(instance);
        entityManager.flush();

        Dungeon dungeon = new Dungeon();
        dungeon.setInstance(instance);
        dungeon.setOwnerPartyId(1);

        entityManager.persist(dungeon);
        entityManager.flush();

        Optional<Dungeon> dungeonByInstanceId = dungeonRepository.findDungeonByInstanceId("GAMELIFT-TEST");

        assertTrue(dungeonByInstanceId.isPresent());
        assertEquals(dungeon, dungeonByInstanceId.get());
    }

    @Test
    public void findMissingDungeonByInstanceId() {
        Optional<Dungeon> dungeonByInstanceId = dungeonRepository.findDungeonByInstanceId("GAMELIFT-TEST");

        assertFalse(dungeonByInstanceId.isPresent());
    }

    @Test
    public void findActiveDungeonsByPartyId() {
        Instance instance1 = new Instance();
        instance1.setAddress("127.0.0.1");
        instance1.setLevel("Test Level");
        instance1.setMembers(Arrays.asList("Nisse", "Hult"));
        instance1.setPort(8080);
        instance1.setState("ACTIVE");
        instance1.setVersion("9999");
        instance1.setId("GAMELIFT-TEST1");

        entityManager.persist(instance1);
        entityManager.flush();

        Dungeon dungeon1 = new Dungeon();
        dungeon1.setInstance(instance1);
        dungeon1.setOwnerPartyId(1);

        entityManager.persist(dungeon1);
        entityManager.flush();

        Instance instance2 = new Instance();
        instance2.setAddress("127.0.0.1");
        instance2.setLevel("Test Level");
        instance2.setMembers(Arrays.asList("Nisse", "Hult"));
        instance2.setPort(8080);
        instance2.setState("FINISHING");
        instance2.setVersion("9999");
        instance2.setId("GAMELIFT-TEST2");

        entityManager.persist(instance2);
        entityManager.flush();

        Dungeon dungeon2 = new Dungeon();
        dungeon2.setInstance(instance2);
        dungeon2.setOwnerPartyId(1);

        entityManager.persist(dungeon2);
        entityManager.flush();

        Instance instance3 = new Instance();
        instance3.setAddress("127.0.0.1");
        instance3.setLevel("Test Level");
        instance3.setMembers(Arrays.asList("Nisse", "Hult"));
        instance3.setPort(8080);
        instance3.setState("ACTIVE");
        instance3.setVersion("9999");
        instance3.setId("GAMELIFT-TEST3");

        entityManager.persist(instance3);
        entityManager.flush();

        Dungeon dungeon3 = new Dungeon();
        dungeon3.setInstance(instance3);
        dungeon3.setOwnerPartyId(1);

        entityManager.persist(dungeon3);
        entityManager.flush();

        List<Dungeon> dungeonList = dungeonRepository.findActiveDungeonsByPartyId(1);

        assertEquals(2, dungeonList.size());

        assertEquals(Arrays.asList(dungeon1, dungeon3), dungeonList);
    }

    @Test
    public void findRelevantDungeonsByPartyId() {
        Instance instance1 = new Instance();
        instance1.setAddress("127.0.0.1");
        instance1.setLevel("Test Level");
        instance1.setMembers(Arrays.asList("Nisse", "Hult"));
        instance1.setPort(8080);
        instance1.setState("ACTIVE");
        instance1.setVersion("9999");
        instance1.setId("GAMELIFT-TEST1");

        entityManager.persist(instance1);
        entityManager.flush();

        Dungeon dungeon1 = new Dungeon();
        dungeon1.setInstance(instance1);
        dungeon1.setOwnerPartyId(1);

        entityManager.persist(dungeon1);
        entityManager.flush();

        Instance instance2 = new Instance();
        instance2.setAddress("127.0.0.1");
        instance2.setLevel("Test Level");
        instance2.setMembers(Arrays.asList("Nisse", "Hult"));
        instance2.setPort(8080);
        instance2.setState("FINISHING");
        instance2.setVersion("9999");
        instance2.setId("GAMELIFT-TEST2");

        entityManager.persist(instance2);
        entityManager.flush();

        Dungeon dungeon2 = new Dungeon();
        dungeon2.setInstance(instance2);
        dungeon2.setOwnerPartyId(1);

        entityManager.persist(dungeon2);
        entityManager.flush();

        Instance instance3 = new Instance();
        instance3.setAddress("127.0.0.1");
        instance3.setLevel("Test Level");
        instance3.setMembers(Arrays.asList("Nisse", "Hult"));
        instance3.setPort(8080);
        instance3.setState("STARTING");
        instance3.setVersion("9999");
        instance3.setId("GAMELIFT-TEST3");

        entityManager.persist(instance3);
        entityManager.flush();

        Dungeon dungeon3 = new Dungeon();
        dungeon3.setInstance(instance3);
        dungeon3.setOwnerPartyId(1);

        entityManager.persist(dungeon3);
        entityManager.flush();

        List<Dungeon> dungeonList = dungeonRepository.findRelevantDungeonsByPartyId(1, "9999");

        assertEquals(2, dungeonList.size());

        assertEquals(Arrays.asList(dungeon1, dungeon3), dungeonList);
    }

    @Test
    public void findDungeonByOwnerUsername() {
        Instance instance = new Instance();
        instance.setAddress("127.0.0.1");
        instance.setLevel("Test Level");
        instance.setMembers(Arrays.asList("Nisse", "Hult"));
        instance.setPort(8080);
        instance.setState("ACTIVE");
        instance.setVersion("9999");
        instance.setId("GAMELIFT-TEST");

        entityManager.persist(instance);
        entityManager.flush();

        Dungeon dungeon = new Dungeon();
        dungeon.setInstance(instance);
        dungeon.setOwnerUsername("Nisse");

        entityManager.persist(dungeon);
        entityManager.flush();

        Optional<Dungeon> dungeonByInstanceId = dungeonRepository.findDungeonByOwnerUsername("Nisse");

        assertTrue(dungeonByInstanceId.isPresent());
        assertEquals(dungeon, dungeonByInstanceId.get());
    }

    @Test
    public void findDungeonByWrongOwnerUsername() {
        Instance instance = new Instance();
        instance.setAddress("127.0.0.1");
        instance.setLevel("Test Level");
        instance.setMembers(Arrays.asList("Nisse", "Hult"));
        instance.setPort(8080);
        instance.setState("ACTIVE");
        instance.setVersion("9999");
        instance.setId("GAMELIFT-TEST");

        entityManager.persist(instance);
        entityManager.flush();

        Dungeon dungeon = new Dungeon();
        dungeon.setInstance(instance);
        dungeon.setOwnerUsername("Nisse");

        entityManager.persist(dungeon);
        entityManager.flush();

        Optional<Dungeon> dungeonByInstanceId = dungeonRepository.findDungeonByOwnerUsername("Nisse Hult");

        assertFalse(dungeonByInstanceId.isPresent());
    }

    @Test
    public void canAccessDungeonWithOwner() {
        Instance instance = new Instance();
        instance.setAddress("127.0.0.1");
        instance.setLevel("Test Level");
        instance.setMembers(Collections.singletonList("Nisse"));
        instance.setPort(8080);
        instance.setState("ACTIVE");
        instance.setVersion("9999");
        instance.setId("GAMELIFT-TEST");

        entityManager.persist(instance);
        entityManager.flush();

        Dungeon dungeon = new Dungeon();
        dungeon.setInstance(instance);
        dungeon.setOwnerUsername("Nisse");

        entityManager.persist(dungeon);
        entityManager.flush();

        assertTrue(dungeonRepository.canAccessDungeon("Nisse", dungeon.getId(), "9999"));
    }

    @Test
    public void canAccessDungeonWithOwnerWrongOwner() {
        Instance instance = new Instance();
        instance.setAddress("127.0.0.1");
        instance.setLevel("Test Level");
        instance.setMembers(Collections.singletonList("Nisse"));
        instance.setPort(8080);
        instance.setState("ACTIVE");
        instance.setVersion("9999");
        instance.setId("GAMELIFT-TEST");

        entityManager.persist(instance);
        entityManager.flush();

        Dungeon dungeon = new Dungeon();
        dungeon.setInstance(instance);
        dungeon.setOwnerUsername("Nisse");

        entityManager.persist(dungeon);
        entityManager.flush();

        assertFalse(dungeonRepository.canAccessDungeon("Hult", dungeon.getId(), "9999"));
    }

    @Test
    public void canAccessDungeonWithOwnerWrongVersion() {
        Instance instance = new Instance();
        instance.setAddress("127.0.0.1");
        instance.setLevel("Test Level");
        instance.setMembers(Collections.singletonList("Nisse"));
        instance.setPort(8080);
        instance.setState("ACTIVE");
        instance.setVersion("9999");
        instance.setId("GAMELIFT-TEST");

        entityManager.persist(instance);
        entityManager.flush();

        Dungeon dungeon = new Dungeon();
        dungeon.setInstance(instance);
        dungeon.setOwnerUsername("Nisse");

        entityManager.persist(dungeon);
        entityManager.flush();

        assertFalse(dungeonRepository.canAccessDungeon("Nisse", dungeon.getId(), "8888"));
    }

    @Test
    public void canAccessDungeonWithOwnerWrongDungeonId() {
        Instance instance = new Instance();
        instance.setAddress("127.0.0.1");
        instance.setLevel("Test Level");
        instance.setMembers(Collections.singletonList("Nisse"));
        instance.setPort(8080);
        instance.setState("ACTIVE");
        instance.setVersion("9999");
        instance.setId("GAMELIFT-TEST");

        entityManager.persist(instance);
        entityManager.flush();

        Dungeon dungeon = new Dungeon();
        dungeon.setInstance(instance);
        dungeon.setOwnerUsername("Nisse");

        entityManager.persist(dungeon);
        entityManager.flush();

        assertFalse(dungeonRepository.canAccessDungeon("Nisse", -1, "9999"));
    }

    @Test
    public void canAccessDungeonWithParty() {
        Instance instance = new Instance();
        instance.setAddress("127.0.0.1");
        instance.setLevel("Test Level");
        instance.setMembers(Arrays.asList("Nisse", "Hult"));
        instance.setPort(8080);
        instance.setState("ACTIVE");
        instance.setVersion("9999");
        instance.setId("GAMELIFT-TEST");

        entityManager.persist(instance);
        entityManager.flush();

        Dungeon dungeon = new Dungeon();
        dungeon.setInstance(instance);
        dungeon.setOwnerPartyId(1);

        entityManager.persist(dungeon);
        entityManager.flush();

        assertTrue(dungeonRepository.canAccessDungeon(1, dungeon.getId(), "9999"));
    }

    @Test
    public void canAccessDungeonWithPartyWrongPartyId() {
        Instance instance = new Instance();
        instance.setAddress("127.0.0.1");
        instance.setLevel("Test Level");
        instance.setMembers(Arrays.asList("Nisse", "Hult"));
        instance.setPort(8080);
        instance.setState("ACTIVE");
        instance.setVersion("9999");
        instance.setId("GAMELIFT-TEST");

        entityManager.persist(instance);
        entityManager.flush();

        Dungeon dungeon = new Dungeon();
        dungeon.setInstance(instance);
        dungeon.setOwnerPartyId(1);

        entityManager.persist(dungeon);
        entityManager.flush();

        assertFalse(dungeonRepository.canAccessDungeon(2, dungeon.getId(), "9999"));
    }

    @Test
    public void canAccessDungeonWithPartyWrongDungeonId() {
        Instance instance = new Instance();
        instance.setAddress("127.0.0.1");
        instance.setLevel("Test Level");
        instance.setMembers(Arrays.asList("Nisse", "Hult"));
        instance.setPort(8080);
        instance.setState("ACTIVE");
        instance.setVersion("9999");
        instance.setId("GAMELIFT-TEST");

        entityManager.persist(instance);
        entityManager.flush();

        Dungeon dungeon = new Dungeon();
        dungeon.setInstance(instance);
        dungeon.setOwnerPartyId(1);

        entityManager.persist(dungeon);
        entityManager.flush();

        assertFalse(dungeonRepository.canAccessDungeon(1, -1, "9999"));
    }

    @Test
    public void canAccessDungeonWithPartyWrongVersion() {
        Instance instance = new Instance();
        instance.setAddress("127.0.0.1");
        instance.setLevel("Test Level");
        instance.setMembers(Arrays.asList("Nisse", "Hult"));
        instance.setPort(8080);
        instance.setState("ACTIVE");
        instance.setVersion("9999");
        instance.setId("GAMELIFT-TEST");

        entityManager.persist(instance);
        entityManager.flush();

        Dungeon dungeon = new Dungeon();
        dungeon.setInstance(instance);
        dungeon.setOwnerPartyId(1);

        entityManager.persist(dungeon);
        entityManager.flush();

        assertFalse(dungeonRepository.canAccessDungeon(1, dungeon.getId(), "8888"));
    }
}
