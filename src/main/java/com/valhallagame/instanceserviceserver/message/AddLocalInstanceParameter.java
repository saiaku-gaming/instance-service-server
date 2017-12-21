package com.valhallagame.instanceserviceserver.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddLocalInstanceParameter {
	private String gameSessionId;
	private String address;
	private Integer port;
	private String mapName;
	private String state;
	private String version;
}
