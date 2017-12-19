package com.valhallagame.instanceserviceserver.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "instance")
public class Instance {
	@Id
	@Column(name = "instance_id")
	private String id;

	@Column(name = "version")
	private String version;

	@Column(name = "level")
	private String level;

	@Column(name = "address")
	private String address;

	@Column(name = "port")
	private int port;

	@Column(name = "state")
	private String state;

	@ElementCollection
	@JoinTable(name = "instance_member", joinColumns = @JoinColumn(name = "instance_id"))
	@Column(name = "username")
	private List<String> members;

	public int getPlayerCount() {
		return members.size();
	}
}
