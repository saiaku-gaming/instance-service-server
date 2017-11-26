package com.valhallagame.instanceserviceserver.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "instance")
public class Instance {

	@Id
	@SequenceGenerator(name = "instance_instance_id_seq", sequenceName = "instance_instance_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "instance_instance_id_seq")
	@Column(name = "instance_id", updatable = false)
	private Integer id;

    @Column(name = "version")
    private String version;

    @Column(name = "level")
    private String level;

    @Column(name = "address")
    private String address;

    @Column(name = "port")
    private int port;

    @Column(name = "player_count")
    private int playerCount;

    @Column(name = "state")
    private String state;
}
