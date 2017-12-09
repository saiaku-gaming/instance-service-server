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

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "hub")
public class Hub {

	@Id
	@SequenceGenerator(name = "hub_hub_id_seq", sequenceName = "hub_hub_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hub_hub_id_seq")
	@Column(name = "hub_id", updatable = false)
	private Integer id;
	
	@OneToOne
	@JoinColumn(name = "instance_id")
	private Instance instance;
	
}
