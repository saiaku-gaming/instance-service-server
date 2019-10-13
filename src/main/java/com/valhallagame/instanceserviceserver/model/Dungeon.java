package com.valhallagame.instanceserviceserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dungeon")
public class Dungeon {
	@Id
	@SequenceGenerator(name = "dungeon_dungeon_id_seq", sequenceName = "dungeon_dungeon_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dungeon_dungeon_id_seq")
	@Column(name = "dungeon_id", updatable = false)
	private Integer id;

	@OneToOne
	@JoinColumn(name = "instance_id")
	private Instance instance;

	@Column(name = "owner_username")
	private String ownerUsername;

	@Column(name = "owner_party_id")
	private Integer ownerPartyId;
}
