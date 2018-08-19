package com.valhallagame.instanceserviceserver;

import com.valhallagame.instanceserviceserver.model.Dungeon;
import com.valhallagame.instanceserviceserver.model.Instance;
import org.junit.Before;

import java.util.Arrays;

public abstract class BaseTest {
    private int instanceCount;
    private int dungeonCount;
    private int hubCount;

    @Before
    public void init() {
        instanceCount = dungeonCount = hubCount = 0;
    }

    Instance createInstance() {
        Instance instance = new Instance();
        instance.setAddress("127.0.0.1");
        instance.setLevel("Test Level");
        instance.setMembers(Arrays.asList("Nisse", "Hult"));
        instance.setPort(8080);
        instance.setState("ACTIVE");
        instance.setVersion("9999");
        instance.setId("GAMELIFT-TEST-" + instanceCount++);

        return instance;
    }

    Dungeon createDungeon(Instance instance) {
        Dungeon dungeon = new Dungeon();
        dungeon.setCreatorUsername("Nisse");
        dungeon.setInstance(instance);
        dungeon.setOwnerPartyId(1);

        return dungeon;
    }
}
