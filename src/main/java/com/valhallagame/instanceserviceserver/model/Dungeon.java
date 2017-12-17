package com.valhallagame.instanceserviceserver.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

	@Column(name = "owner")
	private String owner;
}
