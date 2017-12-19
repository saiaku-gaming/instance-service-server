package com.valhallagame.instanceserviceserver.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "queue_placement")
public class QueuePlacement {
	@Id
	@Column(name = "queue_placement_id")
	private String id;

	@Column(name = "queuer_username")
	private String queuerUsername;

	@Column(name = "status")
	private String status;

	@Column(name = "map_name")
	private String mapName;

	@Column(name = "version")
	private String version;
}
