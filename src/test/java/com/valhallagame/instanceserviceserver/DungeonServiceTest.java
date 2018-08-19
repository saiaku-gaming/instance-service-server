package com.valhallagame.instanceserviceserver;

import com.valhallagame.common.RestResponse;
import com.valhallagame.instanceserviceserver.model.Dungeon;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.model.QueuePlacement;
import com.valhallagame.instanceserviceserver.repository.DungeonRepository;
import com.valhallagame.instanceserviceserver.service.DungeonService;
import com.valhallagame.instanceserviceserver.service.QueuePlacementService;
import com.valhallagame.partyserviceclient.PartyServiceClient;
import com.valhallagame.partyserviceclient.model.PartyData;
import com.valhallagame.partyserviceclient.model.PartyMemberData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class DungeonServiceTest extends BaseTest {

    @TestConfiguration
    static class DungeonServiceTestContextConfiguration {
        @Bean
        public DungeonService dungeonService() {
            return new DungeonService();
        }
    }

    @Autowired
    private DungeonService dungeonService;

    @MockBean
    private DungeonRepository dungeonRepository;

    @MockBean
    private QueuePlacementService queuePlacementService;

    @MockBean
    private PartyServiceClient partyServiceClient;

    @Test
    public void getDungeonByInstance() {
        Instance instance = createInstance();

        Dungeon dungeon = createDungeon(instance);
        dungeon.setId(1);

        when(dungeonRepository.findDungeonByInstanceId(instance.getId())).thenReturn(Optional.of(dungeon));

        Optional<Dungeon> dungeonByInstance = dungeonService.getDungeonByInstance(instance);

        assertTrue(dungeonByInstance.isPresent());
        assertEquals(dungeon, dungeonByInstance.get());
    }

    @Test
    public void getDungeonByMissingInstance() {
        Instance instance = createInstance();

        when(dungeonRepository.findDungeonByInstanceId(instance.getId())).thenReturn(Optional.empty());

        Optional<Dungeon> dungeonByInstance = dungeonService.getDungeonByInstance(instance);

        assertFalse(dungeonByInstance.isPresent());
    }

    @Test
    public void getDungeonByNullInstance() {
        Optional<Dungeon> dungeonByInstance = dungeonService.getDungeonByInstance(null);

        assertFalse(dungeonByInstance.isPresent());
    }

    @Test
    public void canCreateDungeon() throws IOException {
        PartyMemberData partyMemberData = new PartyMemberData();
        partyMemberData.setDisplayUsername("nisse");
        PartyData partyData = new PartyData();
        partyData.setLeader(partyMemberData);

        RestResponse<PartyData> partyResp = new RestResponse<>(HttpStatus.OK, Optional.of(partyData));

        when(partyServiceClient.getParty("Nisse")).thenReturn(partyResp);

        when(dungeonRepository.findDungeonByCreatorUsername("Nisse")).thenReturn(Optional.empty());

        assertTrue(dungeonService.canCreateDungeon("Nisse"));
    }

    @Test
    public void canCreateDungeonWithPreviousFinishedDungeon() throws IOException {
        PartyMemberData partyMemberData = new PartyMemberData();
        partyMemberData.setDisplayUsername("nisse");
        PartyData partyData = new PartyData();
        partyData.setLeader(partyMemberData);

        RestResponse<PartyData> partyResp = new RestResponse<>(HttpStatus.OK, Optional.of(partyData));

        when(partyServiceClient.getParty("Nisse")).thenReturn(partyResp);

        Instance instance = createInstance();
        instance.setState("FINISHING");

        Dungeon dungeon = createDungeon(instance);
        dungeon.setCreatorUsername("Nisse");

        when(dungeonRepository.findDungeonByCreatorUsername("Nisse")).thenReturn(Optional.of(dungeon));

        when(queuePlacementService.getQueuePlacementFromQueuer("Nisse")).thenReturn(Optional.empty());

        assertTrue(dungeonService.canCreateDungeon("Nisse"));
    }

    @Test
    public void canCreateDungeonWithoutBeingLeader() throws IOException {
        PartyMemberData partyMemberData = new PartyMemberData();
        partyMemberData.setDisplayUsername("hult");

        PartyData partyData = new PartyData();
        partyData.setLeader(partyMemberData);
        RestResponse<PartyData> partyResp = new RestResponse<>(HttpStatus.OK, Optional.of(partyData));

        when(partyServiceClient.getParty("Nisse")).thenReturn(partyResp);

        assertFalse(dungeonService.canCreateDungeon("Nisse"));
    }

    @Test
    public void canCreateDungeonWithoutParty() throws IOException {
        RestResponse<PartyData> partyResp = new RestResponse<>(HttpStatus.OK, Optional.empty());

        when(partyServiceClient.getParty("Nisse")).thenReturn(partyResp);

        Instance instance = createInstance();
        instance.setState("FINISHING");

        Dungeon dungeon = createDungeon(instance);
        dungeon.setCreatorUsername("Nisse");

        when(dungeonRepository.findDungeonByCreatorUsername("Nisse")).thenReturn(Optional.of(dungeon));

        when(queuePlacementService.getQueuePlacementFromQueuer("Nisse")).thenReturn(Optional.empty());

        assertTrue(dungeonService.canCreateDungeon("Nisse"));
    }

    @Test
    public void canCreateDungeonWithPreviousNotFinishedDungeon() throws IOException {
        PartyMemberData partyMemberData = new PartyMemberData();
        partyMemberData.setDisplayUsername("nisse");
        PartyData partyData = new PartyData();
        partyData.setLeader(partyMemberData);

        RestResponse<PartyData> partyResp = new RestResponse<>(HttpStatus.OK, Optional.of(partyData));

        when(partyServiceClient.getParty("Nisse")).thenReturn(partyResp);

        Instance instance = createInstance();
        instance.setState("ACTIVE");

        Dungeon dungeon = createDungeon(instance);
        dungeon.setCreatorUsername("Nisse");

        when(dungeonRepository.findDungeonByCreatorUsername("Nisse")).thenReturn(Optional.of(dungeon));

        assertFalse(dungeonService.canCreateDungeon("Nisse"));
    }

    @Test
    public void canCreateDungeonWithQueuePlacement() throws IOException {
        PartyMemberData partyMemberData = new PartyMemberData();
        partyMemberData.setDisplayUsername("nisse");
        PartyData partyData = new PartyData();
        partyData.setLeader(partyMemberData);

        RestResponse<PartyData> partyResp = new RestResponse<>(HttpStatus.OK, Optional.of(partyData));

        when(partyServiceClient.getParty("Nisse")).thenReturn(partyResp);

        Instance instance = createInstance();
        instance.setState("FINISHING");

        Dungeon dungeon = createDungeon(instance);
        dungeon.setCreatorUsername("Nisse");

        when(dungeonRepository.findDungeonByCreatorUsername("Nisse")).thenReturn(Optional.of(dungeon));

        QueuePlacement queuePlacement = new QueuePlacement();

        when(queuePlacementService.getQueuePlacementFromQueuer("Nisse")).thenReturn(Optional.of(queuePlacement));

        assertFalse(dungeonService.canCreateDungeon("Nisse"));
    }

    @Test
    public void findActiveDungeonsByPartyId() {
        Instance instance1 = createInstance();
        Dungeon dungeon1 = createDungeon(instance1);

        Instance instance2 = createInstance();
        Dungeon dungeon2 = createDungeon(instance2);

        when(dungeonRepository.findActiveDungeonsByPartyId(1)).thenReturn(Arrays.asList(dungeon1, dungeon2));

        List<Dungeon> dungeonList = dungeonService.findActiveDungeonsByPartyId(1);

        assertEquals(2, dungeonList.size());
        assertEquals(Arrays.asList(dungeon1, dungeon2), dungeonList);
    }

    @Test
    public void getRelevantDungeonsFromParty() {
        Instance instance1 = createInstance();
        Dungeon dungeon1 = createDungeon(instance1);

        Instance instance2 = createInstance();
        Dungeon dungeon2 = createDungeon(instance2);

        when(dungeonRepository.findRelevantDungeonsByPartyId(1, "9999")).thenReturn(Arrays.asList(dungeon1, dungeon2));

        PartyData partyData = new PartyData();
        partyData.setId(1);
        List<Dungeon> dungeonList = dungeonService.getRelevantDungeonsFromParty(partyData, "9999");

        assertEquals(2, dungeonList.size());
        assertEquals(Arrays.asList(dungeon1, dungeon2), dungeonList);
    }

    @Test
    public void getDungeonFromOwnerUsername() {
        Instance instance = createInstance();
        Dungeon dungeon = createDungeon(instance);

        when(dungeonRepository.findDungeonByOwnerUsername("Nisse")).thenReturn(Optional.of(dungeon));

        Optional<Dungeon> optDungeon = dungeonService.getDungeonFromOwnerUsername("Nisse");

        assertTrue(optDungeon.isPresent());
        assertEquals(dungeon, optDungeon.get());
    }

    @Test
    public void canAccessDungeonWithParty() {
        when(dungeonRepository.canAccessDungeon(1, 1 , "9999")).thenReturn(true);

        assertTrue(dungeonService.canAccessDungeon(1, 1, "9999"));
    }

    @Test
    public void canAccessDungeonWithOwner() {
        when(dungeonRepository.canAccessDungeon("Nisse", 1 , "9999")).thenReturn(true);

        assertTrue(dungeonService.canAccessDungeon("Nisse", 1, "9999"));
    }
}
