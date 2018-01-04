package com.valhallagame.instanceserviceserver.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelevantDungeonsAndPlacement {
	List<Dungeon> relevantDungeons;
	List<QueuePlacement> queuePlacements;
}
